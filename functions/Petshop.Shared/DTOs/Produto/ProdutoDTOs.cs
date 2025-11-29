using System.ComponentModel.DataAnnotations;

namespace Petshop.Shared.DTOs.Produto;

public class ProdutoRequestDTO
{
    [Required(ErrorMessage = "Nome do produto é obrigatório")]
    public string Nome { get; set; } = string.Empty;

    public string? Descricao { get; set; }

    [Required(ErrorMessage = "Preço é obrigatório")]
    [Range(0.01, double.MaxValue, ErrorMessage = "Preço deve ser maior que zero")]
    public double Preco { get; set; }

    [Required(ErrorMessage = "Quantidade em estoque é obrigatória")]
    [Range(0, int.MaxValue, ErrorMessage = "Estoque não pode ser negativo")]
    public int QuantidadeEstoque { get; set; }

    public string? UrlImagem { get; set; }

    public bool Ativo { get; set; } = true;

    [Required(ErrorMessage = "Categoria é obrigatória")]
    public long CategoriaId { get; set; }
}

public class ProdutoResponseDTO
{
    public long Id { get; set; }
    public string Nome { get; set; } = string.Empty;
    public string? Descricao { get; set; }
    public double Preco { get; set; }
    public int QuantidadeEstoque { get; set; }
    public string? UrlImagem { get; set; }
    public bool Ativo { get; set; }
    public long CategoriaId { get; set; }
    public string CategoriaNome { get; set; } = string.Empty;
}

public class EstoqueUpdateDTO
{
    [Required(ErrorMessage = "Quantidade é obrigatória")]
    public int Quantidade { get; set; }
}

public class EstoqueVerificacaoDTO
{
    public long ProdutoId { get; set; }
    public string ProdutoNome { get; set; } = string.Empty;
    public int QuantidadeDisponivel { get; set; }
    public bool Disponivel { get; set; }
}
