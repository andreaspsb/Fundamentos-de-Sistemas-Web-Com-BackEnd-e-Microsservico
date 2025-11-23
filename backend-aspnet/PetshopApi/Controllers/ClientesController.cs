using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Data;
using PetshopApi.DTOs;
using PetshopApi.Models;

namespace PetshopApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ClientesController : ControllerBase
{
    private readonly PetshopContext _context;

    public ClientesController(PetshopContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<ClienteResponseDTO>>> GetAll()
    {
        var clientes = await _context.Clientes
            .Select(c => ToResponseDTO(c))
            .ToListAsync();
        
        return Ok(clientes);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ClienteResponseDTO>> GetById(long id)
    {
        var cliente = await _context.Clientes.FindAsync(id);
        
        if (cliente == null)
            return NotFound();

        return Ok(ToResponseDTO(cliente));
    }

    [HttpGet("cpf/{cpf}")]
    public async Task<ActionResult<ClienteResponseDTO>> GetByCpf(string cpf)
    {
        var cliente = await _context.Clientes.FirstOrDefaultAsync(c => c.Cpf == cpf);
        
        if (cliente == null)
            return NotFound();

        return Ok(ToResponseDTO(cliente));
    }

    [HttpPost]
    public async Task<ActionResult<ClienteResponseDTO>> Create([FromBody] ClienteRequestDTO dto)
    {
        if (await _context.Clientes.AnyAsync(c => c.Cpf == dto.Cpf))
            return BadRequest(new { error = "CPF já cadastrado" });

        if (await _context.Clientes.AnyAsync(c => c.Email == dto.Email))
            return BadRequest(new { error = "Email já cadastrado" });

        var cliente = new Cliente
        {
            Nome = dto.Nome,
            Cpf = dto.Cpf,
            Telefone = dto.Telefone,
            Email = dto.Email,
            DataNascimento = dto.DataNascimento,
            Sexo = dto.Sexo,
            Endereco = dto.Endereco,
            Numero = dto.Numero,
            Complemento = dto.Complemento,
            Bairro = dto.Bairro,
            Cidade = dto.Cidade
        };

        _context.Clientes.Add(cliente);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetById), new { id = cliente.Id }, ToResponseDTO(cliente));
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<ClienteResponseDTO>> Update(long id, [FromBody] ClienteRequestDTO dto)
    {
        var cliente = await _context.Clientes.FindAsync(id);
        if (cliente == null)
            return NotFound();

        cliente.Nome = dto.Nome;
        cliente.Cpf = dto.Cpf;
        cliente.Telefone = dto.Telefone;
        cliente.Email = dto.Email;
        cliente.DataNascimento = dto.DataNascimento;
        cliente.Sexo = dto.Sexo;
        cliente.Endereco = dto.Endereco;
        cliente.Numero = dto.Numero;
        cliente.Complemento = dto.Complemento;
        cliente.Bairro = dto.Bairro;
        cliente.Cidade = dto.Cidade;

        await _context.SaveChangesAsync();

        return Ok(ToResponseDTO(cliente));
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(long id)
    {
        var cliente = await _context.Clientes.FindAsync(id);
        if (cliente == null)
            return NotFound();

        _context.Clientes.Remove(cliente);
        await _context.SaveChangesAsync();

        return NoContent();
    }

    private static ClienteResponseDTO ToResponseDTO(Cliente cliente)
    {
        return new ClienteResponseDTO
        {
            Id = cliente.Id,
            Nome = cliente.Nome,
            Cpf = cliente.Cpf,
            Telefone = cliente.Telefone,
            Email = cliente.Email,
            DataNascimento = cliente.DataNascimento,
            Sexo = cliente.Sexo,
            Endereco = cliente.Endereco,
            Numero = cliente.Numero,
            Complemento = cliente.Complemento,
            Bairro = cliente.Bairro,
            Cidade = cliente.Cidade
        };
    }
}
