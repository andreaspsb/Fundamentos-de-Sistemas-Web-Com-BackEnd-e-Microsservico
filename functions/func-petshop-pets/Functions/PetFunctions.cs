using System.Net;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Petshop.Shared.Data;
using Petshop.Shared.DTOs.Pet;
using Petshop.Shared.Models;
using Petshop.Shared.Security;
using Petshop.Shared.ServiceClients;

namespace Petshop.Functions.Pets;

public class PetFunctions
{
    private readonly ILogger<PetFunctions> _logger;
    private readonly PetshopDbContext _context;
    private readonly JwtService _jwtService;
    private readonly ICustomerServiceClient _customerClient;

    public PetFunctions(
        ILogger<PetFunctions> logger,
        PetshopDbContext context,
        JwtService jwtService,
        ICustomerServiceClient customerClient)
    {
        _logger = logger;
        _context = context;
        _jwtService = jwtService;
        _customerClient = customerClient;
    }

    /// <summary>
    /// GET /api/pets
    /// Lista todos os pets. Requer role ADMIN.
    /// </summary>
    [Function("GetAllPets")]
    public async Task<HttpResponseData> GetAllPets(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "pets")] HttpRequestData req)
    {
        _logger.LogInformation("Listando todos os pets");

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pets = await _context.Pets
                .Include(p => p.Cliente)
                .Select(p => new PetResponseDTO
                {
                    Id = p.Id,
                    Nome = p.Nome,
                    Tipo = p.Tipo,
                    Raca = p.Raca,
                    Idade = p.Idade,
                    Peso = p.Peso,
                    Sexo = p.Sexo,
                    Castrado = p.Castrado,
                    Observacoes = p.Observacoes,
                    TemAlergia = p.TemAlergia,
                    PrecisaMedicacao = p.PrecisaMedicacao,
                    ComportamentoAgressivo = p.ComportamentoAgressivo,
                    ClienteId = p.ClienteId,
                    ClienteNome = p.Cliente != null ? p.Cliente.Nome : ""
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(pets);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar pets");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/pets/{id}
    /// Busca pet por ID.
    /// </summary>
    [Function("GetPetById")]
    public async Task<HttpResponseData> GetPetById(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "pets/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Buscando pet por ID: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pet = await _context.Pets
                .Include(p => p.Cliente)
                .Where(p => p.Id == id)
                .Select(p => new PetResponseDTO
                {
                    Id = p.Id,
                    Nome = p.Nome,
                    Tipo = p.Tipo,
                    Raca = p.Raca,
                    Idade = p.Idade,
                    Peso = p.Peso,
                    Sexo = p.Sexo,
                    Castrado = p.Castrado,
                    Observacoes = p.Observacoes,
                    TemAlergia = p.TemAlergia,
                    PrecisaMedicacao = p.PrecisaMedicacao,
                    ComportamentoAgressivo = p.ComportamentoAgressivo,
                    ClienteId = p.ClienteId,
                    ClienteNome = p.Cliente != null ? p.Cliente.Nome : ""
                })
                .FirstOrDefaultAsync();

            if (pet == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Pet não encontrado" });
                return notFound;
            }

            // Clientes podem ver apenas seus próprios pets
            if (auth.Role != "ADMIN" && auth.ClienteId != pet.ClienteId)
            {
                return await FunctionAuthorization.CreateForbiddenResponse(req, "Acesso não autorizado a este pet");
            }

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(pet);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao buscar pet");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/pets/cliente/{clienteId}
    /// Busca pets por cliente.
    /// </summary>
    [Function("GetPetsByCliente")]
    public async Task<HttpResponseData> GetPetsByCliente(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "pets/cliente/{clienteId:long}")] HttpRequestData req,
        long clienteId)
    {
        _logger.LogInformation("Buscando pets do cliente: {ClienteId}", clienteId);

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        // Clientes podem ver apenas seus próprios pets
        if (auth.Role != "ADMIN" && auth.ClienteId != clienteId)
        {
            return await FunctionAuthorization.CreateForbiddenResponse(req, "Acesso não autorizado");
        }

        try
        {
            var pets = await _context.Pets
                .Include(p => p.Cliente)
                .Where(p => p.ClienteId == clienteId)
                .Select(p => new PetResponseDTO
                {
                    Id = p.Id,
                    Nome = p.Nome,
                    Tipo = p.Tipo,
                    Raca = p.Raca,
                    Idade = p.Idade,
                    Peso = p.Peso,
                    Sexo = p.Sexo,
                    Castrado = p.Castrado,
                    Observacoes = p.Observacoes,
                    TemAlergia = p.TemAlergia,
                    PrecisaMedicacao = p.PrecisaMedicacao,
                    ComportamentoAgressivo = p.ComportamentoAgressivo,
                    ClienteId = p.ClienteId,
                    ClienteNome = p.Cliente != null ? p.Cliente.Nome : ""
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(pets);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao buscar pets do cliente");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/pets/tipo/{tipo}
    /// Busca pets por tipo.
    /// </summary>
    [Function("GetPetsByTipo")]
    public async Task<HttpResponseData> GetPetsByTipo(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "pets/tipo/{tipo}")] HttpRequestData req,
        string tipo)
    {
        _logger.LogInformation("Buscando pets por tipo: {Tipo}", tipo);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pets = await _context.Pets
                .Include(p => p.Cliente)
                .Where(p => p.Tipo.ToLower() == tipo.ToLower())
                .Select(p => new PetResponseDTO
                {
                    Id = p.Id,
                    Nome = p.Nome,
                    Tipo = p.Tipo,
                    Raca = p.Raca,
                    Idade = p.Idade,
                    Peso = p.Peso,
                    Sexo = p.Sexo,
                    Castrado = p.Castrado,
                    Observacoes = p.Observacoes,
                    TemAlergia = p.TemAlergia,
                    PrecisaMedicacao = p.PrecisaMedicacao,
                    ComportamentoAgressivo = p.ComportamentoAgressivo,
                    ClienteId = p.ClienteId,
                    ClienteNome = p.Cliente != null ? p.Cliente.Nome : ""
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(pets);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao buscar pets por tipo");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// POST /api/pets
    /// Cria um novo pet.
    /// </summary>
    [Function("CreatePet")]
    public async Task<HttpResponseData> CreatePet(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "pets")] HttpRequestData req)
    {
        _logger.LogInformation("Criando novo pet");

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var petRequest = await req.ReadFromJsonAsync<PetRequestDTO>();
            
            if (petRequest == null)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Dados do pet são obrigatórios" });
                return badRequest;
            }

            // Clientes só podem criar pets para si mesmos
            if (auth.Role != "ADMIN" && auth.ClienteId != petRequest.ClienteId)
            {
                return await FunctionAuthorization.CreateForbiddenResponse(req, "Não é possível criar pet para outro cliente");
            }

            // Verifica se cliente existe (via serviço HTTP com resiliência)
            var clienteExists = await _customerClient.ClienteExistsAsync(petRequest.ClienteId);
            if (!clienteExists)
            {
                // Fallback: verifica no banco local
                clienteExists = await _context.Clientes.AnyAsync(c => c.Id == petRequest.ClienteId);
            }

            if (!clienteExists)
            {
                var notFound = req.CreateResponse(HttpStatusCode.BadRequest);
                await notFound.WriteAsJsonAsync(new { error = "Cliente não encontrado" });
                return notFound;
            }

            var pet = new Pet
            {
                Nome = petRequest.Nome,
                Tipo = petRequest.Tipo,
                Raca = petRequest.Raca,
                Idade = petRequest.Idade,
                Peso = petRequest.Peso,
                Sexo = petRequest.Sexo,
                Castrado = petRequest.Castrado,
                Observacoes = petRequest.Observacoes,
                TemAlergia = petRequest.TemAlergia,
                PrecisaMedicacao = petRequest.PrecisaMedicacao,
                ComportamentoAgressivo = petRequest.ComportamentoAgressivo,
                ClienteId = petRequest.ClienteId
            };

            _context.Pets.Add(pet);
            await _context.SaveChangesAsync();

            // Busca o cliente para retornar o nome
            var cliente = await _context.Clientes.FindAsync(petRequest.ClienteId);

            var responseDto = new PetResponseDTO
            {
                Id = pet.Id,
                Nome = pet.Nome,
                Tipo = pet.Tipo,
                Raca = pet.Raca,
                Idade = pet.Idade,
                Peso = pet.Peso,
                Sexo = pet.Sexo,
                Castrado = pet.Castrado,
                Observacoes = pet.Observacoes,
                TemAlergia = pet.TemAlergia,
                PrecisaMedicacao = pet.PrecisaMedicacao,
                ComportamentoAgressivo = pet.ComportamentoAgressivo,
                ClienteId = pet.ClienteId,
                ClienteNome = cliente?.Nome ?? ""
            };

            var response = req.CreateResponse(HttpStatusCode.Created);
            await response.WriteAsJsonAsync(responseDto);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao criar pet");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PUT /api/pets/{id}
    /// Atualiza um pet existente.
    /// </summary>
    [Function("UpdatePet")]
    public async Task<HttpResponseData> UpdatePet(
        [HttpTrigger(AuthorizationLevel.Anonymous, "put", Route = "pets/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Atualizando pet: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var petRequest = await req.ReadFromJsonAsync<PetRequestDTO>();
            
            if (petRequest == null)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Dados do pet são obrigatórios" });
                return badRequest;
            }

            var pet = await _context.Pets.Include(p => p.Cliente).FirstOrDefaultAsync(p => p.Id == id);
            
            if (pet == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Pet não encontrado" });
                return notFound;
            }

            // Clientes só podem atualizar seus próprios pets
            if (auth.Role != "ADMIN" && auth.ClienteId != pet.ClienteId)
            {
                return await FunctionAuthorization.CreateForbiddenResponse(req, "Acesso não autorizado a este pet");
            }

            pet.Nome = petRequest.Nome;
            pet.Tipo = petRequest.Tipo;
            pet.Raca = petRequest.Raca;
            pet.Idade = petRequest.Idade;
            pet.Peso = petRequest.Peso;
            pet.Sexo = petRequest.Sexo;
            pet.Castrado = petRequest.Castrado;
            pet.Observacoes = petRequest.Observacoes;
            pet.TemAlergia = petRequest.TemAlergia;
            pet.PrecisaMedicacao = petRequest.PrecisaMedicacao;
            pet.ComportamentoAgressivo = petRequest.ComportamentoAgressivo;

            await _context.SaveChangesAsync();

            var responseDto = new PetResponseDTO
            {
                Id = pet.Id,
                Nome = pet.Nome,
                Tipo = pet.Tipo,
                Raca = pet.Raca,
                Idade = pet.Idade,
                Peso = pet.Peso,
                Sexo = pet.Sexo,
                Castrado = pet.Castrado,
                Observacoes = pet.Observacoes,
                TemAlergia = pet.TemAlergia,
                PrecisaMedicacao = pet.PrecisaMedicacao,
                ComportamentoAgressivo = pet.ComportamentoAgressivo,
                ClienteId = pet.ClienteId,
                ClienteNome = pet.Cliente?.Nome ?? ""
            };

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(responseDto);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao atualizar pet");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// DELETE /api/pets/{id}
    /// Exclui um pet.
    /// </summary>
    [Function("DeletePet")]
    public async Task<HttpResponseData> DeletePet(
        [HttpTrigger(AuthorizationLevel.Anonymous, "delete", Route = "pets/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Excluindo pet: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pet = await _context.Pets.FindAsync(id);
            
            if (pet == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Pet não encontrado" });
                return notFound;
            }

            // Clientes só podem excluir seus próprios pets
            if (auth.Role != "ADMIN" && auth.ClienteId != pet.ClienteId)
            {
                return await FunctionAuthorization.CreateForbiddenResponse(req, "Acesso não autorizado a este pet");
            }

            _context.Pets.Remove(pet);
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.NoContent);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao excluir pet");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }
}
