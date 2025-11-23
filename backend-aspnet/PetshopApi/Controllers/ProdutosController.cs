using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Data;
using PetshopApi.DTOs;
using PetshopApi.Models;

namespace PetshopApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ProdutosController : ControllerBase
{
    private readonly PetshopContext _context;

    public ProdutosController(PetshopContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<ProdutoResponseDTO>>> GetAll()
    {
        var produtos = await _context.Produtos
            .Include(p => p.Categoria)
            .Select(p => ToResponseDTO(p))
            .ToListAsync();
        
        return Ok(produtos);
    }

    [HttpGet("disponiveis")]
    public async Task<ActionResult<IEnumerable<ProdutoResponseDTO>>> GetDisponiveis()
    {
        var produtos = await _context.Produtos
            .Include(p => p.Categoria)
            .Where(p => p.Ativo && p.QuantidadeEstoque > 0)
            .Select(p => ToResponseDTO(p))
            .ToListAsync();
        
        return Ok(produtos);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ProdutoResponseDTO>> GetById(long id)
    {
        var produto = await _context.Produtos
            .Include(p => p.Categoria)
            .FirstOrDefaultAsync(p => p.Id == id);
        
        if (produto == null)
            return NotFound();

        return Ok(ToResponseDTO(produto));
    }

    [HttpGet("categoria/{categoriaId}")]
    public async Task<ActionResult<IEnumerable<ProdutoResponseDTO>>> GetByCategoria(long categoriaId)
    {
        var produtos = await _context.Produtos
            .Include(p => p.Categoria)
            .Where(p => p.CategoriaId == categoriaId)
            .Select(p => ToResponseDTO(p))
            .ToListAsync();
        
        return Ok(produtos);
    }

    [HttpGet("categoria/{categoriaId}/disponiveis")]
    public async Task<ActionResult<IEnumerable<ProdutoResponseDTO>>> GetDisponiveisByCategoria(long categoriaId)
    {
        var produtos = await _context.Produtos
            .Include(p => p.Categoria)
            .Where(p => p.CategoriaId == categoriaId && p.Ativo && p.QuantidadeEstoque > 0)
            .Select(p => ToResponseDTO(p))
            .ToListAsync();
        
        return Ok(produtos);
    }

    [HttpGet("buscar")]
    public async Task<ActionResult<IEnumerable<ProdutoResponseDTO>>> Search([FromQuery] string termo)
    {
        var produtos = await _context.Produtos
            .Include(p => p.Categoria)
            .Where(p => p.Nome.ToLower().Contains(termo.ToLower()))
            .Select(p => ToResponseDTO(p))
            .ToListAsync();
        
        return Ok(produtos);
    }

    [HttpGet("estoque-baixo")]
    public async Task<ActionResult<IEnumerable<ProdutoResponseDTO>>> GetEstoqueBaixo([FromQuery] int quantidade = 10)
    {
        var produtos = await _context.Produtos
            .Include(p => p.Categoria)
            .Where(p => p.QuantidadeEstoque <= quantidade)
            .Select(p => ToResponseDTO(p))
            .ToListAsync();
        
        return Ok(produtos);
    }

    [HttpPost]
    public async Task<ActionResult<ProdutoResponseDTO>> Create([FromBody] ProdutoRequestDTO dto)
    {
        var categoria = await _context.Categorias.FindAsync(dto.CategoriaId);
        if (categoria == null)
            return BadRequest(new { error = "Categoria não encontrada" });

        var produto = new Produto
        {
            Nome = dto.Nome,
            Descricao = dto.Descricao,
            Preco = dto.Preco,
            QuantidadeEstoque = dto.QuantidadeEstoque,
            UrlImagem = dto.UrlImagem,
            Ativo = dto.Ativo,
            CategoriaId = dto.CategoriaId
        };

        _context.Produtos.Add(produto);
        await _context.SaveChangesAsync();

        await _context.Entry(produto).Reference(p => p.Categoria).LoadAsync();

        return CreatedAtAction(nameof(GetById), new { id = produto.Id }, ToResponseDTO(produto));
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<ProdutoResponseDTO>> Update(long id, [FromBody] ProdutoRequestDTO dto)
    {
        var produto = await _context.Produtos.FindAsync(id);
        if (produto == null)
            return NotFound();

        produto.Nome = dto.Nome;
        produto.Descricao = dto.Descricao;
        produto.Preco = dto.Preco;
        produto.QuantidadeEstoque = dto.QuantidadeEstoque;
        produto.UrlImagem = dto.UrlImagem;
        produto.Ativo = dto.Ativo;
        produto.CategoriaId = dto.CategoriaId;

        await _context.SaveChangesAsync();
        await _context.Entry(produto).Reference(p => p.Categoria).LoadAsync();

        return Ok(ToResponseDTO(produto));
    }

    [HttpPatch("{id}/estoque")]
    public async Task<ActionResult<ProdutoResponseDTO>> UpdateEstoque(long id, [FromQuery] int quantidade)
    {
        var produto = await _context.Produtos.Include(p => p.Categoria).FirstOrDefaultAsync(p => p.Id == id);
        if (produto == null)
            return NotFound();

        produto.QuantidadeEstoque = quantidade;
        await _context.SaveChangesAsync();

        return Ok(ToResponseDTO(produto));
    }

    [HttpPatch("{id}/adicionar-estoque")]
    public async Task<ActionResult<ProdutoResponseDTO>> AdicionarEstoque(long id, [FromQuery] int quantidade)
    {
        var produto = await _context.Produtos.Include(p => p.Categoria).FirstOrDefaultAsync(p => p.Id == id);
        if (produto == null)
            return NotFound();

        produto.AdicionarEstoque(quantidade);
        await _context.SaveChangesAsync();

        return Ok(ToResponseDTO(produto));
    }

    [HttpPatch("{id}/ativar")]
    public async Task<IActionResult> Ativar(long id)
    {
        var produto = await _context.Produtos.FindAsync(id);
        if (produto == null)
            return NotFound();

        produto.Ativo = true;
        await _context.SaveChangesAsync();

        return NoContent();
    }

    [HttpPatch("{id}/desativar")]
    public async Task<IActionResult> Desativar(long id)
    {
        var produto = await _context.Produtos.FindAsync(id);
        if (produto == null)
            return NotFound();

        produto.Ativo = false;
        await _context.SaveChangesAsync();

        return NoContent();
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(long id)
    {
        var produto = await _context.Produtos.FindAsync(id);
        if (produto == null)
            return NotFound();

        _context.Produtos.Remove(produto);
        await _context.SaveChangesAsync();

        return NoContent();
    }

    private static ProdutoResponseDTO ToResponseDTO(Produto produto)
    {
        return new ProdutoResponseDTO
        {
            Id = produto.Id,
            Nome = produto.Nome,
            Descricao = produto.Descricao,
            Preco = produto.Preco,
            QuantidadeEstoque = produto.QuantidadeEstoque,
            UrlImagem = produto.UrlImagem,
            Ativo = produto.Ativo,
            CategoriaId = produto.CategoriaId,
            CategoriaNome = produto.Categoria?.Nome ?? string.Empty
        };
    }
}
