using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Petshop.Shared.Enums;

namespace Petshop.Shared.Models;

[Table("agendamentos")]
public class Agendamento
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required(ErrorMessage = "Data do agendamento é obrigatória")]
    [Column("data_agendamento", TypeName = "DATE")]
    public DateTime DataAgendamento { get; set; }

    [Required(ErrorMessage = "Horário é obrigatório")]
    [Column("horario")]
    public TimeSpan Horario { get; set; }

    [Required(ErrorMessage = "Método de atendimento é obrigatório")]
    [StringLength(20)]
    [Column("metodo_atendimento")]
    public string MetodoAtendimento { get; set; } = string.Empty; // telebusca, local

    [StringLength(20)]
    [Column("porte_pet")]
    public string? PortePet { get; set; } // pequeno, medio, grande

    [StringLength(500)]
    [Column("observacoes")]
    public string? Observacoes { get; set; }

    [Required(ErrorMessage = "Valor total é obrigatório")]
    [Range(0.01, double.MaxValue, ErrorMessage = "Valor deve ser maior que zero")]
    [Column("valor_total")]
    public double ValorTotal { get; set; }

    [Column("status")]
    public StatusAgendamento Status { get; set; } = StatusAgendamento.Pendente;

    // Relacionamentos
    [Required]
    [Column("cliente_id")]
    public long ClienteId { get; set; }

    [ForeignKey("ClienteId")]
    public Cliente? Cliente { get; set; }

    [Required]
    [Column("pet_id")]
    public long PetId { get; set; }

    [ForeignKey("PetId")]
    public Pet? Pet { get; set; }

    // Relacionamento Many-to-Many com Servicos
    public ICollection<Servico> Servicos { get; set; } = new List<Servico>();
}
