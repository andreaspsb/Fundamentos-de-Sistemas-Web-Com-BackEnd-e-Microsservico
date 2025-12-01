using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace Petshop.Shared.Messaging;

/// <summary>
/// Extensões para configuração de Dependency Injection do Message Broker.
/// </summary>
public static class MessageBrokerExtensions
{
    /// <summary>
    /// Adiciona o Message Broker baseado na configuração.
    /// </summary>
    /// <param name="services">Service collection</param>
    /// <param name="configuration">Configuration</param>
    /// <returns>Service collection para encadeamento</returns>
    public static IServiceCollection AddMessageBroker(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        // Registrar opções
        services.Configure<MessageBrokerOptions>(configuration.GetSection(MessageBrokerOptions.SectionName));
        
        // Obter configuração
        var options = new MessageBrokerOptions();
        configuration.GetSection(MessageBrokerOptions.SectionName).Bind(options);
        
        // Auto-detectar provider se não especificado
        if (string.IsNullOrEmpty(options.Provider) || options.Provider == "Auto")
        {
            options.Provider = DetectProvider(options, configuration);
        }
        
        // Registrar implementação baseada no provider
        return options.Provider.ToLowerInvariant() switch
        {
            "servicebus" => services.AddSingleton<IMessageBroker, ServiceBusMessageBroker>(),
            "azurestoragequeue" or "storagequeue" => services.AddSingleton<IMessageBroker, AzureStorageQueueBroker>(),
            "inmemory" => services.AddSingleton<IMessageBroker, InMemoryMessageBroker>(),
            _ => services.AddSingleton<IMessageBroker, InMemoryMessageBroker>()
        };
    }
    
    /// <summary>
    /// Adiciona o Message Broker com provider específico.
    /// </summary>
    public static IServiceCollection AddMessageBroker(
        this IServiceCollection services,
        IConfiguration configuration,
        string provider)
    {
        services.Configure<MessageBrokerOptions>(opts =>
        {
            configuration.GetSection(MessageBrokerOptions.SectionName).Bind(opts);
            opts.Provider = provider;
        });
        
        return provider.ToLowerInvariant() switch
        {
            "servicebus" => services.AddSingleton<IMessageBroker, ServiceBusMessageBroker>(),
            "azurestoragequeue" or "storagequeue" => services.AddSingleton<IMessageBroker, AzureStorageQueueBroker>(),
            "inmemory" => services.AddSingleton<IMessageBroker, InMemoryMessageBroker>(),
            _ => services.AddSingleton<IMessageBroker, InMemoryMessageBroker>()
        };
    }
    
    /// <summary>
    /// Adiciona Message Broker em memória (para testes).
    /// </summary>
    public static IServiceCollection AddInMemoryMessageBroker(this IServiceCollection services)
    {
        services.Configure<MessageBrokerOptions>(opts => opts.Provider = "InMemory");
        return services.AddSingleton<IMessageBroker, InMemoryMessageBroker>();
    }
    
    /// <summary>
    /// Adiciona Azure Storage Queue como Message Broker (funciona com Azurite).
    /// </summary>
    public static IServiceCollection AddAzureStorageQueueBroker(
        this IServiceCollection services,
        string connectionString = "UseDevelopmentStorage=true")
    {
        services.Configure<MessageBrokerOptions>(opts =>
        {
            opts.Provider = "AzureStorageQueue";
            opts.AzureStorageConnection = connectionString;
        });
        return services.AddSingleton<IMessageBroker, AzureStorageQueueBroker>();
    }
    
    /// <summary>
    /// Adiciona Azure Service Bus como Message Broker.
    /// </summary>
    public static IServiceCollection AddServiceBusMessageBroker(
        this IServiceCollection services,
        string connectionString)
    {
        services.Configure<MessageBrokerOptions>(opts =>
        {
            opts.Provider = "ServiceBus";
            opts.ServiceBusConnection = connectionString;
        });
        return services.AddSingleton<IMessageBroker, ServiceBusMessageBroker>();
    }
    
    /// <summary>
    /// Detecta automaticamente o provider baseado nas connection strings disponíveis.
    /// </summary>
    private static string DetectProvider(MessageBrokerOptions options, IConfiguration configuration)
    {
        // Verificar Service Bus
        var serviceBusConn = options.ServiceBusConnection 
            ?? configuration["ServiceBusConnection"];
        if (!string.IsNullOrEmpty(serviceBusConn) && 
            serviceBusConn.Contains("Endpoint=") && 
            serviceBusConn.Contains("SharedAccessKey="))
        {
            return "ServiceBus";
        }
        
        // Verificar Azure Storage
        var storageConn = options.AzureStorageConnection 
            ?? configuration["AzureWebJobsStorage"]
            ?? configuration.GetConnectionString("AzureStorage");
        if (!string.IsNullOrEmpty(storageConn))
        {
            // UseDevelopmentStorage=true significa Azurite
            return "AzureStorageQueue";
        }
        
        // Fallback para InMemory
        return "InMemory";
    }
}
