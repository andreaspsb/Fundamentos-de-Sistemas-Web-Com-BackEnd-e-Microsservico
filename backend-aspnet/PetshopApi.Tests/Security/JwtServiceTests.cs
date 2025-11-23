using Moq;
using PetshopApi.Security;
using Microsoft.Extensions.Configuration;
using Xunit;

namespace PetshopApi.Tests.Security;

public class JwtServiceTests
{
    private readonly JwtService _jwtService;
    private readonly Mock<IConfiguration> _mockConfiguration;

    public JwtServiceTests()
    {
        _mockConfiguration = new Mock<IConfiguration>();
        _mockConfiguration.Setup(x => x["Jwt:SecretKey"])
            .Returns("petshop-secret-key-must-be-at-least-256-bits-long-for-HS256-algorithm");
        _mockConfiguration.Setup(x => x["Jwt:ExpirationMinutes"]).Returns("1440");
        _mockConfiguration.Setup(x => x["Jwt:Issuer"]).Returns("PetshopApi");
        _mockConfiguration.Setup(x => x["Jwt:Audience"]).Returns("PetshopFrontend");

        _jwtService = new JwtService(_mockConfiguration.Object);
    }

    [Fact]
    public void GenerateToken_Should_ReturnValidToken()
    {
        // Arrange
        var username = "testuser";
        var role = "CLIENTE";

        // Act
        var token = _jwtService.GenerateToken(username, role);

        // Assert
        Assert.NotNull(token);
        Assert.NotEmpty(token);
        Assert.Equal(3, token.Split('.').Length); // JWT tem 3 partes
    }

    [Fact]
    public void GenerateToken_WithClienteId_Should_IncludeClienteId()
    {
        // Arrange
        var username = "testuser";
        var role = "CLIENTE";
        long clienteId = 123;

        // Act
        var token = _jwtService.GenerateToken(username, role, clienteId);
        var principal = _jwtService.ValidateToken(token);

        // Assert
        Assert.NotNull(principal);
        var clienteIdClaim = principal.FindFirst("clienteId")?.Value;
        Assert.Equal("123", clienteIdClaim);
    }

    [Fact]
    public void ValidateToken_WithValidToken_Should_ReturnPrincipal()
    {
        // Arrange
        var username = "testuser";
        var role = "ADMIN";
        var token = _jwtService.GenerateToken(username, role);

        // Act
        var principal = _jwtService.ValidateToken(token);

        // Assert
        Assert.NotNull(principal);
        Assert.Equal(username, principal.Identity?.Name);
    }

    [Fact]
    public void ValidateToken_WithInvalidToken_Should_ReturnNull()
    {
        // Arrange
        var invalidToken = "invalid.token.here";

        // Act
        var principal = _jwtService.ValidateToken(invalidToken);

        // Assert
        Assert.Null(principal);
    }

    [Fact]
    public void GetUsernameFromToken_Should_ReturnUsername()
    {
        // Arrange
        var username = "testuser";
        var role = "CLIENTE";
        var token = _jwtService.GenerateToken(username, role);

        // Act
        var extractedUsername = _jwtService.GetUsernameFromToken(token);

        // Assert
        Assert.Equal(username, extractedUsername);
    }

    [Fact]
    public void GetRoleFromToken_Should_ReturnRole()
    {
        // Arrange
        var username = "admin";
        var role = "ADMIN";
        var token = _jwtService.GenerateToken(username, role);

        // Act
        var extractedRole = _jwtService.GetRoleFromToken(token);

        // Assert
        Assert.Equal(role, extractedRole);
    }

    [Fact]
    public void GetClienteIdFromToken_WithClienteId_Should_ReturnClienteId()
    {
        // Arrange
        var username = "testuser";
        var role = "CLIENTE";
        long clienteId = 456;
        var token = _jwtService.GenerateToken(username, role, clienteId);

        // Act
        var extractedClienteId = _jwtService.GetClienteIdFromToken(token);

        // Assert
        Assert.Equal(clienteId, extractedClienteId);
    }

    [Fact]
    public void GetClienteIdFromToken_WithoutClienteId_Should_ReturnNull()
    {
        // Arrange
        var username = "testuser";
        var role = "ADMIN";
        var token = _jwtService.GenerateToken(username, role);

        // Act
        var extractedClienteId = _jwtService.GetClienteIdFromToken(token);

        // Assert
        Assert.Null(extractedClienteId);
    }

    [Fact]
    public void MultipleTokens_ForDifferentUsers_Should_BeDifferent()
    {
        // Arrange & Act
        var token1 = _jwtService.GenerateToken("user1", "CLIENTE");
        var token2 = _jwtService.GenerateToken("user2", "ADMIN");

        var username1 = _jwtService.GetUsernameFromToken(token1);
        var username2 = _jwtService.GetUsernameFromToken(token2);

        // Assert
        Assert.NotEqual(token1, token2);
        Assert.Equal("user1", username1);
        Assert.Equal("user2", username2);
    }
}
