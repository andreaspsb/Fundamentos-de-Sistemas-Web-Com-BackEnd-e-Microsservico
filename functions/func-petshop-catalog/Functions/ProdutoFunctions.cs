using System.Net;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Petshop.Shared.Data;
using Petshop.Shared.DTOs.Produto;
using Petshop.Shared.Messages;
using Petshop.Shared.Models;
using Petshop.Shared.Security;

namespace Petshop.Functions.Catalog;

public class ProdutoFunctions
{
    private readonly ILogger<ProdutoFunctions> _logger;
    private readonly PetshopDbContext _context;
    private readonly JwtService _jwtService;
    private readonly IConfiguration _configuration;

    public ProdutoFunctions(
        ILogger<ProdutoFunctions> logger,
        PetshopDbContext context,
        JwtService jwtService,
        IConfiguration configuration)
    {
        _logger = logger;
        _context = context;
        _jwtService = jwtService;
        _configuration = configuration;
    }

    /// <summary>
    /// GET /api/produtos
    /// Lista todos os produtos. Endpoint público.
    /// </summary>
    [Function("GetAllProdutos")]
    public async Task<HttpResponseData> GetAllProdutos(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "produtos")] HttpRequestData req)
    {
        _logger.LogInformation("Listando todos os produtos");

        try
        {
            var produtos = await _context.Produtos
                .Include(p => p.Categoria)
                .Select(p => new ProdutoResponseDTO
                {
                    Id = p.Id,
                    Nome = p.Nome,
                    Descricao = p.Descricao,
                    Preco = p.Preco,
                    QuantidadeEstoque = p.QuantidadeEstoque,
                    UrlImagem = p.UrlImagem,
                    Ativo = p.Ativo,
                    CategoriaId = p.CategoriaId,
                    CategoriaNome = p.Categoria != null ? p.Categoria.Nome : ""
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(produtos);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar produtos");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/produtos/disponiveis
    /// Lista produtos disponíveis (ativos e com estoque). Endpoint público.
    /// </summary>
    [Function("GetProdutosDisponiveis")]
    public async Task<HttpResponseData> GetProdutosDisponiveis(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "produtos/disponiveis")] HttpRequestData req)
    {
        _logger.LogInformation("Listando produtos disponíveis");

        try
        {
            var produtos = await _context.Produtos
                .Include(p => p.Categoria)
                .Where(p => p.Ativo && p.QuantidadeEstoque > 0)
                .Select(p => new ProdutoResponseDTO
                {
                    Id = p.Id,
                    Nome = p.Nome,
                    Descricao = p.Descricao,
                    Preco = p.Preco,
                    QuantidadeEstoque = p.QuantidadeEstoque,
                    UrlImagem = p.UrlImagem,
                    Ativo = p.Ativo,
                    CategoriaId = p.CategoriaId,
                    CategoriaNome = p.Categoria != null ? p.Categoria.Nome : ""
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(produtos);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar produtos disponíveis");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/produtos/{id}
    /// Busca produto por ID. Endpoint público.
    /// </summary>
    [Function("GetProdutoById")]
    public async Task<HttpResponseData> GetProdutoById(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "produtos/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Buscando produto por ID: {Id}", id);

        try
        {
            var produto = await _context.Produtos
                .Include(p => p.Categoria)
                .Where(p => p.Id == id)
                .Select(p => new ProdutoResponseDTO
                {
                    Id = p.Id,
                    Nome = p.Nome,
                    Descricao = p.Descricao,
                    Preco = p.Preco,
                    QuantidadeEstoque = p.QuantidadeEstoque,
                    UrlImagem = p.UrlImagem,
                    Ativo = p.Ativo,
                    CategoriaId = p.CategoriaId,
                    CategoriaNome = p.Categoria != null ? p.Categoria.Nome : ""
                })
                .FirstOrDefaultAsync();

            if (produto == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Produto não encontrado" });
                return notFound;
            }

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(produto);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao buscar produto");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/produtos/categoria/{categoriaId}
    /// Lista produtos por categoria. Endpoint público.
    /// </summary>
    [Function("GetProdutosByCategoria")]
    public async Task<HttpResponseData> GetProdutosByCategoria(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "produtos/categoria/{categoriaId:long}")] HttpRequestData req,
        long categoriaId)
    {
        _logger.LogInformation("Listando produtos da categoria: {CategoriaId}", categoriaId);

        try
        {
            var produtos = await _context.Produtos
                .Include(p => p.Categoria)
                .Where(p => p.CategoriaId == categoriaId)
                .Select(p => new ProdutoResponseDTO
                {
                    Id = p.Id,
                    Nome = p.Nome,
                    Descricao = p.Descricao,
                    Preco = p.Preco,
                    QuantidadeEstoque = p.QuantidadeEstoque,
                    UrlImagem = p.UrlImagem,
                    Ativo = p.Ativo,
                    CategoriaId = p.CategoriaId,
                    CategoriaNome = p.Categoria != null ? p.Categoria.Nome : ""
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(produtos);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar produtos por categoria");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/produtos/categoria/{categoriaId}/disponiveis
    /// Lista produtos disponíveis por categoria. Endpoint público.
    /// </summary>
    [Function("GetProdutosDisponiveisByCategoria")]
    public async Task<HttpResponseData> GetProdutosDisponiveisByCategoria(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "produtos/categoria/{categoriaId:long}/disponiveis")] HttpRequestData req,
        long categoriaId)
    {
        _logger.LogInformation("Listando produtos disponíveis da categoria: {CategoriaId}", categoriaId);

        try
        {
            var produtos = await _context.Produtos
                .Include(p => p.Categoria)
                .Where(p => p.CategoriaId == categoriaId && p.Ativo && p.QuantidadeEstoque > 0)
                .Select(p => new ProdutoResponseDTO
                {
                    Id = p.Id,
                    Nome = p.Nome,
                    Descricao = p.Descricao,
                    Preco = p.Preco,
                    QuantidadeEstoque = p.QuantidadeEstoque,
                    UrlImagem = p.UrlImagem,
                    Ativo = p.Ativo,
                    CategoriaId = p.CategoriaId,
                    CategoriaNome = p.Categoria != null ? p.Categoria.Nome : ""
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(produtos);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar produtos disponíveis por categoria");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/produtos/buscar?termo={termo}
    /// Busca produtos por termo. Endpoint público.
    /// </summary>
    [Function("SearchProdutos")]
    public async Task<HttpResponseData> SearchProdutos(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "produtos/buscar")] HttpRequestData req)
    {
        var query = System.Web.HttpUtility.ParseQueryString(req.Url.Query);
        var termo = query["termo"] ?? "";
        
        _logger.LogInformation("Buscando produtos com termo: {Termo}", termo);

        try
        {
            var produtos = await _context.Produtos
                .Include(p => p.Categoria)
                .Where(p => p.Nome.Contains(termo) || (p.Descricao != null && p.Descricao.Contains(termo)))
                .Select(p => new ProdutoResponseDTO
                {
                    Id = p.Id,
                    Nome = p.Nome,
                    Descricao = p.Descricao,
                    Preco = p.Preco,
                    QuantidadeEstoque = p.QuantidadeEstoque,
                    UrlImagem = p.UrlImagem,
                    Ativo = p.Ativo,
                    CategoriaId = p.CategoriaId,
                    CategoriaNome = p.Categoria != null ? p.Categoria.Nome : ""
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(produtos);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao buscar produtos");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/produtos/estoque-baixo
    /// Lista produtos com estoque baixo. Requer role ADMIN.
    /// </summary>
    [Function("GetProdutosEstoqueBaixo")]
    public async Task<HttpResponseData> GetProdutosEstoqueBaixo(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "produtos/estoque-baixo")] HttpRequestData req)
    {
        _logger.LogInformation("Listando produtos com estoque baixo");

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var threshold = int.Parse(_configuration["LowStockThreshold"] ?? "10");
            
            var produtos = await _context.Produtos
                .Include(p => p.Categoria)
                .Where(p => p.QuantidadeEstoque <= threshold && p.Ativo)
                .Select(p => new ProdutoResponseDTO
                {
                    Id = p.Id,
                    Nome = p.Nome,
                    Descricao = p.Descricao,
                    Preco = p.Preco,
                    QuantidadeEstoque = p.QuantidadeEstoque,
                    UrlImagem = p.UrlImagem,
                    Ativo = p.Ativo,
                    CategoriaId = p.CategoriaId,
                    CategoriaNome = p.Categoria != null ? p.Categoria.Nome : ""
                })
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(produtos);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar produtos com estoque baixo");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/produtos/{id}/verificar-estoque?quantidade={quantidade}
    /// Verifica disponibilidade de estoque. Endpoint interno para outros serviços.
    /// </summary>
    [Function("VerificarEstoque")]
    public async Task<HttpResponseData> VerificarEstoque(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "produtos/{id:long}/verificar-estoque")] HttpRequestData req,
        long id)
    {
        var query = System.Web.HttpUtility.ParseQueryString(req.Url.Query);
        var quantidadeStr = query["quantidade"] ?? "1";
        var quantidade = int.Parse(quantidadeStr);

        _logger.LogInformation("Verificando estoque do produto {Id} para quantidade {Quantidade}", id, quantidade);

        try
        {
            var produto = await _context.Produtos.FindAsync(id);

            if (produto == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Produto não encontrado" });
                return notFound;
            }

            var verificacao = new EstoqueVerificacaoDTO
            {
                ProdutoId = produto.Id,
                ProdutoNome = produto.Nome,
                QuantidadeDisponivel = produto.QuantidadeEstoque,
                Disponivel = produto.TemEstoque(quantidade) && produto.Ativo
            };

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(verificacao);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao verificar estoque");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// POST /api/produtos
    /// Cria um novo produto. Requer role ADMIN.
    /// </summary>
    [Function("CreateProduto")]
    public async Task<HttpResponseData> CreateProduto(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "produtos")] HttpRequestData req)
    {
        _logger.LogInformation("Criando novo produto");

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var produtoRequest = await req.ReadFromJsonAsync<ProdutoRequestDTO>();
            
            if (produtoRequest == null || string.IsNullOrEmpty(produtoRequest.Nome))
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Nome do produto é obrigatório" });
                return badRequest;
            }

            // Verifica se categoria existe
            var categoria = await _context.Categorias.FindAsync(produtoRequest.CategoriaId);
            if (categoria == null)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Categoria não encontrada" });
                return badRequest;
            }

            var produto = new Produto
            {
                Nome = produtoRequest.Nome,
                Descricao = produtoRequest.Descricao,
                Preco = produtoRequest.Preco,
                QuantidadeEstoque = produtoRequest.QuantidadeEstoque,
                UrlImagem = produtoRequest.UrlImagem,
                Ativo = produtoRequest.Ativo,
                CategoriaId = produtoRequest.CategoriaId
            };

            _context.Produtos.Add(produto);
            await _context.SaveChangesAsync();

            var responseDto = new ProdutoResponseDTO
            {
                Id = produto.Id,
                Nome = produto.Nome,
                Descricao = produto.Descricao,
                Preco = produto.Preco,
                QuantidadeEstoque = produto.QuantidadeEstoque,
                UrlImagem = produto.UrlImagem,
                Ativo = produto.Ativo,
                CategoriaId = produto.CategoriaId,
                CategoriaNome = categoria.Nome
            };

            var response = req.CreateResponse(HttpStatusCode.Created);
            await response.WriteAsJsonAsync(responseDto);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao criar produto");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PUT /api/produtos/{id}
    /// Atualiza um produto. Requer role ADMIN.
    /// </summary>
    [Function("UpdateProduto")]
    public async Task<HttpResponseData> UpdateProduto(
        [HttpTrigger(AuthorizationLevel.Anonymous, "put", Route = "produtos/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Atualizando produto: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var produtoRequest = await req.ReadFromJsonAsync<ProdutoRequestDTO>();
            
            if (produtoRequest == null)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Dados do produto são obrigatórios" });
                return badRequest;
            }

            var produto = await _context.Produtos.FindAsync(id);
            
            if (produto == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Produto não encontrado" });
                return notFound;
            }

            // Verifica se categoria existe
            var categoria = await _context.Categorias.FindAsync(produtoRequest.CategoriaId);
            if (categoria == null)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Categoria não encontrada" });
                return badRequest;
            }

            produto.Nome = produtoRequest.Nome;
            produto.Descricao = produtoRequest.Descricao;
            produto.Preco = produtoRequest.Preco;
            produto.QuantidadeEstoque = produtoRequest.QuantidadeEstoque;
            produto.UrlImagem = produtoRequest.UrlImagem;
            produto.Ativo = produtoRequest.Ativo;
            produto.CategoriaId = produtoRequest.CategoriaId;

            await _context.SaveChangesAsync();

            var responseDto = new ProdutoResponseDTO
            {
                Id = produto.Id,
                Nome = produto.Nome,
                Descricao = produto.Descricao,
                Preco = produto.Preco,
                QuantidadeEstoque = produto.QuantidadeEstoque,
                UrlImagem = produto.UrlImagem,
                Ativo = produto.Ativo,
                CategoriaId = produto.CategoriaId,
                CategoriaNome = categoria.Nome
            };

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(responseDto);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao atualizar produto");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PATCH /api/produtos/{id}/estoque
    /// Define estoque de um produto. Requer role ADMIN.
    /// </summary>
    [Function("SetProdutoEstoque")]
    public async Task<HttpResponseData> SetProdutoEstoque(
        [HttpTrigger(AuthorizationLevel.Anonymous, "patch", Route = "produtos/{id:long}/estoque")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Definindo estoque do produto: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var estoqueUpdate = await req.ReadFromJsonAsync<EstoqueUpdateDTO>();
            
            if (estoqueUpdate == null)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Quantidade é obrigatória" });
                return badRequest;
            }

            var produto = await _context.Produtos.FindAsync(id);
            
            if (produto == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Produto não encontrado" });
                return notFound;
            }

            produto.QuantidadeEstoque = estoqueUpdate.Quantidade;
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(new { 
                message = "Estoque atualizado com sucesso",
                quantidadeEstoque = produto.QuantidadeEstoque
            });
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao definir estoque");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PATCH /api/produtos/{id}/adicionar-estoque
    /// Adiciona ao estoque de um produto. Requer role ADMIN.
    /// </summary>
    [Function("AddProdutoEstoque")]
    public async Task<HttpResponseData> AddProdutoEstoque(
        [HttpTrigger(AuthorizationLevel.Anonymous, "patch", Route = "produtos/{id:long}/adicionar-estoque")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Adicionando estoque ao produto: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var estoqueUpdate = await req.ReadFromJsonAsync<EstoqueUpdateDTO>();
            
            if (estoqueUpdate == null || estoqueUpdate.Quantidade <= 0)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Quantidade deve ser maior que zero" });
                return badRequest;
            }

            var produto = await _context.Produtos.FindAsync(id);
            
            if (produto == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Produto não encontrado" });
                return notFound;
            }

            produto.AdicionarEstoque(estoqueUpdate.Quantidade);
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(new { 
                message = "Estoque adicionado com sucesso",
                quantidadeEstoque = produto.QuantidadeEstoque
            });
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao adicionar estoque");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PATCH /api/produtos/{id}/ativar
    /// Ativa um produto. Requer role ADMIN.
    /// </summary>
    [Function("AtivarProduto")]
    public async Task<HttpResponseData> AtivarProduto(
        [HttpTrigger(AuthorizationLevel.Anonymous, "patch", Route = "produtos/{id:long}/ativar")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Ativando produto: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var produto = await _context.Produtos.FindAsync(id);
            
            if (produto == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Produto não encontrado" });
                return notFound;
            }

            produto.Ativo = true;
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(new { message = "Produto ativado com sucesso" });
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao ativar produto");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PATCH /api/produtos/{id}/desativar
    /// Desativa um produto. Requer role ADMIN.
    /// </summary>
    [Function("DesativarProduto")]
    public async Task<HttpResponseData> DesativarProduto(
        [HttpTrigger(AuthorizationLevel.Anonymous, "patch", Route = "produtos/{id:long}/desativar")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Desativando produto: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var produto = await _context.Produtos.FindAsync(id);
            
            if (produto == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Produto não encontrado" });
                return notFound;
            }

            produto.Ativo = false;
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(new { message = "Produto desativado com sucesso" });
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao desativar produto");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// DELETE /api/produtos/{id}
    /// Exclui um produto. Requer role ADMIN.
    /// </summary>
    [Function("DeleteProduto")]
    public async Task<HttpResponseData> DeleteProduto(
        [HttpTrigger(AuthorizationLevel.Anonymous, "delete", Route = "produtos/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Excluindo produto: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var produto = await _context.Produtos
                .Include(p => p.ItensPedido)
                .FirstOrDefaultAsync(p => p.Id == id);
            
            if (produto == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Produto não encontrado" });
                return notFound;
            }

            if (produto.ItensPedido.Any())
            {
                var conflict = req.CreateResponse(HttpStatusCode.Conflict);
                await conflict.WriteAsJsonAsync(new { error = "Não é possível excluir produto com pedidos associados. Considere desativá-lo." });
                return conflict;
            }

            _context.Produtos.Remove(produto);
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.NoContent);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao excluir produto");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }
}
