using System.Net;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Petshop.Shared.Data;
using Petshop.Shared.DTOs.Servico;
using Petshop.Shared.Models;
using Petshop.Shared.Security;

namespace Petshop.Functions.Catalog;

public class ServicoFunctions
{
    private readonly ILogger<ServicoFunctions> _logger;
    private readonly PetshopDbContext _context;
    private readonly JwtService _jwtService;

    public ServicoFunctions(
        ILogger<ServicoFunctions> logger,
        PetshopDbContext context,
        JwtService jwtService)
    {
        _logger = logger;
        _context = context;
        _jwtService = jwtService;
    }

    /// <summary>
    /// GET /api/servicos
    /// Lista todos os serviços. Endpoint público.
    /// </summary>
    [Function("GetAllServicos")]
    public async Task<HttpResponseData> GetAllServicos(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "servicos")] HttpRequestData req)
    {
        _logger.LogInformation("Listando todos os serviços");

        try
        {
            var servicos = await _context.Servicos
                .Select(s => new ServicoResponseDTO
                {
                    Id = s.Id,
                    Nome = s.Nome,
                    Descricao = s.Descricao,
                    Preco = s.Preco,
                    Ativo = s.Ativo
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(servicos);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar serviços");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/servicos/ativos
    /// Lista serviços ativos. Endpoint público.
    /// </summary>
    [Function("GetServicosAtivos")]
    public async Task<HttpResponseData> GetServicosAtivos(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "servicos/ativos")] HttpRequestData req)
    {
        _logger.LogInformation("Listando serviços ativos");

        try
        {
            var servicos = await _context.Servicos
                .Where(s => s.Ativo)
                .Select(s => new ServicoResponseDTO
                {
                    Id = s.Id,
                    Nome = s.Nome,
                    Descricao = s.Descricao,
                    Preco = s.Preco,
                    Ativo = s.Ativo
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(servicos);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar serviços ativos");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/servicos/{id}
    /// Busca serviço por ID. Endpoint público.
    /// </summary>
    [Function("GetServicoById")]
    public async Task<HttpResponseData> GetServicoById(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "servicos/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Buscando serviço por ID: {Id}", id);

        try
        {
            var servico = await _context.Servicos
                .Where(s => s.Id == id)
                .Select(s => new ServicoResponseDTO
                {
                    Id = s.Id,
                    Nome = s.Nome,
                    Descricao = s.Descricao,
                    Preco = s.Preco,
                    Ativo = s.Ativo
                })
                .FirstOrDefaultAsync();

            if (servico == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Serviço não encontrado" });
                return notFound;
            }

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(servico);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao buscar serviço");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// POST /api/servicos
    /// Cria um novo serviço. Requer role ADMIN.
    /// </summary>
    [Function("CreateServico")]
    public async Task<HttpResponseData> CreateServico(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "servicos")] HttpRequestData req)
    {
        _logger.LogInformation("Criando novo serviço");

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var servicoRequest = await req.ReadFromJsonAsync<ServicoRequestDTO>();
            
            if (servicoRequest == null || string.IsNullOrEmpty(servicoRequest.Nome))
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Nome do serviço é obrigatório" });
                return badRequest;
            }

            var servico = new Servico
            {
                Nome = servicoRequest.Nome,
                Descricao = servicoRequest.Descricao,
                Preco = servicoRequest.Preco,
                Ativo = servicoRequest.Ativo
            };

            _context.Servicos.Add(servico);
            await _context.SaveChangesAsync();

            var responseDto = new ServicoResponseDTO
            {
                Id = servico.Id,
                Nome = servico.Nome,
                Descricao = servico.Descricao,
                Preco = servico.Preco,
                Ativo = servico.Ativo
            };

            var response = req.CreateResponse(HttpStatusCode.Created);
            await response.WriteAsJsonAsync(responseDto);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao criar serviço");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PUT /api/servicos/{id}
    /// Atualiza um serviço. Requer role ADMIN.
    /// </summary>
    [Function("UpdateServico")]
    public async Task<HttpResponseData> UpdateServico(
        [HttpTrigger(AuthorizationLevel.Anonymous, "put", Route = "servicos/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Atualizando serviço: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var servicoRequest = await req.ReadFromJsonAsync<ServicoRequestDTO>();
            
            if (servicoRequest == null)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Dados do serviço são obrigatórios" });
                return badRequest;
            }

            var servico = await _context.Servicos.FindAsync(id);
            
            if (servico == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Serviço não encontrado" });
                return notFound;
            }

            servico.Nome = servicoRequest.Nome;
            servico.Descricao = servicoRequest.Descricao;
            servico.Preco = servicoRequest.Preco;
            servico.Ativo = servicoRequest.Ativo;

            await _context.SaveChangesAsync();

            var responseDto = new ServicoResponseDTO
            {
                Id = servico.Id,
                Nome = servico.Nome,
                Descricao = servico.Descricao,
                Preco = servico.Preco,
                Ativo = servico.Ativo
            };

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(responseDto);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao atualizar serviço");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PATCH /api/servicos/{id}/ativar
    /// Ativa um serviço. Requer role ADMIN.
    /// </summary>
    [Function("AtivarServico")]
    public async Task<HttpResponseData> AtivarServico(
        [HttpTrigger(AuthorizationLevel.Anonymous, "patch", Route = "servicos/{id:long}/ativar")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Ativando serviço: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var servico = await _context.Servicos.FindAsync(id);
            
            if (servico == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Serviço não encontrado" });
                return notFound;
            }

            servico.Ativo = true;
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(new { message = "Serviço ativado com sucesso" });
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao ativar serviço");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PATCH /api/servicos/{id}/desativar
    /// Desativa um serviço. Requer role ADMIN.
    /// </summary>
    [Function("DesativarServico")]
    public async Task<HttpResponseData> DesativarServico(
        [HttpTrigger(AuthorizationLevel.Anonymous, "patch", Route = "servicos/{id:long}/desativar")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Desativando serviço: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var servico = await _context.Servicos.FindAsync(id);
            
            if (servico == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Serviço não encontrado" });
                return notFound;
            }

            servico.Ativo = false;
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(new { message = "Serviço desativado com sucesso" });
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao desativar serviço");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// DELETE /api/servicos/{id}
    /// Exclui um serviço. Requer role ADMIN.
    /// </summary>
    [Function("DeleteServico")]
    public async Task<HttpResponseData> DeleteServico(
        [HttpTrigger(AuthorizationLevel.Anonymous, "delete", Route = "servicos/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Excluindo serviço: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var servico = await _context.Servicos
                .Include(s => s.Agendamentos)
                .FirstOrDefaultAsync(s => s.Id == id);
            
            if (servico == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Serviço não encontrado" });
                return notFound;
            }

            if (servico.Agendamentos.Any())
            {
                var conflict = req.CreateResponse(HttpStatusCode.Conflict);
                await conflict.WriteAsJsonAsync(new { error = "Não é possível excluir serviço com agendamentos associados" });
                return conflict;
            }

            _context.Servicos.Remove(servico);
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.NoContent);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao excluir serviço");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }
}
