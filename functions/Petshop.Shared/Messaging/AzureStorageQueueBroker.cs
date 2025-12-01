using System.Text.Json;
using Azure.Storage.Queues;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace Petshop.Shared.Messaging;

/// <summary>
/// Implementação usando Azure Storage Queues.
/// Funciona com Azurite para desenvolvimento local.
/// </summary>
public class AzureStorageQueueBroker : IMessageBroker
{
    private readonly QueueServiceClient _queueServiceClient;
    private readonly ILogger<AzureStorageQueueBroker> _logger;
    private readonly Dictionary<string, QueueClient> _queueClients = new();
    private readonly SemaphoreSlim _semaphore = new(1, 1);

    public AzureStorageQueueBroker(
        IOptions<MessageBrokerOptions> options,
        ILogger<AzureStorageQueueBroker> logger)
    {
        _logger = logger;
        
        var connectionString = options.Value.AzureStorageConnection 
            ?? "UseDevelopmentStorage=true"; // Azurite por padrão
        
        _queueServiceClient = new QueueServiceClient(connectionString);
        _logger.LogInformation("AzureStorageQueueBroker initialized with connection: {Connection}", 
            connectionString.Contains("devstoreaccount1") ? "Azurite (local)" : "Azure Storage");
    }

    private async Task<QueueClient> GetOrCreateQueueAsync(string queueName, CancellationToken cancellationToken)
    {
        // Normalizar nome da fila (Azure Storage Queue só aceita lowercase e hífens)
        var normalizedName = queueName.ToLowerInvariant().Replace("_", "-");
        
        await _semaphore.WaitAsync(cancellationToken);
        try
        {
            if (!_queueClients.TryGetValue(normalizedName, out var client))
            {
                client = _queueServiceClient.GetQueueClient(normalizedName);
                await client.CreateIfNotExistsAsync(cancellationToken: cancellationToken);
                _queueClients[normalizedName] = client;
                _logger.LogDebug("Queue '{QueueName}' created or exists", normalizedName);
            }
            return client;
        }
        finally
        {
            _semaphore.Release();
        }
    }

    public async Task SendMessageAsync<T>(string destination, T message, CancellationToken cancellationToken = default) where T : class
    {
        try
        {
            var queueClient = await GetOrCreateQueueAsync(destination, cancellationToken);
            
            var jsonMessage = JsonSerializer.Serialize(message, new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            });
            
            // Azure Storage Queue aceita mensagens em Base64
            var base64Message = Convert.ToBase64String(System.Text.Encoding.UTF8.GetBytes(jsonMessage));
            
            await queueClient.SendMessageAsync(base64Message, cancellationToken);
            
            _logger.LogInformation("Message sent to queue '{Queue}': {MessageType}", 
                destination, typeof(T).Name);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error sending message to queue '{Queue}'", destination);
            throw;
        }
    }

    public async Task SendMessagesAsync<T>(string destination, IEnumerable<T> messages, CancellationToken cancellationToken = default) where T : class
    {
        var queueClient = await GetOrCreateQueueAsync(destination, cancellationToken);
        
        foreach (var message in messages)
        {
            var jsonMessage = JsonSerializer.Serialize(message, new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            });
            
            var base64Message = Convert.ToBase64String(System.Text.Encoding.UTF8.GetBytes(jsonMessage));
            await queueClient.SendMessageAsync(base64Message, cancellationToken);
        }
        
        _logger.LogInformation("Batch messages sent to queue '{Queue}'", destination);
    }

    public async Task<bool> IsAvailableAsync(CancellationToken cancellationToken = default)
    {
        try
        {
            // Tenta listar filas para verificar conectividade
            await foreach (var _ in _queueServiceClient.GetQueuesAsync(cancellationToken: cancellationToken))
            {
                break; // Só precisa verificar se consegue conectar
            }
            return true;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Azure Storage Queue is not available");
            return false;
        }
    }
}
