# Configuração de Mensageria Local (Azurite)

Este documento descreve como configurar e usar os serviços de mensageria localmente para desenvolvimento.

## Visão Geral

O projeto suporta múltiplos provedores de mensageria:

| Provider | Uso | Requisitos |
|----------|-----|------------|
| **InMemory** | Testes simples | Nenhum |
| **AzureStorageQueue** | Desenvolvimento local | Azurite (Docker) |
| **ServiceBus** | Produção | Azure Service Bus |
| **RabbitMQ** | Alternativa local | RabbitMQ (Docker) |

## Início Rápido

### 1. Iniciar Azurite (recomendado)

```bash
# Opção 1: Via script
./start-dev-services.sh azurite

# Opção 2: Via Docker Compose
docker-compose -f docker-compose.dev.yml up azurite -d

# Opção 3: Via npm (se instalado globalmente)
npm install -g azurite
azurite --silent --location ./azurite-data --debug ./azurite-debug.log
```

### 2. Configurar as Functions

No arquivo `local.settings.json` de cada function:

```json
{
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "MessageBroker:Provider": "AzureStorageQueue",
    "MessageBroker:AzureStorageConnection": "UseDevelopmentStorage=true"
  }
}
```

### 3. Verificar conexão

```bash
# Verificar se Azurite está rodando
curl http://localhost:10001/devstoreaccount1

# Ver filas criadas (via Azure Storage Explorer ou CLI)
az storage queue list --connection-string "UseDevelopmentStorage=true"
```

## Arquitetura de Mensageria

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRODUCERS (Microsserviços)                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐        │
│  │   Orders     │   │  Scheduling  │   │   Catalog    │        │
│  │   Service    │   │   Service    │   │   Service    │        │
│  └──────┬───────┘   └──────┬───────┘   └──────┬───────┘        │
│         │                  │                   │                 │
│         ▼                  ▼                   ▼                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                  IMessageBroker                          │   │
│  │  (Abstração: ServiceBus / AzureStorageQueue / InMemory)  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                            │                                    │
└────────────────────────────┼────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                        MESSAGE QUEUES                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │ stock-deduction  │  │  stock-restore   │  │ order-status  │ │
│  └──────────────────┘  └──────────────────┘  └───────────────┘ │
│                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │ appointment-     │  │   low-stock-     │  │  appointment- │ │
│  │ created          │  │   alert          │  │  reminder     │ │
│  └──────────────────┘  └──────────────────┘  └───────────────┘ │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CONSUMERS (Functions)                         │
├─────────────────────────────────────────────────────────────────┤
│  Queue Triggers para processar mensagens                         │
│  (Em produção: ServiceBusTrigger / Em dev: QueueTrigger)        │
└─────────────────────────────────────────────────────────────────┘
```

## Filas Disponíveis

| Fila | Produtor | Consumidor | Descrição |
|------|----------|------------|-----------|
| `stock-deduction` | Orders | Catalog | Deduz estoque quando pedido é confirmado |
| `stock-restore` | Orders | Catalog | Restaura estoque quando pedido é cancelado |
| `order-confirmed` | Orders | Notifications | Notifica cliente sobre confirmação |
| `order-cancelled` | Orders | Notifications | Notifica cliente sobre cancelamento |
| `order-status-changed` | Orders | Notifications | Notifica mudança de status |
| `appointment-created` | Scheduling | Notifications | Notifica novo agendamento |
| `appointment-reminder` | Timer Trigger | Notifications | Lembrete 24h antes |
| `low-stock-alert` | Catalog | Notifications | Alerta de estoque baixo |

## Configuração por Ambiente

### Desenvolvimento (InMemory)

Sem necessidade de infraestrutura externa. Mensagens são armazenadas em memória.

```json
{
  "MessageBroker:Provider": "InMemory"
}
```

### Desenvolvimento (Azurite)

Emulador local do Azure Storage.

```json
{
  "AzureWebJobsStorage": "UseDevelopmentStorage=true",
  "MessageBroker:Provider": "AzureStorageQueue",
  "MessageBroker:AzureStorageConnection": "UseDevelopmentStorage=true"
}
```

### Produção (Azure Service Bus)

```json
{
  "MessageBroker:Provider": "ServiceBus",
  "MessageBroker:ServiceBusConnection": "Endpoint=sb://your-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=your-key"
}
```

## Usando o IMessageBroker

### Injeção de Dependência

```csharp
// Program.cs
services.AddMessageBroker(configuration);
```

### Envio de Mensagens

```csharp
public class OrderService
{
    private readonly IMessageBroker _messageBroker;

    public OrderService(IMessageBroker messageBroker)
    {
        _messageBroker = messageBroker;
    }

    public async Task ConfirmOrder(Order order)
    {
        // ... lógica de confirmação ...

        // Enviar mensagem para dedução de estoque
        await _messageBroker.SendMessageAsync(
            QueueNames.StockDeduction,
            new StockDeductionMessage
            {
                PedidoId = order.Id,
                Items = order.Items.Select(i => new StockItemMessage
                {
                    ProdutoId = i.ProdutoId,
                    Quantidade = i.Quantidade
                }).ToList()
            });
    }
}
```

## Ferramentas Úteis

### Azure Storage Explorer

Interface gráfica para visualizar e gerenciar dados do Azurite:
- Download: https://azure.microsoft.com/features/storage-explorer/
- Conectar ao Azurite: Use "Local storage emulator"

### Azure CLI

```bash
# Instalar
curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash

# Listar filas
az storage queue list --connection-string "UseDevelopmentStorage=true"

# Ver mensagens em uma fila
az storage message peek --queue-name stock-deduction --connection-string "UseDevelopmentStorage=true"
```

### Docker Commands

```bash
# Ver logs do Azurite
docker logs petshop-azurite -f

# Reiniciar Azurite
docker restart petshop-azurite

# Limpar dados (remove volume)
docker-compose -f docker-compose.dev.yml down -v
```

## Troubleshooting

### Erro: "Connection refused" ao conectar ao Azurite

1. Verifique se o container está rodando:
   ```bash
   docker ps | grep azurite
   ```

2. Verifique os logs:
   ```bash
   docker logs petshop-azurite
   ```

3. Verifique se as portas estão disponíveis:
   ```bash
   netstat -tlnp | grep -E "10000|10001|10002"
   ```

### Erro: "Queue not found"

As filas são criadas automaticamente pelo `AzureStorageQueueBroker` quando a primeira mensagem é enviada. Se precisar criar manualmente:

```bash
az storage queue create --name stock-deduction --connection-string "UseDevelopmentStorage=true"
```

### Mensagens não estão sendo processadas

1. Verifique se o consumer está rodando
2. Verifique se a connection string está correta
3. Use Azure Storage Explorer para inspecionar as mensagens na fila

## Referências

- [Azurite Documentation](https://learn.microsoft.com/azure/storage/common/storage-use-azurite)
- [Azure Storage Queues](https://learn.microsoft.com/azure/storage/queues/)
- [Azure Service Bus](https://learn.microsoft.com/azure/service-bus-messaging/)
