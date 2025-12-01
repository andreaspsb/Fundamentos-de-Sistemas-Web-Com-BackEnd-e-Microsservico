package com.petshop.functions.orders;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.petshop.functions.shared.dto.*;
import com.petshop.functions.shared.model.*;
import com.petshop.functions.shared.model.Pedido.StatusPedido;
import com.petshop.functions.shared.repository.*;
import com.petshop.functions.shared.security.FunctionAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Azure Functions for Order Management
 */
@Component
public class OrderFunctions {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final FunctionAuthorization functionAuthorization;

    @Autowired
    public OrderFunctions(
            PedidoRepository pedidoRepository,
            ItemPedidoRepository itemPedidoRepository,
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            FunctionAuthorization functionAuthorization) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.functionAuthorization = functionAuthorization;
    }

    /**
     * GET /api/pedidos
     * List all orders (Admin) or user's orders (Cliente)
     */
    @FunctionName("getAllOrders")
    public HttpResponseMessage getAllOrders(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "pedidos"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all orders");

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            List<Pedido> pedidos;
            
            if ("Admin".equals(authResult.role())) {
                pedidos = pedidoRepository.findAll();
            } else {
                if (authResult.clienteId() == null) {
                    return request.createResponseBuilder(HttpStatus.OK)
                            .header("Content-Type", "application/json")
                            .body(List.of())
                            .build();
                }
                pedidos = pedidoRepository.findByClienteId(authResult.clienteId());
            }

            List<PedidoResponseDTO> response = pedidos.stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();
        });
    }

    /**
     * GET /api/pedidos/{id}
     * Get order by ID
     */
    @FunctionName("getOrderById")
    public HttpResponseMessage getOrderById(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "pedidos/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Getting order by ID: " + id);

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
            
            if (pedidoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Pedido não encontrado"))
                        .build();
            }

            Pedido pedido = pedidoOpt.get();

            // Cliente can only see their own orders
            if ("Cliente".equals(authResult.role()) && 
                (pedido.getCliente() == null || !pedido.getCliente().getId().equals(authResult.clienteId()))) {
                return functionAuthorization.createForbiddenResponse(request, "Você só pode visualizar seus próprios pedidos");
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(pedido))
                    .build();
        });
    }

    /**
     * GET /api/pedidos/status/{status}
     * Get orders by status (Admin only)
     */
    @FunctionName("getOrdersByStatus")
    public HttpResponseMessage getOrdersByStatus(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "pedidos/status/{status}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("status") String statusStr,
            final ExecutionContext context) {

        context.getLogger().info("Getting orders by status: " + statusStr);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            try {
                StatusPedido status = StatusPedido.valueOf(statusStr.toUpperCase());
                List<Pedido> pedidos = pedidoRepository.findByStatus(status);
                List<PedidoResponseDTO> response = pedidos.stream()
                        .map(this::toResponseDTO)
                        .collect(Collectors.toList());

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(response)
                        .build();
            } catch (IllegalArgumentException e) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Status inválido. Use: PENDENTE, CONFIRMADO, PROCESSANDO, ENVIADO, ENTREGUE, CANCELADO"))
                        .build();
            }
        });
    }

    /**
     * POST /api/pedidos
     * Create new order
     */
    @FunctionName("createOrder")
    public HttpResponseMessage createOrder(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "pedidos"
            ) HttpRequestMessage<Optional<PedidoRequestDTO>> request,
            final ExecutionContext context) {

        context.getLogger().info("Creating new order");

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            Optional<PedidoRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            PedidoRequestDTO dto = bodyOpt.get();

            // Validate items
            if (dto.getItens() == null || dto.getItens().isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "O pedido deve conter pelo menos um item"))
                        .build();
            }

            // Determine clienteId
            Long clienteId = dto.getClienteId();
            if ("Cliente".equals(authResult.role())) {
                clienteId = authResult.clienteId();
            }

            if (clienteId == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Cliente não identificado"))
                        .build();
            }

            Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
            if (clienteOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Cliente não encontrado"))
                        .build();
            }

            // Validate products and calculate total
            double valorTotal = 0.0;
            List<ItemPedido> itens = new ArrayList<>();

            for (ItemPedidoRequestDTO itemDTO : dto.getItens()) {
                Optional<Produto> produtoOpt = produtoRepository.findById(itemDTO.getProdutoId());
                if (produtoOpt.isEmpty()) {
                    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .header("Content-Type", "application/json")
                            .body(Map.of("error", "Produto não encontrado: " + itemDTO.getProdutoId()))
                            .build();
                }

                Produto produto = produtoOpt.get();

                // Check stock
                if (produto.getQuantidadeEstoque() < itemDTO.getQuantidade()) {
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                            .header("Content-Type", "application/json")
                            .body(Map.of("error", "Estoque insuficiente para o produto: " + produto.getNome()))
                            .build();
                }

                ItemPedido item = new ItemPedido();
                item.setProduto(produto);
                item.setQuantidade(itemDTO.getQuantidade());
                item.setPrecoUnitario(produto.getPreco());
                itens.add(item);

                valorTotal += produto.getPreco() * itemDTO.getQuantidade();

                // Deduct stock
                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - itemDTO.getQuantidade());
                produtoRepository.save(produto);
            }

            // Create order
            Pedido pedido = new Pedido();
            pedido.setCliente(clienteOpt.get());
            pedido.setDataPedido(LocalDateTime.now());
            pedido.setStatus(StatusPedido.PENDENTE);
            pedido.setValorTotal(valorTotal);
            pedido.setFormaPagamento(dto.getFormaPagamento());
            pedido.setObservacoes(dto.getObservacoes());

            pedido = pedidoRepository.save(pedido);

            // Save items
            for (ItemPedido item : itens) {
                item.setPedido(pedido);
                itemPedidoRepository.save(item);
            }

            // Reload to get items
            pedido = pedidoRepository.findById(pedido.getId()).orElse(pedido);

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(pedido))
                    .build();
        });
    }

    /**
     * PUT /api/pedidos/{id}/status
     * Update order status (Admin only)
     */
    @FunctionName("updateOrderStatus")
    public HttpResponseMessage updateOrderStatus(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "pedidos/{id}/status"
            ) HttpRequestMessage<Optional<Map<String, String>>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Updating order status: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
            if (pedidoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Pedido não encontrado"))
                        .build();
            }

            Optional<Map<String, String>> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty() || !bodyOpt.get().containsKey("status")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "status é obrigatório"))
                        .build();
            }

            try {
                Pedido pedido = pedidoOpt.get();
                StatusPedido novoStatus = StatusPedido.valueOf(bodyOpt.get().get("status").toUpperCase());
                StatusPedido statusAnterior = pedido.getStatus();

                // If cancelling, restore stock
                if (novoStatus == StatusPedido.CANCELADO && statusAnterior != StatusPedido.CANCELADO) {
                    List<ItemPedido> itens = itemPedidoRepository.findByPedidoId(id);
                    for (ItemPedido item : itens) {
                        Produto produto = item.getProduto();
                        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + item.getQuantidade());
                        produtoRepository.save(produto);
                    }
                }

                pedido.setStatus(novoStatus);
                pedidoRepository.save(pedido);

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(toResponseDTO(pedido))
                        .build();
            } catch (IllegalArgumentException e) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Status inválido. Use: PENDENTE, CONFIRMADO, PROCESSANDO, ENVIADO, ENTREGUE, CANCELADO"))
                        .build();
            }
        });
    }

    /**
     * DELETE /api/pedidos/{id}
     * Cancel order
     */
    @FunctionName("cancelOrder")
    public HttpResponseMessage cancelOrder(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "pedidos/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Cancelling order: " + id);

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
            if (pedidoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Pedido não encontrado"))
                        .build();
            }

            Pedido pedido = pedidoOpt.get();

            // Cliente can only cancel their own orders
            if ("Cliente".equals(authResult.role()) && 
                (pedido.getCliente() == null || !pedido.getCliente().getId().equals(authResult.clienteId()))) {
                return functionAuthorization.createForbiddenResponse(request, "Você só pode cancelar seus próprios pedidos");
            }

            // Can only cancel if status is Pendente
            if (pedido.getStatus() != StatusPedido.PENDENTE && "Cliente".equals(authResult.role())) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Só é possível cancelar pedidos pendentes"))
                        .build();
            }

            // Restore stock
            List<ItemPedido> itens = itemPedidoRepository.findByPedidoId(id);
            for (ItemPedido item : itens) {
                Produto produto = item.getProduto();
                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + item.getQuantidade());
                produtoRepository.save(produto);
            }

            // Update status
            pedido.setStatus(StatusPedido.CANCELADO);
            pedidoRepository.save(pedido);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(Map.of("message", "Pedido cancelado com sucesso"))
                    .build();
        });
    }

    /**
     * GET /api/pedidos/estatisticas
     * Get order statistics (Admin only)
     */
    @FunctionName("getOrderStats")
    public HttpResponseMessage getOrderStats(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "pedidos/estatisticas"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting order statistics");

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            long pendentes = pedidoRepository.countByStatus(StatusPedido.PENDENTE);
            long confirmados = pedidoRepository.countByStatus(StatusPedido.CONFIRMADO);
            long enviados = pedidoRepository.countByStatus(StatusPedido.ENVIADO);
            long entregues = pedidoRepository.countByStatus(StatusPedido.ENTREGUE);
            long cancelados = pedidoRepository.countByStatus(StatusPedido.CANCELADO);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(Map.of(
                            "pendentes", pendentes,
                            "confirmados", confirmados,
                            "enviados", enviados,
                            "entregues", entregues,
                            "cancelados", cancelados,
                            "total", pendentes + confirmados + enviados + entregues + cancelados
                    ))
                    .build();
        });
    }

    private PedidoResponseDTO toResponseDTO(Pedido pedido) {
        List<ItemPedido> itens = itemPedidoRepository.findByPedidoId(pedido.getId());
        
        List<ItemPedidoDTO> itensDTO = itens.stream()
                .map(item -> new ItemPedidoDTO(
                        item.getId(),
                        item.getProduto() != null ? item.getProduto().getId() : null,
                        item.getProduto() != null ? item.getProduto().getNome() : null,
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());

        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setDataPedido(pedido.getDataPedido());
        dto.setValorTotal(pedido.getValorTotal());
        dto.setStatus(pedido.getStatus() != null ? pedido.getStatus().name() : null);
        dto.setFormaPagamento(pedido.getFormaPagamento());
        dto.setObservacoes(pedido.getObservacoes());
        
        if (pedido.getCliente() != null) {
            dto.setClienteId(pedido.getCliente().getId());
            dto.setClienteNome(pedido.getCliente().getNome());
            dto.setClienteTelefone(pedido.getCliente().getTelefone());
        }
        
        dto.setItens(itensDTO);
        
        return dto;
    }
}
