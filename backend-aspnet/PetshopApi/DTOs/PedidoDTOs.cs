using System.ComponentModel.DataAnnotations;

namespace PetshopApi.DTOs;

public class PedidoRequestDTO
{
    [Required(ErrorMessage = "Cliente é obrigatório")]
    public long ClienteId { get; set; }
    
    public string? FormaPagamento { get; set; }
    
    public string? Observacoes { get; set; }
}

public class PedidoResponseDTO
{
    public long Id { get; set; }
    public DateTime DataPedido { get; set; }
    public decimal ValorTotal { get; set; }
    public string Status { get; set; } = string.Empty;
    public string? FormaPagamento { get; set; }
    public string? Observacoes { get; set; }
    public long ClienteId { get; set; }
    public string ClienteNome { get; set; } = string.Empty;
    public string? ClienteTelefone { get; set; }
    public List<ItemPedidoDTO> Itens { get; set; } = new();
}

public class ItemPedidoDTO
{
    public long Id { get; set; }
    public long ProdutoId { get; set; }
    public string ProdutoNome { get; set; } = string.Empty;
    public int Quantidade { get; set; }
    public decimal PrecoUnitario { get; set; }
    public decimal Subtotal { get; set; }
}

public class ItemPedidoRequestDTO
{
    [Required(ErrorMessage = "Produto é obrigatório")]
    public long ProdutoId { get; set; }
    
    [Required(ErrorMessage = "Quantidade é obrigatória")]
    [Range(1, int.MaxValue, ErrorMessage = "Quantidade deve ser maior que zero")]
    public int Quantidade { get; set; }
}
