using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Controllers;
using PetshopApi.Data;
using PetshopApi.DTOs;
using PetshopApi.Models;
using Xunit;

namespace PetshopApi.Tests.Controllers;

public class AgendamentosControllerTests : IDisposable
{
    private readonly PetshopContext _context;
    private readonly AgendamentosController _controller;
    private readonly Cliente _cliente;
    private readonly Pet _pet;
    private readonly Servico _servico;
    private readonly Agendamento _agendamento;

    public AgendamentosControllerTests()
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
            ClienteId = 1
        };

        _servico = new Servico
        {
            Id = 1,
            Nome = "Banho",
            Descricao = "Banho completo",
            Preco = 50.0,
            Ativo = true
        };

        _agendamento = new Agendamento
        {
            Id = 1,
            DataAgendamento = DateTime.Today.AddDays(1),
            Horario = new TimeSpan(10, 0, 0),
            MetodoAtendimento = "local",
            PortePet = "medio",
            ValorTotal = 50.0,
            Status = StatusAgendamento.PENDENTE,
            ClienteId = 1,
            PetId = 1
        };

        _context.Clientes.Add(_cliente);
        _context.Pets.Add(_pet);
        _context.Servicos.Add(_servico);
        _context.Agendamentos.Add(_agendamento);
        _context.SaveChanges();

        // Add many-to-many relationship
        _agendamento.Servicos.Add(_servico);
        _context.SaveChanges();

        _controller = new AgendamentosController(_context);
    }

    #region GetAgendamentos Tests

    [Fact]
    public async Task GetAgendamentos_ReturnsOkWithListOfAgendamentos()
    {
        // Act
        var result = await _controller.GetAgendamentos();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var agendamentos = Assert.IsAssignableFrom<IEnumerable<AgendamentoResponseDTO>>(okResult.Value);
        Assert.Single(agendamentos);
    }

    [Fact]
    public async Task GetAgendamentos_ReturnsOrderedByDate()
    {
        // Arrange
        var agendamento2 = new Agendamento
        {
            Id = 2,
            DataAgendamento = DateTime.Today.AddDays(2),
            Horario = new TimeSpan(14, 0, 0),
            MetodoAtendimento = "telebusca",
            ValorTotal = 70.0,
            Status = StatusAgendamento.PENDENTE,
            ClienteId = 1,
            PetId = 1
        };
        _context.Agendamentos.Add(agendamento2);
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.GetAgendamentos();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var agendamentos = Assert.IsAssignableFrom<IEnumerable<AgendamentoResponseDTO>>(okResult.Value).ToList();
        Assert.Equal(2, agendamentos.Count);
    }

    #endregion

    #region GetAgendamento Tests

    [Fact]
    public async Task GetAgendamento_WithValidId_ReturnsOkWithAgendamento()
    {
        // Act
        var result = await _controller.GetAgendamento(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var agendamento = Assert.IsType<AgendamentoResponseDTO>(okResult.Value);
        Assert.Equal(1, agendamento.Id);
        Assert.Equal("João Silva", agendamento.ClienteNome);
        Assert.Equal("Rex", agendamento.PetNome);
    }

    [Fact]
    public async Task GetAgendamento_WithInvalidId_ReturnsNotFound()
    {
        // Act
        var result = await _controller.GetAgendamento(999);

        // Assert
        Assert.IsType<NotFoundObjectResult>(result.Result);
    }

    #endregion

    #region GetAgendamentosPorCliente Tests

    [Fact]
    public async Task GetAgendamentosPorCliente_ReturnsClienteAgendamentos()
    {
        // Act
        var result = await _controller.GetAgendamentosPorCliente(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var agendamentos = Assert.IsAssignableFrom<IEnumerable<AgendamentoResponseDTO>>(okResult.Value);
        Assert.Single(agendamentos);
    }

    [Fact]
    public async Task GetAgendamentosPorCliente_WithNoAgendamentos_ReturnsEmptyList()
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
        var result = await _controller.GetAgendamentosPorCliente(2);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var agendamentos = Assert.IsAssignableFrom<IEnumerable<AgendamentoResponseDTO>>(okResult.Value);
        Assert.Empty(agendamentos);
    }

    #endregion

    #region GetAgendamentosPorData Tests

    [Fact]
    public async Task GetAgendamentosPorData_ReturnsAgendamentosForDate()
    {
        // Act
        var result = await _controller.GetAgendamentosPorData(_agendamento.DataAgendamento);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var agendamentos = Assert.IsAssignableFrom<IEnumerable<AgendamentoResponseDTO>>(okResult.Value);
        Assert.Single(agendamentos);
    }

    [Fact]
    public async Task GetAgendamentosPorData_WithNoAgendamentos_ReturnsEmptyList()
    {
        // Act
        var result = await _controller.GetAgendamentosPorData(DateTime.Today.AddYears(1));

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var agendamentos = Assert.IsAssignableFrom<IEnumerable<AgendamentoResponseDTO>>(okResult.Value);
        Assert.Empty(agendamentos);
    }

    #endregion

    #region GetAgendamentosPorStatus Tests

    [Fact]
    public async Task GetAgendamentosPorStatus_WithValidStatus_ReturnsAgendamentos()
    {
        // Act
        var result = await _controller.GetAgendamentosPorStatus("pendente");

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var agendamentos = Assert.IsAssignableFrom<IEnumerable<AgendamentoResponseDTO>>(okResult.Value);
        Assert.Single(agendamentos);
    }

    [Fact]
    public async Task GetAgendamentosPorStatus_WithInvalidStatus_ReturnsBadRequest()
    {
        // Act
        var result = await _controller.GetAgendamentosPorStatus("invalid");

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    #endregion

    #region VerificarDisponibilidade Tests

    [Fact]
    public async Task VerificarDisponibilidade_WhenAvailable_ReturnsTrue()
    {
        // Act - Different time
        var result = await _controller.VerificarDisponibilidade(
            _agendamento.DataAgendamento, 
            new TimeSpan(14, 0, 0));

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        Assert.True((bool)okResult.Value!);
    }

    [Fact]
    public async Task VerificarDisponibilidade_WhenNotAvailable_ReturnsFalse()
    {
        // Act - Same date and time
        var result = await _controller.VerificarDisponibilidade(
            _agendamento.DataAgendamento, 
            _agendamento.Horario);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        Assert.False((bool)okResult.Value!);
    }

    [Fact]
    public async Task VerificarDisponibilidade_WhenCanceled_ReturnsTrue()
    {
        // Arrange
        _agendamento.Status = StatusAgendamento.CANCELADO;
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.VerificarDisponibilidade(
            _agendamento.DataAgendamento, 
            _agendamento.Horario);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        Assert.True((bool)okResult.Value!);
    }

    #endregion

    #region CreateAgendamento Tests

    [Fact]
    public async Task CreateAgendamento_WithValidData_ReturnsCreated()
    {
        // Arrange
        var dto = new AgendamentoRequestDTO
        {
            DataAgendamento = DateTime.Today.AddDays(3),
            Horario = new TimeSpan(15, 0, 0),
            MetodoAtendimento = "telebusca",
            PortePet = "grande",
            ValorTotal = 80.0m,
            ClienteId = 1,
            PetId = 1,
            ServicosIds = new List<long> { 1 }
        };

        // Act
        var result = await _controller.CreateAgendamento(dto);

        // Assert
        var createdResult = Assert.IsType<CreatedAtActionResult>(result.Result);
        var agendamento = Assert.IsType<AgendamentoResponseDTO>(createdResult.Value);
        Assert.Equal("telebusca", agendamento.MetodoAtendimento);
        Assert.Equal("PENDENTE", agendamento.Status);
    }

    [Fact]
    public async Task CreateAgendamento_WithInvalidCliente_ReturnsNotFound()
    {
        // Arrange
        var dto = new AgendamentoRequestDTO
        {
            DataAgendamento = DateTime.Today.AddDays(3),
            Horario = new TimeSpan(15, 0, 0),
            MetodoAtendimento = "local",
            ValorTotal = 50.0m,
            ClienteId = 999,
            PetId = 1,
            ServicosIds = new List<long> { 1 }
        };

        // Act
        var result = await _controller.CreateAgendamento(dto);

        // Assert
        Assert.IsType<NotFoundObjectResult>(result.Result);
    }

    [Fact]
    public async Task CreateAgendamento_WithInvalidPet_ReturnsNotFound()
    {
        // Arrange
        var dto = new AgendamentoRequestDTO
        {
            DataAgendamento = DateTime.Today.AddDays(3),
            Horario = new TimeSpan(15, 0, 0),
            MetodoAtendimento = "local",
            ValorTotal = 50.0m,
            ClienteId = 1,
            PetId = 999,
            ServicosIds = new List<long> { 1 }
        };

        // Act
        var result = await _controller.CreateAgendamento(dto);

        // Assert
        Assert.IsType<NotFoundObjectResult>(result.Result);
    }

    [Fact]
    public async Task CreateAgendamento_WithPetNotBelongingToCliente_ReturnsBadRequest()
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

        var dto = new AgendamentoRequestDTO
        {
            DataAgendamento = DateTime.Today.AddDays(3),
            Horario = new TimeSpan(15, 0, 0),
            MetodoAtendimento = "local",
            ValorTotal = 50.0m,
            ClienteId = 2,
            PetId = 1, // Pet belongs to cliente 1
            ServicosIds = new List<long> { 1 }
        };

        // Act
        var result = await _controller.CreateAgendamento(dto);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task CreateAgendamento_WithInvalidServico_ReturnsBadRequest()
    {
        // Arrange
        var dto = new AgendamentoRequestDTO
        {
            DataAgendamento = DateTime.Today.AddDays(3),
            Horario = new TimeSpan(15, 0, 0),
            MetodoAtendimento = "local",
            ValorTotal = 50.0m,
            ClienteId = 1,
            PetId = 1,
            ServicosIds = new List<long> { 999 }
        };

        // Act
        var result = await _controller.CreateAgendamento(dto);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task CreateAgendamento_WithInactiveServico_ReturnsBadRequest()
    {
        // Arrange
        _servico.Ativo = false;
        await _context.SaveChangesAsync();

        var dto = new AgendamentoRequestDTO
        {
            DataAgendamento = DateTime.Today.AddDays(3),
            Horario = new TimeSpan(15, 0, 0),
            MetodoAtendimento = "local",
            ValorTotal = 50.0m,
            ClienteId = 1,
            PetId = 1,
            ServicosIds = new List<long> { 1 }
        };

        // Act
        var result = await _controller.CreateAgendamento(dto);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task CreateAgendamento_WithConflictingTime_ReturnsBadRequest()
    {
        // Arrange
        var dto = new AgendamentoRequestDTO
        {
            DataAgendamento = _agendamento.DataAgendamento,
            Horario = _agendamento.Horario, // Same time
            MetodoAtendimento = "local",
            ValorTotal = 50.0m,
            ClienteId = 1,
            PetId = 1,
            ServicosIds = new List<long> { 1 }
        };

        // Act
        var result = await _controller.CreateAgendamento(dto);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    #endregion

    #region UpdateAgendamento Tests

    [Fact]
    public async Task UpdateAgendamento_WithValidData_ReturnsOk()
    {
        // Arrange
        var dto = new AgendamentoUpdateDTO
        {
            MetodoAtendimento = "telebusca",
            Observacoes = "Nova observação"
        };

        // Act
        var result = await _controller.UpdateAgendamento(1, dto);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var agendamento = Assert.IsType<AgendamentoResponseDTO>(okResult.Value);
        Assert.Equal("telebusca", agendamento.MetodoAtendimento);
        Assert.Equal("Nova observação", agendamento.Observacoes);
    }

    [Fact]
    public async Task UpdateAgendamento_WithInvalidId_ReturnsNotFound()
    {
        // Arrange
        var dto = new AgendamentoUpdateDTO
        {
            MetodoAtendimento = "telebusca"
        };

        // Act
        var result = await _controller.UpdateAgendamento(999, dto);

        // Assert
        Assert.IsType<NotFoundObjectResult>(result.Result);
    }

    [Fact]
    public async Task UpdateAgendamento_WhenConcluido_ReturnsBadRequest()
    {
        // Arrange
        _agendamento.Status = StatusAgendamento.CONCLUIDO;
        await _context.SaveChangesAsync();

        var dto = new AgendamentoUpdateDTO
        {
            MetodoAtendimento = "telebusca"
        };

        // Act
        var result = await _controller.UpdateAgendamento(1, dto);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task UpdateAgendamento_WhenCancelado_ReturnsBadRequest()
    {
        // Arrange
        _agendamento.Status = StatusAgendamento.CANCELADO;
        await _context.SaveChangesAsync();

        var dto = new AgendamentoUpdateDTO
        {
            MetodoAtendimento = "telebusca"
        };

        // Act
        var result = await _controller.UpdateAgendamento(1, dto);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task UpdateAgendamento_WithConflictingTime_ReturnsBadRequest()
    {
        // Arrange
        var agendamento2 = new Agendamento
        {
            Id = 2,
            DataAgendamento = DateTime.Today.AddDays(5),
            Horario = new TimeSpan(14, 0, 0),
            MetodoAtendimento = "local",
            ValorTotal = 50.0,
            Status = StatusAgendamento.PENDENTE,
            ClienteId = 1,
            PetId = 1
        };
        _context.Agendamentos.Add(agendamento2);
        await _context.SaveChangesAsync();

        var dto = new AgendamentoUpdateDTO
        {
            DataAgendamento = agendamento2.DataAgendamento,
            Horario = agendamento2.Horario // Conflicting time
        };

        // Act
        var result = await _controller.UpdateAgendamento(1, dto);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    #endregion

    #region ConfirmarAgendamento Tests

    [Fact]
    public async Task ConfirmarAgendamento_WhenPendente_ReturnsOk()
    {
        // Act
        var result = await _controller.ConfirmarAgendamento(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var agendamento = Assert.IsType<AgendamentoResponseDTO>(okResult.Value);
        Assert.Equal("CONFIRMADO", agendamento.Status);
    }

    [Fact]
    public async Task ConfirmarAgendamento_WhenNotPendente_ReturnsBadRequest()
    {
        // Arrange
        _agendamento.Status = StatusAgendamento.CONFIRMADO;
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.ConfirmarAgendamento(1);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task ConfirmarAgendamento_WithInvalidId_ReturnsNotFound()
    {
        // Act
        var result = await _controller.ConfirmarAgendamento(999);

        // Assert
        Assert.IsType<NotFoundObjectResult>(result.Result);
    }

    #endregion

    #region ConcluirAgendamento Tests

    [Fact]
    public async Task ConcluirAgendamento_WhenConfirmado_ReturnsOk()
    {
        // Arrange
        _agendamento.Status = StatusAgendamento.CONFIRMADO;
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.ConcluirAgendamento(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var agendamento = Assert.IsType<AgendamentoResponseDTO>(okResult.Value);
        Assert.Equal("CONCLUIDO", agendamento.Status);
    }

    [Fact]
    public async Task ConcluirAgendamento_WhenAlreadyConcluido_ReturnsBadRequest()
    {
        // Arrange
        _agendamento.Status = StatusAgendamento.CONCLUIDO;
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.ConcluirAgendamento(1);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task ConcluirAgendamento_WhenCancelado_ReturnsBadRequest()
    {
        // Arrange
        _agendamento.Status = StatusAgendamento.CANCELADO;
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.ConcluirAgendamento(1);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    #endregion

    #region CancelarAgendamento Tests

    [Fact]
    public async Task CancelarAgendamento_WhenPendente_ReturnsOk()
    {
        // Act
        var result = await _controller.CancelarAgendamento(1);

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        var agendamento = Assert.IsType<AgendamentoResponseDTO>(okResult.Value);
        Assert.Equal("CANCELADO", agendamento.Status);
    }

    [Fact]
    public async Task CancelarAgendamento_WhenAlreadyCancelado_ReturnsBadRequest()
    {
        // Arrange
        _agendamento.Status = StatusAgendamento.CANCELADO;
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.CancelarAgendamento(1);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    [Fact]
    public async Task CancelarAgendamento_WhenConcluido_ReturnsBadRequest()
    {
        // Arrange
        _agendamento.Status = StatusAgendamento.CONCLUIDO;
        await _context.SaveChangesAsync();

        // Act
        var result = await _controller.CancelarAgendamento(1);

        // Assert
        Assert.IsType<BadRequestObjectResult>(result.Result);
    }

    #endregion

    #region DeleteAgendamento Tests

    [Fact]
    public async Task DeleteAgendamento_WithValidId_ReturnsNoContent()
    {
        // Act
        var result = await _controller.DeleteAgendamento(1);

        // Assert
        Assert.IsType<NoContentResult>(result);
        var agendamento = await _context.Agendamentos.FindAsync(1L);
        Assert.Null(agendamento);
    }

    [Fact]
    public async Task DeleteAgendamento_WithInvalidId_ReturnsNotFound()
    {
        // Act
        var result = await _controller.DeleteAgendamento(999);

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
