using System.ComponentModel.DataAnnotations;

namespace Petshop.Shared.DTOs.Categoria;

public class CategoriaRequestDTO
{
    [Required(ErrorMessage = "Nome da categoria é obrigatório")]
    public string Nome { get; set; } = string.Empty;

    [StringLength(500)]
    public string? Descricao { get; set; }

    public bool Ativo { get; set; } = true;
}

public class CategoriaResponseDTO
{
    public long Id { get; set; }
    public string Nome { get; set; } = string.Empty;
    public string? Descricao { get; set; }
    public bool Ativo { get; set; }
}
