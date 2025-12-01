using System.Text.Json;
using Azure.Messaging.ServiceBus;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace Petshop.Shared.Messaging;

/// <summary>
/// Implementação usando Azure Service Bus.
/// Para uso em produção no Azure.
/// </summary>
public class ServiceBusMessageBroker : IMessageBroker, IAsyncDisposable
{
    private readonly ServiceBusClient? _client;
    private readonly ILogger<ServiceBusMessageBroker> _logger;
    private readonly Dictionary<string, ServiceBusSender> _senders = new();
    private readonly SemaphoreSlim _semaphore = new(1, 1);
    private bool _isAvailable;

    public ServiceBusMessageBroker(
        IOptions<MessageBrokerOptions> options,
        ILogger<ServiceBusMessageBroker> logger)
    {
        _logger = logger;
        
        var connectionString = options.Value.ServiceBusConnection;
        
        if (!string.IsNullOrEmpty(connectionString) && 
            connectionString.Contains("Endpoint=") && 
            connectionString.Contains("SharedAccessKey="))
        {
            try
            {
                _client = new ServiceBusClient(connectionString);
                _isAvailable = true;
                _logger.LogInformation("ServiceBusMessageBroker initialized successfully");
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "Failed to initialize ServiceBusClient");
                _client = null;
                _isAvailable = false;
            }
        }
        else
        {
            _logger.LogWarning("ServiceBus connection string not configured or invalid");
            _isAvailable = false;
        }
    }

    private async Task<ServiceBusSender?> GetOrCreateSenderAsync(string destination)
    {
        if (_client == null) return null;
        
        await _semaphore.WaitAsync();
        try
        {
            if (!_senders.TryGetValue(destination, out var sender))
            {
                sender = _client.CreateSender(destination);
                _senders[destination] = sender;
            }
            return sender;
        }
        finally
        {
            _semaphore.Release();
        }
    }

    public async Task SendMessageAsync<T>(string destination, T message, CancellationToken cancellationToken = default) where T : class
    {
        if (!_isAvailable || _client == null)
        {
            _logger.LogWarning("ServiceBus not available. Message to '{Destination}' was not sent.", destination);
            return;
        }

        try
        {
            var sender = await GetOrCreateSenderAsync(destination);
            if (sender == null)
            {
                _logger.LogWarning("Could not create sender for '{Destination}'", destination);
                return;
            }
            
            var jsonMessage = JsonSerializer.Serialize(message, new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            });
            
            var serviceBusMessage = new ServiceBusMessage(jsonMessage)
            {
                ContentType = "application/json",
                Subject = typeof(T).Name
            };
            
            await sender.SendMessageAsync(serviceBusMessage, cancellationToken);
            
            _logger.LogInformation("Message sent to ServiceBus queue/topic '{Destination}': {MessageType}", 
                destination, typeof(T).Name);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error sending message to ServiceBus '{Destination}'", destination);
            throw;
        }
    }

    public async Task SendMessagesAsync<T>(string destination, IEnumerable<T> messages, CancellationToken cancellationToken = default) where T : class
    {
        if (!_isAvailable || _client == null)
        {
            _logger.LogWarning("ServiceBus not available. Batch messages to '{Destination}' were not sent.", destination);
            return;
        }

        try
        {
            var sender = await GetOrCreateSenderAsync(destination);
            if (sender == null) return;
            
            var serviceBusMessages = messages.Select(message =>
            {
                var jsonMessage = JsonSerializer.Serialize(message, new JsonSerializerOptions
                {
                    PropertyNamingPolicy = JsonNamingPolicy.CamelCase
                });
                
                return new ServiceBusMessage(jsonMessage)
                {
                    ContentType = "application/json",
                    Subject = typeof(T).Name
                };
            }).ToList();
            
            // Enviar em batches de até 100 mensagens
            const int batchSize = 100;
            for (int i = 0; i < serviceBusMessages.Count; i += batchSize)
            {
                var batch = serviceBusMessages.Skip(i).Take(batchSize);
                await sender.SendMessagesAsync(batch, cancellationToken);
            }
            
            _logger.LogInformation("Batch of {Count} messages sent to ServiceBus '{Destination}'", 
                serviceBusMessages.Count, destination);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error sending batch messages to ServiceBus '{Destination}'", destination);
            throw;
        }
    }

    public Task<bool> IsAvailableAsync(CancellationToken cancellationToken = default)
    {
        return Task.FromResult(_isAvailable && _client != null);
    }

    public async ValueTask DisposeAsync()
    {
        foreach (var sender in _senders.Values)
        {
            await sender.DisposeAsync();
        }
        _senders.Clear();

        if (_client != null)
        {
            await _client.DisposeAsync();
        }
    }
}
