namespace PetshopApi.DTOs;

public class LoginRequestDTO
{
    public string Username { get; set; } = string.Empty;
    public string Senha { get; set; } = string.Empty;
}

public class LoginResponseDTO
{
    public string Token { get; set; } = string.Empty;
    public string Username { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string Role { get; set; } = string.Empty;
    public long? ClienteId { get; set; }
    public string? ClienteNome { get; set; }
}

public class UsuarioRequestDTO
{
    public string Username { get; set; } = string.Empty;
    public string Senha { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string? Role { get; set; }
}
