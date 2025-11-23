using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Data;
using PetshopApi.DTOs;
using PetshopApi.Models;

namespace PetshopApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ServicosController : ControllerBase
{
    private readonly PetshopContext _context;

    public ServicosController(PetshopContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<ServicoResponseDTO>>> GetAll()
    {
        var servicos = await _context.Servicos
            .Select(s => ToResponseDTO(s))
            .ToListAsync();
        
        return Ok(servicos);
    }

    [HttpGet("ativos")]
    public async Task<ActionResult<IEnumerable<ServicoResponseDTO>>> GetAtivos()
    {
        var servicos = await _context.Servicos
            .Where(s => s.Ativo)
            .Select(s => ToResponseDTO(s))
            .ToListAsync();
        
        return Ok(servicos);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ServicoResponseDTO>> GetById(long id)
    {
        var servico = await _context.Servicos.FindAsync(id);
        
        if (servico == null)
            return NotFound();

        return Ok(ToResponseDTO(servico));
    }

    [HttpPost]
    public async Task<ActionResult<ServicoResponseDTO>> Create([FromBody] ServicoRequestDTO dto)
    {
        var servico = new Servico
        {
            Nome = dto.Nome,
            Descricao = dto.Descricao,
            Preco = dto.Preco,
            Ativo = dto.Ativo
        };

        _context.Servicos.Add(servico);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetById), new { id = servico.Id }, ToResponseDTO(servico));
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<ServicoResponseDTO>> Update(long id, [FromBody] ServicoRequestDTO dto)
    {
        var servico = await _context.Servicos.FindAsync(id);
        if (servico == null)
            return NotFound();

        servico.Nome = dto.Nome;
        servico.Descricao = dto.Descricao;
        servico.Preco = dto.Preco;
        servico.Ativo = dto.Ativo;

        await _context.SaveChangesAsync();

        return Ok(ToResponseDTO(servico));
    }

    [HttpPatch("{id}/ativar")]
    public async Task<IActionResult> Ativar(long id)
    {
        var servico = await _context.Servicos.FindAsync(id);
        if (servico == null)
            return NotFound();

        servico.Ativo = true;
        await _context.SaveChangesAsync();

        return NoContent();
    }

    [HttpPatch("{id}/desativar")]
    public async Task<IActionResult> Desativar(long id)
    {
        var servico = await _context.Servicos.FindAsync(id);
        if (servico == null)
            return NotFound();

        servico.Ativo = false;
        await _context.SaveChangesAsync();

        return NoContent();
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(long id)
    {
        var servico = await _context.Servicos.FindAsync(id);
        if (servico == null)
            return NotFound();

        _context.Servicos.Remove(servico);
        await _context.SaveChangesAsync();

        return NoContent();
    }

    private static ServicoResponseDTO ToResponseDTO(Servico servico)
    {
        return new ServicoResponseDTO
        {
            Id = servico.Id,
            Nome = servico.Nome,
            Descricao = servico.Descricao,
            Preco = servico.Preco,
            Ativo = servico.Ativo
        };
    }
}
