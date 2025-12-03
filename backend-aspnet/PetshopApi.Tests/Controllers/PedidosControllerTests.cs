using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Controllers;
using PetshopApi.Data;
using PetshopApi.DTOs;
using PetshopApi.Models;
using Xunit;

namespace PetshopApi.Tests.Controllers;

public class PedidosControllerTests : IDisposable
{
    private readonly PetshopContext _context;
    private readonly PedidosController _controller;
    private readonly Cliente _cliente;
    private readonly Categoria _categoria;
    private readonly Produto _produto;
    private readonly Pedido _pedido;

    public PedidosControllerTests()
    {
        var options = new DbContextOptionsBuilder<PetshopContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
            .Options;

        _context = new PetshopContext(options);

        // Seed data
        _cliente = new Cliente
        {
            Id = 1,
            Nome = "João Silva",
            Email = "joao@email.com",
            Cpf = "12345678901",
            Telefone = "11999999999"
        };

        _categoria = new Categoria
        {
            Id = 1,
            Nome = "Ração",
            Descricao = "Rações para pets"
        };

        _produto = new Produto
        {
            Id = 1,
            Nome = "Ração Premium",
            Descricao = "Ração para cães adultos",
            Preco = 89.90,
            QuantidadeEstoque = 50,
            Ativo = true,
            CategoriaId = 1
        };

        _pedido = new Pedido
        {
            Id = 1,
            DataPedido = DateTime.Now,
            ValorTotal = 0,
            Status = StatusPedido.PENDENTE,
            FormaPagamento = "pix",
            ClienteId = 1
        };

        _context.Clientes.Add(_cliente);
        _context.Categorias.Add(_categoria);
        _context.Produtos.Add(_produto);
        _context.Pedidos.Add(_pedido);
        _context.SaveChanges();

        _controller = new PedidosController(_context);
    }

    #region GetPedidos Tests

    [Fact]
    public async Task GetPedidos_ReturnsOkWithListOfPedidos()
    {
        // Act
        var result = await _controller.GetPedidos();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pedidos = Assert.IsAssignableFrom<IEnumerable<PedidoResponseDTO>>(okResult.Value);
        Assert.Single(pedidos);
    }

    [Fact]
    public async Task GetPedidos_WithMultiplePedidos_ReturnsAllOrdered()
    {
        // Arrange
        var pedido2 = new Pedido
        {
            Id = 2,
            DataPedido = DateTime.Now.AddDays(-1),
            ValorTotal = 100,
            Status = StatusPedido.CONFIRMADO,
            ClienteId = 1
        };
        _context.Pedidos.Add(pedido2);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.GetPedidos();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pedidos = Assert.IsAssignableFrom<IEnumerable<PedidoResponseDTO>>(okResult.Value).ToList();
        Assert.Equal(2, pedidos.Count);
        // First should be newer (ordered by DataPedido descending)
        Assert.True(pedidos[0].DataPedido >= pedidos[1].DataPedido);
    }

    #endregion

    #region GetPedido Tests

    [Fact]
    public async Task GetPedido_WithValidId_ReturnsOkWithPedido()
    {
        // Act
        var result = await _controller.GetPedido(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pedido = Assert.IsType<PedidoResponseDTO>(okResult.Value);
        Assert.Equal(1, pedido.Id);
        Assert.Equal("João Silva", pedido.ClienteNome);
    }

    [Fact]
    public async Task GetPedido_WithInvalidId_ReturnsNotFound()
    {
        // Act
        var result = await _controller.GetPedido(999);

        // Assert
        Assert.IsType<NotFoundObjectResult>(result.Result);
    }

    #endregion

    #region GetPedidosPorCliente Tests

    [Fact]
    public async Task GetPedidosPorCliente_ReturnsClientPedidos()
    {
        // Act
        var result = await _controller.GetPedidosPorCliente(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pedidos = Assert.IsAssignableFrom<IEnumerable<PedidoResponseDTO>>(okResult.Value);
        Assert.Single(pedidos);
    }

    [Fact]
    public async Task GetPedidosPorCliente_WithNoOrders_ReturnsEmptyList()
    {
        // Arrange
        var cliente2 = new Cliente
        {
            Id = 2,
            Nome = "Maria",
            Email = "maria@email.com",
            Cpf = "98765432101",
            Telefone = "11888888888"
        };
        _context.Clientes.Add(cliente2);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.GetPedidosPorCliente(2);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pedidos = Assert.IsAssignableFrom<IEnumerable<PedidoResponseDTO>>(okResult.Value);
        Assert.Empty(pedidos);
    }

    #endregion

    #region GetPedidosPorStatus Tests

    [Fact]
    public async Task GetPedidosPorStatus_WithValidStatus_ReturnsPedidos()
    {
        // Act
        var result = await _controller.GetPedidosPorStatus("pendente");

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pedidos = Assert.IsAssignableFrom<IEnumerable<PedidoResponseDTO>>(okResult.Value);
        Assert.Single(pedidos);
    }

    [Fact]
    public async Task GetPedidosPorStatus_WithInvalidStatus_ReturnsBadRequest()
    {
        // Act
        var result = await _controller.GetPedidosPorStatus("invalid");

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task GetContagemPorStatus_ReturnsCorrectCount()
    {
        // Act
        var result = await _controller.GetContagemPorStatus("pendente");

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var count = Assert.IsType<int>(okResult.Value);
        Assert.Equal(1, count);
    }

    #endregion

    #region CreatePedido Tests

    [Fact]
    public async Task CreatePedido_WithValidData_ReturnsCreated()
    {
        // Arrange
        var dto = new PedidoRequestDTO
        {
            ClienteId = 1,
            FormaPagamento = "credito",
            Observacoes = "Entrega urgente"
        };

        // Act
        var result = await _controller.CreatePedido(dto);

        // Assert
var createdResult = Assert.IsType<CreatedAtActionResult>(result.Result);
        var pedido = Assert.IsType<PedidoResponseDTO>(createdResult.Value);
        Assert.Equal("credito", pedido.FormaPagamento);
        Assert.Equal("PENDENTE", pedido.Status);
    }

    [Fact]
    public async Task CreatePedido_WithInvalidClient_ReturnsNotFound()
    {
        // Arrange
        var dto = new PedidoRequestDTO
        {
            ClienteId = 999,
            FormaPagamento = "pix"
        };

        // Act
        var result = await _controller.CreatePedido(dto);

        // Assert
        Assert.IsType<NotFoundObjectResult>(result.Result);
    }

    #endregion

    #region AdicionarItem Tests

    [Fact]
    public async Task AdicionarItem_WithValidData_ReturnsOkWithUpdatedPedido()
    {
        // Arrange
        var dto = new ItemPedidoRequestDTO
        {
            ProdutoId = 1,
            Quantidade = 2
        };

        // Act
        var result = await _controller.AdicionarItem(1, dto);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pedido = Assert.IsType<PedidoResponseDTO>(okResult.Value);
        Assert.Single(pedido.Itens);
        Assert.Equal(179.80m, pedido.ValorTotal); // 89.90 * 2
    }

    [Fact]
    public async Task AdicionarItem_ToPedidoNotFound_ReturnsNotFound()
    {
        // Arrange
        var dto = new ItemPedidoRequestDTO
        {
            ProdutoId = 1,
            Quantidade = 1
        };

        // Act
        var result = await _controller.AdicionarItem(999, dto);

        // Assert
        Assert.IsType<NotFoundObjectResult>(result.Result);
    }

    [Fact]
    public async Task AdicionarItem_ToConfirmedPedido_ReturnsBadRequest()
    {
        // Arrange
        _pedido.Status = StatusPedido.CONFIRMADO;
        await _context.SaveChangesAsync();

        var dto = new ItemPedidoRequestDTO
        {
            ProdutoId = 1,
            Quantidade = 1
        };

        // Act
        var result = await _controller.AdicionarItem(1, dto);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task AdicionarItem_WithInactiveProduct_ReturnsBadRequest()
    {
        // Arrange
        _produto.Ativo = false;
        await _context.SaveChangesAsync();

        var dto = new ItemPedidoRequestDTO
        {
            ProdutoId = 1,
            Quantidade = 1
        };

        // Act
        var result = await _controller.AdicionarItem(1, dto);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task AdicionarItem_WithInsufficientStock_ReturnsBadRequest()
    {
        // Arrange
        var dto = new ItemPedidoRequestDTO
        {
            ProdutoId = 1,
            Quantidade = 100 // More than 50 in stock
        };

        // Act
        var result = await _controller.AdicionarItem(1, dto);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task AdicionarItem_ExistingProduct_UpdatesQuantity()
    {
        // Arrange
        var item = new ItemPedido
        {
            Id = 1,
            PedidoId = 1,
            ProdutoId = 1,
            Quantidade = 1,
            PrecoUnitario = 89.90
        };
        _context.ItensPedido.Add(item);
        await _context.SaveChangesAsync();

        var dto = new ItemPedidoRequestDTO
        {
            ProdutoId = 1,
            Quantidade = 2
        };

        // Act
        var result = await _controller.AdicionarItem(1, dto);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pedido = Assert.IsType<PedidoResponseDTO>(okResult.Value);
        Assert.Single(pedido.Itens);
        Assert.Equal(3, pedido.Itens.First().Quantidade); // 1 + 2
    }

    #endregion

    #region RemoverItem Tests

    [Fact]
    public async Task RemoverItem_WithValidIds_ReturnsOk()
    {
        // Arrange
        var item = new ItemPedido
        {
            Id = 1,
            PedidoId = 1,
            ProdutoId = 1,
            Quantidade = 1,
            PrecoUnitario = 89.90
        };
        _context.ItensPedido.Add(item);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.RemoverItem(1, 1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pedido = Assert.IsType<PedidoResponseDTO>(okResult.Value);
        Assert.Empty(pedido.Itens);
    }

    [Fact]
    public async Task RemoverItem_FromConfirmedPedido_ReturnsBadRequest()
    {
        // Arrange
        var item = new ItemPedido
        {
            Id = 1,
            PedidoId = 1,
            ProdutoId = 1,
            Quantidade = 1,
            PrecoUnitario = 89.90
        };
        _context.ItensPedido.Add(item);
        _pedido.Status = StatusPedido.CONFIRMADO;
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.RemoverItem(1, 1);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task RemoverItem_ItemNotFound_ReturnsNotFound()
    {
        // Act
        var result = await _controller.RemoverItem(1, 999);

        // Assert
        Assert.IsType<NotFoundObjectResult>(result.Result);
    }

    #endregion

    #region ConfirmarPedido Tests

    [Fact]
    public async Task ConfirmarPedido_WithItems_ReturnsOkAndReducesStock()
    {
        // Arrange
        var item = new ItemPedido
        {
            Id = 1,
            PedidoId = 1,
            ProdutoId = 1,
            Quantidade = 5,
            PrecoUnitario = 89.90
        };
        _context.ItensPedido.Add(item);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.ConfirmarPedido(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pedido = Assert.IsType<PedidoResponseDTO>(okResult.Value);
        Assert.Equal("CONFIRMADO", pedido.Status);

        // Verify stock was reduced
        var produto = await _context.Produtos.FindAsync(1L);
        Assert.Equal(45, produto?.QuantidadeEstoque); // 50 - 5
    }

    [Fact]
    public async Task ConfirmarPedido_WithoutItems_ReturnsBadRequest()
    {
        // Act
        var result = await _controller.ConfirmarPedido(1);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task ConfirmarPedido_AlreadyConfirmed_ReturnsBadRequest()
    {
        // Arrange
        _pedido.Status = StatusPedido.CONFIRMADO;
        var item = new ItemPedido
        {
            Id = 1,
            PedidoId = 1,
            ProdutoId = 1,
            Quantidade = 1,
            PrecoUnitario = 89.90
        };
        _context.ItensPedido.Add(item);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.ConfirmarPedido(1);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task ConfirmarPedido_InsufficientStock_ReturnsBadRequest()
    {
        // Arrange
        var item = new ItemPedido
        {
            Id = 1,
            PedidoId = 1,
            ProdutoId = 1,
            Quantidade = 100, // More than 50 in stock
            PrecoUnitario = 89.90
        };
        _context.ItensPedido.Add(item);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.ConfirmarPedido(1);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    #endregion

    #region AtualizarStatus Tests

    [Fact]
    public async Task AtualizarStatus_WithValidStatus_ReturnsOk()
    {
        // Arrange
        var dto = new StatusUpdateDTO { Status = "ENVIADO" };

        // Act
        var result = await _controller.AtualizarStatus(1, dto);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pedido = Assert.IsType<PedidoResponseDTO>(okResult.Value);
        Assert.Equal("ENVIADO", pedido.Status);
    }

    [Fact]
    public async Task AtualizarStatus_WithInvalidStatus_ReturnsBadRequest()
    {
        // Arrange
        var dto = new StatusUpdateDTO { Status = "InvalidStatus" };

        // Act
        var result = await _controller.AtualizarStatus(1, dto);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    #endregion

    #region CancelarPedido Tests

    [Fact]
    public async Task CancelarPedido_Pending_ReturnsOk()
    {
        // Act
        var result = await _controller.CancelarPedido(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pedido = Assert.IsType<PedidoResponseDTO>(okResult.Value);
        Assert.Equal("CANCELADO", pedido.Status);
    }

    [Fact]
    public async Task CancelarPedido_Confirmed_RestoresStock()
    {
        // Arrange
        var item = new ItemPedido
        {
            Id = 1,
            PedidoId = 1,
            ProdutoId = 1,
            Quantidade = 10,
            PrecoUnitario = 89.90
        };
        _context.ItensPedido.Add(item);
        _pedido.Status = StatusPedido.CONFIRMADO;
        _produto.QuantidadeEstoque = 40; // Already reduced by 10
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.CancelarPedido(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pedido = Assert.IsType<PedidoResponseDTO>(okResult.Value);
        Assert.Equal("CANCELADO", pedido.Status);

        // Verify stock was restored
        var produto = await _context.Produtos.FindAsync(1L);
        Assert.Equal(50, produto?.QuantidadeEstoque); // 40 + 10
    }

    [Fact]
    public async Task CancelarPedido_AlreadyCanceled_ReturnsBadRequest()
    {
        // Arrange
        _pedido.Status = StatusPedido.CANCELADO;
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.CancelarPedido(1);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task CancelarPedido_Delivered_ReturnsBadRequest()
    {
        // Arrange
        _pedido.Status = StatusPedido.ENTREGUE;
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.CancelarPedido(1);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    #endregion

    #region DeletePedido Tests

    [Fact]
    public async Task DeletePedido_WithValidId_ReturnsNoContent()
    {
        // Act
        var result = await _controller.DeletePedido(1);

        // Assert
        Assert.IsType<NoContentResult>(result);
        var pedido = await _context.Pedidos.FindAsync(1L);
        Assert.Null(pedido);
    }

    [Fact]
    public async Task DeletePedido_WithInvalidId_ReturnsNotFound()
    {
        // Act
        var result = await _controller.DeletePedido(999);

        // Assert
        Assert.IsType<NotFoundObjectResult>(result);
    }

    #endregion

    public void Dispose()
    {
        _context.Database.EnsureDeleted();
        _context.Dispose();
    }
}
