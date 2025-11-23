using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Data;
using PetshopApi.DTOs;
using PetshopApi.Models;

namespace PetshopApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class PetsController : ControllerBase
{
    private readonly PetshopContext _context;

    public PetsController(PetshopContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<PetResponseDTO>>> GetAll()
    {
        var pets = await _context.Pets
            .Include(p => p.Cliente)
            .Select(p => ToResponseDTO(p))
            .ToListAsync();
        
        return Ok(pets);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<PetResponseDTO>> GetById(long id)
    {
        var pet = await _context.Pets
            .Include(p => p.Cliente)
            .FirstOrDefaultAsync(p => p.Id == id);
        
        if (pet == null)
            return NotFound();

        return Ok(ToResponseDTO(pet));
    }

    [HttpGet("cliente/{clienteId}")]
    public async Task<ActionResult<IEnumerable<PetResponseDTO>>> GetByCliente(long clienteId)
    {
        var pets = await _context.Pets
            .Include(p => p.Cliente)
            .Where(p => p.ClienteId == clienteId)
            .Select(p => ToResponseDTO(p))
            .ToListAsync();
        
        return Ok(pets);
    }

    [HttpGet("tipo/{tipo}")]
    public async Task<ActionResult<IEnumerable<PetResponseDTO>>> GetByTipo(string tipo)
    {
        var pets = await _context.Pets
            .Include(p => p.Cliente)
            .Where(p => p.Tipo.ToLower() == tipo.ToLower())
            .Select(p => ToResponseDTO(p))
            .ToListAsync();
        
        return Ok(pets);
    }

    [HttpPost]
    public async Task<ActionResult<PetResponseDTO>> Create([FromBody] PetRequestDTO dto)
    {
        var cliente = await _context.Clientes.FindAsync(dto.ClienteId);
        if (cliente == null)
            return BadRequest(new { error = "Cliente não encontrado" });

        var pet = new Pet
        {
            Nome = dto.Nome,
            Tipo = dto.Tipo,
            Raca = dto.Raca,
            Idade = dto.Idade,
            Peso = dto.Peso,
            Sexo = dto.Sexo,
            Castrado = dto.Castrado,
            Observacoes = dto.Observacoes,
            TemAlergia = dto.TemAlergia,
            PrecisaMedicacao = dto.PrecisaMedicacao,
            ComportamentoAgressivo = dto.ComportamentoAgressivo,
            ClienteId = dto.ClienteId
        };

        _context.Pets.Add(pet);
        await _context.SaveChangesAsync();

        await _context.Entry(pet).Reference(p => p.Cliente).LoadAsync();

        return CreatedAtAction(nameof(GetById), new { id = pet.Id }, ToResponseDTO(pet));
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<PetResponseDTO>> Update(long id, [FromBody] PetRequestDTO dto)
    {
        var pet = await _context.Pets.FindAsync(id);
        if (pet == null)
            return NotFound();

        pet.Nome = dto.Nome;
        pet.Tipo = dto.Tipo;
        pet.Raca = dto.Raca;
        pet.Idade = dto.Idade;
        pet.Peso = dto.Peso;
        pet.Sexo = dto.Sexo;
        pet.Castrado = dto.Castrado;
        pet.Observacoes = dto.Observacoes;
        pet.TemAlergia = dto.TemAlergia;
        pet.PrecisaMedicacao = dto.PrecisaMedicacao;
        pet.ComportamentoAgressivo = dto.ComportamentoAgressivo;

        await _context.SaveChangesAsync();
        await _context.Entry(pet).Reference(p => p.Cliente).LoadAsync();

        return Ok(ToResponseDTO(pet));
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(long id)
    {
        var pet = await _context.Pets.FindAsync(id);
        if (pet == null)
            return NotFound();

        _context.Pets.Remove(pet);
        await _context.SaveChangesAsync();

        return NoContent();
    }

    private static PetResponseDTO ToResponseDTO(Pet pet)
    {
        return new PetResponseDTO
        {
            Id = pet.Id,
            Nome = pet.Nome,
            Tipo = pet.Tipo,
            Raca = pet.Raca,
            Idade = pet.Idade,
            Peso = pet.Peso,
            Sexo = pet.Sexo,
            Castrado = pet.Castrado,
            Observacoes = pet.Observacoes,
            TemAlergia = pet.TemAlergia,
            PrecisaMedicacao = pet.PrecisaMedicacao,
            ComportamentoAgressivo = pet.ComportamentoAgressivo,
            ClienteId = pet.ClienteId,
            ClienteNome = pet.Cliente?.Nome ?? string.Empty
        };
    }
}
