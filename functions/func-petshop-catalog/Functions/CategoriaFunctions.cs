using System.Net;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Petshop.Shared.Data;
using Petshop.Shared.DTOs.Categoria;
using Petshop.Shared.Models;
using Petshop.Shared.Security;

namespace Petshop.Functions.Catalog;

public class CategoriaFunctions
{
    private readonly ILogger<CategoriaFunctions> _logger;
    private readonly PetshopDbContext _context;
    private readonly JwtService _jwtService;

    public CategoriaFunctions(
        ILogger<CategoriaFunctions> logger,
        PetshopDbContext context,
        JwtService jwtService)
    {
        _logger = logger;
        _context = context;
        _jwtService = jwtService;
    }

    /// <summary>
    /// GET /api/categorias
    /// Lista todas as categorias. Endpoint público.
    /// </summary>
    [Function("GetAllCategorias")]
    public async Task<HttpResponseData> GetAllCategorias(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "categorias")] HttpRequestData req)
    {
        _logger.LogInformation("Listando todas as categorias");

        try
        {
            var categorias = await _context.Categorias
                .Select(c => new CategoriaResponseDTO
                {
                    Id = c.Id,
                    Nome = c.Nome,
                    Descricao = c.Descricao,
                    Ativo = c.Ativo
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(categorias);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar categorias");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/categorias/{id}
    /// Busca categoria por ID. Endpoint público.
    /// </summary>
    [Function("GetCategoriaById")]
    public async Task<HttpResponseData> GetCategoriaById(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "categorias/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Buscando categoria por ID: {Id}", id);

        try
        {
            var categoria = await _context.Categorias
                .Where(c => c.Id == id)
                .Select(c => new CategoriaResponseDTO
                {
                    Id = c.Id,
                    Nome = c.Nome,
                    Descricao = c.Descricao,
                    Ativo = c.Ativo
                })
                .FirstOrDefaultAsync();

            if (categoria == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Categoria não encontrada" });
                return notFound;
            }

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(categoria);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao buscar categoria");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/categorias/ativas
    /// Lista categorias ativas. Endpoint público.
    /// </summary>
    [Function("GetCategoriasAtivas")]
    public async Task<HttpResponseData> GetCategoriasAtivas(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "categorias/ativas")] HttpRequestData req)
    {
        _logger.LogInformation("Listando categorias ativas");

        try
        {
            var categorias = await _context.Categorias
                .Where(c => c.Ativo)
                .Select(c => new CategoriaResponseDTO
                {
                    Id = c.Id,
                    Nome = c.Nome,
                    Descricao = c.Descricao,
                    Ativo = c.Ativo
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(categorias);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar categorias ativas");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// POST /api/categorias
    /// Cria uma nova categoria. Requer role ADMIN.
    /// </summary>
    [Function("CreateCategoria")]
    public async Task<HttpResponseData> CreateCategoria(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "categorias")] HttpRequestData req)
    {
        _logger.LogInformation("Criando nova categoria");

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var categoriaRequest = await req.ReadFromJsonAsync<CategoriaRequestDTO>();
            
            if (categoriaRequest == null || string.IsNullOrEmpty(categoriaRequest.Nome))
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Nome da categoria é obrigatório" });
                return badRequest;
            }

            var categoria = new Categoria
            {
                Nome = categoriaRequest.Nome,
                Descricao = categoriaRequest.Descricao,
                Ativo = categoriaRequest.Ativo
            };

            _context.Categorias.Add(categoria);
            await _context.SaveChangesAsync();

            var responseDto = new CategoriaResponseDTO
            {
                Id = categoria.Id,
                Nome = categoria.Nome,
                Descricao = categoria.Descricao,
                Ativo = categoria.Ativo
            };

            var response = req.CreateResponse(HttpStatusCode.Created);
            await response.WriteAsJsonAsync(responseDto);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao criar categoria");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PUT /api/categorias/{id}
    /// Atualiza uma categoria. Requer role ADMIN.
    /// </summary>
    [Function("UpdateCategoria")]
    public async Task<HttpResponseData> UpdateCategoria(
        [HttpTrigger(AuthorizationLevel.Anonymous, "put", Route = "categorias/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Atualizando categoria: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var categoriaRequest = await req.ReadFromJsonAsync<CategoriaRequestDTO>();
            
            if (categoriaRequest == null)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Dados da categoria são obrigatórios" });
                return badRequest;
            }

            var categoria = await _context.Categorias.FindAsync(id);
            
            if (categoria == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Categoria não encontrada" });
                return notFound;
            }

            categoria.Nome = categoriaRequest.Nome;
            categoria.Descricao = categoriaRequest.Descricao;
            categoria.Ativo = categoriaRequest.Ativo;

            await _context.SaveChangesAsync();

            var responseDto = new CategoriaResponseDTO
            {
                Id = categoria.Id,
                Nome = categoria.Nome,
                Descricao = categoria.Descricao,
                Ativo = categoria.Ativo
            };

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(responseDto);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao atualizar categoria");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// DELETE /api/categorias/{id}
    /// Exclui uma categoria. Requer role ADMIN.
    /// </summary>
    [Function("DeleteCategoria")]
    public async Task<HttpResponseData> DeleteCategoria(
        [HttpTrigger(AuthorizationLevel.Anonymous, "delete", Route = "categorias/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Excluindo categoria: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var categoria = await _context.Categorias
                .Include(c => c.Produtos)
                .FirstOrDefaultAsync(c => c.Id == id);
            
            if (categoria == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Categoria não encontrada" });
                return notFound;
            }

            if (categoria.Produtos.Any())
            {
                var conflict = req.CreateResponse(HttpStatusCode.Conflict);
                await conflict.WriteAsJsonAsync(new { error = "Não é possível excluir categoria com produtos associados" });
                return conflict;
            }

            _context.Categorias.Remove(categoria);
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.NoContent);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao excluir categoria");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }
}
