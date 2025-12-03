namespace Petshop.Shared.Enums;

/// <summary>
/// Status do agendamento - valores compatíveis com Spring Boot (VARCHAR)
/// </summary>
public enum StatusAgendamento
{
    PENDENTE,
    CONFIRMADO,
    EM_ANDAMENTO,
    CONCLUIDO,
    CANCELADO
}
