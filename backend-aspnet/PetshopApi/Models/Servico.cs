using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace PetshopApi.Models;

[Table("servicos")]
public class Servico
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required(ErrorMessage = "Nome do serviço é obrigatório")]
    [Column("nome")]
    public string Nome { get; set; } = string.Empty;

    [StringLength(500)]
    [Column("descricao")]
    public string? Descricao { get; set; }

    [Required(ErrorMessage = "Preço é obrigatório")]
    [Range(0.01, double.MaxValue, ErrorMessage = "Preço deve ser maior que zero")]
    [Column("preco")]
    public double Preco { get; set; }

    [Column("ativo")]
    public bool Ativo { get; set; } = true;

    // Relacionamento Many-to-Many
    public ICollection<Agendamento> Agendamentos { get; set; } = new List<Agendamento>();
}
