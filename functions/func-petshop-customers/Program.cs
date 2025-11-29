using Microsoft.Azure.Functions.Worker;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Petshop.Shared.Data;
using Petshop.Shared.Security;

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
        services.AddSingleton<JwtService>(sp =>
        {
            var config = sp.GetRequiredService<IConfiguration>();
            return new JwtService(config);
        });
    })
    .Build();

host.Run();
