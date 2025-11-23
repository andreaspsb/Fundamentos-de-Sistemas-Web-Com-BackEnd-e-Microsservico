using System.ComponentModel.DataAnnotations;

namespace PetshopApi.DTOs;

public class ClienteRequestDTO
{
    [Required(ErrorMessage = "Nome é obrigatório")]
    [StringLength(100, MinimumLength = 3, ErrorMessage = "Nome deve ter entre 3 e 100 caracteres")]
    public string Nome { get; set; } = string.Empty;

    [Required(ErrorMessage = "CPF é obrigatório")]
    [StringLength(11, MinimumLength = 11, ErrorMessage = "CPF deve ter 11 dígitos")]
    public string Cpf { get; set; } = string.Empty;

    [Required(ErrorMessage = "Telefone é obrigatório")]
    [StringLength(11, MinimumLength = 10, ErrorMessage = "Telefone deve ter entre 10 e 11 dígitos")]
    public string Telefone { get; set; } = string.Empty;

    [Required(ErrorMessage = "Email é obrigatório")]
    [EmailAddress(ErrorMessage = "Email inválido")]
    public string Email { get; set; } = string.Empty;

    [Required(ErrorMessage = "Data de nascimento é obrigatória")]
    public DateTime DataNascimento { get; set; }

    [Required(ErrorMessage = "Sexo é obrigatório")]
    [StringLength(1)]
    public string Sexo { get; set; } = string.Empty;

    [Required(ErrorMessage = "Endereço é obrigatório")]
    public string Endereco { get; set; } = string.Empty;

    [Required(ErrorMessage = "Número é obrigatório")]
    [StringLength(10)]
    public string Numero { get; set; } = string.Empty;

    [StringLength(50)]
    public string? Complemento { get; set; }

    [Required(ErrorMessage = "Bairro é obrigatório")]
    public string Bairro { get; set; } = string.Empty;

    [Required(ErrorMessage = "Cidade é obrigatória")]
    public string Cidade { get; set; } = string.Empty;
}

public class ClienteResponseDTO
{
    public long Id { get; set; }
    public string Nome { get; set; } = string.Empty;
    public string Cpf { get; set; } = string.Empty;
    public string Telefone { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public DateTime DataNascimento { get; set; }
    public string Sexo { get; set; } = string.Empty;
    public string Endereco { get; set; } = string.Empty;
    public string Numero { get; set; } = string.Empty;
    public string? Complemento { get; set; }
    public string Bairro { get; set; } = string.Empty;
    public string Cidade { get; set; } = string.Empty;
}
