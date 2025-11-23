using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace PetshopApi.Models;

[Table("itens_pedido")]
public class ItemPedido
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required(ErrorMessage = "Quantidade é obrigatória")]
    [Range(1, int.MaxValue, ErrorMessage = "Quantidade deve ser no mínimo 1")]
    [Column("quantidade")]
    public int Quantidade { get; set; }

    [Required(ErrorMessage = "Preço unitário é obrigatório")]
    [Range(0.01, double.MaxValue, ErrorMessage = "Preço deve ser maior que zero")]
    [Column("preco_unitario")]
    public double PrecoUnitario { get; set; }

    [Required]
    [Column("subtotal")]
    public double Subtotal { get; set; }

    // Relacionamentos
    [Required]
    [Column("pedido_id")]
    public long PedidoId { get; set; }

    [ForeignKey("PedidoId")]
    public Pedido? Pedido { get; set; }

    [Required]
    [Column("produto_id")]
    public long ProdutoId { get; set; }

    [ForeignKey("ProdutoId")]
    public Produto? Produto { get; set; }

    // Método auxiliar
    public void CalcularSubtotal()
    {
        Subtotal = Quantidade * PrecoUnitario;
    }
}
