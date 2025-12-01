using System.Text.Json;

namespace Petshop.Shared.Messaging;

/// <summary>
/// Interface para abstração de mensageria.
/// Permite trocar entre Azure Service Bus, RabbitMQ, ou Azure Storage Queues.
/// </summary>
public interface IMessageBroker
{
    /// <summary>
    /// Envia uma mensagem para uma fila ou tópico.
    /// </summary>
    Task SendMessageAsync<T>(string destination, T message, CancellationToken cancellationToken = default) where T : class;
    
    /// <summary>
    /// Envia múltiplas mensagens em batch.
    /// </summary>
    Task SendMessagesAsync<T>(string destination, IEnumerable<T> messages, CancellationToken cancellationToken = default) where T : class;
    
    /// <summary>
    /// Verifica se o broker está disponível.
    /// </summary>
    Task<bool> IsAvailableAsync(CancellationToken cancellationToken = default);
}

/// <summary>
/// Configuração do Message Broker
/// </summary>
public class MessageBrokerOptions
{
    public const string SectionName = "MessageBroker";
    
    /// <summary>
    /// Tipo do broker: ServiceBus, RabbitMQ, AzureStorageQueue, InMemory
    /// </summary>
    public string Provider { get; set; } = "InMemory";
    
    /// <summary>
    /// Connection string do Azure Service Bus
    /// </summary>
    public string? ServiceBusConnection { get; set; }
    
    /// <summary>
    /// Connection string do RabbitMQ (amqp://user:pass@host:port/vhost)
    /// </summary>
    public string? RabbitMQConnection { get; set; }
    
    /// <summary>
    /// Connection string do Azure Storage (para Queue Storage)
    /// </summary>
    public string? AzureStorageConnection { get; set; }
}

/// <summary>
/// Implementação em memória para testes e desenvolvimento sem infraestrutura.
/// </summary>
public class InMemoryMessageBroker : IMessageBroker
{
    private readonly Dictionary<string, List<object>> _queues = new();
    private readonly object _lock = new();

    public Task SendMessageAsync<T>(string destination, T message, CancellationToken cancellationToken = default) where T : class
    {
        lock (_lock)
        {
            if (!_queues.ContainsKey(destination))
            {
                _queues[destination] = new List<object>();
            }
            _queues[destination].Add(message);
        }
        
        Console.WriteLine($"[InMemory] Message sent to '{destination}': {JsonSerializer.Serialize(message)}");
        return Task.CompletedTask;
    }

    public Task SendMessagesAsync<T>(string destination, IEnumerable<T> messages, CancellationToken cancellationToken = default) where T : class
    {
        foreach (var message in messages)
        {
            SendMessageAsync(destination, message, cancellationToken);
        }
        return Task.CompletedTask;
    }

    public Task<bool> IsAvailableAsync(CancellationToken cancellationToken = default)
    {
        return Task.FromResult(true);
    }

    /// <summary>
    /// Obtém mensagens de uma fila (para testes).
    /// </summary>
    public List<T> GetMessages<T>(string destination) where T : class
    {
        lock (_lock)
        {
            if (_queues.TryGetValue(destination, out var messages))
            {
                return messages.Cast<T>().ToList();
            }
            return new List<T>();
        }
    }

    /// <summary>
    /// Limpa todas as filas (para testes).
    /// </summary>
    public void Clear()
    {
        lock (_lock)
        {
            _queues.Clear();
        }
    }
}

/// <summary>
/// Nomes das filas/tópicos usados na aplicação.
/// </summary>
public static class QueueNames
{
    public const string StockDeduction = "stock-deduction";
    public const string StockRestore = "stock-restore";
    public const string OrderConfirmed = "order-confirmed";
    public const string OrderCancelled = "order-cancelled";
    public const string OrderStatusChanged = "order-status-changed";
    public const string AppointmentCreated = "appointment-created";
    public const string AppointmentReminder = "appointment-reminder";
    public const string LowStockAlert = "low-stock-alert";
}
