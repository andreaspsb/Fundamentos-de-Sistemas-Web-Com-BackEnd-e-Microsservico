using System.Text.Json;
using Microsoft.Azure.Functions.Worker;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Petshop.Shared.Data;
using Petshop.Shared.Messages;

namespace Petshop.Functions.Catalog;

/// <summary>
/// Triggers de Service Bus para processamento assíncrono de estoque
/// </summary>
public class StockTriggers
{
    private readonly ILogger<StockTriggers> _logger;
    private readonly PetshopDbContext _context;
    private readonly IConfiguration _configuration;

    public StockTriggers(
        ILogger<StockTriggers> logger,
        PetshopDbContext context,
        IConfiguration configuration)
    {
        _logger = logger;
        _context = context;
        _configuration = configuration;
    }

    /// <summary>
    /// Processa dedução de estoque quando um pedido é confirmado.
    /// Fila: stock-deduction
    /// </summary>
    [Function("ProcessStockDeduction")]
    public async Task ProcessStockDeduction(
        [ServiceBusTrigger("stock-deduction", Connection = "ServiceBusConnection")] string messageBody)
    {
        _logger.LogInformation("Processando dedução de estoque");

        try
        {
            var message = JsonSerializer.Deserialize<StockDeductionMessage>(messageBody);
            
            if (message == null)
            {
                _logger.LogWarning("Mensagem de dedução de estoque inválida");
                return;
            }

            foreach (var item in message.Items)
            {
                var produto = await _context.Produtos.FindAsync(item.ProdutoId);
                
                if (produto == null)
                {
                    _logger.LogWarning("Produto {ProdutoId} não encontrado para dedução de estoque do pedido {PedidoId}", 
                        item.ProdutoId, message.PedidoId);
                    continue;
                }

                if (produto.TemEstoque(item.Quantidade))
                {
                    produto.ReduzirEstoque(item.Quantidade);
                    _logger.LogInformation("Estoque deduzido: Produto {ProdutoId}, Quantidade {Quantidade}, Pedido {PedidoId}", 
                        item.ProdutoId, item.Quantidade, message.PedidoId);
                }
                else
                {
                    _logger.LogWarning("Estoque insuficiente: Produto {ProdutoId}, Disponível {Disponivel}, Solicitado {Solicitado}", 
                        item.ProdutoId, produto.QuantidadeEstoque, item.Quantidade);
                }
            }

            await _context.SaveChangesAsync();
            _logger.LogInformation("Dedução de estoque concluída para pedido {PedidoId}", message.PedidoId);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao processar dedução de estoque");
            throw; // Re-throw para mover mensagem para dead-letter queue
        }
    }

    /// <summary>
    /// Restaura estoque quando um pedido é cancelado.
    /// Fila: stock-restore
    /// </summary>
    [Function("ProcessStockRestore")]
    public async Task ProcessStockRestore(
        [ServiceBusTrigger("stock-restore", Connection = "ServiceBusConnection")] string messageBody)
    {
        _logger.LogInformation("Processando restauração de estoque");

        try
        {
            var message = JsonSerializer.Deserialize<StockRestoreMessage>(messageBody);
            
            if (message == null)
            {
                _logger.LogWarning("Mensagem de restauração de estoque inválida");
                return;
            }

            foreach (var item in message.Items)
            {
                var produto = await _context.Produtos.FindAsync(item.ProdutoId);
                
                if (produto == null)
                {
                    _logger.LogWarning("Produto {ProdutoId} não encontrado para restauração de estoque do pedido {PedidoId}", 
                        item.ProdutoId, message.PedidoId);
                    continue;
                }

                produto.AdicionarEstoque(item.Quantidade);
                _logger.LogInformation("Estoque restaurado: Produto {ProdutoId}, Quantidade {Quantidade}, Pedido {PedidoId}", 
                    item.ProdutoId, item.Quantidade, message.PedidoId);
            }

            await _context.SaveChangesAsync();
            _logger.LogInformation("Restauração de estoque concluída para pedido {PedidoId}", message.PedidoId);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao processar restauração de estoque");
            throw;
        }
    }

    /// <summary>
    /// Timer trigger para verificar produtos com estoque baixo diariamente.
    /// Executa às 8h da manhã, horário de Brasília.
    /// </summary>
    [Function("CheckLowStockAlerts")]
    public async Task CheckLowStockAlerts(
        [TimerTrigger("0 0 11 * * *")] TimerInfo timer) // 11:00 UTC = 08:00 BRT
    {
        _logger.LogInformation("Verificação de estoque baixo iniciada às {Time}", DateTime.UtcNow);

        try
        {
            var threshold = int.Parse(_configuration["LowStockThreshold"] ?? "10");
            
            var produtosEstoqueBaixo = await _context.Produtos
                .Where(p => p.QuantidadeEstoque <= threshold && p.Ativo)
                .Select(p => new { p.Id, p.Nome, p.QuantidadeEstoque })
                .ToListAsync();

            if (produtosEstoqueBaixo.Any())
            {
                _logger.LogWarning("ALERTA: {Count} produtos com estoque baixo (≤ {Threshold}):", 
                    produtosEstoqueBaixo.Count, threshold);
                
                foreach (var produto in produtosEstoqueBaixo)
                {
                    _logger.LogWarning("  - Produto {Id}: {Nome} - Estoque: {Estoque}", 
                        produto.Id, produto.Nome, produto.QuantidadeEstoque);
                }

                // TODO: Enviar notificação por email ou Service Bus para sistema de notificações
            }
            else
            {
                _logger.LogInformation("Nenhum produto com estoque baixo encontrado");
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro na verificação de estoque baixo");
        }

        if (timer.ScheduleStatus is not null)
        {
            _logger.LogInformation("Próxima verificação de estoque baixo: {NextRun}", 
                timer.ScheduleStatus.Next);
        }
    }
}
