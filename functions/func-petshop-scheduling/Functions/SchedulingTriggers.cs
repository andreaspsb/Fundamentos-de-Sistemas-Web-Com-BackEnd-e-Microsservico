using System.Text.Json;
using Microsoft.Azure.Functions.Worker;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Petshop.Shared.Data;
using Petshop.Shared.Enums;
using Petshop.Shared.Messages;

namespace Petshop.Functions.Scheduling;

/// <summary>
/// Triggers automáticos para gerenciamento de agendamentos
/// </summary>
public class SchedulingTriggers
{
    private readonly ILogger<SchedulingTriggers> _logger;
    private readonly PetshopDbContext _context;

    public SchedulingTriggers(
        ILogger<SchedulingTriggers> logger,
        PetshopDbContext context)
    {
        _logger = logger;
        _context = context;
    }

    /// <summary>
    /// Timer trigger para enviar lembretes de agendamentos do dia seguinte.
    /// Executa às 18h (horário de Brasília).
    /// </summary>
    [Function("SendAgendamentoReminders")]
    public async Task SendAgendamentoReminders(
        [TimerTrigger("0 0 21 * * *")] TimerInfo timer) // 21:00 UTC = 18:00 BRT
    {
        _logger.LogInformation("Enviando lembretes de agendamentos às {Time}", DateTime.UtcNow);

        try
        {
            var amanha = DateTime.UtcNow.Date.AddDays(1);

            var agendamentosAmanha = await _context.Agendamentos
                .Include(a => a.Cliente)
                .Include(a => a.Pet)
                .Include(a => a.Servicos)
                .Where(a => a.DataAgendamento.Date == amanha)
                .Where(a => a.Status == StatusAgendamento.PENDENTE || a.Status == StatusAgendamento.CONFIRMADO)
                .ToListAsync();

            if (agendamentosAmanha.Any())
            {
                _logger.LogInformation("Encontrados {Count} agendamentos para amanhã", agendamentosAmanha.Count);

                foreach (var agendamento in agendamentosAmanha)
                {
                    var servicosNomes = string.Join(", ", agendamento.Servicos.Select(s => s.Nome));
                    _logger.LogInformation(
                        "LEMBRETE: Agendamento #{Id} - Cliente: {Cliente}, Pet: {Pet}, Serviços: {Servicos} às {Hora}",
                        agendamento.Id,
                        agendamento.Cliente?.Nome ?? "N/A",
                        agendamento.Pet?.Nome ?? "N/A",
                        servicosNomes,
                        agendamento.Horario.ToString(@"hh\:mm"));

                    // TODO: Integrar com serviço de notificação (email, SMS, push)
                    // Enviar mensagem para fila de notificações
                }
            }
            else
            {
                _logger.LogInformation("Nenhum agendamento para amanhã");
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao enviar lembretes de agendamentos");
        }

        if (timer.ScheduleStatus is not null)
        {
            _logger.LogInformation("Próximo envio de lembretes: {NextRun}", timer.ScheduleStatus.Next);
        }
    }

    /// <summary>
    /// Timer trigger para marcar agendamentos não comparecidos.
    /// Executa a cada hora para verificar agendamentos que passaram.
    /// </summary>
    [Function("CheckMissedAppointments")]
    public async Task CheckMissedAppointments(
        [TimerTrigger("0 0 * * * *")] TimerInfo timer) // A cada hora
    {
        _logger.LogInformation("Verificando agendamentos não comparecidos às {Time}", DateTime.UtcNow);

        try
        {
            // Agendamentos que passaram há mais de 2 horas e ainda estão pendentes/confirmados
            var agora = DateTime.UtcNow;
            var hojeData = agora.Date;
            var horaLimite = agora.TimeOfDay.Subtract(TimeSpan.FromHours(2));

            var agendamentosNaoComparecidos = await _context.Agendamentos
                .Where(a => a.DataAgendamento.Date < hojeData || 
                           (a.DataAgendamento.Date == hojeData && a.Horario < horaLimite))
                .Where(a => a.Status == StatusAgendamento.PENDENTE || a.Status == StatusAgendamento.CONFIRMADO)
                .ToListAsync();

            if (agendamentosNaoComparecidos.Any())
            {
                _logger.LogWarning("Encontrados {Count} agendamentos não comparecidos", agendamentosNaoComparecidos.Count);

                foreach (var agendamento in agendamentosNaoComparecidos)
                {
                    agendamento.Status = StatusAgendamento.CANCELADO;
                    agendamento.Observacoes = (agendamento.Observacoes ?? "") + " [Cancelado automaticamente - não compareceu]";
                    _logger.LogWarning("Agendamento #{Id} marcado como cancelado (não compareceu)", agendamento.Id);
                }

                await _context.SaveChangesAsync();
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao verificar agendamentos não comparecidos");
        }
    }

    /// <summary>
    /// Timer trigger para iniciar agendamentos na hora marcada.
    /// Executa a cada 15 minutos.
    /// </summary>
    [Function("AutoStartAppointments")]
    public async Task AutoStartAppointments(
        [TimerTrigger("0 */15 * * * *")] TimerInfo timer) // A cada 15 minutos
    {
        _logger.LogInformation("Verificando agendamentos para iniciar automaticamente às {Time}", DateTime.UtcNow);

        try
        {
            var agora = DateTime.UtcNow;
            var hojeData = agora.Date;
            var horaAtual = agora.TimeOfDay;
            var tolerancia = TimeSpan.FromMinutes(10); // 10 minutos de tolerância

            var agendamentosParaIniciar = await _context.Agendamentos
                .Where(a => a.DataAgendamento.Date == hojeData)
                .Where(a => a.Horario >= horaAtual.Subtract(tolerancia) && a.Horario <= horaAtual)
                .Where(a => a.Status == StatusAgendamento.CONFIRMADO)
                .ToListAsync();

            if (agendamentosParaIniciar.Any())
            {
                _logger.LogInformation("Iniciando {Count} agendamentos automaticamente", agendamentosParaIniciar.Count);

                foreach (var agendamento in agendamentosParaIniciar)
                {
                    agendamento.Status = StatusAgendamento.EM_ANDAMENTO;
                    _logger.LogInformation("Agendamento #{Id} iniciado automaticamente", agendamento.Id);
                }

                await _context.SaveChangesAsync();
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao iniciar agendamentos automaticamente");
        }
    }

    /// <summary>
    /// Timer trigger para gerar relatório diário de agendamentos.
    /// Executa às 23h (horário de Brasília).
    /// </summary>
    [Function("GenerateDailySchedulingReport")]
    public async Task GenerateDailySchedulingReport(
        [TimerTrigger("0 0 2 * * *")] TimerInfo timer) // 02:00 UTC = 23:00 BRT
    {
        _logger.LogInformation("Gerando relatório diário de agendamentos às {Time}", DateTime.UtcNow);

        try
        {
            var hoje = DateTime.UtcNow.Date;
            var ontem = hoje.AddDays(-1);

            var agendamentosHoje = await _context.Agendamentos
                .Where(a => a.DataAgendamento.Date == ontem)
                .ToListAsync();

            var total = agendamentosHoje.Count;
            var concluidos = agendamentosHoje.Count(a => a.Status == StatusAgendamento.CONCLUIDO);
            var cancelados = agendamentosHoje.Count(a => a.Status == StatusAgendamento.CANCELADO);
            var valorTotal = agendamentosHoje.Where(a => a.Status == StatusAgendamento.CONCLUIDO).Sum(a => a.ValorTotal);

            _logger.LogInformation("=== RELATÓRIO DIÁRIO DE AGENDAMENTOS ({Data:yyyy-MM-dd}) ===", ontem);
            _logger.LogInformation("Total de agendamentos: {Total}", total);
            _logger.LogInformation("Concluídos: {Concluidos}", concluidos);
            _logger.LogInformation("Cancelados: {Cancelados}", cancelados);
            _logger.LogInformation("Receita total: R$ {Valor:N2}", valorTotal);
            _logger.LogInformation("Taxa de conclusão: {Taxa:P2}", total > 0 ? (double)concluidos / total : 0);
            _logger.LogInformation("=========================================");

            // TODO: Enviar relatório por email ou salvar em storage
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao gerar relatório diário de agendamentos");
        }

        if (timer.ScheduleStatus is not null)
        {
            _logger.LogInformation("Próximo relatório: {NextRun}", timer.ScheduleStatus.Next);
        }
    }

    /// <summary>
    /// Processa confirmação de agendamento via Service Bus.
    /// Fila: scheduling-confirmation
    /// </summary>
    [Function("ProcessSchedulingConfirmation")]
    public async Task ProcessSchedulingConfirmation(
        [ServiceBusTrigger("scheduling-confirmation", Connection = "ServiceBusConnection")] string messageBody)
    {
        _logger.LogInformation("Processando confirmação de agendamento");

        try
        {
            var message = JsonSerializer.Deserialize<SchedulingConfirmationMessage>(messageBody);
            
            if (message == null)
            {
                _logger.LogWarning("Mensagem de confirmação inválida");
                return;
            }

            var agendamento = await _context.Agendamentos.FindAsync(message.AgendamentoId);
            
            if (agendamento == null)
            {
                _logger.LogWarning("Agendamento {AgendamentoId} não encontrado", message.AgendamentoId);
                return;
            }

            if (message.Confirmado)
            {
                agendamento.Status = StatusAgendamento.CONFIRMADO;
                _logger.LogInformation("Agendamento #{Id} confirmado pelo cliente", agendamento.Id);
            }
            else
            {
                agendamento.Status = StatusAgendamento.CANCELADO;
                if (!string.IsNullOrEmpty(message.MotivoRecusa))
                {
                    agendamento.Observacoes = (agendamento.Observacoes ?? "") + $" [Cancelado: {message.MotivoRecusa}]";
                }
                _logger.LogInformation("Agendamento #{Id} cancelado pelo cliente", agendamento.Id);
            }

            await _context.SaveChangesAsync();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao processar confirmação de agendamento");
            throw;
        }
    }
}
