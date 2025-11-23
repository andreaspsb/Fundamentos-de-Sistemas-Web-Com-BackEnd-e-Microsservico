using System.ComponentModel.DataAnnotations;

namespace PetshopApi.DTOs;

public class AgendamentoRequestDTO
{
    [Required(ErrorMessage = "Data do agendamento é obrigatória")]
    public DateTime DataAgendamento { get; set; }
    
    [Required(ErrorMessage = "Horário é obrigatório")]
    public TimeSpan Horario { get; set; }
    
    [Required(ErrorMessage = "Método de atendimento é obrigatório")]
    [MaxLength(20)]
    public string MetodoAtendimento { get; set; } = string.Empty; // "telebusca" ou "local"
    
    [MaxLength(20)]
    public string? PortePet { get; set; } // "pequeno", "medio", "grande"
    
    [MaxLength(500)]
    public string? Observacoes { get; set; }
    
    [Required(ErrorMessage = "Valor total é obrigatório")]
    [Range(0.01, double.MaxValue, ErrorMessage = "Valor deve ser maior que zero")]
    public decimal ValorTotal { get; set; }
    
    [Required(ErrorMessage = "Cliente é obrigatório")]
    public long ClienteId { get; set; }
    
    [Required(ErrorMessage = "Pet é obrigatório")]
    public long PetId { get; set; }
    
    [Required(ErrorMessage = "Pelo menos um serviço é obrigatório")]
    [MinLength(1, ErrorMessage = "Pelo menos um serviço é obrigatório")]
    public List<long> ServicosIds { get; set; } = new();
}

public class AgendamentoResponseDTO
{
    public long Id { get; set; }
    public DateTime DataAgendamento { get; set; }
    public TimeSpan Horario { get; set; }
    public string MetodoAtendimento { get; set; } = string.Empty;
    public string? PortePet { get; set; }
    public string? Observacoes { get; set; }
    public decimal ValorTotal { get; set; }
    public string Status { get; set; } = string.Empty;
    
    // Cliente
    public long ClienteId { get; set; }
    public string ClienteNome { get; set; } = string.Empty;
    public string? ClienteTelefone { get; set; }
    
    // Pet
    public long PetId { get; set; }
    public string PetNome { get; set; } = string.Empty;
    public string PetTipo { get; set; } = string.Empty;
    
    // Serviços
    public List<ServicoAgendadoDTO> Servicos { get; set; } = new();
}

public class ServicoAgendadoDTO
{
    public long Id { get; set; }
    public string Nome { get; set; } = string.Empty;
    public decimal Preco { get; set; }
}

public class AgendamentoUpdateDTO
{
    public DateTime? DataAgendamento { get; set; }
    public TimeSpan? Horario { get; set; }
    public string? MetodoAtendimento { get; set; }
    public string? PortePet { get; set; }
    public string? Observacoes { get; set; }
    public decimal? ValorTotal { get; set; }
    public long? PetId { get; set; }
    public List<long>? ServicosIds { get; set; }
}
