using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Data;
using PetshopApi.Models;

namespace PetshopApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class CategoriasController : ControllerBase
{
    private readonly PetshopContext _context;

    public CategoriasController(PetshopContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<Categoria>>> GetAll()
    {
        return await _context.Categorias.ToListAsync();
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<Categoria>> GetById(long id)
    {
        var categoria = await _context.Categorias.FindAsync(id);
        
        if (categoria == null)
            return NotFound();

        return categoria;
    }

    [HttpGet("ativas")]
    public async Task<ActionResult<IEnumerable<Categoria>>> GetAtivas()
    {
        return await _context.Categorias
            .Where(c => c.Ativo)
            .ToListAsync();
    }

    [HttpPost]
    public async Task<ActionResult<Categoria>> Create(Categoria categoria)
    {
        _context.Categorias.Add(categoria);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetById), new { id = categoria.Id }, categoria);
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> Update(long id, Categoria categoria)
    {
        if (id != categoria.Id)
            return BadRequest();

        _context.Entry(categoria).State = EntityState.Modified;

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateConcurrencyException)
        {
            if (!await _context.Categorias.AnyAsync(c => c.Id == id))
                return NotFound();
            throw;
        }

        return NoContent();
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(long id)
    {
        var categoria = await _context.Categorias.FindAsync(id);
        
        if (categoria == null)
            return NotFound();

        _context.Categorias.Remove(categoria);
        await _context.SaveChangesAsync();

        return NoContent();
    }
}
