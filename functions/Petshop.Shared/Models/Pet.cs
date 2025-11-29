using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Petshop.Shared.Models;

[Table("pets")]
public class Pet
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required(ErrorMessage = "Nome do pet é obrigatório")]
    [StringLength(100, MinimumLength = 2, ErrorMessage = "Nome do pet deve ter entre 2 e 100 caracteres")]
    [Column("nome")]
    public string Nome { get; set; } = string.Empty;

    [Required(ErrorMessage = "Tipo de pet é obrigatório")]
    [StringLength(20)]
    [Column("tipo")]
    public string Tipo { get; set; } = string.Empty; // cao, gato, passaro, coelho, outro

    [Required(ErrorMessage = "Raça é obrigatória")]
    [Column("raca")]
    public string Raca { get; set; } = string.Empty;

    [Required(ErrorMessage = "Idade é obrigatória")]
    [Range(0, 30, ErrorMessage = "Idade deve estar entre 0 e 30 anos")]
    [Column("idade")]
    public int Idade { get; set; }

    [Range(0.1, 100.0, ErrorMessage = "Peso deve estar entre 0.1 e 100 kg")]
    [Column("peso")]
    public double? Peso { get; set; }

    [Required(ErrorMessage = "Sexo do pet é obrigatório")]
    [StringLength(1)]
    [Column("sexo")]
    public string Sexo { get; set; } = string.Empty; // M, F

    [Column("castrado")]
    public bool Castrado { get; set; } = false;

    [StringLength(500)]
    [Column("observacoes")]
    public string? Observacoes { get; set; }

    // Necessidades especiais
    [Column("tem_alergia")]
    public bool TemAlergia { get; set; } = false;

    [Column("precisa_medicacao")]
    public bool PrecisaMedicacao { get; set; } = false;

    [Column("comportamento_agressivo")]
    public bool ComportamentoAgressivo { get; set; } = false;

    // Relacionamentos
    [Required]
    [Column("cliente_id")]
    public long ClienteId { get; set; }

    [ForeignKey("ClienteId")]
    public Cliente? Cliente { get; set; }

    public ICollection<Agendamento> Agendamentos { get; set; } = new List<Agendamento>();
}
