using System.ComponentModel.DataAnnotations;

namespace PetshopApi.DTOs;

public class PetRequestDTO
{
    [Required(ErrorMessage = "Nome do pet é obrigatório")]
    [StringLength(100, MinimumLength = 2, ErrorMessage = "Nome do pet deve ter entre 2 e 100 caracteres")]
    public string Nome { get; set; } = string.Empty;

    [Required(ErrorMessage = "Tipo de pet é obrigatório")]
    [StringLength(20)]
    public string Tipo { get; set; } = string.Empty;

    [Required(ErrorMessage = "Raça é obrigatória")]
    public string Raca { get; set; } = string.Empty;

    [Required(ErrorMessage = "Idade é obrigatória")]
    [Range(0, 30, ErrorMessage = "Idade deve estar entre 0 e 30 anos")]
    public int Idade { get; set; }

    [Range(0.1, 100.0, ErrorMessage = "Peso deve estar entre 0.1 e 100 kg")]
    public double? Peso { get; set; }

    [Required(ErrorMessage = "Sexo do pet é obrigatório")]
    [StringLength(1)]
    public string Sexo { get; set; } = string.Empty;

    public bool Castrado { get; set; } = false;

    [StringLength(500)]
    public string? Observacoes { get; set; }

    public bool TemAlergia { get; set; } = false;
    public bool PrecisaMedicacao { get; set; } = false;
    public bool ComportamentoAgressivo { get; set; } = false;

    [Required(ErrorMessage = "Cliente é obrigatório")]
    public long ClienteId { get; set; }
}

public class PetResponseDTO
{
    public long Id { get; set; }
    public string Nome { get; set; } = string.Empty;
    public string Tipo { get; set; } = string.Empty;
    public string Raca { get; set; } = string.Empty;
    public int Idade { get; set; }
    public double? Peso { get; set; }
    public string Sexo { get; set; } = string.Empty;
    public bool Castrado { get; set; }
    public string? Observacoes { get; set; }
    public bool TemAlergia { get; set; }
    public bool PrecisaMedicacao { get; set; }
    public bool ComportamentoAgressivo { get; set; }
    public long ClienteId { get; set; }
    public string ClienteNome { get; set; } = string.Empty;
}
