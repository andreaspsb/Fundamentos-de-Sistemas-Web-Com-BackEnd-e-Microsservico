namespace Petshop.Shared.Enums;

/// <summary>
/// Status do pedido - valores compatíveis com Spring Boot (VARCHAR)
/// </summary>
public enum StatusPedido
{
    PENDENTE,
    CONFIRMADO,
    PROCESSANDO,
    ENVIADO,
    ENTREGUE,
    CANCELADO
}
