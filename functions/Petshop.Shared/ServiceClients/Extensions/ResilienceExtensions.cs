using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Http.Resilience;
using Polly;

namespace Petshop.Shared.ServiceClients.Extensions;

public static class ResilienceExtensions
{
    public static IHttpStandardResiliencePipelineBuilder AddPetshopResilience(this IHttpClientBuilder builder)
    {
        return builder.AddStandardResilienceHandler(options =>
        {
            // Retry: 3 tentativas com exponential backoff
            options.Retry.MaxRetryAttempts = 3;
            options.Retry.Delay = TimeSpan.FromMilliseconds(200);
            options.Retry.BackoffType = Polly.DelayBackoffType.Exponential;

            // Timeout: 5 segundos por requisição individual
            options.AttemptTimeout.Timeout = TimeSpan.FromSeconds(5);

            // Circuit Breaker: Abre após 50% de falhas
            // SamplingDuration deve ser > tempo máximo de retry (3 retries x 5s = 15s, usar 60s para segurança)
            options.CircuitBreaker.FailureRatio = 0.5;
            options.CircuitBreaker.SamplingDuration = TimeSpan.FromSeconds(60);
            options.CircuitBreaker.BreakDuration = TimeSpan.FromSeconds(15);
            options.CircuitBreaker.MinimumThroughput = 2;

            // Timeout total (incluindo retries): 30 segundos
            options.TotalRequestTimeout.Timeout = TimeSpan.FromSeconds(30);
        });
    }

    public static IServiceCollection AddServiceClients(this IServiceCollection services, ServiceClientsConfiguration config)
    {
        // Customer Service Client
        services.AddHttpClient<ICustomerServiceClient, CustomerServiceClient>(client =>
        {
            client.BaseAddress = new Uri(config.CustomerServiceUrl);
            client.DefaultRequestHeaders.Add("Accept", "application/json");
        })
        .AddStandardResilienceHandler();

        // Pet Service Client
        services.AddHttpClient<IPetServiceClient, PetServiceClient>(client =>
        {
            client.BaseAddress = new Uri(config.PetServiceUrl);
            client.DefaultRequestHeaders.Add("Accept", "application/json");
        })
        .AddStandardResilienceHandler();

        // Catalog Service Client
        services.AddHttpClient<ICatalogServiceClient, CatalogServiceClient>(client =>
        {
            client.BaseAddress = new Uri(config.CatalogServiceUrl);
            client.DefaultRequestHeaders.Add("Accept", "application/json");
        })
        .AddStandardResilienceHandler();

        return services;
    }
}

public class ServiceClientsConfiguration
{
    public string CustomerServiceUrl { get; set; } = "http://localhost:7071/api/";
    public string PetServiceUrl { get; set; } = "http://localhost:7072/api/";
    public string CatalogServiceUrl { get; set; } = "http://localhost:7073/api/";
    public string SchedulingServiceUrl { get; set; } = "http://localhost:7074/api/";
    public string OrderServiceUrl { get; set; } = "http://localhost:7075/api/";
}
