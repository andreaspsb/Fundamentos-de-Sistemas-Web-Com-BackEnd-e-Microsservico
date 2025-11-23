using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace PetshopApi.Models;

[Table("categorias")]
public class Categoria
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required(ErrorMessage = "Nome da categoria é obrigatório")]
    [Column("nome")]
    public string Nome { get; set; } = string.Empty;

    [StringLength(500)]
    [Column("descricao")]
    public string? Descricao { get; set; }

    [Column("ativo")]
    public bool Ativo { get; set; } = true;

    // Relacionamento
    public ICollection<Produto> Produtos { get; set; } = new List<Produto>();
}
