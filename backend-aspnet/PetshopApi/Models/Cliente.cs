using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace PetshopApi.Models;

[Table("clientes")]
public class Cliente
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required(ErrorMessage = "Nome é obrigatório")]
    [StringLength(100, MinimumLength = 3, ErrorMessage = "Nome deve ter entre 3 e 100 caracteres")]
    [Column("nome")]
    public string Nome { get; set; } = string.Empty;

    [Required(ErrorMessage = "CPF é obrigatório")]
    [StringLength(11, MinimumLength = 11, ErrorMessage = "CPF deve ter 11 dígitos")]
    [Column("cpf")]
    public string Cpf { get; set; } = string.Empty;

    [Required(ErrorMessage = "Telefone é obrigatório")]
    [StringLength(11, MinimumLength = 10, ErrorMessage = "Telefone deve ter entre 10 e 11 dígitos")]
    [Column("telefone")]
    public string Telefone { get; set; } = string.Empty;

    [Required(ErrorMessage = "Email é obrigatório")]
    [EmailAddress(ErrorMessage = "Email inválido")]
    [Column("email")]
    public string Email { get; set; } = string.Empty;

    [Required(ErrorMessage = "Data de nascimento é obrigatória")]
    [Column("data_nascimento", TypeName = "DATE")]
    public DateTime DataNascimento { get; set; }

    [Required(ErrorMessage = "Sexo é obrigatório")]
    [StringLength(1)]
    [Column("sexo")]
    public string Sexo { get; set; } = string.Empty; // M, F, O

    [Required(ErrorMessage = "Endereço é obrigatório")]
    [Column("endereco")]
    public string Endereco { get; set; } = string.Empty;

    [Required(ErrorMessage = "Número é obrigatório")]
    [StringLength(10)]
    [Column("numero")]
    public string Numero { get; set; } = string.Empty;

    [StringLength(50)]
    [Column("complemento")]
    public string? Complemento { get; set; }

    [Required(ErrorMessage = "Bairro é obrigatório")]
    [Column("bairro")]
    public string Bairro { get; set; } = string.Empty;

    [Required(ErrorMessage = "Cidade é obrigatória")]
    [Column("cidade")]
    public string Cidade { get; set; } = string.Empty;

    // Relacionamentos
    public ICollection<Pet> Pets { get; set; } = new List<Pet>();
    public ICollection<Agendamento> Agendamentos { get; set; } = new List<Agendamento>();
    public ICollection<Pedido> Pedidos { get; set; } = new List<Pedido>();
}
