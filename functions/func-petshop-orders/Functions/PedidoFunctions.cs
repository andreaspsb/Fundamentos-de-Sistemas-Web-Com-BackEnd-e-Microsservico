using System.Net;
using System.Text.Json;
using Azure.Messaging.ServiceBus;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Petshop.Shared.Data;
using Petshop.Shared.DTOs.Pedido;
using Petshop.Shared.Enums;
using Petshop.Shared.Messages;
using Petshop.Shared.Models;
using Petshop.Shared.Security;
using Petshop.Shared.ServiceClients;

namespace Petshop.Functions.Orders;

public class PedidoFunctions
{
    private readonly ILogger<PedidoFunctions> _logger;
    private readonly PetshopDbContext _context;
    private readonly JwtService _jwtService;
    private readonly ICustomerServiceClient _customerClient;
    private readonly ICatalogServiceClient _catalogClient;
    private readonly ServiceBusClient _serviceBusClient;

    public PedidoFunctions(
        ILogger<PedidoFunctions> logger,
        PetshopDbContext context,
        JwtService jwtService,
        ICustomerServiceClient customerClient,
        ICatalogServiceClient catalogClient,
        ServiceBusClient serviceBusClient)
    {
        _logger = logger;
        _context = context;
        _jwtService = jwtService;
        _customerClient = customerClient;
        _catalogClient = catalogClient;
        _serviceBusClient = serviceBusClient;
    }

    /// <summary>
    /// GET /api/pedidos
    /// Lista todos os pedidos. Requer role ADMIN.
    /// </summary>
    [Function("GetAllPedidos")]
    public async Task<HttpResponseData> GetAllPedidos(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "pedidos")] HttpRequestData req)
    {
        _logger.LogInformation("Listando todos os pedidos");

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pedidos = await _context.Pedidos
                .Include(p => p.Cliente)
                .Include(p => p.Itens)
                    .ThenInclude(i => i.Produto)
                .OrderByDescending(p => p.DataPedido)
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(pedidos.Select(MapToResponseDTO));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar pedidos");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/pedidos/{id}
    /// Busca pedido por ID. Requer autenticação.
    /// </summary>
    [Function("GetPedidoById")]
    public async Task<HttpResponseData> GetPedidoById(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "pedidos/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Buscando pedido por ID: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pedido = await _context.Pedidos
                .Include(p => p.Cliente)
                .Include(p => p.Itens)
                    .ThenInclude(i => i.Produto)
                .FirstOrDefaultAsync(p => p.Id == id);

            if (pedido == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Pedido não encontrado" });
                return notFound;
            }

            // Verifica permissão: admin ou dono do pedido
            if (auth.Role != "ADMIN" && pedido.ClienteId != auth.ClienteId)
            {
                var forbidden = req.CreateResponse(HttpStatusCode.Forbidden);
                await forbidden.WriteAsJsonAsync(new { error = "Acesso negado" });
                return forbidden;
            }

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(MapToResponseDTO(pedido));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao buscar pedido");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/pedidos/cliente/{clienteId}
    /// Lista pedidos de um cliente. Requer autenticação.
    /// </summary>
    [Function("GetPedidosByCliente")]
    public async Task<HttpResponseData> GetPedidosByCliente(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "pedidos/cliente/{clienteId:long}")] HttpRequestData req,
        long clienteId)
    {
        _logger.LogInformation("Listando pedidos do cliente: {ClienteId}", clienteId);

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            // Verifica permissão: admin ou próprio cliente
            if (auth.Role != "ADMIN" && clienteId != auth.ClienteId)
            {
                var forbidden = req.CreateResponse(HttpStatusCode.Forbidden);
                await forbidden.WriteAsJsonAsync(new { error = "Acesso negado" });
                return forbidden;
            }

            var pedidos = await _context.Pedidos
                .Include(p => p.Cliente)
                .Include(p => p.Itens)
                    .ThenInclude(i => i.Produto)
                .Where(p => p.ClienteId == clienteId)
                .OrderByDescending(p => p.DataPedido)
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(pedidos.Select(MapToResponseDTO));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar pedidos do cliente");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/pedidos/meus
    /// Lista pedidos do cliente autenticado.
    /// </summary>
    [Function("GetMeusPedidos")]
    public async Task<HttpResponseData> GetMeusPedidos(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "pedidos/meus")] HttpRequestData req)
    {
        _logger.LogInformation("Listando meus pedidos");

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pedidos = await _context.Pedidos
                .Include(p => p.Cliente)
                .Include(p => p.Itens)
                    .ThenInclude(i => i.Produto)
                .Where(p => p.ClienteId == auth.ClienteId)
                .OrderByDescending(p => p.DataPedido)
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(pedidos.Select(MapToResponseDTO));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar meus pedidos");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/pedidos/status/{status}
    /// Lista pedidos por status. Requer role ADMIN.
    /// </summary>
    [Function("GetPedidosByStatus")]
    public async Task<HttpResponseData> GetPedidosByStatus(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "pedidos/status/{status}")] HttpRequestData req,
        string status)
    {
        _logger.LogInformation("Listando pedidos por status: {Status}", status);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            if (!Enum.TryParse<StatusPedido>(status, true, out var statusEnum))
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { 
                    error = "Status inválido",
                    statusValidos = Enum.GetNames<StatusPedido>()
                });
                return badRequest;
            }

            var pedidos = await _context.Pedidos
                .Include(p => p.Cliente)
                .Include(p => p.Itens)
                    .ThenInclude(i => i.Produto)
                .Where(p => p.Status == statusEnum)
                .OrderByDescending(p => p.DataPedido)
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(pedidos.Select(MapToResponseDTO));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar pedidos por status");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// GET /api/pedidos/recentes
    /// Lista pedidos recentes (últimos 30 dias). Requer role ADMIN.
    /// </summary>
    [Function("GetPedidosRecentes")]
    public async Task<HttpResponseData> GetPedidosRecentes(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "pedidos/recentes")] HttpRequestData req)
    {
        _logger.LogInformation("Listando pedidos recentes");

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var dataLimite = DateTime.UtcNow.AddDays(-30);
            var pedidos = await _context.Pedidos
                .Include(p => p.Cliente)
                .Include(p => p.Itens)
                    .ThenInclude(i => i.Produto)
                .Where(p => p.DataPedido >= dataLimite)
                .OrderByDescending(p => p.DataPedido)
                .ToListAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(pedidos.Select(MapToResponseDTO));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao listar pedidos recentes");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// POST /api/pedidos
    /// Cria um novo pedido. Requer autenticação.
    /// </summary>
    [Function("CreatePedido")]
    public async Task<HttpResponseData> CreatePedido(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "pedidos")] HttpRequestData req)
    {
        _logger.LogInformation("Criando novo pedido");

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pedidoRequest = await req.ReadFromJsonAsync<PedidoRequestDTO>();
            
            if (pedidoRequest == null)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Dados do pedido são obrigatórios" });
                return badRequest;
            }

            if (pedidoRequest.Itens == null || !pedidoRequest.Itens.Any())
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Pedido deve conter pelo menos um item" });
                return badRequest;
            }

            // Verifica cliente
            var cliente = await _context.Clientes.FindAsync(pedidoRequest.ClienteId);
            if (cliente == null)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Cliente não encontrado" });
                return badRequest;
            }

            // Verifica permissão: admin ou próprio cliente
            if (auth.Role != "ADMIN" && pedidoRequest.ClienteId != auth.ClienteId)
            {
                var forbidden = req.CreateResponse(HttpStatusCode.Forbidden);
                await forbidden.WriteAsJsonAsync(new { error = "Acesso negado" });
                return forbidden;
            }

            // Valida produtos e estoque
            var itensValidados = new List<(Produto produto, int quantidade)>();
            foreach (var item in pedidoRequest.Itens)
            {
                var produto = await _context.Produtos.FindAsync(item.ProdutoId);
                if (produto == null)
                {
                    var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                    await badRequest.WriteAsJsonAsync(new { error = $"Produto {item.ProdutoId} não encontrado" });
                    return badRequest;
                }

                if (!produto.Ativo)
                {
                    var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                    await badRequest.WriteAsJsonAsync(new { error = $"Produto '{produto.Nome}' está indisponível" });
                    return badRequest;
                }

                if (!produto.TemEstoque(item.Quantidade))
                {
                    var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                    await badRequest.WriteAsJsonAsync(new { 
                        error = $"Estoque insuficiente para '{produto.Nome}'. Disponível: {produto.QuantidadeEstoque}" 
                    });
                    return badRequest;
                }

                itensValidados.Add((produto, item.Quantidade));
            }

            // Cria o pedido
            var pedido = new Pedido
            {
                ClienteId = pedidoRequest.ClienteId,
                DataPedido = DateTime.UtcNow,
                Status = StatusPedido.Pendente,
                FormaPagamento = pedidoRequest.FormaPagamento,
                Observacoes = pedidoRequest.Observacoes,
                Itens = new List<ItemPedido>()
            };

            double valorTotal = 0;
            foreach (var (produto, quantidade) in itensValidados)
            {
                var itemPedido = new ItemPedido
                {
                    ProdutoId = produto.Id,
                    Quantidade = quantidade,
                    PrecoUnitario = produto.Preco,
                    Subtotal = quantidade * produto.Preco
                };
                pedido.Itens.Add(itemPedido);
                valorTotal += itemPedido.Subtotal;
            }

            pedido.ValorTotal = valorTotal;

            _context.Pedidos.Add(pedido);
            await _context.SaveChangesAsync();

            // Reload com includes
            var pedidoCriado = await _context.Pedidos
                .Include(p => p.Cliente)
                .Include(p => p.Itens)
                    .ThenInclude(i => i.Produto)
                .FirstAsync(p => p.Id == pedido.Id);

            var response = req.CreateResponse(HttpStatusCode.Created);
            await response.WriteAsJsonAsync(MapToResponseDTO(pedidoCriado));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao criar pedido");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PATCH /api/pedidos/{id}/confirmar
    /// Confirma um pedido. Requer role ADMIN.
    /// Envia mensagem para deduzir estoque.
    /// </summary>
    [Function("ConfirmarPedido")]
    public async Task<HttpResponseData> ConfirmarPedido(
        [HttpTrigger(AuthorizationLevel.Anonymous, "patch", Route = "pedidos/{id:long}/confirmar")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Confirmando pedido: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pedido = await _context.Pedidos
                .Include(p => p.Itens)
                .FirstOrDefaultAsync(p => p.Id == id);
            
            if (pedido == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Pedido não encontrado" });
                return notFound;
            }

            if (pedido.Status != StatusPedido.Pendente)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Apenas pedidos pendentes podem ser confirmados" });
                return badRequest;
            }

            pedido.Status = StatusPedido.Confirmado;
            await _context.SaveChangesAsync();

            // Envia mensagem para deduzir estoque
            await SendStockDeductionMessage(pedido);

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(new { message = "Pedido confirmado com sucesso" });
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao confirmar pedido");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PATCH /api/pedidos/{id}/processar
    /// Marca pedido como em processamento. Requer role ADMIN.
    /// </summary>
    [Function("ProcessarPedido")]
    public async Task<HttpResponseData> ProcessarPedido(
        [HttpTrigger(AuthorizationLevel.Anonymous, "patch", Route = "pedidos/{id:long}/processar")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Processando pedido: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pedido = await _context.Pedidos.FindAsync(id);
            
            if (pedido == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Pedido não encontrado" });
                return notFound;
            }

            if (pedido.Status != StatusPedido.Confirmado)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Apenas pedidos confirmados podem ser processados" });
                return badRequest;
            }

            pedido.Status = StatusPedido.Processando;
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(new { message = "Pedido em processamento" });
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao processar pedido");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PATCH /api/pedidos/{id}/enviar
    /// Marca pedido como enviado. Requer role ADMIN.
    /// </summary>
    [Function("EnviarPedido")]
    public async Task<HttpResponseData> EnviarPedido(
        [HttpTrigger(AuthorizationLevel.Anonymous, "patch", Route = "pedidos/{id:long}/enviar")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Enviando pedido: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pedido = await _context.Pedidos.FindAsync(id);
            
            if (pedido == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Pedido não encontrado" });
                return notFound;
            }

            if (pedido.Status != StatusPedido.Confirmado && pedido.Status != StatusPedido.Processando)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Pedido precisa estar confirmado ou em processamento para ser enviado" });
                return badRequest;
            }

            pedido.Status = StatusPedido.Enviado;
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(new { message = "Pedido enviado com sucesso" });
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao enviar pedido");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PATCH /api/pedidos/{id}/entregar
    /// Marca pedido como entregue. Requer role ADMIN.
    /// </summary>
    [Function("EntregarPedido")]
    public async Task<HttpResponseData> EntregarPedido(
        [HttpTrigger(AuthorizationLevel.Anonymous, "patch", Route = "pedidos/{id:long}/entregar")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Entregando pedido: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pedido = await _context.Pedidos.FindAsync(id);
            
            if (pedido == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Pedido não encontrado" });
                return notFound;
            }

            if (pedido.Status != StatusPedido.Enviado)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Apenas pedidos enviados podem ser marcados como entregues" });
                return badRequest;
            }

            pedido.Status = StatusPedido.Entregue;
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(new { message = "Pedido entregue com sucesso" });
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao entregar pedido");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// PATCH /api/pedidos/{id}/cancelar
    /// Cancela um pedido. Requer autenticação.
    /// Envia mensagem para restaurar estoque se já foi confirmado.
    /// </summary>
    [Function("CancelarPedido")]
    public async Task<HttpResponseData> CancelarPedido(
        [HttpTrigger(AuthorizationLevel.Anonymous, "patch", Route = "pedidos/{id:long}/cancelar")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Cancelando pedido: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService);
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pedido = await _context.Pedidos
                .Include(p => p.Cliente)
                .Include(p => p.Itens)
                .FirstOrDefaultAsync(p => p.Id == id);
            
            if (pedido == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Pedido não encontrado" });
                return notFound;
            }

            // Verifica permissão: admin ou dono do pedido
            if (auth.Role != "ADMIN" && pedido.ClienteId != auth.ClienteId)
            {
                var forbidden = req.CreateResponse(HttpStatusCode.Forbidden);
                await forbidden.WriteAsJsonAsync(new { error = "Acesso negado" });
                return forbidden;
            }

            if (pedido.Status == StatusPedido.Entregue || pedido.Status == StatusPedido.Cancelado)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Pedido já finalizado" });
                return badRequest;
            }

            var precisaRestaurarEstoque = pedido.Status == StatusPedido.Confirmado || 
                                          pedido.Status == StatusPedido.Processando ||
                                          pedido.Status == StatusPedido.Enviado;

            pedido.Status = StatusPedido.Cancelado;
            await _context.SaveChangesAsync();

            // Restaura estoque se necessário
            if (precisaRestaurarEstoque)
            {
                await SendStockRestoreMessage(pedido);
            }

            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(new { message = "Pedido cancelado com sucesso" });
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao cancelar pedido");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    /// <summary>
    /// DELETE /api/pedidos/{id}
    /// Exclui um pedido. Requer role ADMIN.
    /// </summary>
    [Function("DeletePedido")]
    public async Task<HttpResponseData> DeletePedido(
        [HttpTrigger(AuthorizationLevel.Anonymous, "delete", Route = "pedidos/{id:long}")] HttpRequestData req,
        long id)
    {
        _logger.LogInformation("Excluindo pedido: {Id}", id);

        var auth = FunctionAuthorization.Authorize(req, _jwtService, "ADMIN");
        if (!auth.IsAuthorized)
        {
            return await FunctionAuthorization.CreateUnauthorizedResponse(req, auth.ErrorMessage!);
        }

        try
        {
            var pedido = await _context.Pedidos
                .Include(p => p.Itens)
                .FirstOrDefaultAsync(p => p.Id == id);
            
            if (pedido == null)
            {
                var notFound = req.CreateResponse(HttpStatusCode.NotFound);
                await notFound.WriteAsJsonAsync(new { error = "Pedido não encontrado" });
                return notFound;
            }

            // Apenas pedidos cancelados podem ser excluídos
            if (pedido.Status != StatusPedido.Cancelado)
            {
                var badRequest = req.CreateResponse(HttpStatusCode.BadRequest);
                await badRequest.WriteAsJsonAsync(new { error = "Apenas pedidos cancelados podem ser excluídos" });
                return badRequest;
            }

            _context.Pedidos.Remove(pedido);
            await _context.SaveChangesAsync();

            var response = req.CreateResponse(HttpStatusCode.NoContent);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao excluir pedido");
            var error = req.CreateResponse(HttpStatusCode.InternalServerError);
            await error.WriteAsJsonAsync(new { error = "Erro interno do servidor" });
            return error;
        }
    }

    private async Task SendStockDeductionMessage(Pedido pedido)
    {
        try
        {
            var message = new StockDeductionMessage
            {
                PedidoId = pedido.Id,
                Items = pedido.Itens.Select(i => new StockItemMessage
                {
                    ProdutoId = i.ProdutoId,
                    Quantidade = i.Quantidade
                }).ToList()
            };

            var sender = _serviceBusClient.CreateSender("stock-deduction");
            var messageBody = JsonSerializer.Serialize(message);
            await sender.SendMessageAsync(new ServiceBusMessage(messageBody));
            
            _logger.LogInformation("Mensagem de dedução de estoque enviada para pedido {PedidoId}", pedido.Id);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao enviar mensagem de dedução de estoque");
            // Não falha a operação, apenas loga o erro
        }
    }

    private async Task SendStockRestoreMessage(Pedido pedido)
    {
        try
        {
            var message = new StockRestoreMessage
            {
                PedidoId = pedido.Id,
                Items = pedido.Itens.Select(i => new StockItemMessage
                {
                    ProdutoId = i.ProdutoId,
                    Quantidade = i.Quantidade
                }).ToList()
            };

            var sender = _serviceBusClient.CreateSender("stock-restore");
            var messageBody = JsonSerializer.Serialize(message);
            await sender.SendMessageAsync(new ServiceBusMessage(messageBody));
            
            _logger.LogInformation("Mensagem de restauração de estoque enviada para pedido {PedidoId}", pedido.Id);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao enviar mensagem de restauração de estoque");
        }
    }

    private static PedidoResponseDTO MapToResponseDTO(Pedido pedido)
    {
        return new PedidoResponseDTO
        {
            Id = pedido.Id,
            ClienteId = pedido.ClienteId,
            ClienteNome = pedido.Cliente?.Nome ?? "",
            ClienteTelefone = pedido.Cliente?.Telefone,
            DataPedido = pedido.DataPedido,
            Status = pedido.Status.ToString(),
            ValorTotal = (decimal)pedido.ValorTotal,
            FormaPagamento = pedido.FormaPagamento,
            Observacoes = pedido.Observacoes,
            Itens = pedido.Itens.Select(i => new ItemPedidoDTO
            {
                Id = i.Id,
                ProdutoId = i.ProdutoId,
                ProdutoNome = i.Produto?.Nome ?? "",
                Quantidade = i.Quantidade,
                PrecoUnitario = (decimal)i.PrecoUnitario,
                Subtotal = (decimal)i.Subtotal
            }).ToList()
        };
    }
}
