using System.Net;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Petshop.Shared.Data;
using Petshop.Shared.DTOs.Cliente;
using Petshop.Shared.Models;
using Petshop.Shared.Security;

namespace Petshop.Functions.Customers;

public class CustomerFunctions
{
    private readonly ILogger<CustomerFunctions> _logger;
    private readonly PetshopDbContext _context;
    private readonly JwtService _jwtService;

    public CustomerFunctions(
        ILogger<CustomerFunctions> logger,
        PetshopDbContext context,
        JwtService jwtService)
    {
        _logger = logger;
        _context = context;
        _jwtService = jwtService;
    }

    /// <summary>
    /// GET /api/clientes
    /// Lista todos os clientes. Requer role ADMIN.
    /// </summary>
    [Function("GetAllClientes")]
    public async Task<HttpResponseData> GetAllClientes(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "clientes")] HttpRequestData req)
    {
        _logger.LogInformation("Listando todos os clientes");

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var clientes = await _context.Clientes
                .Select(c => new ClienteResponseDTO
                {
                    Id = c.Id,
                    Nome = c.Nome,
                    Cpf = c.Cpf,
                    Telefone = c.Telefone,
                    Email = c.Email,
                    DataNascimento = c.DataNascimento,
                    Sexo = c.Sexo,
                    Endereco = c.Endereco,
                    Numero = c.Numero,
                    Complemento = c.Complemento,
                    Bairro = c.Bairro,
                    Cidade = c.Cidade
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(clientes);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar clientes");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/clientes/{id}
    /// Busca cliente por ID.
    /// </summary>
    [Function("GetClienteById")]
    public async Task<HttpResponseData> GetClienteById(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "clientes/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Buscando cliente por ID: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        // Clientes podem ver apenas seu próprio perfil, admins podem ver qualquer um
        if (auth.Role != "ADMIN" && auth.ClienteId != id)
        {
            return await FunctionAuthorization.CreateForbiddenResponse(req, "Acesso não autorizado a este cliente");
        }

        try
        {
            var cliente = await _context.Clientes
                .Where(c => c.Id == id)
                .Select(c => new ClienteResponseDTO
                {
                    Id = c.Id,
                    Nome = c.Nome,
                    Cpf = c.Cpf,
                    Telefone = c.Telefone,
                    Email = c.Email,
                    DataNascimento = c.DataNascimento,
                    Sexo = c.Sexo,
                    Endereco = c.Endereco,
                    Numero = c.Numero,
                    Complemento = c.Complemento,
                    Bairro = c.Bairro,
                    Cidade = c.Cidade
                })
                .FirstOrDefaultAsync();

            if (cliente == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Cliente não encontrado" });
                return notFound;
            }

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(cliente);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao buscar cliente");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/clientes/cpf/{cpf}
    /// Busca cliente por CPF.
    /// </summary>
    [Function("GetClienteByCpf")]
    public async Task<HttpResponseData> GetClienteByCpf(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "clientes/cpf/{cpf}")] HttpRequestData req,
        string cpf)
    {
        _logger.LogInformation("Buscando cliente por CPF");

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var cliente = await _context.Clientes
                .Where(c => c.Cpf == cpf)
                .Select(c => new ClienteResponseDTO
                {
                    Id = c.Id,
                    Nome = c.Nome,
                    Cpf = c.Cpf,
                    Telefone = c.Telefone,
                    Email = c.Email,
                    DataNascimento = c.DataNascimento,
                    Sexo = c.Sexo,
                    Endereco = c.Endereco,
                    Numero = c.Numero,
                    Complemento = c.Complemento,
                    Bairro = c.Bairro,
                    Cidade = c.Cidade
                })
                .FirstOrDefaultAsync();

            if (cliente == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Cliente não encontrado" });
                return notFound;
            }

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(cliente);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao buscar cliente por CPF");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// POST /api/clientes
    /// Cria um novo cliente. Endpoint público para cadastro.
    /// </summary>
    [Function("CreateCliente")]
    public async Task<HttpResponseData> CreateCliente(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "clientes")] HttpRequestData req)
    {
        _logger.LogInformation("Criando novo cliente");

        try
        {
            var clienteRequest = await req.ReadFromJsonAsync<ClienteRequestDTO>();
            
            if (clienteRequest == null)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Dados do cliente são obrigatórios" });
                return badRequest;
            }

            // Verifica se CPF ou email já existem
            var existingCliente = await _context.Clientes
                .FirstOrDefaultAsync(c => c.Cpf == clienteRequest.Cpf || c.Email == clienteRequest.Email);
            
            if (existingCliente != null)
            {
                var conflict = req.CreateResponse(HttpStatusCode.Conflict);
                await conflict.WriteAsJsonAsync(new { error = "CPF ou email já cadastrado" });
                return conflict;
            }

            var cliente = new Cliente
            {
                Nome = clienteRequest.Nome,
                Cpf = clienteRequest.Cpf,
                Telefone = clienteRequest.Telefone,
                Email = clienteRequest.Email,
                DataNascimento = clienteRequest.DataNascimento,
                Sexo = clienteRequest.Sexo,
                Endereco = clienteRequest.Endereco,
                Numero = clienteRequest.Numero,
                Complemento = clienteRequest.Complemento,
                Bairro = clienteRequest.Bairro,
                Cidade = clienteRequest.Cidade
            };

            _context.Clientes.Add(cliente);
            await _context.SaveChangesAsync();

            var responseDto = new ClienteResponseDTO
            {
                Id = cliente.Id,
                Nome = cliente.Nome,
                Cpf = cliente.Cpf,
                Telefone = cliente.Telefone,
                Email = cliente.Email,
                DataNascimento = cliente.DataNascimento,
                Sexo = cliente.Sexo,
                Endereco = cliente.Endereco,
                Numero = cliente.Numero,
                Complemento = cliente.Complemento,
                Bairro = cliente.Bairro,
                Cidade = cliente.Cidade
            };

            var response = req.CreateResponse(HttpStatusCode.Created);
            await response.WriteAsJsonAsync(responseDto);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao criar cliente");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PUT /api/clientes/{id}
    /// Atualiza um cliente existente.
    /// </summary>
    [Function("UpdateCliente")]
    public async Task<HttpResponseData> UpdateCliente(
        [HttpTrigger(AuthorizationLevel.Anonymous, "put", Route = "clientes/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Atualizando cliente: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        // Clientes podem atualizar apenas seu próprio perfil
        if (auth.Role != "ADMIN" && auth.ClienteId != id)
        {
            return await FunctionAuthorization.CreateForbiddenResponse(req, "Acesso não autorizado a este cliente");
        }

        try
        {
            var clienteRequest = await req.ReadFromJsonAsync<ClienteRequestDTO>();
            
            if (clienteRequest == null)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Dados do cliente são obrigatórios" });
                return badRequest;
            }

            var cliente = await _context.Clientes.FindAsync(id);
            
            if (cliente == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Cliente não encontrado" });
                return notFound;
            }

            // Verifica se CPF ou email já existem em outro cliente
            var existingCliente = await _context.Clientes
                .FirstOrDefaultAsync(c => c.Id != id && (c.Cpf == clienteRequest.Cpf || c.Email == clienteRequest.Email));
            
            if (existingCliente != null)
            {
                var conflict = req.CreateResponse(HttpStatusCode.Conflict);
                await conflict.WriteAsJsonAsync(new { error = "CPF ou email já cadastrado para outro cliente" });
                return conflict;
            }

            cliente.Nome = clienteRequest.Nome;
            cliente.Cpf = clienteRequest.Cpf;
            cliente.Telefone = clienteRequest.Telefone;
            cliente.Email = clienteRequest.Email;
            cliente.DataNascimento = clienteRequest.DataNascimento;
            cliente.Sexo = clienteRequest.Sexo;
            cliente.Endereco = clienteRequest.Endereco;
            cliente.Numero = clienteRequest.Numero;
            cliente.Complemento = clienteRequest.Complemento;
            cliente.Bairro = clienteRequest.Bairro;
            cliente.Cidade = clienteRequest.Cidade;

            await _context.SaveChangesAsync();

            var responseDto = new ClienteResponseDTO
            {
                Id = cliente.Id,
                Nome = cliente.Nome,
                Cpf = cliente.Cpf,
                Telefone = cliente.Telefone,
                Email = cliente.Email,
                DataNascimento = cliente.DataNascimento,
                Sexo = cliente.Sexo,
                Endereco = cliente.Endereco,
                Numero = cliente.Numero,
                Complemento = cliente.Complemento,
                Bairro = cliente.Bairro,
                Cidade = cliente.Cidade
            };

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(responseDto);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao atualizar cliente");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// DELETE /api/clientes/{id}
    /// Exclui um cliente. Requer role ADMIN.
    /// </summary>
    [Function("DeleteCliente")]
    public async Task<HttpResponseData> DeleteCliente(
        [HttpTrigger(AuthorizationLevel.Anonymous, "delete", Route = "clientes/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Excluindo cliente: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var cliente = await _context.Clientes.FindAsync(id);
            
            if (cliente == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Cliente não encontrado" });
                return notFound;
            }

            _context.Clientes.Remove(cliente);
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.NoContent);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao excluir cliente");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }
}
