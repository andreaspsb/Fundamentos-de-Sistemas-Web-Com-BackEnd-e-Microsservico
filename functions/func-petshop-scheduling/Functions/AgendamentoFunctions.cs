using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Petshop.Shared.Data;
using Petshop.Shared.DTOs.Agendamento;
using Petshop.Shared.Enums;
using Petshop.Shared.Models;
using Petshop.Shared.Security;
using System.Net;
using System.Text.Json;

namespace FuncPetshopScheduling.Functions;

public class AgendamentoFunctions
{
    private readonly PetshopDbContext _context;
    private readonly ILogger<AgendamentoFunctions> _logger;
    private readonly JwtService _jwtService;

    public AgendamentoFunctions(PetshopDbContext context, ILogger<AgendamentoFunctions> logger, JwtService jwtService)
    {
        _context = context;
        _logger = logger;
        _jwtService = jwtService;
    }

    [Function("GetAgendamentos")]
    public async Task<HttpResponseData> GetAgendamentos(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "agendamentos")] HttpRequestData req)
    {
        _logger.LogInformation("Buscando todos os agendamentos");

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        var agendamentos = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .OrderByDescending(a => a.DataAgendamento)
            .ThenByDescending(a => a.Horario)
            .ToListAsync();

        var response = agendamentos.Select(MapToResponseDTO);

        var httpResponse = req.CreateResponse(HttpStatusCode.OK);
        await httpResponse.WriteAsJsonAsync(response);
        return httpResponse;
    }

    [Function("GetAgendamento")]
    public async Task<HttpResponseData> GetAgendamento(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "agendamentos/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Buscando agendamento {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        var agendamento = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .FirstOrDefaultAsync(a => a.Id == id);

        if (agendamento == null)
        {
            var notFoundResponse = req.CreateResponse(HttpStatusCode.NotFound);
            await notFoundResponse.WriteAsJsonAsync(new { message = "Agendamento não encontrado" });
            return notFoundResponse;
        }

        // Verifica permissão
        if (auth.Role != "ADMIN" && agendamento.ClienteId != auth.ClienteId)
        {
            return await FunctionAuthorization.CreateForbiddenResponse(req, "Acesso negado");
        }

        var httpResponse = req.CreateResponse(HttpStatusCode.OK);
        await httpResponse.WriteAsJsonAsync(MapToResponseDTO(agendamento));
        return httpResponse;
    }

    [Function("GetAgendamentosByCliente")]
    public async Task<HttpResponseData> GetAgendamentosByCliente(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "agendamentos/cliente/{clienteId:long}")] HttpRequestData req,
        long clienteId)
    {
        _logger.LogInformation("Buscando agendamentos do cliente {ClienteId}", clienteId);

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        // Verifica permissão
        if (auth.Role != "ADMIN" && clienteId != auth.ClienteId)
        {
            return await FunctionAuthorization.CreateForbiddenResponse(req, "Acesso negado");
        }

        var agendamentos = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .Where(a => a.ClienteId == clienteId)
            .OrderByDescending(a => a.DataAgendamento)
            .ThenByDescending(a => a.Horario)
            .ToListAsync();

        var response = agendamentos.Select(MapToResponseDTO);

        var httpResponse = req.CreateResponse(HttpStatusCode.OK);
        await httpResponse.WriteAsJsonAsync(response);
        return httpResponse;
    }

    [Function("GetMeusAgendamentos")]
    public async Task<HttpResponseData> GetMeusAgendamentos(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "agendamentos/meus")] HttpRequestData req)
    {
        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        _logger.LogInformation("Buscando agendamentos do cliente autenticado {ClienteId}", auth.ClienteId);

        var agendamentos = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .Where(a => a.ClienteId == auth.ClienteId)
            .OrderByDescending(a => a.DataAgendamento)
            .ThenByDescending(a => a.Horario)
            .ToListAsync();

        var response = agendamentos.Select(MapToResponseDTO);

        var httpResponse = req.CreateResponse(HttpStatusCode.OK);
        await httpResponse.WriteAsJsonAsync(response);
        return httpResponse;
    }

    [Function("CreateAgendamento")]
    public async Task<HttpResponseData> CreateAgendamento(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "agendamentos")] HttpRequestData req)
    {
        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        var dto = await req.ReadFromJsonAsync<AgendamentoRequestDTO>();
        if (dto == null)
        {
            var badRequestResponse = req.CreateResponse(HttpStatusCode.BadRequest);
            await badRequestResponse.WriteAsJsonAsync(new { message = "Dados inválidos" });
            return badRequestResponse;
        }

        // Verifica permissão
        if (auth.Role != "ADMIN" && dto.ClienteId != auth.ClienteId)
        {
            return await FunctionAuthorization.CreateForbiddenResponse(req, "Acesso negado");
        }

        _logger.LogInformation("Criando agendamento para cliente {ClienteId}", dto.ClienteId);

        // Buscar os serviços pelos IDs
        var servicos = await _context.Servicos
            .Where(s => dto.ServicosIds.Contains(s.Id))
            .ToListAsync();

        if (!servicos.Any())
        {
            var notFoundResponse = req.CreateResponse(HttpStatusCode.BadRequest);
            await notFoundResponse.WriteAsJsonAsync(new { message = "Nenhum serviço válido encontrado" });
            return notFoundResponse;
        }

        // Calcular valor total
        var valorTotal = servicos.Sum(s => s.Preco);

        var agendamento = new Agendamento
        {
            ClienteId = dto.ClienteId,
            PetId = dto.PetId,
            DataAgendamento = dto.DataAgendamento,
            Horario = dto.Horario,
            MetodoAtendimento = dto.MetodoAtendimento,
            PortePet = dto.PortePet,
            Observacoes = dto.Observacoes,
            Status = StatusAgendamento.Pendente,
            ValorTotal = valorTotal,
            Servicos = servicos
        };

        _context.Agendamentos.Add(agendamento);
        await _context.SaveChangesAsync();

        // Recarregar com relacionamentos
        await _context.Entry(agendamento).Reference(a => a.Cliente).LoadAsync();
        await _context.Entry(agendamento).Reference(a => a.Pet).LoadAsync();

        var httpResponse = req.CreateResponse(HttpStatusCode.Created);
        await httpResponse.WriteAsJsonAsync(MapToResponseDTO(agendamento));
        return httpResponse;
    }

    [Function("UpdateAgendamento")]
    public async Task<HttpResponseData> UpdateAgendamento(
        [HttpTrigger(AuthorizationLevel.Anonymous, "put", Route = "agendamentos/{id:long}")] HttpRequestData req,
        long id)
    {
        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        var agendamento = await _context.Agendamentos
            .Include(a => a.Servicos)
            .FirstOrDefaultAsync(a => a.Id == id);

        if (agendamento == null)
        {
            var notFoundResponse = req.CreateResponse(HttpStatusCode.NotFound);
            await notFoundResponse.WriteAsJsonAsync(new { message = "Agendamento não encontrado" });
            return notFoundResponse;
        }

        // Verificar se o agendamento pertence ao cliente (se não for admin)
        if (auth.Role != "ADMIN" && agendamento.ClienteId != auth.ClienteId)
        {
            return await FunctionAuthorization.CreateForbiddenResponse(req, "Acesso negado");
        }

        var dto = await req.ReadFromJsonAsync<AgendamentoUpdateDTO>();
        if (dto == null)
        {
            var badRequestResponse = req.CreateResponse(HttpStatusCode.BadRequest);
            await badRequestResponse.WriteAsJsonAsync(new { message = "Dados inválidos" });
            return badRequestResponse;
        }

        _logger.LogInformation("Atualizando agendamento {Id}", id);

        // Atualizar propriedades se fornecidas
        if (dto.DataAgendamento.HasValue)
            agendamento.DataAgendamento = dto.DataAgendamento.Value;
        if (dto.Horario.HasValue)
            agendamento.Horario = dto.Horario.Value;
        if (!string.IsNullOrEmpty(dto.MetodoAtendimento))
            agendamento.MetodoAtendimento = dto.MetodoAtendimento;
        if (dto.PortePet != null)
            agendamento.PortePet = dto.PortePet;
        if (dto.Observacoes != null)
            agendamento.Observacoes = dto.Observacoes;
        if (dto.PetId.HasValue)
            agendamento.PetId = dto.PetId.Value;

        // Atualizar serviços se fornecidos
        if (dto.ServicosIds != null && dto.ServicosIds.Any())
        {
            var servicos = await _context.Servicos
                .Where(s => dto.ServicosIds.Contains(s.Id))
                .ToListAsync();
            
            agendamento.Servicos = servicos;
            agendamento.ValorTotal = servicos.Sum(s => s.Preco);
        }

        await _context.SaveChangesAsync();

        // Recarregar relacionamentos
        await _context.Entry(agendamento).Reference(a => a.Cliente).LoadAsync();
        await _context.Entry(agendamento).Reference(a => a.Pet).LoadAsync();
        await _context.Entry(agendamento).Collection(a => a.Servicos).LoadAsync();

        var httpResponse = req.CreateResponse(HttpStatusCode.OK);
        await httpResponse.WriteAsJsonAsync(MapToResponseDTO(agendamento));
        return httpResponse;
    }

    [Function("UpdateAgendamentoStatus")]
    public async Task<HttpResponseData> UpdateAgendamentoStatus(
        [HttpTrigger(AuthorizationLevel.Anonymous, "patch", Route = "agendamentos/{id:long}/status")] HttpRequestData req,
        long id)
    {
        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        var agendamento = await _context.Agendamentos.FindAsync(id);
        if (agendamento == null)
        {
            var notFoundResponse = req.CreateResponse(HttpStatusCode.NotFound);
            await notFoundResponse.WriteAsJsonAsync(new { message = "Agendamento não encontrado" });
            return notFoundResponse;
        }

        var body = await JsonSerializer.DeserializeAsync<JsonElement>(req.Body);
        if (!body.TryGetProperty("status", out var statusElement))
        {
            var badRequestResponse = req.CreateResponse(HttpStatusCode.BadRequest);
            await badRequestResponse.WriteAsJsonAsync(new { message = "Status não informado" });
            return badRequestResponse;
        }

        var statusStr = statusElement.GetString();
        if (!Enum.TryParse<StatusAgendamento>(statusStr, out var novoStatus))
        {
            var badRequestResponse = req.CreateResponse(HttpStatusCode.BadRequest);
            await badRequestResponse.WriteAsJsonAsync(new { 
                message = "Status inválido", 
                statusValidos = Enum.GetNames<StatusAgendamento>() 
            });
            return badRequestResponse;
        }

        _logger.LogInformation("Atualizando status do agendamento {Id} para {Status}", id, novoStatus);

        agendamento.Status = novoStatus;
        await _context.SaveChangesAsync();

        var httpResponse = req.CreateResponse(HttpStatusCode.OK);
        await httpResponse.WriteAsJsonAsync(new { message = "Status atualizado com sucesso" });
        return httpResponse;
    }

    [Function("DeleteAgendamento")]
    public async Task<HttpResponseData> DeleteAgendamento(
        [HttpTrigger(AuthorizationLevel.Anonymous, "delete", Route = "agendamentos/{id:long}")] HttpRequestData req,
        long id)
    {
        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        var agendamento = await _context.Agendamentos.FindAsync(id);
        if (agendamento == null)
        {
            var notFoundResponse = req.CreateResponse(HttpStatusCode.NotFound);
            await notFoundResponse.WriteAsJsonAsync(new { message = "Agendamento não encontrado" });
            return notFoundResponse;
        }

        // Verificar se o agendamento pertence ao cliente (se não for admin)
        if (auth.Role != "ADMIN" && agendamento.ClienteId != auth.ClienteId)
        {
            return await FunctionAuthorization.CreateForbiddenResponse(req, "Acesso negado");
        }

        _logger.LogInformation("Deletando agendamento {Id}", id);

        _context.Agendamentos.Remove(agendamento);
        await _context.SaveChangesAsync();

        var httpResponse = req.CreateResponse(HttpStatusCode.NoContent);
        return httpResponse;
    }

    [Function("GetHorariosDisponiveis")]
    public async Task<HttpResponseData> GetHorariosDisponiveis(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "agendamentos/horarios/{data}")] HttpRequestData req,
        string data)
    {
        _logger.LogInformation("Buscando horários disponíveis para {Data}", data);

        if (!DateTime.TryParse(data, out var dataAgendamento))
        {
            var badRequestResponse = req.CreateResponse(HttpStatusCode.BadRequest);
            await badRequestResponse.WriteAsJsonAsync(new { message = "Data inválida" });
            return badRequestResponse;
        }

        // Horários de funcionamento
        var horariosFuncionamento = new List<TimeSpan>
        {
            new TimeSpan(8, 0, 0),
            new TimeSpan(9, 0, 0),
            new TimeSpan(10, 0, 0),
            new TimeSpan(11, 0, 0),
            new TimeSpan(14, 0, 0),
            new TimeSpan(15, 0, 0),
            new TimeSpan(16, 0, 0),
            new TimeSpan(17, 0, 0)
        };

        // Buscar horários já agendados
        var horariosOcupados = await _context.Agendamentos
            .Where(a => a.DataAgendamento.Date == dataAgendamento.Date && 
                       a.Status != StatusAgendamento.Cancelado)
            .Select(a => a.Horario)
            .ToListAsync();

        var horariosDisponiveis = horariosFuncionamento
            .Except(horariosOcupados)
            .OrderBy(h => h)
            .Select(h => h.ToString(@"hh\:mm"))
            .ToList();

        var httpResponse = req.CreateResponse(HttpStatusCode.OK);
        await httpResponse.WriteAsJsonAsync(new DisponibilidadeResponseDTO 
        { 
            Data = dataAgendamento, 
            HorariosDisponiveis = horariosFuncionamento.Except(horariosOcupados).OrderBy(h => h).ToList()
        });
        return httpResponse;
    }

    private static AgendamentoResponseDTO MapToResponseDTO(Agendamento agendamento)
    {
        return new AgendamentoResponseDTO
        {
            Id = agendamento.Id,
            ClienteId = agendamento.ClienteId,
            ClienteNome = agendamento.Cliente?.Nome ?? string.Empty,
            ClienteTelefone = agendamento.Cliente?.Telefone,
            PetId = agendamento.PetId,
            PetNome = agendamento.Pet?.Nome ?? string.Empty,
            PetTipo = agendamento.Pet?.Tipo ?? string.Empty,
            Servicos = agendamento.Servicos.Select(s => new ServicoAgendadoDTO
            {
                Id = s.Id,
                Nome = s.Nome,
                Preco = (decimal)s.Preco
            }).ToList(),
            DataAgendamento = agendamento.DataAgendamento,
            Horario = agendamento.Horario,
            MetodoAtendimento = agendamento.MetodoAtendimento,
            PortePet = agendamento.PortePet,
            Observacoes = agendamento.Observacoes,
            Status = agendamento.Status.ToString(),
            ValorTotal = (decimal)agendamento.ValorTotal
        };
    }
}
