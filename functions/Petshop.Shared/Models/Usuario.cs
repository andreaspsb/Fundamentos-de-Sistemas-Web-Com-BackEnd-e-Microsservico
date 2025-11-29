using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Petshop.Shared.Models;

[Table("usuarios")]
public class Usuario
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required(ErrorMessage = "Nome de usuário é obrigatório")]
    [StringLength(50, MinimumLength = 3, ErrorMessage = "Nome de usuário deve ter entre 3 e 50 caracteres")]
    [Column("username")]
    public string Username { get; set; } = string.Empty;

    [Required(ErrorMessage = "Senha é obrigatória")]
    [Column("senha")]
    public string Senha { get; set; } = string.Empty;

    [Required(ErrorMessage = "Email é obrigatório")]
    [EmailAddress(ErrorMessage = "Email inválido")]
    [Column("email")]
    public string Email { get; set; } = string.Empty;

    [Required(ErrorMessage = "Role é obrigatória")]
    [StringLength(20)]
    [Column("role")]
    public string Role { get; set; } = string.Empty; // ADMIN, CLIENTE

    [Column("ativo")]
    public bool Ativo { get; set; } = true;

    [Column("data_criacao")]
    public DateTime DataCriacao { get; set; } = DateTime.Now;

    [Column("ultimo_acesso")]
    public DateTime? UltimoAcesso { get; set; }

    // Relacionamento
    [Column("cliente_id")]
    public long? ClienteId { get; set; }

    [ForeignKey("ClienteId")]
    public Cliente? Cliente { get; set; }
}
