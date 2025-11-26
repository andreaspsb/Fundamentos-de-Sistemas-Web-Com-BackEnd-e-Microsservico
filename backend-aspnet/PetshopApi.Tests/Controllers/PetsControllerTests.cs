using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Controllers;
using PetshopApi.Data;
using PetshopApi.DTOs;
using PetshopApi.Models;
using Xunit;

namespace PetshopApi.Tests.Controllers;

public class PetsControllerTests : IDisposable
{
    private readonly PetshopContext _context;
    private readonly PetsController _controller;
    private readonly Cliente _cliente;
    private readonly Pet _pet;

    public PetsControllerTests()
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

        _pet = new Pet
        {
            Id = 1,
            Nome = "Rex",
            Tipo = "cao",
            Raca = "Labrador",
            Idade = 3,
            Peso = 25.0,
            Sexo = "M",
            Castrado = true,
            TemAlergia = false,
            PrecisaMedicacao = false,
            ComportamentoAgressivo = false,
            ClienteId = 1
        };

        _context.Clientes.Add(_cliente);
        _context.Pets.Add(_pet);
        _context.SaveChanges();

        _controller = new PetsController(_context);
    }

    #region GetAll Tests

    [Fact]
    public async Task GetAll_ReturnsOkWithListOfPets()
    {
        // Act
        var result = await _controller.GetAll();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pets = Assert.IsAssignableFrom<IEnumerable<PetResponseDTO>>(okResult.Value);
        Assert.Single(pets);
    }

    [Fact]
    public async Task GetAll_WithMultiplePets_ReturnsAll()
    {
        // Arrange
        var pet2 = new Pet
        {
            Id = 2,
            Nome = "Mimi",
            Tipo = "gato",
            Raca = "Persa",
            Idade = 2,
            Sexo = "F",
            ClienteId = 1
        };
        _context.Pets.Add(pet2);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.GetAll();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pets = Assert.IsAssignableFrom<IEnumerable<PetResponseDTO>>(okResult.Value).ToList();
        Assert.Equal(2, pets.Count);
    }

    #endregion

    #region GetById Tests

    [Fact]
    public async Task GetById_WithValidId_ReturnsOkWithPet()
    {
        // Act
        var result = await _controller.GetById(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pet = Assert.IsType<PetResponseDTO>(okResult.Value);
        Assert.Equal("Rex", pet.Nome);
        Assert.Equal("João Silva", pet.ClienteNome);
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

    #region GetByCliente Tests

    [Fact]
    public async Task GetByCliente_ReturnsPetsForCliente()
    {
        // Act
        var result = await _controller.GetByCliente(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pets = Assert.IsAssignableFrom<IEnumerable<PetResponseDTO>>(okResult.Value);
        Assert.Single(pets);
    }

    [Fact]
    public async Task GetByCliente_WithNoPets_ReturnsEmptyList()
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
        var result = await _controller.GetByCliente(2);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pets = Assert.IsAssignableFrom<IEnumerable<PetResponseDTO>>(okResult.Value);
        Assert.Empty(pets);
    }

    #endregion

    #region GetByTipo Tests

    [Fact]
    public async Task GetByTipo_ReturnsPetsOfType()
    {
        // Act
        var result = await _controller.GetByTipo("cao");

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pets = Assert.IsAssignableFrom<IEnumerable<PetResponseDTO>>(okResult.Value);
        Assert.Single(pets);
    }

    [Fact]
    public async Task GetByTipo_CaseInsensitive_ReturnsPets()
    {
        // Act
        var result = await _controller.GetByTipo("CAO");

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pets = Assert.IsAssignableFrom<IEnumerable<PetResponseDTO>>(okResult.Value);
        Assert.Single(pets);
    }

    [Fact]
    public async Task GetByTipo_WithNoMatch_ReturnsEmptyList()
    {
        // Act
        var result = await _controller.GetByTipo("passaro");

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pets = Assert.IsAssignableFrom<IEnumerable<PetResponseDTO>>(okResult.Value);
        Assert.Empty(pets);
    }

    #endregion

    #region Create Tests

    [Fact]
    public async Task Create_WithValidData_ReturnsCreated()
    {
        // Arrange
        var dto = new PetRequestDTO
        {
            Nome = "Thor",
            Tipo = "cao",
            Raca = "Golden Retriever",
            Idade = 1,
            Peso = 20.0,
            Sexo = "M",
            Castrado = false,
            ClienteId = 1
        };

        // Act
        var result = await _controller.Create(dto);

        // Assert
        var createdResult = Assert.IsType<CreatedAtActionResult>(result.Result);
        var pet = Assert.IsType<PetResponseDTO>(createdResult.Value);
        Assert.Equal("Thor", pet.Nome);
        Assert.Equal("Golden Retriever", pet.Raca);
    }

    [Fact]
    public async Task Create_WithInvalidCliente_ReturnsBadRequest()
    {
        // Arrange
        var dto = new PetRequestDTO
        {
            Nome = "Thor",
            Tipo = "cao",
            Raca = "Golden Retriever",
            Idade = 1,
            Sexo = "M",
            ClienteId = 999
        };

        // Act
        var result = await _controller.Create(dto);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task Create_WithSpecialNeeds_SetsAllFields()
    {
        // Arrange
        var dto = new PetRequestDTO
        {
            Nome = "Max",
            Tipo = "cao",
            Raca = "Poodle",
            Idade = 5,
            Sexo = "M",
            TemAlergia = true,
            PrecisaMedicacao = true,
            ComportamentoAgressivo = true,
            Observacoes = "Precisa de cuidados especiais",
            ClienteId = 1
        };

        // Act
        var result = await _controller.Create(dto);

        // Assert
        var createdResult = Assert.IsType<CreatedAtActionResult>(result.Result);
        var pet = Assert.IsType<PetResponseDTO>(createdResult.Value);
        Assert.True(pet.TemAlergia);
        Assert.True(pet.PrecisaMedicacao);
        Assert.True(pet.ComportamentoAgressivo);
    }

    #endregion

    #region Update Tests

    [Fact]
    public async Task Update_WithValidData_ReturnsOk()
    {
        // Arrange
        var dto = new PetRequestDTO
        {
            Nome = "Rex Atualizado",
            Tipo = "cao",
            Raca = "Labrador Retriever",
            Idade = 4,
            Peso = 28.0,
            Sexo = "M",
            Castrado = true,
            ClienteId = 1
        };

        // Act
        var result = await _controller.Update(1, dto);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var pet = Assert.IsType<PetResponseDTO>(okResult.Value);
        Assert.Equal("Rex Atualizado", pet.Nome);
        Assert.Equal(4, pet.Idade);
    }

    [Fact]
    public async Task Update_WithInvalidId_ReturnsNotFound()
    {
        // Arrange
        var dto = new PetRequestDTO
        {
            Nome = "Rex",
            Tipo = "cao",
            Raca = "Labrador",
            Idade = 3,
            Sexo = "M",
            ClienteId = 1
        };

        // Act
        var result = await _controller.Update(999, dto);

        // Assert
        Assert.IsType<NotFoundResult>(result.Result);
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
        var pet = await _context.Pets.FindAsync(1L);
        Assert.Null(pet);
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
