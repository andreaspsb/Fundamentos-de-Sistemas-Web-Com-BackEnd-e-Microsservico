// Deploy Azure - 01/12/2025
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using System.Text;
using PetshopApi.Data;
using PetshopApi.Security;
using PetshopApi.Middleware;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container
builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.ReferenceHandler = System.Text.Json.Serialization.ReferenceHandler.IgnoreCycles;
        options.JsonSerializerOptions.DefaultIgnoreCondition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull;
    });

// Configure Database (SQL Server/Azure SQL em produção, SQLite em desenvolvimento/Docker)
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");

// Sobrescrever com variável de ambiente se disponível (Azure App Service ou Docker)
connectionString = Environment.GetEnvironmentVariable("SQLAZURECONNSTR_DefaultConnection") 
    ?? Environment.GetEnvironmentVariable("ConnectionStrings__DefaultConnection")
    ?? connectionString;

// Detectar tipo de banco pelo connection string
var isAzureSql = connectionString != null && 
    (connectionString.Contains("database.windows.net") || 
     connectionString.Contains("Server=tcp:") ||
     connectionString.Contains("sqlserver"));

if (isAzureSql)
{
    // Azure SQL Database
    Console.WriteLine("📦 Usando Azure SQL Database");
    builder.Services.AddDbContext<PetshopContext>(options =>
        options.UseSqlServer(connectionString));
}
else
{
    // SQLite para desenvolvimento local e Docker (padrão)
    var sqliteConnection = connectionString ?? "Data Source=petshop.db";
    Console.WriteLine($"📦 Usando SQLite: {sqliteConnection}");
    builder.Services.AddDbContext<PetshopContext>(options =>
        options.UseSqlite(sqliteConnection));
}

// Register JWT Service
builder.Services.AddSingleton<JwtService>();

// Configure JWT Authentication
var jwtSecretKey = builder.Configuration["Jwt:SecretKey"] ?? 
    "petshop-secret-key-must-be-at-least-256-bits-long-for-HS256-algorithm";
var key = Encoding.UTF8.GetBytes(jwtSecretKey);

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.RequireHttpsMetadata = false; // true em produção
    options.SaveToken = true;
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(key),
        ValidateIssuer = true,
        ValidIssuer = builder.Configuration["Jwt:Issuer"] ?? "PetshopApi",
        ValidateAudience = true,
        ValidAudience = builder.Configuration["Jwt:Audience"] ?? "PetshopFrontend",
        ValidateLifetime = true,
        ClockSkew = TimeSpan.Zero
    };
});

builder.Services.AddAuthorization();

// Configure CORS with environment-specific settings
builder.Services.AddCors(options =>
{
    // Development: Allow all origins (for local testing)
    options.AddPolicy("Development", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });

    // Production: Restrict to specific origins
    options.AddPolicy("Production", policy =>
    {
        // Read from environment variable (CORS_ALLOWED_ORIGINS) or configuration section
        var corsOriginsEnv = Environment.GetEnvironmentVariable("CORS_ALLOWED_ORIGINS");
        var allowedOrigins = !string.IsNullOrEmpty(corsOriginsEnv) 
            ? corsOriginsEnv.Split(',', StringSplitOptions.RemoveEmptyEntries)
            : builder.Configuration.GetSection("Cors:AllowedOrigins").Get<string[]>() 
              ?? new[] 
              { 
                  "https://petshop.com",
                  "https://www.petshop.com",
                  "https://api.petshop.com"
              };

        policy.WithOrigins(allowedOrigins)
              .AllowAnyMethod()
              .AllowAnyHeader()
              .WithExposedHeaders("X-Pagination", "X-Total-Count")
              .SetPreflightMaxAge(TimeSpan.FromMinutes(10));
    });
});

// Configure Swagger/OpenAPI
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new Microsoft.OpenApi.Models.OpenApiInfo
    {
        Title = "Petshop API",
        Version = "v1",
        Description = "API REST para gerenciamento de Pet Shop com autenticação JWT",
        Contact = new Microsoft.OpenApi.Models.OpenApiContact
        {
            Name = "Petshop Team",
            Email = "contato@petshop.com"
        }
    });

    // Configurar JWT no Swagger
    c.AddSecurityDefinition("Bearer", new Microsoft.OpenApi.Models.OpenApiSecurityScheme
    {
        Description = "JWT Authorization header usando o esquema Bearer. Exemplo: \"Bearer {token}\"",
        Name = "Authorization",
        In = Microsoft.OpenApi.Models.ParameterLocation.Header,
        Type = Microsoft.OpenApi.Models.SecuritySchemeType.ApiKey,
        Scheme = "Bearer"
    });

    c.AddSecurityRequirement(new Microsoft.OpenApi.Models.OpenApiSecurityRequirement
    {
        {
            new Microsoft.OpenApi.Models.OpenApiSecurityScheme
            {
                Reference = new Microsoft.OpenApi.Models.OpenApiReference
                {
                    Type = Microsoft.OpenApi.Models.ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            Array.Empty<string>()
        }
    });
});

var app = builder.Build();

// Initialize database with seed data
using (var scope = app.Services.CreateScope())
{
    var services = scope.ServiceProvider;
    var context = services.GetRequiredService<PetshopContext>();
    DataInitializer.Initialize(context);
}

// Configure the HTTP request pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "Petshop API v1");
        c.RoutePrefix = string.Empty; // Swagger na raiz
    });
}

app.UseHttpsRedirection();

// Use environment-specific CORS policy
var corsPolicy = app.Environment.IsDevelopment() ? "Development" : "Production";
app.UseCors(corsPolicy);

// Use JWT Middleware
app.UseMiddleware<JwtMiddleware>();

// Use Authentication and Authorization
app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

app.Run();
