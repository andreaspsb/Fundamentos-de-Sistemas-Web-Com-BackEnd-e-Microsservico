using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Data;
using PetshopApi.DTOs;
using PetshopApi.Models;

namespace PetshopApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class PedidosController : ControllerBase
{
    private readonly PetshopContext _context;

    public PedidosController(PetshopContext context)
    {
        _context = context;
    }

    // GET: api/pedidos
    [HttpGet]
    public async Task<ActionResult<IEnumerable<PedidoResponseDTO>>> GetPedidos()
    {
        var pedidos = await _context.Pedidos
            .Include(p => p.Cliente)
            .Include(p => p.Itens)
                .ThenInclude(i => i.Produto)
            .OrderByDescending(p => p.DataPedido)
            .ToListAsync();

        return Ok(pedidos.Select(ToPedidoResponseDTO));
    }

    // GET: api/pedidos/5
    [HttpGet("{id}")]
    public async Task<ActionResult<PedidoResponseDTO>> GetPedido(long id)
    {
        var pedido = await _context.Pedidos
            .Include(p => p.Cliente)
            .Include(p => p.Itens)
                .ThenInclude(i => i.Produto)
            .FirstOrDefaultAsync(p => p.Id == id);

        if (pedido == null)
            return NotFound(new { message = "Pedido não encontrado" });

        return Ok(ToPedidoResponseDTO(pedido));
    }

    // GET: api/pedidos/cliente/5
    [HttpGet("cliente/{clienteId}")]
    public async Task<ActionResult<IEnumerable<PedidoResponseDTO>>> GetPedidosPorCliente(long clienteId)
    {
        var pedidos = await _context.Pedidos
            .Include(p => p.Cliente)
            .Include(p => p.Itens)
                .ThenInclude(i => i.Produto)
            .Where(p => p.ClienteId == clienteId)
            .OrderByDescending(p => p.DataPedido)
            .ToListAsync();

        return Ok(pedidos.Select(ToPedidoResponseDTO));
    }

    // GET: api/pedidos/status/pendente
    [HttpGet("status/{status}")]
    public async Task<ActionResult<IEnumerable<PedidoResponseDTO>>> GetPedidosPorStatus(string status)
    {
        if (!Enum.TryParse<StatusPedido>(status, true, out var statusEnum))
            return BadRequest(new { message = "Status inválido" });

        var pedidos = await _context.Pedidos
            .Include(p => p.Cliente)
            .Include(p => p.Itens)
                .ThenInclude(i => i.Produto)
            .Where(p => p.Status == statusEnum)
            .OrderByDescending(p => p.DataPedido)
            .ToListAsync();

        return Ok(pedidos.Select(ToPedidoResponseDTO));
    }

    // GET: api/pedidos/status/pendente/count
    [HttpGet("status/{status}/count")]
    public async Task<ActionResult<long>> GetContagemPorStatus(string status)
    {
        if (!Enum.TryParse<StatusPedido>(status, true, out var statusEnum))
            return BadRequest(new { message = "Status inválido" });

        var count = await _context.Pedidos
            .Where(p => p.Status == statusEnum)
            .CountAsync();

        return Ok(count);
    }

    // POST: api/pedidos
    [HttpPost]
    public async Task<ActionResult<PedidoResponseDTO>> CreatePedido(PedidoRequestDTO dto)
    {
        var cliente = await _context.Clientes.FindAsync(dto.ClienteId);
        if (cliente == null)
            return NotFound(new { message = "Cliente não encontrado" });

        var pedido = new Pedido
        {
            ClienteId = dto.ClienteId,
            DataPedido = DateTime.Now,
            Status = StatusPedido.PENDENTE,
            FormaPagamento = dto.FormaPagamento,
            Observacoes = dto.Observacoes,
            ValorTotal = 0
        };

        _context.Pedidos.Add(pedido);
        await _context.SaveChangesAsync();

        // Recarregar com includes
        pedido = await _context.Pedidos
            .Include(p => p.Cliente)
            .Include(p => p.Itens)
                .ThenInclude(i => i.Produto)
            .FirstAsync(p => p.Id == pedido.Id);

        return CreatedAtAction(nameof(GetPedido), new { id = pedido.Id }, ToPedidoResponseDTO(pedido));
    }

    // POST: api/pedidos/5/itens
    [HttpPost("{pedidoId}/itens")]
    public async Task<ActionResult<PedidoResponseDTO>> AdicionarItem(long pedidoId, ItemPedidoRequestDTO dto)
    {
        var pedido = await _context.Pedidos
            .Include(p => p.Cliente)
            .Include(p => p.Itens)
                .ThenInclude(i => i.Produto)
            .FirstOrDefaultAsync(p => p.Id == pedidoId);

        if (pedido == null)
            return NotFound(new { message = "Pedido não encontrado" });

        if (pedido.Status != StatusPedido.PENDENTE)
            return BadRequest(new { message = "Não é possível adicionar itens a um pedido já confirmado" });

        var produto = await _context.Produtos.FindAsync(dto.ProdutoId);
        if (produto == null)
            return NotFound(new { message = "Produto não encontrado" });

        if (!produto.Ativo)
            return BadRequest(new { message = "Produto não está disponível" });

        if (produto.QuantidadeEstoque < dto.Quantidade)
            return BadRequest(new { message = $"Estoque insuficiente. Disponível: {produto.QuantidadeEstoque}" });

        // Verificar se o item já existe no pedido
        var itemExistente = pedido.Itens.FirstOrDefault(i => i.ProdutoId == dto.ProdutoId);
        if (itemExistente != null)
        {
            itemExistente.Quantidade += dto.Quantidade;
        }
        else
        {
            var item = new ItemPedido
            {
                PedidoId = pedidoId,
                ProdutoId = dto.ProdutoId,
                Quantidade = dto.Quantidade,
                PrecoUnitario = produto.Preco
            };
            _context.ItensPedido.Add(item);
        }

        // Atualizar valor total
        await _context.SaveChangesAsync();
        pedido.ValorTotal = pedido.Itens.Sum(i => i.PrecoUnitario * i.Quantidade);
        await _context.SaveChangesAsync();

        // Recarregar
        pedido = await _context.Pedidos
            .Include(p => p.Cliente)
            .Include(p => p.Itens)
                .ThenInclude(i => i.Produto)
            .FirstAsync(p => p.Id == pedidoId);

        return Ok(ToPedidoResponseDTO(pedido));
    }

    // DELETE: api/pedidos/5/itens/3
    [HttpDelete("{pedidoId}/itens/{itemId}")]
    public async Task<ActionResult<PedidoResponseDTO>> RemoverItem(long pedidoId, long itemId)
    {
        var pedido = await _context.Pedidos
            .Include(p => p.Cliente)
            .Include(p => p.Itens)
                .ThenInclude(i => i.Produto)
            .FirstOrDefaultAsync(p => p.Id == pedidoId);

        if (pedido == null)
            return NotFound(new { message = "Pedido não encontrado" });

        if (pedido.Status != StatusPedido.PENDENTE)
            return BadRequest(new { message = "Não é possível remover itens de um pedido já confirmado" });

        var item = pedido.Itens.FirstOrDefault(i => i.Id == itemId);
        if (item == null)
            return NotFound(new { message = "Item não encontrado no pedido" });

        _context.ItensPedido.Remove(item);
        await _context.SaveChangesAsync();

        // Atualizar valor total
        pedido.ValorTotal = pedido.Itens.Sum(i => i.PrecoUnitario * i.Quantidade);
        await _context.SaveChangesAsync();

        // Recarregar
        pedido = await _context.Pedidos
            .Include(p => p.Cliente)
            .Include(p => p.Itens)
                .ThenInclude(i => i.Produto)
            .FirstAsync(p => p.Id == pedidoId);

        return Ok(ToPedidoResponseDTO(pedido));
    }

    // POST: api/pedidos/5/confirmar
    [HttpPost("{id}/confirmar")]
    public async Task<ActionResult<PedidoResponseDTO>> ConfirmarPedido(long id)
    {
        var pedido = await _context.Pedidos
            .Include(p => p.Cliente)
            .Include(p => p.Itens)
                .ThenInclude(i => i.Produto)
            .FirstOrDefaultAsync(p => p.Id == id);

        if (pedido == null)
            return NotFound(new { message = "Pedido não encontrado" });

        if (pedido.Status != StatusPedido.PENDENTE)
            return BadRequest(new { message = "Apenas pedidos pendentes podem ser confirmados" });

        if (pedido.Itens.Count == 0)
            return BadRequest(new { message = "Pedido não possui itens" });

        // Validar estoque de todos os itens
        foreach (var item in pedido.Itens)
        {
            if (item.Produto!.QuantidadeEstoque < item.Quantidade)
            {
                return BadRequest(new
                {
                    message = $"Estoque insuficiente para o produto {item.Produto.Nome}. Disponível: {item.Produto.QuantidadeEstoque}"
                });
            }
        }

        // Deduzir estoque
        foreach (var item in pedido.Itens)
        {
            item.Produto!.QuantidadeEstoque -= item.Quantidade;
        }

        pedido.Status = StatusPedido.CONFIRMADO;
        await _context.SaveChangesAsync();

        return Ok(ToPedidoResponseDTO(pedido));
    }

    // PATCH: api/pedidos/5/status
    [HttpPatch("{id}/status")]
    public async Task<ActionResult<PedidoResponseDTO>> AtualizarStatus(long id, [FromBody] StatusUpdateDTO dto)
    {
        var pedido = await _context.Pedidos
            .Include(p => p.Cliente)
            .Include(p => p.Itens)
                .ThenInclude(i => i.Produto)
            .FirstOrDefaultAsync(p => p.Id == id);

        if (pedido == null)
            return NotFound(new { message = "Pedido não encontrado" });

        if (!Enum.TryParse<StatusPedido>(dto.Status, true, out var novoStatus))
            return BadRequest(new { message = "Status inválido" });

        pedido.Status = novoStatus;
        await _context.SaveChangesAsync();

        return Ok(ToPedidoResponseDTO(pedido));
    }

    // POST: api/pedidos/5/cancelar
    [HttpPost("{id}/cancelar")]
    public async Task<ActionResult<PedidoResponseDTO>> CancelarPedido(long id)
    {
        var pedido = await _context.Pedidos
            .Include(p => p.Cliente)
            .Include(p => p.Itens)
                .ThenInclude(i => i.Produto)
            .FirstOrDefaultAsync(p => p.Id == id);

        if (pedido == null)
            return NotFound(new { message = "Pedido não encontrado" });

        if (pedido.Status == StatusPedido.CANCELADO)
            return BadRequest(new { message = "Pedido já está cancelado" });

        if (pedido.Status == StatusPedido.ENTREGUE)
            return BadRequest(new { message = "Não é possível cancelar um pedido já entregue" });

        // Se já foi confirmado, devolver estoque
        if (pedido.Status == StatusPedido.CONFIRMADO || 
            pedido.Status == StatusPedido.ENVIADO)
        {
            foreach (var item in pedido.Itens)
            {
                item.Produto!.QuantidadeEstoque += item.Quantidade;
            }
        }

        pedido.Status = StatusPedido.CANCELADO;
        await _context.SaveChangesAsync();

        return Ok(ToPedidoResponseDTO(pedido));
    }

    // DELETE: api/pedidos/5
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeletePedido(long id)
    {
        var pedido = await _context.Pedidos
            .Include(p => p.Itens)
            .FirstOrDefaultAsync(p => p.Id == id);

        if (pedido == null)
            return NotFound(new { message = "Pedido não encontrado" });

        _context.Pedidos.Remove(pedido);
        await _context.SaveChangesAsync();

        return NoContent();
    }

    // Helper methods
    private PedidoResponseDTO ToPedidoResponseDTO(Pedido pedido)
    {
        return new PedidoResponseDTO
        {
            Id = pedido.Id,
            DataPedido = pedido.DataPedido,
            ValorTotal = (decimal)pedido.ValorTotal,
            Status = pedido.Status.ToString(),
            FormaPagamento = pedido.FormaPagamento,
            Observacoes = pedido.Observacoes,
            ClienteId = pedido.ClienteId,
            ClienteNome = pedido.Cliente?.Nome ?? string.Empty,
            ClienteTelefone = pedido.Cliente?.Telefone,
            Itens = pedido.Itens.Select(i => new ItemPedidoDTO
            {
                Id = i.Id,
                ProdutoId = i.ProdutoId,
                ProdutoNome = i.Produto?.Nome ?? string.Empty,
                Quantidade = i.Quantidade,
                PrecoUnitario = (decimal)i.PrecoUnitario,
                Subtotal = (decimal)(i.PrecoUnitario * i.Quantidade)
            }).ToList()
        };
    }
}

public class StatusUpdateDTO
{
    public string Status { get; set; } = string.Empty;
}
