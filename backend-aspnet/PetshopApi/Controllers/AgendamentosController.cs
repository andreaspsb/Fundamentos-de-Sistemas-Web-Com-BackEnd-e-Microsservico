using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Data;
using PetshopApi.DTOs;
using PetshopApi.Models;

namespace PetshopApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AgendamentosController : ControllerBase
{
    private readonly PetshopContext _context;

    public AgendamentosController(PetshopContext context)
    {
        _context = context;
    }

    // GET: api/agendamentos
    [HttpGet]
    public async Task<ActionResult<IEnumerable<AgendamentoResponseDTO>>> GetAgendamentos()
    {
        var agendamentos = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .OrderByDescending(a => a.DataAgendamento)
                .ThenBy(a => a.Horario)
            .ToListAsync();

        return Ok(agendamentos.Select(ToAgendamentoResponseDTO));
    }

    // GET: api/agendamentos/5
    [HttpGet("{id}")]
    public async Task<ActionResult<AgendamentoResponseDTO>> GetAgendamento(long id)
    {
        var agendamento = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .FirstOrDefaultAsync(a => a.Id == id);

        if (agendamento == null)
            return NotFound(new { message = "Agendamento não encontrado" });

        return Ok(ToAgendamentoResponseDTO(agendamento));
    }

    // GET: api/agendamentos/cliente/5
    [HttpGet("cliente/{clienteId}")]
    public async Task<ActionResult<IEnumerable<AgendamentoResponseDTO>>> GetAgendamentosPorCliente(long clienteId)
    {
        var agendamentos = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .Where(a => a.ClienteId == clienteId)
            .OrderByDescending(a => a.DataAgendamento)
                .ThenBy(a => a.Horario)
            .ToListAsync();

        return Ok(agendamentos.Select(ToAgendamentoResponseDTO));
    }

    // GET: api/agendamentos/data/2024-11-22
    [HttpGet("data/{data}")]
    public async Task<ActionResult<IEnumerable<AgendamentoResponseDTO>>> GetAgendamentosPorData(DateTime data)
    {
        var dataDate = data.Date; // Considera apenas a data, sem hora
        var agendamentos = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .Where(a => a.DataAgendamento.Date == dataDate)
            .OrderBy(a => a.Horario)
            .ToListAsync();

        return Ok(agendamentos.Select(ToAgendamentoResponseDTO));
    }

    // GET: api/agendamentos/status/pendente
    [HttpGet("status/{status}")]
    public async Task<ActionResult<IEnumerable<AgendamentoResponseDTO>>> GetAgendamentosPorStatus(string status)
    {
        if (!Enum.TryParse<StatusAgendamento>(status, true, out var statusEnum))
            return BadRequest(new { message = "Status inválido" });

        var agendamentos = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .Where(a => a.Status == statusEnum)
            .OrderByDescending(a => a.DataAgendamento)
                .ThenBy(a => a.Horario)
            .ToListAsync();

        return Ok(agendamentos.Select(ToAgendamentoResponseDTO));
    }

    // GET: api/agendamentos/disponibilidade?data=2024-11-22&horario=10:00
    [HttpGet("disponibilidade")]
    public async Task<ActionResult<bool>> VerificarDisponibilidade(
        [FromQuery] DateTime data, 
        [FromQuery] TimeSpan horario)
    {
        var dataDate = data.Date; // Considera apenas a data, sem hora
        var agendamentoExistente = await _context.Agendamentos
            .Where(a => a.DataAgendamento.Date == dataDate && 
                        a.Horario == horario && 
                        a.Status != StatusAgendamento.Cancelado)
            .AnyAsync();

        return Ok(!agendamentoExistente); // Retorna true se está disponível
    }

    // POST: api/agendamentos
    [HttpPost]
    public async Task<ActionResult<AgendamentoResponseDTO>> CreateAgendamento(AgendamentoRequestDTO dto)
    {
        // Validar cliente
        var cliente = await _context.Clientes.FindAsync(dto.ClienteId);
        if (cliente == null)
            return NotFound(new { message = "Cliente não encontrado" });

        // Validar pet
        var pet = await _context.Pets.FindAsync(dto.PetId);
        if (pet == null)
            return NotFound(new { message = "Pet não encontrado" });

        // Validar pet pertence ao cliente
        if (pet.ClienteId != dto.ClienteId)
            return BadRequest(new { message = "Pet não pertence ao cliente informado" });

        // Validar serviços
        var servicos = await _context.Servicos
            .Where(s => dto.ServicosIds.Contains(s.Id))
            .ToListAsync();

        if (servicos.Count != dto.ServicosIds.Count)
            return BadRequest(new { message = "Um ou mais serviços não foram encontrados" });

        // Verificar se todos os serviços estão ativos
        if (servicos.Any(s => !s.Ativo))
            return BadRequest(new { message = "Um ou mais serviços não estão disponíveis" });

        // Verificar disponibilidade
        var jaAgendado = await _context.Agendamentos
            .AnyAsync(a => a.DataAgendamento == dto.DataAgendamento && 
                          a.Horario == dto.Horario && 
                          a.Status != StatusAgendamento.Cancelado);

        if (jaAgendado)
            return BadRequest(new { message = "Horário já está agendado" });

        // Criar agendamento
        var agendamento = new Agendamento
        {
            DataAgendamento = dto.DataAgendamento,
            Horario = dto.Horario,
            MetodoAtendimento = dto.MetodoAtendimento,
            PortePet = dto.PortePet,
            Observacoes = dto.Observacoes,
            ValorTotal = (double)dto.ValorTotal,
            Status = StatusAgendamento.Pendente,
            ClienteId = dto.ClienteId,
            PetId = dto.PetId,
            Servicos = servicos
        };

        _context.Agendamentos.Add(agendamento);
        await _context.SaveChangesAsync();

        // Recarregar com includes
        agendamento = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .FirstAsync(a => a.Id == agendamento.Id);

        return CreatedAtAction(nameof(GetAgendamento), new { id = agendamento.Id }, ToAgendamentoResponseDTO(agendamento));
    }

    // PUT: api/agendamentos/5
    [HttpPut("{id}")]
    public async Task<ActionResult<AgendamentoResponseDTO>> UpdateAgendamento(long id, AgendamentoUpdateDTO dto)
    {
        var agendamento = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .FirstOrDefaultAsync(a => a.Id == id);

        if (agendamento == null)
            return NotFound(new { message = "Agendamento não encontrado" });

        if (agendamento.Status == StatusAgendamento.Concluido)
            return BadRequest(new { message = "Não é possível alterar um agendamento concluído" });

        if (agendamento.Status == StatusAgendamento.Cancelado)
            return BadRequest(new { message = "Não é possível alterar um agendamento cancelado" });

        // Atualizar campos se fornecidos
        if (dto.DataAgendamento.HasValue)
        {
            // Verificar disponibilidade da nova data/horário
            var horario = dto.Horario ?? agendamento.Horario;
            var jaAgendado = await _context.Agendamentos
                .AnyAsync(a => a.Id != id &&
                              a.DataAgendamento == dto.DataAgendamento.Value && 
                              a.Horario == horario && 
                              a.Status != StatusAgendamento.Cancelado);

            if (jaAgendado)
                return BadRequest(new { message = "Horário já está agendado" });

            agendamento.DataAgendamento = dto.DataAgendamento.Value;
        }

        if (dto.Horario.HasValue)
        {
            // Verificar disponibilidade do novo horário
            var data = dto.DataAgendamento ?? agendamento.DataAgendamento;
            var jaAgendado = await _context.Agendamentos
                .AnyAsync(a => a.Id != id &&
                              a.DataAgendamento == data && 
                              a.Horario == dto.Horario.Value && 
                              a.Status != StatusAgendamento.Cancelado);

            if (jaAgendado)
                return BadRequest(new { message = "Horário já está agendado" });

            agendamento.Horario = dto.Horario.Value;
        }

        if (!string.IsNullOrEmpty(dto.MetodoAtendimento))
            agendamento.MetodoAtendimento = dto.MetodoAtendimento;

        if (dto.PortePet != null)
            agendamento.PortePet = dto.PortePet;

        if (dto.Observacoes != null)
            agendamento.Observacoes = dto.Observacoes;

        if (dto.ValorTotal.HasValue)
            agendamento.ValorTotal = (double)dto.ValorTotal.Value;

        if (dto.PetId.HasValue)
        {
            var pet = await _context.Pets.FindAsync(dto.PetId.Value);
            if (pet == null)
                return NotFound(new { message = "Pet não encontrado" });

            if (pet.ClienteId != agendamento.ClienteId)
                return BadRequest(new { message = "Pet não pertence ao cliente do agendamento" });

            agendamento.PetId = dto.PetId.Value;
        }

        if (dto.ServicosIds != null && dto.ServicosIds.Count > 0)
        {
            var servicos = await _context.Servicos
                .Where(s => dto.ServicosIds.Contains(s.Id))
                .ToListAsync();

            if (servicos.Count != dto.ServicosIds.Count)
                return BadRequest(new { message = "Um ou mais serviços não foram encontrados" });

            if (servicos.Any(s => !s.Ativo))
                return BadRequest(new { message = "Um ou mais serviços não estão disponíveis" });

            agendamento.Servicos = servicos;
        }

        await _context.SaveChangesAsync();

        return Ok(ToAgendamentoResponseDTO(agendamento));
    }

    // PATCH: api/agendamentos/5/confirmar
    [HttpPatch("{id}/confirmar")]
    public async Task<ActionResult<AgendamentoResponseDTO>> ConfirmarAgendamento(long id)
    {
        var agendamento = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .FirstOrDefaultAsync(a => a.Id == id);

        if (agendamento == null)
            return NotFound(new { message = "Agendamento não encontrado" });

        if (agendamento.Status != StatusAgendamento.Pendente)
            return BadRequest(new { message = "Apenas agendamentos pendentes podem ser confirmados" });

        agendamento.Status = StatusAgendamento.Confirmado;
        await _context.SaveChangesAsync();

        return Ok(ToAgendamentoResponseDTO(agendamento));
    }

    // PATCH: api/agendamentos/5/concluir
    [HttpPatch("{id}/concluir")]
    public async Task<ActionResult<AgendamentoResponseDTO>> ConcluirAgendamento(long id)
    {
        var agendamento = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .FirstOrDefaultAsync(a => a.Id == id);

        if (agendamento == null)
            return NotFound(new { message = "Agendamento não encontrado" });

        if (agendamento.Status == StatusAgendamento.Concluido)
            return BadRequest(new { message = "Agendamento já está concluído" });

        if (agendamento.Status == StatusAgendamento.Cancelado)
            return BadRequest(new { message = "Não é possível concluir um agendamento cancelado" });

        agendamento.Status = StatusAgendamento.Concluido;
        await _context.SaveChangesAsync();

        return Ok(ToAgendamentoResponseDTO(agendamento));
    }

    // PATCH: api/agendamentos/5/cancelar
    [HttpPatch("{id}/cancelar")]
    public async Task<ActionResult<AgendamentoResponseDTO>> CancelarAgendamento(long id)
    {
        var agendamento = await _context.Agendamentos
            .Include(a => a.Cliente)
            .Include(a => a.Pet)
            .Include(a => a.Servicos)
            .FirstOrDefaultAsync(a => a.Id == id);

        if (agendamento == null)
            return NotFound(new { message = "Agendamento não encontrado" });

        if (agendamento.Status == StatusAgendamento.Cancelado)
            return BadRequest(new { message = "Agendamento já está cancelado" });

        if (agendamento.Status == StatusAgendamento.Concluido)
            return BadRequest(new { message = "Não é possível cancelar um agendamento concluído" });

        agendamento.Status = StatusAgendamento.Cancelado;
        await _context.SaveChangesAsync();

        return Ok(ToAgendamentoResponseDTO(agendamento));
    }

    // DELETE: api/agendamentos/5
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteAgendamento(long id)
    {
        var agendamento = await _context.Agendamentos.FindAsync(id);
        if (agendamento == null)
            return NotFound(new { message = "Agendamento não encontrado" });

        _context.Agendamentos.Remove(agendamento);
        await _context.SaveChangesAsync();

        return NoContent();
    }

    // Helper methods
    private AgendamentoResponseDTO ToAgendamentoResponseDTO(Agendamento agendamento)
    {
        return new AgendamentoResponseDTO
        {
            Id = agendamento.Id,
            DataAgendamento = agendamento.DataAgendamento,
            Horario = agendamento.Horario,
            MetodoAtendimento = agendamento.MetodoAtendimento,
            PortePet = agendamento.PortePet,
            Observacoes = agendamento.Observacoes,
            ValorTotal = (decimal)agendamento.ValorTotal,
            Status = agendamento.Status.ToString(),
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
            }).ToList()
        };
    }
}
