using System.Net.Http.Json;
using Petshop.Shared.DTOs.Produto;
using Petshop.Shared.DTOs.Servico;

namespace Petshop.Shared.ServiceClients;

public interface ICatalogServiceClient
{
    Task<ProdutoResponseDTO?> GetProdutoByIdAsync(long id);
    Task<EstoqueVerificacaoDTO?> VerificarEstoqueAsync(long produtoId, int quantidade);
    Task<ServicoResponseDTO?> GetServicoByIdAsync(long id);
    Task<List<ServicoResponseDTO>> GetServicosByIdsAsync(List<long> ids);
}

public class CatalogServiceClient : ICatalogServiceClient
{
    private readonly HttpClient _httpClient;

    public CatalogServiceClient(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<ProdutoResponseDTO?> GetProdutoByIdAsync(long id)
    {
        try
        {
            var response = await _httpClient.GetAsync($"produtos/{id}");
            if (response.IsSuccessStatusCode)
            {
                return await response.Content.ReadFromJsonAsync<ProdutoResponseDTO>();
            }
            return null;
        }
        catch
        {
            return null;
        }
    }

    public async Task<EstoqueVerificacaoDTO?> VerificarEstoqueAsync(long produtoId, int quantidade)
    {
        try
        {
            var response = await _httpClient.GetAsync($"produtos/{produtoId}/verificar-estoque?quantidade={quantidade}");
            if (response.IsSuccessStatusCode)
            {
                return await response.Content.ReadFromJsonAsync<EstoqueVerificacaoDTO>();
            }
            return null;
        }
        catch
        {
            return null;
        }
    }

    public async Task<ServicoResponseDTO?> GetServicoByIdAsync(long id)
    {
        try
        {
            var response = await _httpClient.GetAsync($"servicos/{id}");
            if (response.IsSuccessStatusCode)
            {
                return await response.Content.ReadFromJsonAsync<ServicoResponseDTO>();
            }
            return null;
        }
        catch
        {
            return null;
        }
    }

    public async Task<List<ServicoResponseDTO>> GetServicosByIdsAsync(List<long> ids)
    {
        var result = new List<ServicoResponseDTO>();
        foreach (var id in ids)
        {
            var servico = await GetServicoByIdAsync(id);
            if (servico != null)
            {
                result.Add(servico);
            }
        }
        return result;
    }
}
