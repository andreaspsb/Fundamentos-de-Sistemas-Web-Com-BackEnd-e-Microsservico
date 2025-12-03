# Copilot Instructions - Pet Shop Full-Stack

## Architecture Overview

This is a **multi-backend, multi-frontend** Pet Shop system with 4 interchangeable backends and 2 frontends. All backends expose identical REST APIs.

### Backend Stack (pick any - same API contract)
| Backend | Port | Type | Path |
|---------|------|------|------|
| **ASP.NET Core** (reference) | 5000 | Monolith | `backend-aspnet/` |
| **Spring Boot** | 8080 | Monolith | `backend-springboot/` |
| **C# Azure Functions** | 7071-7076 | Microservices | `functions/` |
| **Java Azure Functions** | 7081-7086 | Microservices | `functions-java/` |

### Frontend Stack
- `frontend/` - Vanilla JS + Bootstrap 5 (web)
- `mobile/` - React Native/Expo (iOS, Android, Web)

### Entities
Cliente → Pet (1:N), Cliente → Pedido (1:N), Pedido → ItemPedido (1:N), Produto → Categoria (N:1), Agendamento → Servico (N:1)

### Shared Database Architecture
**All 4 backends connect to the SAME database** - this is fundamental:
- **Production**: Azure SQL Database (`petshop-db.database.windows.net`)
- **Development**: SQLite (ASP.NET), H2 (Spring Boot/Java Functions), or shared SQL Server via Docker

This shared database design means:
- Schema changes affect ALL backends simultaneously
- Data created by one backend is immediately visible to others
- EF Core migrations in `backend-aspnet/` define the canonical schema
- Frontend can switch backends mid-session without data loss

## Critical Consistency Rules

**Every backend change MUST be replicated to all 4 backends.** This is non-negotiable.

1. Implement in `backend-aspnet/` first (reference implementation)
2. Replicate to `backend-springboot/` with identical endpoint/DTO structure
3. Replicate to `functions/` (C# microservices)
4. Replicate to `functions-java/` (Java microservices)
5. Update `frontend/` and `mobile/` if API contract changes

### JSON Serialization Contract
- **All fields use camelCase** in JSON responses
- ASP.NET: `JsonNamingPolicy.CamelCase` in `Program.cs`
- Spring Boot: `spring.jackson.property-naming-strategy=LOWER_CAMEL_CASE`
- Frontend has `normalizeResponse()` in `js/api-config.js` that converts PascalCase→camelCase
- Dates: ISO 8601 format (`yyyy-MM-dd` or `yyyy-MM-ddTHH:mm:ss`)

## Database Migrations

**EF Core is the source of truth for schema:**
```bash
cd backend-aspnet/PetshopApi
dotnet ef migrations add <MigrationName>
dotnet ef migrations script > ../../scripts/XXX-description.sql
```
- Scripts in `scripts/` folder with naming: `001-initial.sql`, `002-add-field.sql`
- Other backends apply SQL scripts directly - **never use Flyway/Liquibase auto-migrations**

## Developer Workflow

### Quick Start (Docker - recommended)
```bash
./start-all.sh              # Start all services
./start-all.sh status       # Check status
./start-all.sh stop         # Stop all
./start-all.sh full         # Start with dev infrastructure (Azurite, Redis, etc.)
```

### Manual Backend Start
```bash
# ASP.NET (reference)
cd backend-aspnet/PetshopApi && dotnet run

# Spring Boot
cd backend-springboot && mvn spring-boot:run

# C# Functions (6 microservices)
cd functions && ./start-all.sh

# Java Functions (6 microservices)
cd functions-java && ./start-all-java.sh
```

### Frontend Development
```bash
# Web (use Live Server in VS Code or)
cd frontend && python3 -m http.server 5500

# Mobile
cd mobile && npm install && npx expo start
```

### Running Tests

#### E2E Tests (Playwright)
**Prerequisites**: Backend must be running before E2E tests. Frontend is auto-started by Playwright.

```bash
# 1. First, install Chromium (one-time setup)
npx playwright install chromium

# 2. Start all services
./start-all.sh

# 3. Run E2E tests
npm test                    # All browsers, headless
npm run test:ui             # Interactive UI
npm run test:debug          # Debug mode
```

#### Unit Tests (Backend)
```bash
cd backend-aspnet/PetshopApi.Tests && dotnet test
cd backend-springboot && mvn test
```

## API Patterns

### Endpoint Naming
- URLs: kebab-case (`/api/clientes`, `/api/pets/{id}`)
- Controllers follow pattern: `{Entity}Controller` with standard CRUD + custom actions

### Authentication
- JWT Bearer tokens with 24h expiry
- Default admin: `admin` / `admin123`
- Roles: `ADMIN`, `CLIENTE`
- Token validation: `GET /api/auth/validar`

### Frontend Backend Toggle
The frontend can switch backends dynamically via `localStorage.getItem('backend-selecionado')`:
- `SPRINGBOOT`, `ASPNET`, `FUNCTIONS`, `FUNCTIONS_JAVA`
- Configuration in `frontend/js/api-config.js`

## Microservices Port Map (Azure Functions)

| Service | C# Port | Java Port | Responsibilities |
|---------|---------|-----------|------------------|
| Auth | 7071 | 7081 | Login, JWT, User management |
| Customers | 7072 | 7082 | Cliente CRUD |
| Pets | 7073 | 7083 | Pet CRUD |
| Catalog | 7074 | 7084 | Produtos, Categorias, Serviços |
| Scheduling | 7075 | 7085 | Agendamentos |
| Orders | 7076 | 7086 | Pedidos, ItemPedido |

## Key Files Reference

- `frontend/js/api-config.js` - API client, backend toggle, response normalization
- `frontend/js/auth.js` - AuthManager class for frontend auth
- `backend-aspnet/PetshopApi/Program.cs` - ASP.NET DI, CORS, JWT config
- `backend-springboot/src/.../config/` - Spring Boot configuration
- `docker-compose.yml` - Full stack containerization
- `start-all.sh` - Unified startup script

## Documentation

- `docs/QUICKSTART.md` - Getting started guide
- `docs/JWT_AUTHENTICATION.md` - Auth implementation details
- `docs/CORS_CONFIGURATION.md` - CORS setup
- `docs/MICROSSERVICOS.md` - Microservices architecture
- `tests/e2e/README.md` - E2E test documentation

## Azure Deployment

### Production URLs
| Component | URL |
|-----------|-----|
| Frontend | `https://yellow-field-047215b0f.3.azurestaticapps.net` |
| Spring Boot | `https://petshop-backend-spring.azurewebsites.net` |
| ASP.NET Core | `https://petshop-backend-aspnet.azurewebsites.net` |
| C# Functions | `https://func-petshop-{service}.azurewebsites.net` |
| Java Functions | `https://func-petshop-{service}-java.azurewebsites.net` |

### CI/CD Pipeline (`.github/workflows/cd-azure.yml`)
- **Automatic detection**: Only deploys changed components
- **Cost optimization**: Upgrades to B1 plan during deploy, downgrades to F1 after
- **Parallel deployment**: Functions deploy in parallel using matrix strategy
- **Smoke tests**: Validates endpoints after deployment

### Required GitHub Secrets
- `AZURE_CREDENTIALS` - Service principal JSON for Azure login
- `AZURE_STATIC_WEB_APPS_API_TOKEN_*` - Static Web Apps deployment token
- `EXPO_TOKEN` - For mobile APK builds

### Manual Deployment
```bash
# Backend Spring Boot
cd backend-springboot && mvn clean package
az webapp deploy --name petshop-backend-spring ...

# Backend ASP.NET Core
cd backend-aspnet/PetshopApi && dotnet publish -c Release
az webapp deploy --name petshop-backend-aspnet ...
```

### Database Connection (Azure SQL)
All backends use the same Azure SQL Database via connection strings configured in Azure App Service settings. See `docs/DEPLOY.md` for full deployment guide.

## VS Code Extensions (Recommended)

### Azure Development
- **Azure Tools** (`ms-vscode.vscode-node-azure-pack`) - Complete pack for Azure development
- **SQL Server (mssql)** (`ms-mssql.mssql`) - Connect and execute queries on Azure SQL Database

### Azure Login & Database Access
1. Use **Azure: Sign In** command (`Ctrl+Shift+P`) to authenticate
2. For Azure SQL queries, use **MS SQL: Connect** with:
   - Server: `petshop-db.database.windows.net`
   - Database: `petshop-db`
   - User: `petshop_admin`
3. Execute SQL scripts with `Ctrl+Shift+E` on `.sql` files

### Firewall Rules (WSL/Remote)
If connection fails due to firewall, add your IP via Azure CLI:
```bash
az login --use-device-code
az sql server firewall-rule create \
  --resource-group petshop-rg \
  --server petshop-db \
  --name MeuIP \
  --start-ip-address YOUR_IP \
  --end-ip-address YOUR_IP
```

## Enum Conventions (CRITICAL)

**All status enums use SCREAMING_SNAKE_CASE** across all backends:

### StatusPedido
`PENDENTE`, `CONFIRMADO`, `PROCESSANDO`, `ENVIADO`, `ENTREGUE`, `CANCELADO`

### StatusAgendamento
`PENDENTE`, `CONFIRMADO`, `EM_ANDAMENTO`, `CONCLUIDO`, `CANCELADO`

This ensures consistency when switching backends with the shared database.

