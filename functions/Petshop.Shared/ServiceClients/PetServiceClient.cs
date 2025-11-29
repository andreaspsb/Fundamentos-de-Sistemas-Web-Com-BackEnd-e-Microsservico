using System.Net.Http.Json;
using Petshop.Shared.DTOs.Pet;

namespace Petshop.Shared.ServiceClients;

public interface IPetServiceClient
{
    Task<PetResponseDTO?> GetPetByIdAsync(long id);
    Task<bool> PetExistsAsync(long id);
    Task<bool> PetBelongsToClienteAsync(long petId, long clienteId);
}

public class PetServiceClient : IPetServiceClient
{
    private readonly HttpClient _httpClient;

    public PetServiceClient(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<PetResponseDTO?> GetPetByIdAsync(long id)
    {
        try
        {
            var response = await _httpClient.GetAsync($"pets/{id}");
            if (response.IsSuccessStatusCode)
            {
                return await response.Content.ReadFromJsonAsync<PetResponseDTO>();
            }
            return null;
        }
        catch
        {
            return null;
        }
    }

    public async Task<bool> PetExistsAsync(long id)
    {
        var pet = await GetPetByIdAsync(id);
        return pet != null;
    }

    public async Task<bool> PetBelongsToClienteAsync(long petId, long clienteId)
    {
        var pet = await GetPetByIdAsync(petId);
        return pet != null && pet.ClienteId == clienteId;
    }
}
