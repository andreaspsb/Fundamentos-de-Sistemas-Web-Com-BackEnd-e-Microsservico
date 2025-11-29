using Microsoft.Azure.Functions.Worker;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Petshop.Shared.Data;
using Petshop.Shared.Security;
using Petshop.Shared.ServiceClients;
using Petshop.Shared.ServiceClients.Extensions;

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

        // Customer Service Client com resiliência
        var customerServiceUrl = context.Configuration["Services:Customers"] ?? "http://localhost:7071/api/";
        services.AddHttpClient<ICustomerServiceClient, CustomerServiceClient>(client =>
        {
            client.BaseAddress = new Uri(customerServiceUrl);
            client.DefaultRequestHeaders.Add("Accept", "application/json");
        })
        .AddPetshopResilience();
    })
    .Build();

host.Run();
