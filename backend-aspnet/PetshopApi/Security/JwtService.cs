using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace PetshopApi.Security
{
    public class JwtService
    {
        private readonly IConfiguration _configuration;

        public JwtService(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        public virtual string GenerateToken(string username, string role, long? clienteId = null)
        {
            var key = Encoding.UTF8.GetBytes(
                _configuration["Jwt:SecretKey"] ?? 
                "petshop-secret-key-must-be-at-least-256-bits-long-for-HS256-algorithm"
            );
            var expirationMinutes = int.Parse(_configuration["Jwt:ExpirationMinutes"] ?? "1440"); // 24 horas

            var tokenHandler = new JwtSecurityTokenHandler();
            var tokenDescriptor = new SecurityTokenDescriptor
            {
                Subject = new ClaimsIdentity(new[]
                {
                    new Claim(ClaimTypes.Name, username),
                    new Claim(ClaimTypes.Role, role),
                    new Claim("clienteId", clienteId?.ToString() ?? "")
                }),
                Expires = DateTime.UtcNow.AddMinutes(expirationMinutes),
                SigningCredentials = new SigningCredentials(
                    new SymmetricSecurityKey(key),
                    SecurityAlgorithms.HmacSha256Signature
                ),
                Issuer = _configuration["Jwt:Issuer"] ?? "PetshopApi",
                Audience = _configuration["Jwt:Audience"] ?? "PetshopFrontend"
            };

            var token = tokenHandler.CreateToken(tokenDescriptor);
            return tokenHandler.WriteToken(token);
        }

        public virtual ClaimsPrincipal? ValidateToken(string token)
        {
            var key = Encoding.UTF8.GetBytes(
                _configuration["Jwt:SecretKey"] ?? 
                "petshop-secret-key-must-be-at-least-256-bits-long-for-HS256-algorithm"
            );

            var tokenHandler = new JwtSecurityTokenHandler();
            
            try
            {
                var validationParameters = new TokenValidationParameters
                {
                    ValidateIssuerSigningKey = true,
                    IssuerSigningKey = new SymmetricSecurityKey(key),
                    ValidateIssuer = true,
                    ValidIssuer = _configuration["Jwt:Issuer"] ?? "PetshopApi",
                    ValidateAudience = true,
                    ValidAudience = _configuration["Jwt:Audience"] ?? "PetshopFrontend",
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
    }
}
