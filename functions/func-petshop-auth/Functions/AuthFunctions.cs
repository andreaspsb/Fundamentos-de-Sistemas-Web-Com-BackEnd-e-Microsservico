using System.Net;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using BCrypt.Net;
using Petshop.Shared.Data;
using Petshop.Shared.DTOs.Auth;
using Petshop.Shared.Models;
using Petshop.Shared.Security;

namespace Petshop.Functions.Auth;

public class AuthFunctions
{
    private readonly ILogger<AuthFunctions> _logger;
    private readonly PetshopDbContext _context;
    private readonly JwtService _jwtService;

    public AuthFunctions(
        ILogger<AuthFunctions> logger,
        PetshopDbContext context,
        JwtService jwtService)
    {
        _logger = logger;
        _context = context;
        _jwtService = jwtService;
    }

    /// <summary>
    /// POST /api/auth/login
    /// Autentica um usuário e retorna um token JWT.
    /// </summary>
    [Function("Login")]
    public async Task<HttpResponseData> Login(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "auth/login")] HttpRequestData req)
    {
        _logger.LogInformation("Processando requisição de login");

        try
        {
            var loginRequest = await req.ReadFromJsonAsync<LoginRequestDTO>();
            
            if (loginRequest == null || string.IsNullOrEmpty(loginRequest.Username) || string.IsNullOrEmpty(loginRequest.Senha))
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Username e senha são obrigatórios" });
                return badRequest;
            }

            var usuario = await _context.Usuarios
                .Include(u => u.Cliente)
                .FirstOrDefaultAsync(u => u.Username == loginRequest.Username && u.Ativo);

            if (usuario == null || !BCrypt.Net.BCrypt.Verify(loginRequest.Senha, usuario.Senha))
            {
                var unauthorized = req.CreateResponse(HttpStatusCode.Unauthorized);
                await unauthorized.WriteAsJsonAsync(new { error = "Credenciais inválidas" });
                return unauthorized;
            }

            // Atualiza último acesso
            usuario.UltimoAcesso = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            // Gera token
            var token = _jwtService.GenerateToken(usuario.Username, usuario.Role, usuario.ClienteId);

            var responseDto = new LoginResponseDTO
            {
                Token = token,
                Username = usuario.Username,
                Email = usuario.Email,
                Role = usuario.Role,
                ClienteId = usuario.ClienteId,
                ClienteNome = usuario.Cliente?.Nome
            };

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(responseDto);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao processar login");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// POST /api/auth/registrar
    /// Registra um novo usuário no sistema.
    /// </summary>
    [Function("Register")]
    public async Task<HttpResponseData> Register(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "auth/registrar")] HttpRequestData req)
    {
        _logger.LogInformation("Processando requisição de registro");

        try
        {
            var registerRequest = await req.ReadFromJsonAsync<UsuarioRequestDTO>();
            
            if (registerRequest == null || 
                string.IsNullOrEmpty(registerRequest.Username) || 
                string.IsNullOrEmpty(registerRequest.Senha) ||
                string.IsNullOrEmpty(registerRequest.Email))
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Username, senha e email são obrigatórios" });
                return badRequest;
            }

            // Verifica se usuário já existe
            var existingUser = await _context.Usuarios
                .FirstOrDefaultAsync(u => u.Username == registerRequest.Username || u.Email == registerRequest.Email);
            
            if (existingUser != null)
            {
                var conflict = req.CreateResponse(HttpStatusCode.Conflict);
                await conflict.WriteAsJsonAsync(new { error = "Username ou email já cadastrado" });
                return conflict;
            }

            // Cria novo usuário
            var usuario = new Usuario
            {
                Username = registerRequest.Username,
                Senha = BCrypt.Net.BCrypt.HashPassword(registerRequest.Senha),
                Email = registerRequest.Email,
                Role = registerRequest.Role ?? "CLIENTE",
                Ativo = true,
                DataCriacao = DateTime.UtcNow
            };

            _context.Usuarios.Add(usuario);
            await _context.SaveChangesAsync();

            // Gera token para login automático
            var token = _jwtService.GenerateToken(usuario.Username, usuario.Role, usuario.ClienteId);

            var responseDto = new LoginResponseDTO
            {
                Token = token,
                Username = usuario.Username,
                Email = usuario.Email,
                Role = usuario.Role,
                ClienteId = usuario.ClienteId,
                ClienteNome = null
            };

            var response = req.CreateResponse(HttpStatusCode.Created);
            await response.WriteAsJsonAsync(responseDto);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao processar registro");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/auth/validar
    /// Valida um token JWT e retorna informações do usuário.
    /// </summary>
    [Function("ValidateToken")]
    public async Task<HttpResponseData> ValidateToken(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "auth/validar")] HttpRequestData req)
    {
        _logger.LogInformation("Processando validação de token");

        try
        {
            var authHeader = req.Headers.TryGetValues("Authorization", out var values) 
                ? values.FirstOrDefault() 
                : null;

            if (string.IsNullOrEmpty(authHeader) || !authHeader.StartsWith("Bearer "))
            {
                var response = req.CreateResponse(HttpStatusCode.OK);
                await response.WriteAsJsonAsync(new TokenValidationResponseDTO { Valid = false });
                return response;
            }

            var token = authHeader.Substring("Bearer ".Length);
            var (isValid, username, role, clienteId) = _jwtService.ValidateAndExtract(token);

            var validationResponse = new TokenValidationResponseDTO
            {
                Valid = isValid,
                Username = username,
                Role = role,
                ClienteId = clienteId
            };

            var httpResponse = req.CreateResponse(HttpStatusCode.OK);
            await httpResponse.WriteAsJsonAsync(validationResponse);
            return httpResponse;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao validar token");
            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(new TokenValidationResponseDTO { Valid = false });
            return response;
        }
    }
}
