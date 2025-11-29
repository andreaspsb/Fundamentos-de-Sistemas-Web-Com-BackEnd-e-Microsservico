using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Petshop.Shared.Models;

[Table("produtos")]
public class Produto
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required(ErrorMessage = "Nome do produto é obrigatório")]
    [Column("nome")]
    public string Nome { get; set; } = string.Empty;

    [StringLength(1000)]
    [Column("descricao")]
    public string? Descricao { get; set; }

    [Required(ErrorMessage = "Preço é obrigatório")]
    [Range(0.01, double.MaxValue, ErrorMessage = "Preço deve ser maior que zero")]
    [Column("preco")]
    public double Preco { get; set; }

    [Required(ErrorMessage = "Quantidade em estoque é obrigatória")]
    [Range(0, int.MaxValue, ErrorMessage = "Estoque não pode ser negativo")]
    [Column("quantidade_estoque")]
    public int QuantidadeEstoque { get; set; }

    [Column("url_imagem")]
    public string? UrlImagem { get; set; }

    [Column("ativo")]
    public bool Ativo { get; set; } = true;

    // Relacionamentos
    [Required]
    [Column("categoria_id")]
    public long CategoriaId { get; set; }

    [ForeignKey("CategoriaId")]
    public Categoria? Categoria { get; set; }

    public ICollection<ItemPedido> ItensPedido { get; set; } = new List<ItemPedido>();

    // Métodos auxiliares
    public bool TemEstoque(int quantidade) => QuantidadeEstoque >= quantidade;

    public void ReduzirEstoque(int quantidade)
    {
        if (!TemEstoque(quantidade))
            throw new InvalidOperationException("Estoque insuficiente");
        QuantidadeEstoque -= quantidade;
    }

    public void AdicionarEstoque(int quantidade) => QuantidadeEstoque += quantidade;
}
