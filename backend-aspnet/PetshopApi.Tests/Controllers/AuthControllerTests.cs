using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Moq;
using PetshopApi.Controllers;
using PetshopApi.Data;
using PetshopApi.DTOs;
using PetshopApi.Models;
using PetshopApi.Security;
using Xunit;
using BC = BCrypt.Net.BCrypt;

namespace PetshopApi.Tests.Controllers;

public class AuthControllerTests
{
    private readonly Mock<JwtService> _mockJwtService;
    private readonly PetshopContext _context;
    private readonly AuthController _controller;

    public AuthControllerTests()
    {
        // Setup in-memory database
        var options = new DbContextOptionsBuilder<PetshopContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
            .Options;
        _context = new PetshopContext(options);

        // Setup mock JWT service
        _mockJwtService = new Mock<JwtService>(Mock.Of<Microsoft.Extensions.Configuration.IConfiguration>());

        _controller = new AuthController(_context, _mockJwtService.Object);

        // Seed test data
        SeedTestData();
    }

    private void SeedTestData()
    {
        var usuario = new Usuario
        {
            Username = "testuser",
            Email = "test@petshop.com",
            Senha = BC.HashPassword("senha123"),
            Role = "CLIENTE",
            Ativo = true
        };

        _context.Usuarios.Add(usuario);
        _context.SaveChanges();
    }

    [Fact]
    public async Task Login_WithValidCredentials_ShouldReturnToken()
    {
        // Arrange
        var loginRequest = new LoginRequestDTO
        {
            Username = "testuser",
            Senha = "senha123"
        };

        _mockJwtService.Setup(x => x.GenerateToken(It.IsAny<string>(), It.IsAny<string>(), It.IsAny<long?>()))
            .Returns("fake-jwt-token");

        // Act
        var result = await _controller.Login(loginRequest);

        // Assert
        var okResult = Assert.IsType<ActionResult<LoginResponseDTO>>(result);
        var okObjectResult = Assert.IsType<OkObjectResult>(okResult.Result);
        var loginResponse = Assert.IsType<LoginResponseDTO>(okObjectResult.Value);
        
        Assert.Equal("fake-jwt-token", loginResponse.Token);
        Assert.Equal("testuser", loginResponse.Username);
        Assert.Equal("test@petshop.com", loginResponse.Email);
        Assert.Equal("CLIENTE", loginResponse.Role);
    }

    [Fact]
    public async Task Login_WithInvalidUsername_ShouldReturnUnauthorized()
    {
        // Arrange
        var loginRequest = new LoginRequestDTO
        {
            Username = "usernaoinexiste",
            Senha = "senha123"
        };

        // Act
        var result = await _controller.Login(loginRequest);

        // Assert
        var unauthorizedResult = Assert.IsType<UnauthorizedObjectResult>(result.Result);
        Assert.NotNull(unauthorizedResult.Value);
    }

    [Fact]
    public async Task Login_WithInvalidPassword_ShouldReturnUnauthorized()
    {
        // Arrange
        var loginRequest = new LoginRequestDTO
        {
            Username = "testuser",
            Senha = "senhaerrada"
        };

        // Act
        var result = await _controller.Login(loginRequest);

        // Assert
        var unauthorizedResult = Assert.IsType<UnauthorizedObjectResult>(result.Result);
        Assert.NotNull(unauthorizedResult.Value);
    }

    [Fact]
    public async Task Registrar_WithValidData_ShouldReturnCreated()
    {
        // Arrange
        var usuarioRequest = new UsuarioRequestDTO
        {
            Username = "newuser",
            Email = "newuser@petshop.com",
            Senha = "senha123",
            Role = "CLIENTE"
        };

        // Act
        var result = await _controller.Registrar(usuarioRequest);

        // Assert
        var createdResult = Assert.IsType<CreatedAtActionResult>(result);
        Assert.NotNull(createdResult.Value);

        // Verify user was created in database
        var usuario = await _context.Usuarios.FirstOrDefaultAsync(u => u.Username == "newuser");
        Assert.NotNull(usuario);
        Assert.Equal("newuser@petshop.com", usuario.Email);
    }

    [Fact]
    public async Task Registrar_WithExistingUsername_ShouldReturnBadRequest()
    {
        // Arrange
        var usuarioRequest = new UsuarioRequestDTO
        {
            Username = "testuser", // Already exists
            Email = "another@petshop.com",
            Senha = "senha123"
        };

        // Act
        var result = await _controller.Registrar(usuarioRequest);

        // Assert
        var badRequestResult = Assert.IsType<BadRequestObjectResult>(result);
        Assert.NotNull(badRequestResult.Value);
    }

    [Fact]
    public async Task Registrar_WithExistingEmail_ShouldReturnBadRequest()
    {
        // Arrange
        var usuarioRequest = new UsuarioRequestDTO
        {
            Username = "anotheruser",
            Email = "test@petshop.com", // Already exists
            Senha = "senha123"
        };

        // Act
        var result = await _controller.Registrar(usuarioRequest);

        // Assert
        var badRequestResult = Assert.IsType<BadRequestObjectResult>(result);
        Assert.NotNull(badRequestResult.Value);
    }

    [Fact]
    public void ValidarToken_WithValidToken_ShouldReturnTrue()
    {
        // Arrange
        _controller.ControllerContext.HttpContext = new Microsoft.AspNetCore.Http.DefaultHttpContext();
        _controller.Request.Headers["Authorization"] = "Bearer valid-token";

        _mockJwtService.Setup(x => x.ValidateToken("valid-token"))
            .Returns(new System.Security.Claims.ClaimsPrincipal());

        // Act
        var result = _controller.ValidarToken();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result);
        var response = System.Text.Json.JsonSerializer.Deserialize<System.Collections.Generic.Dictionary<string, object>>(System.Text.Json.JsonSerializer.Serialize(okResult.Value));
        Assert.NotNull(response);
        Assert.True(response.ContainsKey("valido"));
        Assert.Equal("True", response["valido"].ToString());
    }

    [Fact]
    public void ValidarToken_WithInvalidToken_ShouldReturnFalse()
    {
        // Arrange
        _controller.ControllerContext.HttpContext = new Microsoft.AspNetCore.Http.DefaultHttpContext();
        _controller.Request.Headers["Authorization"] = "Bearer invalid-token";

        _mockJwtService.Setup(x => x.ValidateToken("invalid-token"))
            .Returns((System.Security.Claims.ClaimsPrincipal?)null);

        // Act
        var result = _controller.ValidarToken();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result);
        var response = System.Text.Json.JsonSerializer.Deserialize<System.Collections.Generic.Dictionary<string, object>>(System.Text.Json.JsonSerializer.Serialize(okResult.Value));
        Assert.NotNull(response);
        Assert.True(response.ContainsKey("valido"));
        Assert.Equal("False", response["valido"].ToString());
    }

    [Fact]
    public void ValidarToken_WithoutAuthHeader_ShouldReturnFalse()
    {
        // Arrange
        _controller.ControllerContext.HttpContext = new Microsoft.AspNetCore.Http.DefaultHttpContext();

        // Act
        var result = _controller.ValidarToken();

        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result);
        var response = System.Text.Json.JsonSerializer.Deserialize<System.Collections.Generic.Dictionary<string, object>>(System.Text.Json.JsonSerializer.Serialize(okResult.Value));
        Assert.NotNull(response);
        Assert.True(response.ContainsKey("valido"));
        Assert.Equal("False", response["valido"].ToString());
    }
}
