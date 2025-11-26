using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Controllers;
using PetshopApi.Data;
using PetshopApi.DTOs;
using PetshopApi.Models;
using Xunit;

namespace PetshopApi.Tests.Controllers;

public class ServicosControllerTests : IDisposable
{
    private readonly PetshopContext _context;
    private readonly ServicosController _controller;
    private readonly Servico _servico;

    public ServicosControllerTests()
    {
        var options = new DbContextOptionsBuilder<PetshopContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
            .Options;

        _context = new PetshopContext(options);

        // Seed data
        _servico = new Servico
        {
            Id = 1,
            Nome = "Banho",
            Descricao = "Banho completo com secagem",
            Preco = 50.0,
            Ativo = true
        };

        _context.Servicos.Add(_servico);
        _context.SaveChanges();

        _controller = new ServicosController(_context);
    }

    #region GetAll Tests

    [Fact]
    public async Task GetAll_ReturnsOkWithListOfServicos()
    {
        // Act
        var result = await _controller.GetAll();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var servicos = Assert.IsAssignableFrom<IEnumerable<ServicoResponseDTO>>(okResult.Value);
        Assert.Single(servicos);
    }

    [Fact]
    public async Task GetAll_IncludesInactiveServicos()
    {
        // Arrange
        var servicoInativo = new Servico
        {
            Id = 2,
            Nome = "Tosa",
            Preco = 40.0,
            Ativo = false
        };
        _context.Servicos.Add(servicoInativo);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.GetAll();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var servicos = Assert.IsAssignableFrom<IEnumerable<ServicoResponseDTO>>(okResult.Value).ToList();
        Assert.Equal(2, servicos.Count);
    }

    #endregion

    #region GetAtivos Tests

    [Fact]
    public async Task GetAtivos_ReturnsOnlyActiveServicos()
    {
        // Arrange
        var servicoInativo = new Servico
        {
            Id = 2,
            Nome = "Tosa",
            Preco = 40.0,
            Ativo = false
        };
        _context.Servicos.Add(servicoInativo);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.GetAtivos();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var servicos = Assert.IsAssignableFrom<IEnumerable<ServicoResponseDTO>>(okResult.Value).ToList();
        Assert.Single(servicos);
        Assert.All(servicos, s => Assert.True(s.Ativo));
    }

    #endregion

    #region GetById Tests

    [Fact]
    public async Task GetById_WithValidId_ReturnsOkWithServico()
    {
        // Act
        var result = await _controller.GetById(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var servico = Assert.IsType<ServicoResponseDTO>(okResult.Value);
        Assert.Equal("Banho", servico.Nome);
        Assert.Equal(50.0, servico.Preco);
    }

    [Fact]
    public async Task GetById_WithInvalidId_ReturnsNotFound()
    {
        // Act
        var result = await _controller.GetById(999);

        // Assert
        Assert.IsType<NotFoundResult>(result.Result);
    }

    #endregion

    #region Create Tests

    [Fact]
    public async Task Create_WithValidData_ReturnsCreated()
    {
        // Arrange
        var dto = new ServicoRequestDTO
        {
            Nome = "Tosa",
            Descricao = "Tosa completa",
            Preco = 45.0,
            Ativo = true
        };

        // Act
        var result = await _controller.Create(dto);

        // Assert
        var createdResult = Assert.IsType<CreatedAtActionResult>(result.Result);
        var servico = Assert.IsType<ServicoResponseDTO>(createdResult.Value);
        Assert.Equal("Tosa", servico.Nome);
        Assert.Equal(45.0, servico.Preco);
    }

    [Fact]
    public async Task Create_WithoutDescription_ReturnsCreated()
    {
        // Arrange
        var dto = new ServicoRequestDTO
        {
            Nome = "Consulta Veterinária",
            Preco = 120.0,
            Ativo = true
        };

        // Act
        var result = await _controller.Create(dto);

        // Assert
        var createdResult = Assert.IsType<CreatedAtActionResult>(result.Result);
        var servico = Assert.IsType<ServicoResponseDTO>(createdResult.Value);
        Assert.Null(servico.Descricao);
    }

    #endregion

    #region Update Tests

    [Fact]
    public async Task Update_WithValidData_ReturnsOk()
    {
        // Arrange
        var dto = new ServicoRequestDTO
        {
            Nome = "Banho Premium",
            Descricao = "Banho premium com produtos especiais",
            Preco = 75.0,
            Ativo = true
        };

        // Act
        var result = await _controller.Update(1, dto);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var servico = Assert.IsType<ServicoResponseDTO>(okResult.Value);
        Assert.Equal("Banho Premium", servico.Nome);
        Assert.Equal(75.0, servico.Preco);
    }

    [Fact]
    public async Task Update_WithInvalidId_ReturnsNotFound()
    {
        // Arrange
        var dto = new ServicoRequestDTO
        {
            Nome = "Banho",
            Preco = 50.0,
            Ativo = true
        };

        // Act
        var result = await _controller.Update(999, dto);

        // Assert
        Assert.IsType<NotFoundResult>(result.Result);
    }

    #endregion

    #region Ativar/Desativar Tests

    [Fact]
    public async Task Ativar_WithValidId_ReturnsNoContent()
    {
        // Arrange
        _servico.Ativo = false;
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.Ativar(1);

        // Assert
        Assert.IsType<NoContentResult>(result);
        var servico = await _context.Servicos.FindAsync(1L);
        Assert.True(servico?.Ativo);
    }

    [Fact]
    public async Task Ativar_WithInvalidId_ReturnsNotFound()
    {
        // Act
        var result = await _controller.Ativar(999);

        // Assert
        Assert.IsType<NotFoundResult>(result);
    }

    [Fact]
    public async Task Desativar_WithValidId_ReturnsNoContent()
    {
        // Act
        var result = await _controller.Desativar(1);

        // Assert
        Assert.IsType<NoContentResult>(result);
        var servico = await _context.Servicos.FindAsync(1L);
        Assert.False(servico?.Ativo);
    }

    [Fact]
    public async Task Desativar_WithInvalidId_ReturnsNotFound()
    {
        // Act
        var result = await _controller.Desativar(999);

        // Assert
        Assert.IsType<NotFoundResult>(result);
    }

    #endregion

    #region Delete Tests

    [Fact]
    public async Task Delete_WithValidId_ReturnsNoContent()
    {
        // Act
        var result = await _controller.Delete(1);

        // Assert
        Assert.IsType<NoContentResult>(result);
        var servico = await _context.Servicos.FindAsync(1L);
        Assert.Null(servico);
    }

    [Fact]
    public async Task Delete_WithInvalidId_ReturnsNotFound()
    {
        // Act
        var result = await _controller.Delete(999);

        // Assert
        Assert.IsType<NotFoundResult>(result);
    }

    #endregion

    public void Dispose()
    {
        _context.Database.EnsureDeleted();
        _context.Dispose();
    }
}
