using Azure.Messaging.ServiceBus;
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

        // Service Bus Client para enviar mensagens (opcional em desenvolvimento)
        var serviceBusConnectionString = context.Configuration["ServiceBusConnection"];
        if (!string.IsNullOrEmpty(serviceBusConnectionString) && 
            !serviceBusConnectionString.Equals("UseDevelopmentStorage=true", StringComparison.OrdinalIgnoreCase) &&
            serviceBusConnectionString.Contains("Endpoint=") &&
            serviceBusConnectionString.Contains("SharedAccessKey="))
        {
            try
            {
                services.AddSingleton(new ServiceBusClient(serviceBusConnectionString));
            }
            catch (Exception)
            {
                // Em caso de erro na conexão, registra null
                services.AddSingleton<ServiceBusClient>(sp => null!);
            }
        }
        else
        {
            // Em desenvolvimento local sem Service Bus configurado
            services.AddSingleton<ServiceBusClient>(sp => null!);
        }

        // HTTP Clients com Resilience para comunicação entre microsserviços
        var customerServiceUrl = context.Configuration["Services:Customers"] ?? "http://localhost:7072/api";
        var catalogServiceUrl = context.Configuration["Services:Catalog"] ?? "http://localhost:7074/api";

        services.AddHttpClient<ICustomerServiceClient, CustomerServiceClient>(client =>
        {
            client.BaseAddress = new Uri(customerServiceUrl);
            client.Timeout = TimeSpan.FromSeconds(30);
        })
        .AddStandardResilienceHandler(options =>
        {
            options.Retry.MaxRetryAttempts = 2;
            options.Retry.Delay = TimeSpan.FromMilliseconds(200);
            options.AttemptTimeout.Timeout = TimeSpan.FromSeconds(5);
            options.CircuitBreaker.SamplingDuration = TimeSpan.FromSeconds(60);
            options.TotalRequestTimeout.Timeout = TimeSpan.FromSeconds(20);
        });

        services.AddHttpClient<ICatalogServiceClient, CatalogServiceClient>(client =>
        {
            client.BaseAddress = new Uri(catalogServiceUrl);
            client.Timeout = TimeSpan.FromSeconds(30);
        })
        .AddStandardResilienceHandler(options =>
        {
            options.Retry.MaxRetryAttempts = 2;
            options.Retry.Delay = TimeSpan.FromMilliseconds(200);
            options.AttemptTimeout.Timeout = TimeSpan.FromSeconds(5);
            options.CircuitBreaker.SamplingDuration = TimeSpan.FromSeconds(60);
            options.TotalRequestTimeout.Timeout = TimeSpan.FromSeconds(20);
        });
    })
    .Build();

host.Run();
