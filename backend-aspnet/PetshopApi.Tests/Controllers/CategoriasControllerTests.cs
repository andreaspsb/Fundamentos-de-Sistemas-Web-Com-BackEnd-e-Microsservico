using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Controllers;
using PetshopApi.Data;
using PetshopApi.Models;
using Xunit;

namespace PetshopApi.Tests.Controllers;

public class CategoriasControllerTests : IDisposable
{
    private readonly PetshopContext _context;
    private readonly CategoriasController _controller;
    private readonly Categoria _categoria;

    public CategoriasControllerTests()
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
            Descricao = "Rações para pets",
            Ativo = true
        };

        _context.Categorias.Add(_categoria);
        _context.SaveChanges();

        _controller = new CategoriasController(_context);
    }

    #region GetAll Tests

    [Fact]
    public async Task GetAll_ReturnsOkWithListOfCategorias()
    {
        // Act
        var result = await _controller.GetAll();

        // Assert
        var okResult = Assert.IsType<ActionResult<IEnumerable<Categoria>>>(result);
        var categorias = Assert.IsAssignableFrom<IEnumerable<Categoria>>(okResult.Value);
        Assert.Single(categorias);
    }

    [Fact]
    public async Task GetAll_IncludesInactiveCategorias()
    {
        // Arrange
        var categoriaInativa = new Categoria
        {
            Id = 2,
            Nome = "Brinquedos",
            Ativo = false
        };
        _context.Categorias.Add(categoriaInativa);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.GetAll();

        // Assert
        var okResult = Assert.IsType<ActionResult<IEnumerable<Categoria>>>(result);
        var categorias = Assert.IsAssignableFrom<IEnumerable<Categoria>>(okResult.Value).ToList();
        Assert.Equal(2, categorias.Count);
    }

    #endregion

    #region GetById Tests

    [Fact]
    public async Task GetById_WithValidId_ReturnsOkWithCategoria()
    {
        // Act
        var result = await _controller.GetById(1);

        // Assert
        var categoria = Assert.IsType<Categoria>(result.Value);
        Assert.Equal("Ração", categoria.Nome);
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

    #region GetAtivas Tests

    [Fact]
    public async Task GetAtivas_ReturnsOnlyActiveCategorias()
    {
        // Arrange
        var categoriaInativa = new Categoria
        {
            Id = 2,
            Nome = "Brinquedos",
            Ativo = false
        };
        _context.Categorias.Add(categoriaInativa);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.GetAtivas();

        // Assert
        var okResult = Assert.IsType<ActionResult<IEnumerable<Categoria>>>(result);
        var categorias = Assert.IsAssignableFrom<IEnumerable<Categoria>>(okResult.Value).ToList();
        Assert.Single(categorias);
        Assert.All(categorias, c => Assert.True(c.Ativo));
    }

    #endregion

    #region Create Tests

    [Fact]
    public async Task Create_WithValidData_ReturnsCreated()
    {
        // Arrange
        var categoria = new Categoria
        {
            Nome = "Medicamentos",
            Descricao = "Medicamentos para pets",
            Ativo = true
        };

        // Act
        var result = await _controller.Create(categoria);

        // Assert
        var createdResult = Assert.IsType<CreatedAtActionResult>(result.Result);
        var categoriaCreated = Assert.IsType<Categoria>(createdResult.Value);
        Assert.Equal("Medicamentos", categoriaCreated.Nome);
    }

    [Fact]
    public async Task Create_WithoutDescription_ReturnsCreated()
    {
        // Arrange
        var categoria = new Categoria
        {
            Nome = "Acessórios",
            Ativo = true
        };

        // Act
        var result = await _controller.Create(categoria);

        // Assert
        var createdResult = Assert.IsType<CreatedAtActionResult>(result.Result);
        var categoriaCreated = Assert.IsType<Categoria>(createdResult.Value);
        Assert.Null(categoriaCreated.Descricao);
    }

    #endregion

    #region Update Tests

    [Fact]
    public async Task Update_WithValidData_ReturnsNoContent()
    {
        // Arrange - Detach the tracked entity first
        _context.Entry(_categoria).State = EntityState.Detached;
        
        var categoriaAtualizada = new Categoria
        {
            Id = 1,
            Nome = "Ração Premium",
            Descricao = "Rações premium para pets",
            Ativo = true
        };

        // Act
        var result = await _controller.Update(1, categoriaAtualizada);

        // Assert
        Assert.IsType<NoContentResult>(result);
        var categoria = await _context.Categorias.AsNoTracking().FirstOrDefaultAsync(c => c.Id == 1);
        Assert.Equal("Ração Premium", categoria?.Nome);
    }

    [Fact]
    public async Task Update_WithMismatchedId_ReturnsBadRequest()
    {
        // Arrange
        var categoriaAtualizada = new Categoria
        {
            Id = 2, // Different from route id
            Nome = "Ração",
            Ativo = true
        };

        // Act
        var result = await _controller.Update(1, categoriaAtualizada);

        // Assert
        Assert.IsType<BadRequestResult>(result);
    }

    [Fact]
    public async Task Update_WithInvalidId_ReturnsNotFound()
    {
        // Arrange
        var categoriaAtualizada = new Categoria
        {
            Id = 999,
            Nome = "Categoria",
            Ativo = true
        };

        // Act
        var result = await _controller.Update(999, categoriaAtualizada);

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
        var categoria = await _context.Categorias.FindAsync(1L);
        Assert.Null(categoria);
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
