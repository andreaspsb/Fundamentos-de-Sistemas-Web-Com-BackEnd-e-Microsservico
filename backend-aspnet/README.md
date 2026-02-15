# Petshop Backend - ASP.NET Core

> **📌 Arquitetura Multi-Backend:** Este é **um dos 4 backends intercambiáveis** do projeto:
> - **Spring Boot** (porta 8080) - Monolito Java
> - **ASP.NET Core** (porta 5000) - **Monolito C# (este backend)**
> - **C# Azure Functions** (portas 7071-7076) - Microsserviços C#
> - **Java Azure Functions** (portas 7081-7086) - Microsserviços Java
>
> **Todos compartilham o mesmo banco de dados** (Azure SQL em produção, SQLite/H2 em desenvolvimento).
> O frontend pode alternar entre eles dinamicamente usando o sistema de toggle.

## 🚀 Tecnologias

- **.NET 8.0** - Framework multiplataforma da Microsoft
- **ASP.NET Core Web API** - Framework para criar APIs REST
- **Entity Framework Core** - ORM para acesso ao banco de dados
- **SQLite** - Banco de dados baseado em arquivo
- **BCrypt.NET** - Biblioteca para hash de senhas
- **Swagger/OpenAPI** - Documentação interativa da API

## 📦 Dependências Incluídas

- **Microsoft.EntityFrameworkCore.Sqlite** (8.0.11) - Provedor SQLite para EF Core
- **Microsoft.EntityFrameworkCore.Design** (8.0.11) - Ferramentas de design do EF Core
- **Swashbuckle.AspNetCore** - Geração automática de documentação Swagger
- **BCrypt.Net-Next** (4.0.3) - Hashing de senhas

## 🏗️ Estrutura do Projeto

```
PetshopApi/
├── Controllers/           # Endpoints da API REST
│   ├── AuthController.cs         # Autenticação e registro
│   ├── CategoriasController.cs   # CRUD de categorias
│   ├── ProdutosController.cs     # Gerenciamento de produtos
│   ├── ClientesController.cs     # Gerenciamento de clientes
│   ├── PetsController.cs         # Gerenciamento de pets
│   └── ServicosController.cs     # Gerenciamento de serviços
├── Data/                  # Contexto e inicialização do banco
│   ├── PetshopContext.cs         # DbContext do EF Core
│   └── DataInitializer.cs        # Dados iniciais (seed)
├── DTOs/                  # Data Transfer Objects
│   ├── AuthDTOs.cs               # DTOs de autenticação
│   ├── ProdutoDTOs.cs            # DTOs de produtos
│   ├── ClienteDTOs.cs            # DTOs de clientes
│   ├── PetDTOs.cs                # DTOs de pets
│   └── ServicoDTOs.cs            # DTOs de serviços
├── Models/                # Entidades do banco de dados
│   ├── Cliente.cs
│   ├── Usuario.cs
│   ├── Categoria.cs
│   ├── Produto.cs
│   ├── Pet.cs
│   ├── Servico.cs
│   ├── Agendamento.cs
│   ├── Pedido.cs
│   └── ItemPedido.cs
├── Program.cs             # Configuração e inicialização da aplicação
├── appsettings.json       # Configurações (connection string, etc)
└── petshop.db             # Banco de dados SQLite (gerado automaticamente)
```

## 🔧 Como Executar

### Pré-requisitos
- .NET SDK 8.0 ou superior instalado

### Instalação e Execução

```bash
# Navegar até o diretório do projeto
cd backend-aspnet/PetshopApi

# Restaurar dependências (se necessário)
dotnet restore

# Executar a aplicação
dotnet run
```

A aplicação estará disponível em:
- **API**: http://localhost:5000 (HTTP) ou https://localhost:5001 (HTTPS)
- **Swagger UI**: http://localhost:5000 ou https://localhost:5001

**Portas dos Backends:**
- **Spring Boot**: 8080
- **ASP.NET Core** (este): 5000/5001
- **C# Functions**: 7071-7076 (cada função em uma porta)
- **Java Functions**: 7081-7086 (cada função em uma porta)

## 📚 Endpoints Disponíveis

### Autenticação
- `POST /api/auth/login` - Fazer login
- `POST /api/auth/registrar` - Registrar novo usuário

### Categorias
- `GET /api/categorias` - Listar todas as categorias
- `GET /api/categorias/{id}` - Buscar categoria por ID
- `GET /api/categorias/ativas` - Listar categorias ativas
- `POST /api/categorias` - Criar nova categoria
- `PUT /api/categorias/{id}` - Atualizar categoria
- `DELETE /api/categorias/{id}` - Deletar categoria

### Produtos
- `GET /api/produtos` - Listar todos os produtos
- `GET /api/produtos/disponiveis` - Listar produtos disponíveis (ativos e com estoque)
- `GET /api/produtos/{id}` - Buscar produto por ID
- `GET /api/produtos/categoria/{categoriaId}` - Listar produtos por categoria
- `GET /api/produtos/categoria/{categoriaId}/disponiveis` - Listar produtos disponíveis por categoria
- `GET /api/produtos/buscar?termo={termo}` - Buscar produtos por nome
- `GET /api/produtos/estoque-baixo?quantidade={quantidade}` - Listar produtos com estoque baixo
- `POST /api/produtos` - Criar novo produto
- `PUT /api/produtos/{id}` - Atualizar produto
- `PATCH /api/produtos/{id}/estoque?quantidade={quantidade}` - Atualizar estoque
- `PATCH /api/produtos/{id}/adicionar-estoque?quantidade={quantidade}` - Adicionar ao estoque
- `PATCH /api/produtos/{id}/ativar` - Ativar produto
- `PATCH /api/produtos/{id}/desativar` - Desativar produto
- `DELETE /api/produtos/{id}` - Deletar produto

### Clientes
- `GET /api/clientes` - Listar todos os clientes
- `GET /api/clientes/{id}` - Buscar cliente por ID
- `GET /api/clientes/cpf/{cpf}` - Buscar cliente por CPF
- `POST /api/clientes` - Criar novo cliente
- `PUT /api/clientes/{id}` - Atualizar cliente
- `DELETE /api/clientes/{id}` - Deletar cliente

### Pets
- `GET /api/pets` - Listar todos os pets
- `GET /api/pets/{id}` - Buscar pet por ID
- `GET /api/pets/cliente/{clienteId}` - Listar pets por cliente
- `GET /api/pets/tipo/{tipo}` - Listar pets por tipo (cao, gato, etc)
- `POST /api/pets` - Criar novo pet
- `PUT /api/pets/{id}` - Atualizar pet
- `DELETE /api/pets/{id}` - Deletar pet

### Serviços
- `GET /api/servicos` - Listar todos os serviços
- `GET /api/servicos/ativos` - Listar serviços ativos
- `GET /api/servicos/{id}` - Buscar serviço por ID
- `POST /api/servicos` - Criar novo serviço
- `PUT /api/servicos/{id}` - Atualizar serviço
- `PATCH /api/servicos/{id}/ativar` - Ativar serviço
- `PATCH /api/servicos/{id}/desativar` - Desativar serviço
- `DELETE /api/servicos/{id}` - Deletar serviço

## 💾 Banco de Dados

### Desenvolvimento Local
O projeto usa **SQLite**, um banco de dados baseado em arquivo que não requer instalação de servidor. 

**Características do SQLite:**
- ✅ Armazena dados em arquivo (`petshop.db`)
- ✅ Persistência em disco (diferente do H2 em memória)
- ✅ Ideal para desenvolvimento e aplicações pequenas/médias
- ✅ Não requer configuração de servidor
- ⚠️  Pode ser configurado para rodar em memória, mas por padrão persiste em arquivo

### Produção (Azure)
**Todos os 4 backends compartilham o mesmo Azure SQL Database:**
- **Server**: `petshop-db.database.windows.net`
- **Database**: `petshop-db`
- **Implicação**: Dados criados em um backend são **imediatamente visíveis** em todos os outros
- **Consistência**: Todos os backends usam as mesmas convenções (enums em SCREAMING_SNAKE_CASE)

### Connection String
```json
"ConnectionStrings": {
  "DefaultConnection": "Data Source=petshop.db"
}
```

### Dados Iniciais (Seed Data)

O banco é inicializado automaticamente com dados **idênticos aos outros 3 backends**:

- **3 Categorias**:
  - Rações e Alimentação
  - Higiene e Cuidados
  - Acessórios e Brinquedos

- **6 Produtos**:
  - Ração Premium para Cães Adultos (R$ 150,00)
  - Ração Hipoalergênica para Gatos (R$ 95,00)
  - Kit Xampu e Condicionador para Peles Sensíveis (R$ 65,00)
  - Antipulgas e Carrapatos (R$ 85,00)
  - Kit Coleira e Guia Resistente (R$ 45,00)
  - Cama Ortopédica para Cães (R$ 180,00)

- **3 Serviços**:
  - Banho (R$ 50,00)
  - Tosa (R$ 40,00)
  - Banho + Tosa (R$ 80,00)

- **2 Usuários**:
  - Admin: `username: admin` / `senha: admin123`
  - Cliente: `username: maria.silva` / `senha: senha123`

- **1 Cliente** de exemplo (Maria Silva)
- **1 Pet** de exemplo (Rex - Labrador)

## 🔐 Autenticação

O sistema implementa autenticação simples com:
- Hash de senhas usando BCrypt
- Token baseado em Base64 (para produção, use JWT)
- Roles: `ADMIN` e `CLIENTE`

## 🌐 CORS

CORS está configurado para permitir todas as origens durante o desenvolvimento:
```csharp
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});
```

## 🛠️ Comandos Úteis

```bash
# Compilar o projeto
dotnet build

# Executar testes (quando implementados)
dotnet test

# Criar um pacote de publicação
dotnet publish -c Release

# Limpar arquivos de build
dotnet clean

# Adicionar nova migração do EF Core (se necessário)
dotnet ef migrations add NomeDaMigracao

# Aplicar migrações ao banco
dotnet ef database update
```

## 📝 Notas de Desenvolvimento

### Diferenças entre SQLite e H2

| Característica | SQLite | H2 (usado no Spring Boot) |
|----------------|---------|---------------------------|
| Tipo | Arquivo no disco | Pode ser memória ou arquivo |
| Padrão | Persistente | Em memória por padrão |
| Configuração | `Data Source=arquivo.db` | `jdbc:h2:mem:database` |
| Console Admin | Não tem | Tem console web integrado |

### Entity Framework Core

O projeto usa EF Core com abordagem **Code First**:
- Modelos são definidos em C#
- Banco é criado automaticamente
- Relacionamentos são configurados via fluent API

### JSON Serialization

Configurado para:
- Ignorar ciclos de referência
- Ignorar valores nulos
- Converter enums para string

## 🚧 Próximos Passos

- [ ] Implementar controllers para Agendamentos e Pedidos
- [ ] Adicionar autenticação JWT real
- [ ] Implementar validações customizadas
- [ ] Implementar paginação nos endpoints de listagem
- [ ] Adicionar filtros e ordenação avançada
- [ ] Implementar upload de imagens
- [ ] Adicionar testes unitários e de integração
- [ ] Implementar logging estruturado

## 📖 Documentação

Acesse o Swagger UI em http://localhost:5000 para documentação interativa e teste dos endpoints.

## 🤝 Compatibilidade com Frontend

Esta API implementa **exatamente o mesmo contrato de API** dos outros 3 backends:

| Característica | Spring Boot | ASP.NET Core | C# Functions | Java Functions |
|----------------|-------------|--------------|--------------|----------------|
| Endpoints | ✅ | ✅ (este) | ✅ | ✅ |
| DTOs (camelCase JSON) | ✅ | ✅ (este) | ✅ | ✅ |
| Enum Convention | SCREAMING_SNAKE_CASE | SCREAMING_SNAKE_CASE | SCREAMING_SNAKE_CASE | SCREAMING_SNAKE_CASE |
| Banco de Dados | Compartilhado | Compartilhado | Compartilhado | Compartilhado |
| Swagger/OpenAPI | ✅ | ✅ (este) | ❌ | ❌ |
| Arquitetura | Monolito | Monolito (este) | Microsserviços | Microsserviços |

**Frontend Toggle System:**
O frontend pode alternar dinamicamente entre os 4 backends usando o sistema de toggle visual.
Todas as requisições funcionam identicamente, independente do backend escolhido.

## 📄 Licença

Este projeto é parte de um material educacional sobre desenvolvimento web full stack.
