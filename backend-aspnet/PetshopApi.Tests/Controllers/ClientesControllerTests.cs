using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Controllers;
using PetshopApi.Data;
using PetshopApi.Models;
using Xunit;

namespace PetshopApi.Tests.Controllers;

public class ClientesControllerTests : IDisposable
{
    private readonly PetshopContext _context;
    private readonly ClientesController _controller;
    private readonly Cliente _cliente;

    public ClientesControllerTests()
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
            Cpf = "12345678901",
            Telefone = "11987654321",
            Email = "joao@example.com",
            DataNascimento = new DateTime(1990, 5, 15),
            Sexo = "M",
            Endereco = "Rua A",
            Numero = "123",
            Complemento = "Apt 45",
            Bairro = "Centro",
            Cidade = "São Paulo"
        };

        _context.Clientes.Add(_cliente);
        _context.SaveChanges();

        _controller = new ClientesController(_context);
    }

    [Fact]
    public async Task GetAll_ReturnsOkWithListOfClientes()
    {
        // Act
        var result = await _controller.GetAll();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var clientes = Assert.IsAssignableFrom<IEnumerable<dynamic>>(okResult.Value);
        Assert.Single(clientes);
    }

    [Fact]
    public async Task GetById_WithValidId_ReturnsOkWithCliente()
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
    public async Task GetByCpf_WithValidCpf_ReturnsOkWithCliente()
    {
        // Act
        var result = await _controller.GetByCpf("12345678901");

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        Assert.NotNull(okResult.Value);
    }

    [Fact]
    public async Task GetByCpf_WithInvalidCpf_ReturnsNotFound()
    {
        // Act
        var result = await _controller.GetByCpf("99999999999");

        // Assert
        Assert.IsType<NotFoundResult>(result.Result);
    }

    [Fact]
    public async Task Create_WithValidData_ReturnsCreatedResult()
    {
        // Arrange
        var novoCliente = new PetshopApi.DTOs.ClienteRequestDTO
        {
            Nome = "Maria Santos",
            Cpf = "98765432100",
            Telefone = "11912345678",
            Email = "maria@example.com",
            DataNascimento = new DateTime(1985, 3, 20),
            Sexo = "F",
            Endereco = "Rua B",
            Numero = "456",
            Bairro = "Jardins",
            Cidade = "São Paulo"
        };

        // Act
        var result = await _controller.Create(novoCliente);

        // Assert
        var createdResult = Assert.IsType<CreatedAtActionResult>(result.Result);
        Assert.NotNull(createdResult.Value);
        Assert.Equal(nameof(_controller.GetById), createdResult.ActionName);
    }

    [Fact]
    public async Task Create_WithDuplicateCpf_ReturnsBadRequest()
    {
        // Arrange
        var novoCliente = new PetshopApi.DTOs.ClienteRequestDTO
        {
            Nome = "Pedro Costa",
            Cpf = "12345678901", // CPF duplicado
            Telefone = "11999999999",
            Email = "pedro@example.com",
            DataNascimento = new DateTime(1988, 7, 10),
            Sexo = "M",
            Endereco = "Rua C",
            Numero = "789",
            Bairro = "Vila Madalena",
            Cidade = "São Paulo"
        };

        // Act
        var result = await _controller.Create(novoCliente);

        // Assert
        var badRequestResult = Assert.IsType<BadRequestObjectResult>(result.Result);
        Assert.NotNull(badRequestResult.Value);
    }

    [Fact]
    public async Task Create_WithDuplicateEmail_ReturnsBadRequest()
    {
        // Arrange
        var novoCliente = new PetshopApi.DTOs.ClienteRequestDTO
        {
            Nome = "Ana Paula",
            Cpf = "11111111111",
            Telefone = "11988888888",
            Email = "joao@example.com", // Email duplicado
            DataNascimento = new DateTime(1992, 11, 25),
            Sexo = "F",
            Endereco = "Rua D",
            Numero = "321",
            Bairro = "Pinheiros",
            Cidade = "São Paulo"
        };

        // Act
        var result = await _controller.Create(novoCliente);

        // Assert
        var badRequestResult = Assert.IsType<BadRequestObjectResult>(result.Result);
        Assert.NotNull(badRequestResult.Value);
    }

    [Fact]
    public async Task Update_WithValidData_ReturnsOk()
    {
        // Arrange
        var clienteAtualizado = new PetshopApi.DTOs.ClienteRequestDTO
        {
            Nome = "João Silva Santos",
            Cpf = "12345678901",
            Telefone = "11987654322",
            Email = "joao.silva@example.com",
            DataNascimento = new DateTime(1990, 5, 15),
            Sexo = "M",
            Endereco = "Rua A Nova",
            Numero = "123A",
            Complemento = "Apt 46",
            Bairro = "Centro",
            Cidade = "São Paulo"
        };

        // Act
        var result = await _controller.Update(1, clienteAtualizado);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        Assert.NotNull(okResult.Value);
    }

    [Fact]
    public async Task Update_WithInvalidId_ReturnsNotFound()
    {
        // Arrange
        var clienteAtualizado = new PetshopApi.DTOs.ClienteRequestDTO
        {
            Nome = "Cliente Inexistente",
            Cpf = "99999999999",
            Telefone = "11999999999",
            Email = "inexistente@example.com",
            DataNascimento = new DateTime(1990, 1, 1),
            Sexo = "M",
            Endereco = "Rua X",
            Numero = "999",
            Bairro = "Bairro",
            Cidade = "Cidade"
        };

        // Act
        var result = await _controller.Update(999, clienteAtualizado);

        // Assert
        Assert.IsType<NotFoundResult>(result.Result);
    }

    [Fact]
    public async Task Delete_WithValidId_ReturnsNoContent()
    {
        // Act
        var result = await _controller.Delete(1);

        // Assert
        Assert.IsType<NoContentResult>(result);
        var cliente = await _context.Clientes.FindAsync(1L);
        Assert.Null(cliente);
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
