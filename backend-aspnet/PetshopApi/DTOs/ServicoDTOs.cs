using System.ComponentModel.DataAnnotations;

namespace PetshopApi.DTOs;

public class ServicoRequestDTO
{
    [Required(ErrorMessage = "Nome do serviço é obrigatório")]
    public string Nome { get; set; } = string.Empty;

    [StringLength(500)]
    public string? Descricao { get; set; }

    [Required(ErrorMessage = "Preço é obrigatório")]
    [Range(0.01, double.MaxValue, ErrorMessage = "Preço deve ser maior que zero")]
    public double Preco { get; set; }

    public bool Ativo { get; set; } = true;
}

public class ServicoResponseDTO
{
    public long Id { get; set; }
    public string Nome { get; set; } = string.Empty;
    public string? Descricao { get; set; }
    public double Preco { get; set; }
    public bool Ativo { get; set; }
}
