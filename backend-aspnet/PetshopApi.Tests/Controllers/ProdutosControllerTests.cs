using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Controllers;
using PetshopApi.Data;
using PetshopApi.Models;
using Xunit;

namespace PetshopApi.Tests.Controllers;

public class ProdutosControllerTests : IDisposable
{
    private readonly PetshopContext _context;
    private readonly ProdutosController _controller;
    private readonly Categoria _categoria;
    private readonly Produto _produto;

    public ProdutosControllerTests()
    {
        var options = new DbContextOptionsBuilder<PetshopContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
            .Options;

        _context = new PetshopContext(options);

        // Seed data
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
            CategoriaId = 1,
            Categoria = _categoria
        };

        _context.Categorias.Add(_categoria);
        _context.Produtos.Add(_produto);
        _context.SaveChanges();

        _controller = new ProdutosController(_context);
    }

    [Fact]
    public async Task GetAll_ReturnsOkWithListOfProdutos()
    {
        // Act
        var result = await _controller.GetAll();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var produtos = Assert.IsAssignableFrom<IEnumerable<dynamic>>(okResult.Value);
        Assert.Single(produtos);
    }

    [Fact]
    public async Task GetDisponiveis_ReturnsOnlyActiveProdutos()
    {
        // Arrange
        var produtoInativo = new Produto
        {
            Id = 2,
            Nome = "Produto Inativo",
            Preco = 50.0,
            QuantidadeEstoque = 10,
            Ativo = false,
            CategoriaId = 1
        };
        _context.Produtos.Add(produtoInativo);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.GetDisponiveis();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var produtos = Assert.IsAssignableFrom<IEnumerable<dynamic>>(okResult.Value);
        Assert.Single(produtos);
    }

    [Fact]
    public async Task GetById_WithValidId_ReturnsOkWithProduto()
    {
        // Act
        var result = await _controller.GetById(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        Assert.NotNull(okResult.Value);
    }

    [Fact]
    public async Task GetById_WithInvalidId_ReturnsNotFound()
    {
        // Act
        var result = await _controller.GetById(999);

        // Assert
        Assert.IsType<NotFoundResult>(result.Result);
    }

    [Fact]
    public async Task GetByCategoria_ReturnsProductsFromCategory()
    {
        // Act
        var result = await _controller.GetByCategoria(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var produtos = Assert.IsAssignableFrom<IEnumerable<dynamic>>(okResult.Value);
        Assert.Single(produtos);
    }

    [Fact]
    public async Task Search_WithValidTerm_ReturnsMatchingProducts()
    {
        // Act
        var result = await _controller.Search("Ração");

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var produtos = Assert.IsAssignableFrom<IEnumerable<dynamic>>(okResult.Value);
        Assert.Single(produtos);
    }

    [Fact]
    public async Task Search_WithNoMatch_ReturnsEmptyList()
    {
        // Act
        var result = await _controller.Search("XYZ123");

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var produtos = Assert.IsAssignableFrom<IEnumerable<dynamic>>(okResult.Value);
        Assert.Empty(produtos);
    }

    [Fact]
    public async Task GetEstoqueBaixo_ReturnsProductsWithLowStock()
    {
        // Arrange
        var produtoBaixoEstoque = new Produto
        {
            Id = 3,
            Nome = "Produto Baixo Estoque",
            Preco = 30.0,
            QuantidadeEstoque = 5,
            Ativo = true,
            CategoriaId = 1
        };
        _context.Produtos.Add(produtoBaixoEstoque);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.GetEstoqueBaixo(10);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var produtos = Assert.IsAssignableFrom<IEnumerable<dynamic>>(okResult.Value);
        Assert.Single(produtos);
    }

    [Fact]
    public async Task Create_WithValidData_ReturnsCreatedResult()
    {
        // Arrange
        var novoProduto = new PetshopApi.DTOs.ProdutoRequestDTO
        {
            Nome = "Novo Produto",
            Descricao = "Descrição",
            Preco = 45.90,
            QuantidadeEstoque = 20,
            Ativo = true,
            CategoriaId = 1
        };

        // Act
        var result = await _controller.Create(novoProduto);

        // Assert
        var createdResult = Assert.IsType<CreatedAtActionResult>(result.Result);
        Assert.NotNull(createdResult.Value);
        Assert.Equal(nameof(_controller.GetById), createdResult.ActionName);
    }

    [Fact]
    public async Task Create_WithInvalidCategory_ReturnsBadRequest()
    {
        // Arrange
        var novoProduto = new PetshopApi.DTOs.ProdutoRequestDTO
        {
            Nome = "Novo Produto",
            Preco = 45.90,
            QuantidadeEstoque = 20,
            CategoriaId = 999 // Categoria inexistente
        };

        // Act
        var result = await _controller.Create(novoProduto);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task Update_WithValidData_ReturnsOk()
    {
        // Arrange
        var produtoAtualizado = new PetshopApi.DTOs.ProdutoRequestDTO
        {
            Nome = "Ração Premium Plus",
            Descricao = "Nova descrição",
            Preco = 99.90,
            QuantidadeEstoque = 60,
            Ativo = true,
            CategoriaId = 1
        };

        // Act
        var result = await _controller.Update(1, produtoAtualizado);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        Assert.NotNull(okResult.Value);
    }

    [Fact]
    public async Task Update_WithInvalidId_ReturnsNotFound()
    {
        // Arrange
        var produtoAtualizado = new PetshopApi.DTOs.ProdutoRequestDTO
        {
            Nome = "Produto",
            Preco = 50.0,
            QuantidadeEstoque = 10,
            CategoriaId = 1
        };

        // Act
        var result = await _controller.Update(999, produtoAtualizado);

        // Assert
        Assert.IsType<NotFoundResult>(result.Result);
    }

    [Fact]
    public async Task UpdateEstoque_WithValidData_ReturnsOk()
    {
        // Act
        var result = await _controller.UpdateEstoque(1, 100);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        Assert.NotNull(okResult.Value);
    }

    [Fact]
    public async Task AdicionarEstoque_IncreasesStock()
    {
        // Act
        var result = await _controller.AdicionarEstoque(1, 25);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var produto = await _context.Produtos.FindAsync(1L);
        Assert.Equal(75, produto?.QuantidadeEstoque); // 50 + 25
    }

    [Fact]
    public async Task Ativar_ActivatesProduct()
    {
        // Arrange
        _produto.Ativo = false;
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.Ativar(1);

        // Assert
        Assert.IsType<NoContentResult>(result);
        var produto = await _context.Produtos.FindAsync(1L);
        Assert.True(produto?.Ativo);
    }

    [Fact]
    public async Task Desativar_DeactivatesProduct()
    {
        // Act
        var result = await _controller.Desativar(1);

        // Assert
        Assert.IsType<NoContentResult>(result);
        var produto = await _context.Produtos.FindAsync(1L);
        Assert.False(produto?.Ativo);
    }

    [Fact]
    public async Task Delete_WithValidId_ReturnsNoContent()
    {
        // Act
        var result = await _controller.Delete(1);

        // Assert
        Assert.IsType<NoContentResult>(result);
        var produto = await _context.Produtos.FindAsync(1L);
        Assert.Null(produto);
    }

    [Fact]
    public async Task Delete_WithInvalidId_ReturnsNotFound()
    {
        // Act
        var result = await _controller.Delete(999);

        // Assert
        Assert.IsType<NotFoundResult>(result);
    }

    public void Dispose()
    {
        _context.Database.EnsureDeleted();
        _context.Dispose();
    }
}
