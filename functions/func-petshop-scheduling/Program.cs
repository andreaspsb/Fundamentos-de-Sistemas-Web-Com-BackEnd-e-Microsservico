using Microsoft.Azure.Functions.Worker;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Petshop.Shared.Data;
using Petshop.Shared.Security;
using Petshop.Shared.ServiceClients;

var host = new HostBuilder()
    .ConfigureFunctionsWebApplication()
    .ConfigureServices((context, services) =>
    {
        services.AddApplicationInsightsTelemetryWorkerService();
        services.ConfigureFunctionsApplicationInsights();

        // Database - suporta SQLite (dev) ou SQL Server (prod)
        var databaseProvider = context.Configuration["DatabaseProvider"] ?? "SqlServer";
        var connectionString = context.Configuration.GetConnectionString("DefaultConnection") 
            ?? context.Configuration.GetConnectionString("SqlConnection")
            ?? context.Configuration["SqlConnection"];
        
        services.AddDbContext<PetshopDbContext>(options =>
        {
            if (databaseProvider.Equals("SQLite", StringComparison.OrdinalIgnoreCase))
            {
                options.UseSqlite(connectionString);
            }
            else
            {
                options.UseSqlServer(connectionString);
            }
        });

        // JWT Service
        services.AddSingleton<JwtService>(provider =>
        {
            var configuration = provider.GetRequiredService<IConfiguration>();
            return new JwtService(
                secretKey: configuration["Jwt:SecretKey"] ?? throw new InvalidOperationException("Jwt:SecretKey not configured"),
                issuer: configuration["Jwt:Issuer"] ?? "PetshopApi",
                audience: configuration["Jwt:Audience"] ?? "PetshopFrontend",
                expirationMinutes: int.Parse(configuration["Jwt:ExpirationMinutes"] ?? "1440")
            );
        });

        // HTTP Clients com Resilience para comunicação entre microsserviços
        var customerServiceUrl = context.Configuration["CustomerServiceBaseUrl"] ?? "http://localhost:7072";
        var petServiceUrl = context.Configuration["PetServiceBaseUrl"] ?? "http://localhost:7073";
        var catalogServiceUrl = context.Configuration["CatalogServiceBaseUrl"] ?? "http://localhost:7074";

        services.AddHttpClient<ICustomerServiceClient, CustomerServiceClient>(client =>
        {
            client.BaseAddress = new Uri(customerServiceUrl);
            client.Timeout = TimeSpan.FromSeconds(30);
        })
        .AddStandardResilienceHandler();

        services.AddHttpClient<IPetServiceClient, PetServiceClient>(client =>
        {
            client.BaseAddress = new Uri(petServiceUrl);
            client.Timeout = TimeSpan.FromSeconds(30);
        })
        .AddStandardResilienceHandler();

        services.AddHttpClient<ICatalogServiceClient, CatalogServiceClient>(client =>
        {
            client.BaseAddress = new Uri(catalogServiceUrl);
            client.Timeout = TimeSpan.FromSeconds(30);
        })
        .AddStandardResilienceHandler();
    })
    .Build();

host.Run();
