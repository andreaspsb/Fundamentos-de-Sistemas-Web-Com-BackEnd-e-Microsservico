namespace Petshop.Shared.Messages;

/// <summary>
/// Mensagem enviada quando um pedido é confirmado.
/// O Catalog Service consome para deduzir estoque.
/// </summary>
public class OrderConfirmedMessage
{
    public long PedidoId { get; set; }
    public long ClienteId { get; set; }
    public DateTime DataPedido { get; set; }
    public List<OrderItemMessage> Itens { get; set; } = new();
}

public class OrderItemMessage
{
    public long ProdutoId { get; set; }
    public int Quantidade { get; set; }
}

/// <summary>
/// Mensagem enviada quando um pedido é cancelado.
/// O Catalog Service consome para restaurar estoque.
/// </summary>
public class OrderCancelledMessage
{
    public long PedidoId { get; set; }
    public long ClienteId { get; set; }
    public List<OrderItemMessage> Itens { get; set; } = new();
}

/// <summary>
/// Mensagem enviada quando o status de um pedido muda.
/// Pode ser consumida por um serviço de notificação.
/// </summary>
public class OrderStatusChangedMessage
{
    public long PedidoId { get; set; }
    public long ClienteId { get; set; }
    public string ClienteEmail { get; set; } = string.Empty;
    public string StatusAnterior { get; set; } = string.Empty;
    public string StatusNovo { get; set; } = string.Empty;
    public DateTime DataAlteracao { get; set; }
}

/// <summary>
/// Mensagem enviada quando um agendamento é criado.
/// Pode ser consumida por um serviço de notificação.
/// </summary>
public class AppointmentCreatedMessage
{
    public long AgendamentoId { get; set; }
    public long ClienteId { get; set; }
    public string ClienteEmail { get; set; } = string.Empty;
    public string ClienteNome { get; set; } = string.Empty;
    public string PetNome { get; set; } = string.Empty;
    public DateTime DataAgendamento { get; set; }
    public TimeSpan Horario { get; set; }
    public List<string> Servicos { get; set; } = new();
}

/// <summary>
/// Mensagem para lembretes de agendamento.
/// Enviada pelo Timer Trigger 24h antes.
/// </summary>
public class AppointmentReminderMessage
{
    public long AgendamentoId { get; set; }
    public string ClienteEmail { get; set; } = string.Empty;
    public string ClienteNome { get; set; } = string.Empty;
    public string PetNome { get; set; } = string.Empty;
    public DateTime DataAgendamento { get; set; }
    public TimeSpan Horario { get; set; }
    public string MetodoAtendimento { get; set; } = string.Empty;
}

/// <summary>
/// Mensagem para alertas de estoque baixo.
/// Enviada pelo Timer Trigger do Catalog Service.
/// </summary>
public class LowStockAlertMessage
{
    public long ProdutoId { get; set; }
    public string ProdutoNome { get; set; } = string.Empty;
    public int QuantidadeAtual { get; set; }
    public int LimiteMinimo { get; set; }
    public DateTime DataAlerta { get; set; }
}

/// <summary>
/// Mensagem para dedução de estoque.
/// Consumida pelo Catalog Service.
/// </summary>
public class StockDeductionMessage
{
    public long PedidoId { get; set; }
    public List<StockItemMessage> Items { get; set; } = new();
}

/// <summary>
/// Mensagem para restauração de estoque.
/// Consumida pelo Catalog Service quando pedido é cancelado.
/// </summary>
public class StockRestoreMessage
{
    public long PedidoId { get; set; }
    public List<StockItemMessage> Items { get; set; } = new();
}

public class StockItemMessage
{
    public long ProdutoId { get; set; }
    public int Quantidade { get; set; }
}

/// <summary>
/// Mensagem para confirmação de agendamento pelo cliente.
/// </summary>
public class SchedulingConfirmationMessage
{
    public long AgendamentoId { get; set; }
    public long ClienteId { get; set; }
    public bool Confirmado { get; set; }
    public string? MotivoRecusa { get; set; }
}

