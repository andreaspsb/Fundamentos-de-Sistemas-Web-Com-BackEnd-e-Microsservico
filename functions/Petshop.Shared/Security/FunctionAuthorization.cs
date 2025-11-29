using Microsoft.Azure.Functions.Worker.Http;
using System.Net;

namespace Petshop.Shared.Security;

public class AuthorizationResult
{
    public bool IsAuthorized { get; set; }
    public string? Username { get; set; }
    public string? Role { get; set; }
    public long? ClienteId { get; set; }
    public string? ErrorMessage { get; set; }
}

public static class FunctionAuthorization
{
    public static AuthorizationResult Authorize(HttpRequestData req, JwtService jwtService, params string[] allowedRoles)
    {
        var authHeader = req.Headers.TryGetValues("Authorization", out var values) 
            ? values.FirstOrDefault() 
            : null;

        if (string.IsNullOrEmpty(authHeader) || !authHeader.StartsWith("Bearer "))
        {
            return new AuthorizationResult
            {
                IsAuthorized = false,
                ErrorMessage = "Token não fornecido"
            };
        }

        var token = authHeader.Substring("Bearer ".Length);
        var (isValid, username, role, clienteId) = jwtService.ValidateAndExtract(token);

        if (!isValid)
        {
            return new AuthorizationResult
            {
                IsAuthorized = false,
                ErrorMessage = "Token inválido ou expirado"
            };
        }

        if (allowedRoles.Length > 0 && !allowedRoles.Contains(role, StringComparer.OrdinalIgnoreCase))
        {
            return new AuthorizationResult
            {
                IsAuthorized = false,
                ErrorMessage = "Acesso não autorizado para este perfil"
            };
        }

        return new AuthorizationResult
        {
            IsAuthorized = true,
            Username = username,
            Role = role,
            ClienteId = clienteId
        };
    }

    public static async Task<HttpResponseData> CreateUnauthorizedResponse(HttpRequestData req, string message)
    {
        var response = req.CreateResponse(HttpStatusCode.Unauthorized);
        await response.WriteAsJsonAsync(new { error = message });
        return response;
    }

    public static async Task<HttpResponseData> CreateForbiddenResponse(HttpRequestData req, string message)
    {
        var response = req.CreateResponse(HttpStatusCode.Forbidden);
        await response.WriteAsJsonAsync(new { error = message });
        return response;
    }
}
