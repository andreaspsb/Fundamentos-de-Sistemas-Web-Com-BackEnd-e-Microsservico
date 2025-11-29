using Microsoft.Extensions.Configuration;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace Petshop.Shared.Security;

public class JwtService
{
    private readonly string _secretKey;
    private readonly string _issuer;
    private readonly string _audience;
    private readonly int _expirationMinutes;

    public JwtService(IConfiguration configuration)
    {
        _secretKey = configuration["Jwt:SecretKey"] ?? 
            "petshop-secret-key-must-be-at-least-256-bits-long-for-HS256-algorithm";
        _issuer = configuration["Jwt:Issuer"] ?? "PetshopApi";
        _audience = configuration["Jwt:Audience"] ?? "PetshopFrontend";
        _expirationMinutes = int.Parse(configuration["Jwt:ExpirationMinutes"] ?? "1440");
    }

    public JwtService(string secretKey, string issuer, string audience, int expirationMinutes)
    {
        _secretKey = secretKey;
        _issuer = issuer;
        _audience = audience;
        _expirationMinutes = expirationMinutes;
    }

    public virtual string GenerateToken(string username, string role, long? clienteId = null)
    {
        var key = Encoding.UTF8.GetBytes(_secretKey);

        var tokenHandler = new JwtSecurityTokenHandler();
        var tokenDescriptor = new SecurityTokenDescriptor
        {
            Subject = new ClaimsIdentity(new[]
            {
                new Claim(ClaimTypes.Name, username),
                new Claim(ClaimTypes.Role, role),
                new Claim("clienteId", clienteId?.ToString() ?? "")
            }),
            Expires = DateTime.UtcNow.AddMinutes(_expirationMinutes),
            SigningCredentials = new SigningCredentials(
                new SymmetricSecurityKey(key),
                SecurityAlgorithms.HmacSha256Signature
            ),
            Issuer = _issuer,
            Audience = _audience
        };

        var token = tokenHandler.CreateToken(tokenDescriptor);
        return tokenHandler.WriteToken(token);
    }

    public virtual ClaimsPrincipal? ValidateToken(string token)
    {
        var key = Encoding.UTF8.GetBytes(_secretKey);
        var tokenHandler = new JwtSecurityTokenHandler();
        
        try
        {
            var validationParameters = new TokenValidationParameters
            {
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = new SymmetricSecurityKey(key),
                ValidateIssuer = true,
                ValidIssuer = _issuer,
                ValidateAudience = true,
                ValidAudience = _audience,
                ValidateLifetime = true,
                ClockSkew = TimeSpan.Zero
            };

            return tokenHandler.ValidateToken(token, validationParameters, out _);
        }
        catch
        {
            return null;
        }
    }

    public virtual string? GetUsernameFromToken(string token)
    {
        var principal = ValidateToken(token);
        return principal?.Identity?.Name;
    }

    public virtual string? GetRoleFromToken(string token)
    {
        var principal = ValidateToken(token);
        return principal?.FindFirst(ClaimTypes.Role)?.Value;
    }

    public virtual long? GetClienteIdFromToken(string token)
    {
        var principal = ValidateToken(token);
        var clienteIdClaim = principal?.FindFirst("clienteId")?.Value;
        return string.IsNullOrEmpty(clienteIdClaim) ? null : long.Parse(clienteIdClaim);
    }

    public virtual (bool IsValid, string? Username, string? Role, long? ClienteId) ValidateAndExtract(string token)
    {
        var principal = ValidateToken(token);
        if (principal == null)
            return (false, null, null, null);

        var username = principal.Identity?.Name;
        var role = principal.FindFirst(ClaimTypes.Role)?.Value;
        var clienteIdStr = principal.FindFirst("clienteId")?.Value;
        var clienteId = string.IsNullOrEmpty(clienteIdStr) ? null : (long?)long.Parse(clienteIdStr);

        return (true, username, role, clienteId);
    }
}
