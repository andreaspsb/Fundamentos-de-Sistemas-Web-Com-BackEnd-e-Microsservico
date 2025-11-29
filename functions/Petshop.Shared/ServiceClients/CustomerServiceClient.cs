using System.Net.Http.Json;
using Petshop.Shared.DTOs.Cliente;

namespace Petshop.Shared.ServiceClients;

public interface ICustomerServiceClient
{
    Task<ClienteResponseDTO?> GetClienteByIdAsync(long id);
    Task<bool> ClienteExistsAsync(long id);
}

public class CustomerServiceClient : ICustomerServiceClient
{
    private readonly HttpClient _httpClient;

    public CustomerServiceClient(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<ClienteResponseDTO?> GetClienteByIdAsync(long id)
    {
        try
        {
            var response = await _httpClient.GetAsync($"clientes/{id}");
            if (response.IsSuccessStatusCode)
            {
                return await response.Content.ReadFromJsonAsync<ClienteResponseDTO>();
            }
            return null;
        }
        catch
        {
            return null;
        }
    }

    public async Task<bool> ClienteExistsAsync(long id)
    {
        var cliente = await GetClienteByIdAsync(id);
        return cliente != null;
    }
}
