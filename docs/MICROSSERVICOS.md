# Arquitetura de Microsserviços - Pet Shop

## Visão Geral

O projeto Pet Shop implementa uma arquitetura flexível que suporta **4 backends diferentes**, demonstrando a evolução de uma aplicação monolítica para microsserviços:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           FRONTEND (HTML/CSS/JS)                            │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    Backend Toggle Component                          │   │
│  │   [ Spring Boot ] [ ASP.NET Core ] [ C# Funcs ] [ Java Funcs ]      │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌───────────────┐         ┌───────────────┐         ┌───────────────────────┐
│  MONOLITOS    │         │ MICROSSERVIÇOS│         │   MICROSSERVIÇOS      │
│               │         │     C#        │         │      JAVA             │
│ Spring Boot   │         │  (Functions)  │         │   (Functions)         │
│ ASP.NET Core  │         │               │         │                       │
│               │         │ Portas:       │         │ Portas:               │
│ Porta: 8080   │         │ 7071-7076     │         │ 7081-7086             │
│ Porta: 5000   │         │               │         │                       │
└───────────────┘         └───────────────┘         └───────────────────────┘
        │                         │                           │
        └─────────────────────────┼───────────────────────────┘
                                  │
                                  ▼
                        ┌─────────────────┐
                        │    DATABASE     │
                        │    (SQLite)     │
                        │   petshop.db    │
                        └─────────────────┘
```

## Backends Disponíveis

### 1. Spring Boot (Monolito Java)
- **Porta:** 8080
- **Tecnologia:** Java 17, Spring Boot 3
- **Base URL:** `http://localhost:8080/api`
- **Características:**
  - Monolito tradicional
  - Banco de dados SQLite
  - Autenticação JWT
  - Swagger/OpenAPI

### 2. ASP.NET Core (Monolito .NET)
- **Porta:** 5000
- **Tecnologia:** .NET 8, ASP.NET Core
- **Base URL:** `http://localhost:5000/api`
- **Características:**
  - Monolito tradicional
  - Entity Framework Core
  - Banco de dados SQLite
  - Autenticação JWT

### 3. C# Azure Functions (Microsserviços .NET)
- **Portas:** 7071-7076
- **Tecnologia:** .NET 8, Azure Functions Isolated Worker
- **Características:**
  - Arquitetura de microsserviços
  - Comunicação via HTTP
  - Circuit Breaker (Polly)
  - Service Bus para mensageria (opcional)

| Serviço | Porta | Endpoint Base |
|---------|-------|---------------|
| Auth | 7071 | `/api/auth` |
| Customers | 7072 | `/api/clientes` |
| Pets | 7073 | `/api/pets` |
| Catalog | 7074 | `/api/produtos`, `/api/categorias`, `/api/servicos` |
| Scheduling | 7075 | `/api/agendamentos` |
| Orders | 7076 | `/api/pedidos` |

### 4. Java Azure Functions (Microsserviços Java)
- **Portas:** 7081-7086
- **Tecnologia:** Java 17, Azure Functions
- **Características:**
  - Arquitetura de microsserviços
  - Banco H2 in-memory
  - Comunicação via HTTP

| Serviço | Porta | Endpoint Base |
|---------|-------|---------------|
| Auth | 7081 | `/api/auth` |
| Customers | 7082 | `/api/clientes` |
| Pets | 7083 | `/api/pets` |
| Catalog | 7084 | `/api/produtos`, `/api/categorias`, `/api/servicos` |
| Scheduling | 7085 | `/api/agendamentos` |
| Orders | 7086 | `/api/pedidos` |

## Estrutura de Diretórios

```
├── backend-springboot/          # Monolito Spring Boot
├── backend-aspnet/              # Monolito ASP.NET Core
├── functions/                   # Microsserviços C#
│   ├── func-petshop-auth/
│   ├── func-petshop-customers/
│   ├── func-petshop-pets/
│   ├── func-petshop-catalog/
│   ├── func-petshop-scheduling/
│   ├── func-petshop-orders/
│   └── Petshop.Shared/         # Código compartilhado
├── functions-java/              # Microsserviços Java
│   ├── func-petshop-auth-java/
│   ├── func-petshop-customers-java/
│   ├── func-petshop-pets-java/
│   ├── func-petshop-catalog-java/
│   ├── func-petshop-scheduling-java/
│   ├── func-petshop-orders-java/
│   └── petshop-functions-shared/
└── frontend/                    # Frontend HTML/CSS/JS
```

## Comunicação entre Microsserviços

### Padrões Implementados

#### 1. Circuit Breaker (Polly - C# Functions)
```csharp
// Configuração de resiliência
options.Retry.MaxRetryAttempts = 3;
options.Retry.Delay = TimeSpan.FromMilliseconds(200);
options.AttemptTimeout.Timeout = TimeSpan.FromSeconds(5);
options.CircuitBreaker.FailureRatio = 0.5;
options.CircuitBreaker.SamplingDuration = TimeSpan.FromSeconds(60);
```

#### 2. Service Discovery
- Configuração via `local.settings.json` (desenvolvimento)
- Variáveis de ambiente (produção)

#### 3. Mensageria (Service Bus - Opcional)
```
Order Confirmed → stock-deduction queue → Catalog Service
Order Cancelled → stock-restore queue → Catalog Service
```

## Mapeamento de Endpoints

| Recurso | Monolito | C# Functions | Java Functions |
|---------|----------|--------------|----------------|
| Auth | `/api/auth/*` | `7071/api/auth/*` | `7081/api/auth/*` |
| Clientes | `/api/clientes/*` | `7072/api/clientes/*` | `7082/api/clientes/*` |
| Pets | `/api/pets/*` | `7073/api/pets/*` | `7083/api/pets/*` |
| Produtos | `/api/produtos/*` | `7074/api/produtos/*` | `7084/api/produtos/*` |
| Categorias | `/api/categorias/*` | `7074/api/categorias/*` | `7084/api/categorias/*` |
| Serviços | `/api/servicos/*` | `7074/api/servicos/*` | `7084/api/servicos/*` |
| Agendamentos | `/api/agendamentos/*` | `7075/api/agendamentos/*` | `7085/api/agendamentos/*` |
| Pedidos | `/api/pedidos/*` | `7076/api/pedidos/*` | `7086/api/pedidos/*` |

## Como Executar

### Monolitos

**Spring Boot:**
```bash
cd backend-springboot
./mvnw spring-boot:run
```

**ASP.NET Core:**
```bash
cd backend-aspnet/PetshopApi
dotnet run
```

### Microsserviços C# Functions
```bash
cd functions
./start-all.sh
```

### Microsserviços Java Functions
```bash
cd functions-java
./start-all-java.sh
```

### Todos os backends com Docker
```bash
docker-compose up
```

## Frontend - Toggle de Backend

O frontend inclui um componente de toggle que permite alternar entre os backends em tempo real:

```javascript
// api-config.js
const BACKENDS = {
  SPRINGBOOT: { url: 'http://localhost:8080/api', type: 'monolith' },
  ASPNET: { url: 'http://localhost:5000/api', type: 'monolith' },
  FUNCTIONS: { url: 'http://localhost:7071/api', type: 'microservices' },
  FUNCTIONS_JAVA: { url: 'http://localhost:7081/api', type: 'microservices' }
};
```

Para microsserviços, o frontend roteia automaticamente para o serviço correto baseado no tipo de recurso:
- `/api/auth/*` → Auth Service
- `/api/clientes/*` → Customers Service
- `/api/pets/*` → Pets Service
- etc.

## Vantagens da Arquitetura de Microsserviços

1. **Escalabilidade Independente** - Cada serviço pode escalar de acordo com sua demanda
2. **Deploy Independente** - Atualizações isoladas sem afetar outros serviços
3. **Resiliência** - Falha de um serviço não derruba todo o sistema
4. **Tecnologia Heterogênea** - C# e Java coexistem na mesma solução
5. **Equipes Autônomas** - Times podem trabalhar em serviços diferentes

## Desafios e Soluções

| Desafio | Solução Implementada |
|---------|---------------------|
| Comunicação entre serviços | HTTP + Service Bus |
| Consistência de dados | Transações locais + Eventual Consistency |
| Falhas em cascata | Circuit Breaker (Polly) |
| Service Discovery | Configuração estática (dev) / Azure Service Discovery (prod) |
| Autenticação distribuída | JWT compartilhado |

## Monitoramento

- **Application Insights** - Telemetria e logs centralizados
- **Azure Monitor** - Métricas de performance
- **Health Checks** - Endpoints `/api/health` em cada serviço

## Próximos Passos

- [ ] Implementar API Gateway (Azure API Management)
- [ ] Adicionar Kubernetes para orquestração
- [ ] Implementar Event Sourcing
- [ ] Adicionar Distributed Tracing (OpenTelemetry)
