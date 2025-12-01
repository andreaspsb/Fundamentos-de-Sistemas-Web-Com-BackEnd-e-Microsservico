package com.petshop.functions.shared.messages;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Helper para publicar mensagens no Azure Service Bus
 */
@Component
public class ServiceBusPublisher {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBusPublisher.class);

    @Value("${azure.servicebus.connection-string:}")
    private String connectionString;

    private final ObjectMapper objectMapper;

    // Queue names
    public static final String QUEUE_STOCK_DEDUCTION = "stock-deduction";
    public static final String QUEUE_STOCK_RESTORE = "stock-restore";
    public static final String QUEUE_ORDER_CONFIRMED = "order-confirmed";
    public static final String QUEUE_ORDER_STATUS_CHANGED = "order-status-changed";
    public static final String QUEUE_SCHEDULING_CREATED = "scheduling-created";
    public static final String QUEUE_SCHEDULING_CANCELLED = "scheduling-cancelled";
    public static final String QUEUE_CUSTOMER_REGISTERED = "customer-registered";
    public static final String QUEUE_NOTIFICATIONS = "notifications";

    public ServiceBusPublisher() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Publica uma mensagem em uma queue do Service Bus
     */
    public <T> boolean publish(String queueName, T message) {
        if (connectionString == null || connectionString.isBlank()) {
            logger.warn("Service Bus connection string não configurada. Mensagem não enviada para queue: {}", queueName);
            return false;
        }

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            try (ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .sender()
                    .queueName(queueName)
                    .buildClient()) {
                
                ServiceBusMessage busMessage = new ServiceBusMessage(jsonMessage);
                busMessage.setContentType("application/json");
                
                senderClient.sendMessage(busMessage);
                logger.info("Mensagem publicada na queue {}: {}", queueName, jsonMessage);
                return true;
            }
        } catch (Exception e) {
            logger.error("Erro ao publicar mensagem na queue {}: {}", queueName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Publica mensagem de dedução de estoque
     */
    public boolean publishStockDeduction(ServiceBusMessages.StockDeductionMessage message) {
        return publish(QUEUE_STOCK_DEDUCTION, message);
    }

    /**
     * Publica mensagem de restauração de estoque
     */
    public boolean publishStockRestore(ServiceBusMessages.StockRestoreMessage message) {
        return publish(QUEUE_STOCK_RESTORE, message);
    }

    /**
     * Publica mensagem de pedido confirmado
     */
    public boolean publishOrderConfirmed(ServiceBusMessages.OrderConfirmedMessage message) {
        return publish(QUEUE_ORDER_CONFIRMED, message);
    }

    /**
     * Publica mensagem de mudança de status de pedido
     */
    public boolean publishOrderStatusChanged(ServiceBusMessages.OrderStatusChangedMessage message) {
        return publish(QUEUE_ORDER_STATUS_CHANGED, message);
    }

    /**
     * Publica mensagem de agendamento criado
     */
    public boolean publishSchedulingCreated(ServiceBusMessages.SchedulingCreatedMessage message) {
        return publish(QUEUE_SCHEDULING_CREATED, message);
    }

    /**
     * Publica mensagem de agendamento cancelado
     */
    public boolean publishSchedulingCancelled(ServiceBusMessages.SchedulingCancelledMessage message) {
        return publish(QUEUE_SCHEDULING_CANCELLED, message);
    }

    /**
     * Publica mensagem de cliente registrado
     */
    public boolean publishCustomerRegistered(ServiceBusMessages.CustomerRegisteredMessage message) {
        return publish(QUEUE_CUSTOMER_REGISTERED, message);
    }

    /**
     * Publica notificação
     */
    public boolean publishNotification(ServiceBusMessages.NotificationMessage message) {
        return publish(QUEUE_NOTIFICATIONS, message);
    }
}
