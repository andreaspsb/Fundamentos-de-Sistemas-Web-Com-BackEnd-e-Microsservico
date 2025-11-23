# 🚀 Quick Start - Backend ASP.NET

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

✅ API 100% compatível com o backend Spring Boot
✅ Mesmos endpoints
✅ Mesmos dados iniciais
✅ Mesma estrutura de DTOs

## Tecnologias

- .NET 8.0
- ASP.NET Core Web API
- Entity Framework Core
- SQLite
- Swagger/OpenAPI
