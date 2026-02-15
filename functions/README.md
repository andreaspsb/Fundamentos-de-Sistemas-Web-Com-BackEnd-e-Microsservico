# 🐾 PetShop Microservices - Azure Functions

> **📌 Arquitetura Multi-Backend:** Este é **um dos 4 backends intercambiáveis** do projeto:
> - **Spring Boot** (porta 8080) - Monolito Java
> - **ASP.NET Core** (porta 5000) - Monolito C#
> - **C# Azure Functions** (portas 7071-7076) - **Microsserviços C# (este backend)**
> - **Java Azure Functions** (portas 7081-7086) - Microsserviços Java
>
> **Todos compartilham o mesmo banco de dados** (Azure SQL em produção, SQLite/H2 em desenvolvimento).
> O frontend pode alternar entre eles dinamicamente usando o sistema de toggle.

Este projeto demonstra uma **arquitetura de microsserviços** utilizando **Azure Functions**, contrastando com a arquitetura monolítica dos backends Spring Boot e ASP.NET Core.

## 📋 Índice

- [Arquitetura](#arquitetura)
- [Microsserviços](#microsserviços)
- [Comunicação entre Serviços](#comunicação-entre-serviços)
- [Resiliência](#resiliência)
- [Pré-requisitos](#pré-requisitos)
- [Como Executar](#como-executar)
- [Endpoints](#endpoints)
- [Configuração](#configuração)

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────────────┐
│                           Frontend                                   │
│                    (HTML/JS + React Native)                         │
└─────────────────────────────┬───────────────────────────────────────┘
                              │ HTTP
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Azure Functions                                 │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │   Auth   │ │ Customer │ │   Pet    │ │ Catalog  │ │Scheduling│  │
│  │ Service  │ │ Service  │ │ Service  │ │ Service  │ │ Service  │  │
│  │ :7071    │ │ :7072    │ │ :7073    │ │ :7074    │ │ :7075    │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘  │
│                                                                      │
│  ┌──────────┐                                                       │
│  │  Order   │                                                       │
│  │ Service  │                                                       │
│  │ :7076    │                                                       │
│  └──────────┘                                                       │
└──────────────┬────────────────────────────────────┬─────────────────┘
               │ HTTP (sync)                        │ Service Bus (async)
               ▼                                    ▼
┌──────────────────────────┐          ┌──────────────────────────┐
│     Azure SQL Database   │          │    Azure Service Bus     │
│    (Shared - inicial)    │          │  - stock-deduction       │
│                          │          │  - stock-restore         │
│                          │          │  - scheduling-confirmation│
└──────────────────────────┘          └──────────────────────────┘
```

## 🔧 Microsserviços

### 1. Auth Service (`func-petshop-auth`)
**Porta:** 7071

Responsável pela autenticação e autorização.

| Função | Método | Rota | Descrição |
|--------|--------|------|-----------|
| Login | POST | /api/auth/login | Autentica usuário e retorna JWT |
| Register | POST | /api/auth/register | Registra novo usuário |
| ValidateToken | GET | /api/auth/validate | Valida token JWT |

### 2. Customer Service (`func-petshop-customers`)
**Porta:** 7072

Gerenciamento de clientes.

| Função | Método | Rota | Descrição |
|--------|--------|------|-----------|
| GetAllClientes | GET | /api/clientes | Lista todos os clientes (ADMIN) |
| GetClienteById | GET | /api/clientes/{id} | Busca cliente por ID |
| GetClienteByCpf | GET | /api/clientes/cpf/{cpf} | Busca cliente por CPF |
| CreateCliente | POST | /api/clientes | Cria novo cliente |
| UpdateCliente | PUT | /api/clientes/{id} | Atualiza cliente |
| DeleteCliente | DELETE | /api/clientes/{id} | Exclui cliente (ADMIN) |

### 3. Pet Service (`func-petshop-pets`)
**Porta:** 7073

Gerenciamento de pets.

| Função | Método | Rota | Descrição |
|--------|--------|------|-----------|
| GetAllPets | GET | /api/pets | Lista todos os pets (ADMIN) |
| GetPetById | GET | /api/pets/{id} | Busca pet por ID |
| GetPetsByCliente | GET | /api/pets/cliente/{clienteId} | Lista pets de um cliente |
| GetPetsByTipo | GET | /api/pets/tipo/{tipo} | Lista pets por tipo (ADMIN) |
| CreatePet | POST | /api/pets | Cria novo pet |
| UpdatePet | PUT | /api/pets/{id} | Atualiza pet |
| DeletePet | DELETE | /api/pets/{id} | Exclui pet |

### 4. Catalog Service (`func-petshop-catalog`)
**Porta:** 7074

Gerenciamento unificado de categorias, produtos e serviços.

#### Categorias
| Função | Método | Rota | Descrição |
|--------|--------|------|-----------|
| GetAllCategorias | GET | /api/categorias | Lista todas as categorias |
| GetCategoriaById | GET | /api/categorias/{id} | Busca categoria por ID |
| GetCategoriasAtivas | GET | /api/categorias/ativas | Lista categorias ativas |
| CreateCategoria | POST | /api/categorias | Cria categoria (ADMIN) |
| UpdateCategoria | PUT | /api/categorias/{id} | Atualiza categoria (ADMIN) |
| DeleteCategoria | DELETE | /api/categorias/{id} | Exclui categoria (ADMIN) |

#### Produtos
| Função | Método | Rota | Descrição |
|--------|--------|------|-----------|
| GetAllProdutos | GET | /api/produtos | Lista todos os produtos |
| GetProdutosDisponiveis | GET | /api/produtos/disponiveis | Lista produtos disponíveis |
| GetProdutoById | GET | /api/produtos/{id} | Busca produto por ID |
| GetProdutosByCategoria | GET | /api/produtos/categoria/{id} | Lista por categoria |
| SearchProdutos | GET | /api/produtos/buscar?termo= | Busca por termo |
| GetProdutosEstoqueBaixo | GET | /api/produtos/estoque-baixo | Estoque baixo (ADMIN) |
| VerificarEstoque | GET | /api/produtos/{id}/verificar-estoque | Verifica disponibilidade |
| CreateProduto | POST | /api/produtos | Cria produto (ADMIN) |
| UpdateProduto | PUT | /api/produtos/{id} | Atualiza produto (ADMIN) |
| SetProdutoEstoque | PATCH | /api/produtos/{id}/estoque | Define estoque (ADMIN) |
| AddProdutoEstoque | PATCH | /api/produtos/{id}/adicionar-estoque | Adiciona estoque (ADMIN) |
| AtivarProduto | PATCH | /api/produtos/{id}/ativar | Ativa produto (ADMIN) |
| DesativarProduto | PATCH | /api/produtos/{id}/desativar | Desativa produto (ADMIN) |
| DeleteProduto | DELETE | /api/produtos/{id} | Exclui produto (ADMIN) |

#### Serviços
| Função | Método | Rota | Descrição |
|--------|--------|------|-----------|
| GetAllServicos | GET | /api/servicos | Lista todos os serviços |
| GetServicosAtivos | GET | /api/servicos/ativos | Lista serviços ativos |
| GetServicoById | GET | /api/servicos/{id} | Busca serviço por ID |
| CreateServico | POST | /api/servicos | Cria serviço (ADMIN) |
| UpdateServico | PUT | /api/servicos/{id} | Atualiza serviço (ADMIN) |
| AtivarServico | PATCH | /api/servicos/{id}/ativar | Ativa serviço (ADMIN) |
| DesativarServico | PATCH | /api/servicos/{id}/desativar | Desativa serviço (ADMIN) |
| DeleteServico | DELETE | /api/servicos/{id} | Exclui serviço (ADMIN) |

#### Service Bus Triggers
| Função | Fila | Descrição |
|--------|------|-----------|
| ProcessStockDeduction | stock-deduction | Deduz estoque após confirmação de pedido |
| ProcessStockRestore | stock-restore | Restaura estoque após cancelamento |

#### Timer Triggers
| Função | Schedule | Descrição |
|--------|----------|-----------|
| CheckLowStockAlerts | 0 0 11 * * * | Verifica estoque baixo (diário às 8h BRT) |

### 5. Scheduling Service (`func-petshop-scheduling`)
**Porta:** 7075

Gerenciamento de agendamentos.

| Função | Método | Rota | Descrição |
|--------|--------|------|-----------|
| GetAllAgendamentos | GET | /api/agendamentos | Lista todos (ADMIN) |
| GetAgendamentoById | GET | /api/agendamentos/{id} | Busca por ID |
| GetAgendamentosByCliente | GET | /api/agendamentos/cliente/{id} | Por cliente |
| GetAgendamentosByPet | GET | /api/agendamentos/pet/{id} | Por pet |
| GetAgendamentosByData | GET | /api/agendamentos/data/{data} | Por data (ADMIN) |
| GetAgendamentosByStatus | GET | /api/agendamentos/status/{status} | Por status (ADMIN) |
| GetProximosAgendamentos | GET | /api/agendamentos/proximos | Próximos (ADMIN) |
| CreateAgendamento | POST | /api/agendamentos | Cria agendamento |
| UpdateAgendamento | PUT | /api/agendamentos/{id} | Atualiza agendamento |
| ConfirmarAgendamento | PATCH | /api/agendamentos/{id}/confirmar | Confirma (ADMIN) |
| ConcluirAgendamento | PATCH | /api/agendamentos/{id}/concluir | Conclui (ADMIN) |
| CancelarAgendamento | PATCH | /api/agendamentos/{id}/cancelar | Cancela |
| DeleteAgendamento | DELETE | /api/agendamentos/{id} | Exclui (ADMIN) |

#### Timer Triggers
| Função | Schedule | Descrição |
|--------|----------|-----------|
| SendAgendamentoReminders | 0 0 21 * * * | Lembretes (18h BRT) |
| CheckMissedAppointments | 0 0 * * * * | Verifica não comparecidos (horário) |
| AutoStartAppointments | 0 */15 * * * * | Auto-inicia agendamentos (15 min) |

### 6. Order Service (`func-petshop-orders`)
**Porta:** 7076

Gerenciamento de pedidos.

| Função | Método | Rota | Descrição |
|--------|--------|------|-----------|
| GetAllPedidos | GET | /api/pedidos | Lista todos (ADMIN) |
| GetPedidoById | GET | /api/pedidos/{id} | Busca por ID |
| GetPedidosByCliente | GET | /api/pedidos/cliente/{id} | Por cliente |
| GetPedidosByStatus | GET | /api/pedidos/status/{status} | Por status (ADMIN) |
| GetPedidosRecentes | GET | /api/pedidos/recentes | Últimos 30 dias (ADMIN) |
| CreatePedido | POST | /api/pedidos | Cria pedido |
| ConfirmarPedido | PATCH | /api/pedidos/{id}/confirmar | Confirma (ADMIN) |
| EnviarPedido | PATCH | /api/pedidos/{id}/enviar | Envia (ADMIN) |
| EntregarPedido | PATCH | /api/pedidos/{id}/entregar | Entrega (ADMIN) |
| CancelarPedido | PATCH | /api/pedidos/{id}/cancelar | Cancela |
| DeletePedido | DELETE | /api/pedidos/{id} | Exclui (ADMIN) |

#### Timer Triggers
| Função | Schedule | Descrição |
|--------|----------|-----------|
| CancelAbandonedOrders | 0 0 * * * * | Cancela abandonados (horário) |
| GenerateDailyOrderReport | 0 0 2 * * * | Relatório diário (23h BRT) |
| CheckDelayedOrders | 0 0 */4 * * * | Verifica atrasados (4h) |

## 🔄 Comunicação entre Serviços

### Padrão Híbrido

1. **HTTP Síncrono** - Para validações e consultas:
   - Pet Service → Customer Service (valida ClienteId)
   - Order Service → Catalog Service (verifica estoque)
   - Scheduling Service → Customer, Pet, Catalog Services

2. **Service Bus Assíncrono** - Para operações de estado:
   - Order Service → `stock-deduction` → Catalog Service
   - Order Service → `stock-restore` → Catalog Service
   - Notifications → `scheduling-confirmation` → Scheduling Service

## 🛡️ Resiliência

Implementada com `Microsoft.Extensions.Http.Resilience`:

```csharp
// Configuração padrão para todos os HTTP clients
services.AddHttpClient<ICustomerServiceClient, CustomerServiceClient>(client =>
{
    client.BaseAddress = new Uri(customerServiceUrl);
    client.Timeout = TimeSpan.FromSeconds(30);
})
.AddStandardResilienceHandler();
```

### Políticas Incluídas:
- **Retry**: 3 tentativas com backoff exponencial
- **Circuit Breaker**: Abre após 50% de falhas em janela de 30s
- **Timeout**: 10s por requisição
- **Bulkhead**: Limita requisições concorrentes

## 📋 Pré-requisitos

- [.NET 8 SDK](https://dotnet.microsoft.com/download/dotnet/8.0)
- [Azure Functions Core Tools v4](https://docs.microsoft.com/azure/azure-functions/functions-run-local)
- [Azurite](https://docs.microsoft.com/azure/storage/common/storage-use-azurite) (emulador de Storage)
- [SQL Server](https://www.microsoft.com/sql-server) ou Azure SQL
- [Azure Service Bus](https://azure.microsoft.com/services/service-bus/) ou emulador

## 🚀 Como Executar

### 1. Clonar e Restaurar

```bash
cd functions
dotnet restore Petshop.Functions.sln
```

### 2. Iniciar Dependências

```bash
# Iniciar Azurite (Storage Emulator)
azurite --silent --location ./azurite --debug ./azurite/debug.log

# Iniciar SQL Server (Docker)
docker run -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=YourStrong@Passw0rd' \
  -p 1433:1433 --name sql1 -d mcr.microsoft.com/mssql/server:2022-latest
```

### 3. Iniciar os Serviços

Em terminais separados:

```bash
# Auth Service
cd func-petshop-auth && func start --port 7071

# Customer Service
cd func-petshop-customers && func start --port 7072

# Pet Service
cd func-petshop-pets && func start --port 7073

# Catalog Service
cd func-petshop-catalog && func start --port 7074

# Scheduling Service
cd func-petshop-scheduling && func start --port 7075

# Order Service
cd func-petshop-orders && func start --port 7076
```

### 4. Executar Todos (Script)

**Recomendado:** Use o script `start-all.sh` (Linux/Mac) ou `start-all.ps1` (Windows):

```bash
# Linux/Mac
chmod +x start-all.sh
./start-all.sh

# Windows PowerShell
.\start-all.ps1
```

O script inicia automaticamente todas as 6 funções nas portas corretas.

### 5. Teste de Integração com Outros Backends

Como todos os 4 backends compartilham o mesmo banco de dados:

```bash
# 1. Criar um produto usando ASP.NET Core (porta 5000)
curl -X POST http://localhost:5000/api/produtos \
  -H "Content-Type: application/json" \
  -d '{"nome": "Teste", "preco": 100, "categoriaId": 1}'

# 2. Verificar que o produto é visível nas C# Functions (porta 7074)
curl http://localhost:7074/api/produtos

# 3. Alternar no frontend para Functions e ver o mesmo dado
# Abrir Developer Tools (F12) e executar:
alternarBackend('FUNCTIONS')
```

### 5. Teste de Integração com Outros Backends

Como todos os 4 backends compartilham o mesmo banco de dados:

```bash
# 1. Criar um produto usando ASP.NET Core (porta 5000)
curl -X POST http://localhost:5000/api/produtos \
  -H "Content-Type: application/json" \
  -d '{"nome": "Teste", "preco": 100, "categoriaId": 1}'

# 2. Verificar que o produto é visível nas C# Functions (porta 7074)
curl http://localhost:7074/api/produtos

# 3. Alternar no frontend para Functions e ver o mesmo dado
# Abrir Developer Tools (F12) e executar:
alternarBackend('FUNCTIONS')
```

## ⚙️ Configuração

Cada serviço possui um `local.settings.json`:

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "FUNCTIONS_WORKER_RUNTIME": "dotnet-isolated",
    "SqlConnection": "Server=localhost;Database=PetshopDb;...",
    "ServiceBusConnection": "Endpoint=sb://localhost;...",
    "Jwt:SecretKey": "sua-chave-secreta-256-bits",
    "Jwt:Issuer": "PetshopApi",
    "Jwt:Audience": "PetshopFrontend"
  }
}
```

### Variáveis de Ambiente (Produção)

| Variável | Descrição |
|----------|-----------|
| `SqlConnection` | Connection string do Azure SQL |
| `ServiceBusConnection` | Connection string do Service Bus |
| `Jwt:SecretKey` | Chave secreta para JWT (256+ bits) |
| `CustomerServiceBaseUrl` | URL do Customer Service |
| `PetServiceBaseUrl` | URL do Pet Service |
| `CatalogServiceBaseUrl` | URL do Catalog Service |

## 📊 Monitoramento

- **Application Insights**: Telemetria e logging
- **Azure Monitor**: Métricas e alertas
- **Log Analytics**: Consultas e dashboards

## 🧪 Testes

```bash
# Executar testes unitários
dotnet test

# Com cobertura
dotnet test --collect:"XPlat Code Coverage"
```

## 📁 Estrutura do Projeto

```
functions/
├── Petshop.Shared/           # Biblioteca compartilhada
│   ├── Data/                 # DbContext
│   ├── DTOs/                 # Data Transfer Objects
│   ├── Enums/                # Enumerações
│   ├── Messages/             # Mensagens Service Bus
│   ├── Models/               # Entidades
│   ├── Security/             # JWT e autorização
│   └── ServiceClients/       # Clientes HTTP resilientes
├── func-petshop-auth/        # Microsserviço de autenticação
├── func-petshop-customers/   # Microsserviço de clientes
├── func-petshop-pets/        # Microsserviço de pets
├── func-petshop-catalog/     # Microsserviço de catálogo
├── func-petshop-scheduling/  # Microsserviço de agendamentos
├── func-petshop-orders/      # Microsserviço de pedidos
└── Petshop.Functions.sln     # Solution file
```

## 📜 Licença

MIT License - veja [LICENSE](../LICENSE) para detalhes.
