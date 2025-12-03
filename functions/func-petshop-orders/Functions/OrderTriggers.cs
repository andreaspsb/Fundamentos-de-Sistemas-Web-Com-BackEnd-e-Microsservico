using Microsoft.Azure.Functions.Worker;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Petshop.Shared.Data;
using Petshop.Shared.Enums;

namespace Petshop.Functions.Orders;

/// <summary>
/// Triggers automáticos para gerenciamento de pedidos
/// </summary>
public class OrderTriggers
{
    private readonly ILogger<OrderTriggers> _logger;
    private readonly PetshopDbContext _context;

    public OrderTriggers(
        ILogger<OrderTriggers> logger,
        PetshopDbContext context)
    {
        _logger = logger;
        _context = context;
    }

    /// <summary>
    /// Timer trigger para cancelar pedidos pendentes há mais de 24 horas.
    /// Executa a cada hora.
    /// </summary>
    [Function("CancelAbandonedOrders")]
    public async Task CancelAbandonedOrders(
        [TimerTrigger("0 0 * * * *")] TimerInfo timer) // A cada hora
    {
        _logger.LogInformation("Verificando pedidos abandonados às {Time}", DateTime.UtcNow);

        try
        {
            var limite = DateTime.UtcNow.AddHours(-24);

            var pedidosAbandonados = await _context.Pedidos
                .Where(p => p.Status == StatusPedido.PENDENTE)
                .Where(p => p.DataPedido < limite)
                .ToListAsync();

            if (pedidosAbandonados.Any())
            {
                _logger.LogWarning("Encontrados {Count} pedidos abandonados para cancelamento", pedidosAbandonados.Count);

                foreach (var pedido in pedidosAbandonados)
                {
                    pedido.Status = StatusPedido.CANCELADO;
                    pedido.Observacoes = (pedido.Observacoes ?? "") + " [Cancelado automaticamente por inatividade]";
                    _logger.LogWarning("Pedido #{Id} cancelado automaticamente", pedido.Id);
                }

                await _context.SaveChangesAsync();
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao cancelar pedidos abandonados");
        }

        if (timer.ScheduleStatus is not null)
        {
            _logger.LogInformation("Próxima verificação de pedidos abandonados: {NextRun}", timer.ScheduleStatus.Next);
        }
    }

    /// <summary>
    /// Timer trigger para gerar relatório diário de pedidos.
    /// Executa às 23h (horário de Brasília).
    /// </summary>
    [Function("GenerateDailyOrderReport")]
    public async Task GenerateDailyOrderReport(
        [TimerTrigger("0 0 2 * * *")] TimerInfo timer) // 02:00 UTC = 23:00 BRT
    {
        _logger.LogInformation("Gerando relatório diário de pedidos às {Time}", DateTime.UtcNow);

        try
        {
            var hoje = DateTime.UtcNow.Date;
            var ontem = hoje.AddDays(-1);

            var pedidosHoje = await _context.Pedidos
                .Where(p => p.DataPedido >= ontem && p.DataPedido < hoje)
                .ToListAsync();

            var totalPedidos = pedidosHoje.Count;
            var valorTotal = pedidosHoje.Sum(p => p.ValorTotal);
            var pedidosConcluidos = pedidosHoje.Count(p => p.Status == StatusPedido.ENTREGUE);
            var pedidosCancelados = pedidosHoje.Count(p => p.Status == StatusPedido.CANCELADO);

            _logger.LogInformation("=== RELATÓRIO DIÁRIO DE PEDIDOS ({Data:yyyy-MM-dd}) ===", ontem);
            _logger.LogInformation("Total de pedidos: {Total}", totalPedidos);
            _logger.LogInformation("Valor total: R$ {Valor:N2}", valorTotal);
            _logger.LogInformation("Pedidos entregues: {Entregues}", pedidosConcluidos);
            _logger.LogInformation("Pedidos cancelados: {Cancelados}", pedidosCancelados);
            _logger.LogInformation("Ticket médio: R$ {Ticket:N2}", totalPedidos > 0 ? valorTotal / totalPedidos : 0);
            _logger.LogInformation("=========================================");

            // TODO: Enviar relatório por email ou salvar em storage
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao gerar relatório diário de pedidos");
        }

        if (timer.ScheduleStatus is not null)
        {
            _logger.LogInformation("Próximo relatório: {NextRun}", timer.ScheduleStatus.Next);
        }
    }

    /// <summary>
    /// Timer trigger para verificar pedidos em atraso.
    /// Executa a cada 4 horas.
    /// </summary>
    [Function("CheckDelayedOrders")]
    public async Task CheckDelayedOrders(
        [TimerTrigger("0 0 */4 * * *")] TimerInfo timer) // A cada 4 horas
    {
        _logger.LogInformation("Verificando pedidos em atraso às {Time}", DateTime.UtcNow);

        try
        {
            // Pedidos confirmados há mais de 2 dias que ainda não foram enviados
            var limiteConfirmacao = DateTime.UtcNow.AddDays(-2);
            
            var pedidosAtrasados = await _context.Pedidos
                .Include(p => p.Cliente)
                .Where(p => p.Status == StatusPedido.CONFIRMADO || p.Status == StatusPedido.PROCESSANDO)
                .Where(p => p.DataPedido < limiteConfirmacao)
                .ToListAsync();

            if (pedidosAtrasados.Any())
            {
                _logger.LogWarning("ALERTA: {Count} pedidos em atraso!", pedidosAtrasados.Count);

                foreach (var pedido in pedidosAtrasados)
                {
                    _logger.LogWarning("  - Pedido #{Id}: Cliente {Cliente}, Data: {Data:yyyy-MM-dd}, Status: {Status}",
                        pedido.Id,
                        pedido.Cliente?.Nome ?? "N/A",
                        pedido.DataPedido,
                        pedido.Status);
                }

                // TODO: Enviar notificação para equipe de operações
            }
            else
            {
                _logger.LogInformation("Nenhum pedido em atraso");
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao verificar pedidos em atraso");
        }
    }
}
