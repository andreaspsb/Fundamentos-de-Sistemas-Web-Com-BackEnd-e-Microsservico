using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using PetshopApi.Data;
using PetshopApi.DTOs;
using PetshopApi.Models;
using PetshopApi.Security;
using BC = BCrypt.Net.BCrypt;

namespace PetshopApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly PetshopContext _context;
    private readonly JwtService _jwtService;

    public AuthController(PetshopContext context, JwtService jwtService)
    {
        _context = context;
        _jwtService = jwtService;
    }

    [HttpPost("login")]
    public async Task<ActionResult<LoginResponseDTO>> Login([FromBody] LoginRequestDTO request)
    {
        var usuario = await _context.Usuarios
            .Include(u => u.Cliente)
            .FirstOrDefaultAsync(u => u.Username == request.Username);

        if (usuario == null || !BC.Verify(request.Senha, usuario.Senha))
        {
            return Unauthorized(new { error = "Credenciais inválidas" });
        }

        if (!usuario.Ativo)
        {
            return Unauthorized(new { error = "Usuário inativo" });
        }

        usuario.UltimoAcesso = DateTime.Now;
        await _context.SaveChangesAsync();

        var token = _jwtService.GenerateToken(usuario.Username, usuario.Role, usuario.ClienteId);

        return Ok(new LoginResponseDTO
        {
            Token = token,
            Username = usuario.Username,
            Email = usuario.Email,
            Role = usuario.Role,
            ClienteId = usuario.ClienteId,
            ClienteNome = usuario.Cliente?.Nome
        });
    }

    [HttpPost("registrar")]
    public async Task<ActionResult> Registrar([FromBody] UsuarioRequestDTO request)
    {
        if (await _context.Usuarios.AnyAsync(u => u.Username == request.Username))
        {
            return BadRequest(new { error = "Nome de usuário já existe" });
        }

        if (await _context.Usuarios.AnyAsync(u => u.Email == request.Email))
        {
            return BadRequest(new { error = "Email já cadastrado" });
        }

        var usuario = new Usuario
        {
            Username = request.Username,
            Email = request.Email,
            Senha = BC.HashPassword(request.Senha),
            Role = request.Role ?? "CLIENTE"
        };

        _context.Usuarios.Add(usuario);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(Login), new
        {
            message = "Usuário registrado com sucesso",
            username = usuario.Username,
            email = usuario.Email
        });
    }

    [HttpGet("validar")]
    public ActionResult ValidarToken()
    {
        var token = Request.Headers["Authorization"].FirstOrDefault()?.Split(" ").Last();
        
        if (string.IsNullOrEmpty(token))
        {
            return Ok(new { valido = false });
        }

        var isValid = _jwtService.ValidateToken(token) != null;
        return Ok(new { valido = isValid });
    }
}
