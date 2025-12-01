package com.petshop.functions.shared.messages;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Classes de mensagens para Azure Service Bus
 */
public final class ServiceBusMessages {

    private ServiceBusMessages() {}

    /**
     * Mensagem para dedução de estoque
     */
    public record StockDeductionMessage(
            Long pedidoId,
            List<StockItem> items,
            LocalDateTime timestamp
    ) implements Serializable {
        public record StockItem(Long produtoId, int quantidade) implements Serializable {}
    }

    /**
     * Mensagem para restauração de estoque (cancelamento)
     */
    public record StockRestoreMessage(
            Long pedidoId,
            List<StockDeductionMessage.StockItem> items,
            String reason,
            LocalDateTime timestamp
    ) implements Serializable {}

    /**
     * Mensagem de confirmação de pedido
     */
    public record OrderConfirmedMessage(
            Long pedidoId,
            Long clienteId,
            BigDecimal valorTotal,
            String status,
            LocalDateTime timestamp
    ) implements Serializable {}

    /**
     * Mensagem de atualização de status de pedido
     */
    public record OrderStatusChangedMessage(
            Long pedidoId,
            String statusAnterior,
            String statusNovo,
            LocalDateTime timestamp
    ) implements Serializable {}

    /**
     * Mensagem de agendamento criado
     */
    public record SchedulingCreatedMessage(
            Long agendamentoId,
            Long clienteId,
            Long petId,
            Long servicoId,
            LocalDateTime dataHora,
            LocalDateTime timestamp
    ) implements Serializable {}

    /**
     * Mensagem de agendamento cancelado
     */
    public record SchedulingCancelledMessage(
            Long agendamentoId,
            String motivo,
            LocalDateTime timestamp
    ) implements Serializable {}

    /**
     * Mensagem de cliente registrado
     */
    public record CustomerRegisteredMessage(
            Long clienteId,
            Long usuarioId,
            String nome,
            String email,
            LocalDateTime timestamp
    ) implements Serializable {}

    /**
     * Mensagem de notificação genérica
     */
    public record NotificationMessage(
            String tipo,
            String destinatario,
            String assunto,
            String conteudo,
            LocalDateTime timestamp
    ) implements Serializable {}
}
