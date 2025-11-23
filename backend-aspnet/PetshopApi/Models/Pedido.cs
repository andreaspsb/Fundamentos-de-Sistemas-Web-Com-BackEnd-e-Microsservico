using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace PetshopApi.Models;

public enum StatusPedido
{
    Pendente,
    Confirmado,
    Processando,
    Enviado,
    Entregue,
    Cancelado
}

[Table("pedidos")]
public class Pedido
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required(ErrorMessage = "Data do pedido é obrigatória")]
    [Column("data_pedido")]
    public DateTime DataPedido { get; set; } = DateTime.Now;

    [Required(ErrorMessage = "Valor total é obrigatório")]
    [Range(0.01, double.MaxValue, ErrorMessage = "Valor deve ser maior que zero")]
    [Column("valor_total")]
    public double ValorTotal { get; set; }

    [Column("status")]
    public StatusPedido Status { get; set; } = StatusPedido.Pendente;

    [StringLength(50)]
    [Column("forma_pagamento")]
    public string? FormaPagamento { get; set; } // credito, debito, pix, boleto

    [StringLength(1000)]
    [Column("observacoes")]
    public string? Observacoes { get; set; }

    // Relacionamentos
    [Required]
    [Column("cliente_id")]
    public long ClienteId { get; set; }

    [ForeignKey("ClienteId")]
    public Cliente? Cliente { get; set; }

    public ICollection<ItemPedido> Itens { get; set; } = new List<ItemPedido>();

    // Métodos auxiliares
    public void CalcularValorTotal()
    {
        ValorTotal = Itens.Sum(item => item.Subtotal);
    }

    public void AdicionarItem(ItemPedido item)
    {
        Itens.Add(item);
        item.Pedido = this;
        CalcularValorTotal();
    }

    public void RemoverItem(ItemPedido item)
    {
        Itens.Remove(item);
        item.Pedido = null;
        CalcularValorTotal();
    }
}
