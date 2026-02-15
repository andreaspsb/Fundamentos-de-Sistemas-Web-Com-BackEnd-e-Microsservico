# 🚀 Quick Start - Backend ASP.NET Core

> **📌 Nota:** Este é **um dos 4 backends intercambiáveis** do projeto.
> Você pode usar Spring Boot (8080), ASP.NET Core (5000), C# Functions (7071-7076) ou Java Functions (7081-7086).
> Todos compartilham o mesmo banco de dados.

## Executar o Projeto

```bash
cd backend-aspnet/PetshopApi
dotnet run
```

Acesse: **http://localhost:5000**

## Credenciais de Teste

- **Admin**: `admin` / `admin123`
- **Cliente**: `maria.silva` / `senha123`

## Endpoints Principais

### Testar Login
```bash
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","senha":"admin123"}'
```

### Listar Produtos
```bash
curl http://localhost:5000/api/produtos
```

### Listar Categorias
```bash
curl http://localhost:5000/api/categorias
```

### Listar Serviços
```bash
curl http://localhost:5000/api/servicos
```

## Estrutura de Dados

**3 Categorias:**
- Rações e Alimentação
- Higiene e Cuidados  
- Acessórios e Brinquedos

**6 Produtos** (idênticos ao Spring Boot)
**3 Serviços:** Banho, Tosa, Banho + Tosa

## Compatibilidade

✅ API 100% compatível com os outros 3 backends
✅ Mesmos endpoints (Spring Boot, C# Functions, Java Functions)
✅ Mesmos dados iniciais (banco compartilhado)
✅ Mesma estrutura de DTOs (camelCase JSON)
✅ Frontend pode alternar dinamicamente usando o toggle

**Teste de Integração:**
```bash
# 1. Criar produto no ASP.NET Core
curl -X POST http://localhost:5000/api/produtos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teste","preco":100,"categoriaId":1}'

# 2. Ver mesmo produto no Spring Boot (banco compartilhado)
curl http://localhost:8080/api/produtos

# 3. Ou nas C# Functions
curl http://localhost:7074/api/produtos
```

## Tecnologias

- .NET 8.0
- ASP.NET Core Web API
- Entity Framework Core
- SQLite
- Swagger/OpenAPI
