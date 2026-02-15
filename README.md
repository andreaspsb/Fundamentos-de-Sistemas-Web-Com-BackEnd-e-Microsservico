# Fundamentos de Sistemas Web - Pet Shop (Full Stack)

Sistema completo de e-commerce e gerenciamento para Pet Shop, desenvolvido com **quatro backends intercambiáveis** (Spring Boot, ASP.NET Core, C# Azure Functions e Java Azure Functions), **Frontend Web** (HTML5, CSS3, Bootstrap 5, JavaScript) e **App Mobile** (React Native/Expo) com sistema de toggle para alternar entre backends dinamicamente.

## 📋 Descrição do Projeto

Este projeto consiste em um **sistema full-stack** completo para Pet Shop, incluindo:
- 🛒 **E-commerce** - Catálogo de produtos, carrinho de compras, checkout e pedidos
- 📅 **Agendamento** - Sistema de agendamento de serviços (banho e tosa)
- 👤 **Autenticação** - Login/logout com BCrypt e JWT
- 🔐 **Autorização** - Sistema de roles (ADMIN, CLIENTE)
- 🛠️ **Painel Admin** - CRUD completo de produtos, clientes, agendamentos e pedidos
- 📦 **Multi-Backend (4 opções)** - Spring Boot (Java Monolito), ASP.NET Core (C#/.NET Monolito), C# Azure Functions (Microsserviços), Java Azure Functions (Microsserviços)
- 🔄 **Backend Toggle** - Sistema de alternância dinâmica entre os 4 backends
- 📱 **App Mobile** - React Native/Expo com suporte a Android, iOS e Web
- 💾 **Banco de Dados** - H2 (Spring Boot), SQLite (ASP.NET Core), SQL Server (Azure Functions), Azure SQL Database (Produção)

### 🗄️ Arquitetura de Banco de Dados Compartilhado

**Conceito Fundamental:** Todos os 4 backends conectam ao **MESMO banco de dados**. Esta é uma característica arquitetural crítica do projeto.

#### Implicações:
- 📊 **Schema único** - Mudanças no schema afetam todos os backends simultaneamente
- 🔄 **Dados compartilhados** - Dados criados por um backend são imediatamente visíveis aos outros
- 📝 **EF Core como fonte de verdade** - Migrations em `backend-aspnet/` definem o schema canônico
- 📜 **Scripts SQL** - Migrations exportadas para `scripts/` são aplicadas aos outros backends
- ⚖️ **Consistência obrigatória** - Enums, DTOs e contratos de API devem ser idênticos

#### Bancos por Ambiente:
- **Produção**: Azure SQL Database (`petshop-db.database.windows.net`)
- **Desenvolvimento**: 
  - Spring Boot: H2 (in-memory)
  - ASP.NET Core: SQLite (arquivo local)
  - Azure Functions (C# e Java): SQL Server via Docker

> 💡 **Exemplo prático:** Se você criar um produto usando o backend Spring Boot, esse produto estará imediatamente disponível ao buscar via ASP.NET Core ou Azure Functions, pois todos compartilham o mesmo banco.

## 🎯 Funcionalidades

### 🏠 Frontend (Cliente)

#### Página Principal (`index.html`)
- Header com gradiente animado
- Navbar responsiva com menu collapse para mobile
- **Carrossel automático** de promoções com 3 slides
- 4 cards de categorias com efeitos hover
- Footer completo com informações de contato

#### Autenticação e Autorização
- **Login** (`login.html`) - Autenticação com BCrypt e tokens (24h de validade)
- **Cadastro** (`cadastro.html`) - Registro de cliente + pet + usuário (obrigatório)
- **Logout** - Limpeza de sessão e redirecionamento
- **Navbar dinâmica** - Mostra "🔐 Entrar" ou dropdown do usuário
- **Proteção de rotas** - Páginas protegidas redirecionam para login

#### Carrinho de Compras (`carrinho.html`)
- ✅ Visualização de produtos com imagem, quantidade e preço
- ✅ Ajustar quantidade (validação de estoque)
- ✅ Remover itens individuais
- ✅ Limpar carrinho completo
- ✅ Contador no navbar
- ✅ Resumo com subtotal e total
- ✅ Persistência em localStorage

#### Checkout (`checkout.html`)
- ✅ Formulário de endereço e telefone
- ✅ Seleção de forma de pagamento (PIX, Cartão, Boleto)
- ✅ Campo para observações
- ✅ Resumo do pedido
- ✅ Criação de pedido via API (integração com backend)
- ✅ Modal de sucesso após pedido
- ✅ Limpa carrinho automaticamente

#### Meus Pedidos (`meus-pedidos.html`)
- ✅ Lista de todos os pedidos do cliente
- ✅ Cards com: número, data, status, valor, forma de pagamento
- ✅ Modal com detalhes completos do pedido
- ✅ Tabela de itens do pedido
- ✅ Status coloridos (Pendente, Confirmado, Em Preparação, Enviado, Entregue, Cancelado)

### Categorias de Produtos

O sistema apresenta **3 categorias de produtos** integradas com a API:

#### 1. Rações e Alimentação (`/categorias/racoes-alimentacao/`)
- Carregamento dinâmico de produtos via API
- Botão "Adicionar ao Carrinho" com validação de estoque
- Imagens, preços e descrições do banco de dados

#### 2. Acessórios e Brinquedos (`/categorias/acessorios-brinquedos/`)
- Mesma estrutura da categoria anterior
- Integração completa com CarrinhoManager

#### 3. Higiene e Cuidados (`/categorias/higiene-cuidados/`)
- Listagem de produtos de higiene
- Sistema de carrinho integrado

### Serviços e Agendamento (`/servicos/`)

O pet shop oferece serviços de banho e tosa com **sistema de agendamento online integrado à API**:

#### Serviços Disponíveis
- **Banho** - R$ 50,00
- **Tosa** - R$ 40,00
- **Combo Banho + Tosa** - R$ 80,00

#### Agendamento Online (`/servicos/agendamento.html`)
- ✅ Formulário completo de agendamento
- ✅ Seleção interativa de serviços (cards clicáveis)
- ✅ Validação de data (não permite domingos)
- ✅ Validação de horário (8h às 18h)
- ✅ Campos para dados do cliente e pet
- ✅ Integração com API - criação de agendamento no banco
- ✅ **Requer autenticação** - protegido com AuthManager
- ✅ Confirmação com modal de sucesso

### 🛠️ Painel Administrativo (`/admin/`)

Painel completo para gerenciamento do sistema (requer role ADMIN):

#### Dashboard (`/admin/index.html`)
- 📊 Estatísticas gerais (clientes, pets, agendamentos, pedidos)
- 📈 Cards de gerenciamento com links para CRUDs
- 🎨 Interface moderna com gradiente purple

#### CRUD de Produtos (`/admin/produtos.html`)
- ✅ Listagem completa de produtos
- ✅ Modal para criar/editar produtos
- ✅ Campos: nome, descrição, preço, estoque, imagem, categoria, ativo
- ✅ Exclusão de produtos
- ✅ Integração com API
- ✅ Carregamento de categorias do banco

#### CRUD de Clientes (`/admin/clientes.html`)
- ✅ Listagem de clientes cadastrados
- ✅ Visualização de: nome, email, telefone, CPF
- ✅ Exclusão de clientes

#### CRUD de Agendamentos (`/admin/agendamentos.html`)
- ✅ Listagem de agendamentos
- ✅ Visualização de: cliente, pet, serviço, data, status
- ✅ Exclusão de agendamentos

#### CRUD de Pedidos (`/admin/pedidos.html`)
- ✅ Listagem de pedidos
- ✅ Visualização de: cliente, data, status, total
- ✅ Exclusão de pedidos

### 📱 App Mobile (`/mobile/`)

Aplicativo mobile desenvolvido com React Native e Expo:

#### Funcionalidades
- ✅ Login e Cadastro de usuário
- ✅ Navegação por categorias de produtos
- ✅ Busca de produtos
- ✅ Detalhes do produto com imagem
- ✅ Carrinho de compras persistente
- ✅ Checkout com múltiplas formas de pagamento
- ✅ Histórico de pedidos
- ✅ Agendamento de serviços (banho, tosa)
- ✅ Perfil do usuário

#### Tecnologias
- **React Native** com Expo
- **TypeScript** para tipagem
- **React Navigation** para navegação
- **React Native Paper** (Material Design)
- **Axios** para requisições HTTP
- **Expo SecureStore** para armazenamento seguro

### 🔧 Backend (Spring Boot)

#### Entidades (9 totais)
1. **Usuario** - Autenticação (username, senha hash BCrypt, email, role, ativo)
2. **Cliente** - Dados do cliente (nome, CPF, telefone, email, dataNascimento, sexo, endereço)
3. **Pet** - Dados do pet (nome, tipo, raça, idade, peso, sexo, castrado, observações)
4. **Categoria** - Categorias de produtos (nome, descrição, ativo)
5. **Produto** - Produtos da loja (nome, descrição, preço, estoque, urlImagem, categoria, ativo)
6. **Servico** - Serviços oferecidos (nome, descrição, preço, ativo)
7. **Agendamento** - Agendamentos de serviços (cliente, pet, servico, dataHora, metodoEntrega, observações, status)
8. **Pedido** - Pedidos de compra (cliente, dataPedido, status, formaPagamento, valorTotal, observações)
9. **ItemPedido** - Itens do pedido (pedido, produto, quantidade, precoUnitario)

#### API REST Endpoints

**Autenticação (`/api/auth`)**
- `POST /login` - Login (retorna token)
- `POST /registrar` - Registro de usuário
- `GET /validar-token` - Validação de token
- `POST /logout` - Logout

**Clientes (`/api/clientes`)**
- `GET /` - Listar todos
- `GET /{id}` - Buscar por ID
- `POST /` - Criar
- `PUT /{id}` - Atualizar
- `DELETE /{id}` - Excluir

**Produtos (`/api/produtos`)**
- `GET /` - Listar todos
- `GET /{id}` - Buscar por ID
- `GET /categoria/{categoriaId}` - Buscar por categoria
- `POST /` - Criar
- `PUT /{id}` - Atualizar
- `DELETE /{id}` - Excluir

**Pedidos (`/api/pedidos`)**
- `GET /` - Listar todos
- `GET /{id}` - Buscar por ID
- `GET /cliente/{clienteId}` - Buscar por cliente
- `POST /` - Criar pedido
- `POST /{pedidoId}/itens` - Adicionar item
- `DELETE /{pedidoId}/itens/{itemId}` - Remover item
- `POST /{id}/confirmar` - Confirmar pedido
- `PATCH /{id}/status` - Atualizar status
- `POST /{id}/cancelar` - Cancelar pedido

**Agendamentos (`/api/agendamentos`)**
- `GET /` - Listar todos
- `GET /{id}` - Buscar por ID
- `GET /cliente/{clienteId}` - Buscar por cliente
- `POST /` - Criar
- `PUT /{id}` - Atualizar
- `DELETE /{id}` - Excluir

**Categorias (`/api/categorias`)**
- `GET /` - Listar todas
- `GET /{id}` - Buscar por ID

**Serviços (`/api/servicos`)**
- `GET /` - Listar todos
- `GET /{id}` - Buscar por ID

#### Dados Iniciais (DataInitializer)

O sistema cria automaticamente ao iniciar:
- ✅ **3 Categorias** (Rações, Higiene, Acessórios)
- ✅ **6 Produtos** (2 por categoria com estoque)
- ✅ **3 Serviços** (Banho, Tosa, Combo)
- ✅ **1 Usuário Admin** (admin/admin123 com senha hash BCrypt)

### 🔧 Backend (ASP.NET Core)

Backend monolítico em C#/.NET com estrutura similar ao Spring Boot. Veja documentação completa em [backend-aspnet/README.md](backend-aspnet/README.md).

**Características:**
- ✅ Entity Framework Core com SQLite
- ✅ Swagger/OpenAPI integrado
- ✅ Mesmas 9 entidades e endpoints do Spring Boot
- ✅ BCrypt.NET para hash de senhas
- ✅ Migrations como fonte de verdade do schema

**URL Base:** http://localhost:5000/api

### ⚡ Backend (Azure Functions - Microsserviços C#)

Arquitetura de microsserviços usando Azure Functions HTTP Triggers. Cada serviço é independente e possui seu próprio endpoint. Veja documentação completa em [functions/README.md](functions/README.md).

#### Serviços e Portas

| Serviço | Porta | Responsabilidades |
|---------|-------|-------------------|
| **Auth** | 7071 | Login, JWT, validação de token, gestão de usuários |
| **Customers** | 7072 | CRUD de clientes, busca por CPF |
| **Pets** | 7073 | CRUD de pets, listagem por cliente |
| **Catalog** | 7074 | Produtos, categorias, serviços, gestão de estoque |
| **Scheduling** | 7075 | Agendamentos, disponibilidade, confirmações |
| **Orders** | 7076 | Pedidos, itens de pedido, checkout |

#### Características Técnicas
- ✅ **Azure Functions v4** - Serverless compute platform
- ✅ **HTTP Triggers** - REST API endpoints
- ✅ **Timer Triggers** - Jobs agendados (limpeza, notificações)
- ✅ **Polly** - Resilience policies (retry, circuit breaker, timeout)
- ✅ **SQL Server** - Banco compartilhado via Docker
- ✅ **Shared Library** - `Petshop.Shared` com models, DTOs e utils

#### Como Iniciar
```bash
cd functions
./start-all.sh  # Linux/Mac
# ou
./start-all.ps1 # Windows PowerShell
```

**URLs Base:** 
- Auth: http://localhost:7071/api
- Customers: http://localhost:7072/api
- Pets: http://localhost:7073/api
- Catalog: http://localhost:7074/api
- Scheduling: http://localhost:7075/api
- Orders: http://localhost:7076/api

### ⚡ Backend (Azure Functions - Microsserviços Java)

Implementação Java dos microsserviços com estrutura idêntica às C# Functions. Veja documentação completa em [functions-java/README.md](functions-java/README.md).

#### Serviços e Portas

| Serviço | Porta | Responsabilidades |
|---------|-------|-------------------|
| **Auth (Java)** | 7081 | Login, JWT, validação de token, gestão de usuários |
| **Customers (Java)** | 7082 | CRUD de clientes, busca por CPF |
| **Pets (Java)** | 7083 | CRUD de pets, listagem por cliente |
| **Catalog (Java)** | 7084 | Produtos, categorias, serviços, gestão de estoque |
| **Scheduling (Java)** | 7085 | Agendamentos, disponibilidade, confirmações |
| **Orders (Java)** | 7086 | Pedidos, itens de pedido, checkout |

#### Características Técnicas
- ✅ **Azure Functions Java SDK** - Java 17+
- ✅ **Maven** - Build e gerenciamento de dependências
- ✅ **Resilience4j** - Padrões de resiliência
- ✅ **SQL Server** - Banco compartilhado via Docker
- ✅ **Shared Module** - `petshop-functions-shared` com models e utils

#### Como Iniciar
```bash
cd functions-java
./start-all-java.sh  # Linux/Mac
# ou
./start-all-java.ps1 # Windows PowerShell
```

**URLs Base:**
- Auth: http://localhost:7081/api
- Customers: http://localhost:7082/api
- Pets: http://localhost:7083/api
- Catalog: http://localhost:7084/api
- Scheduling: http://localhost:7085/api
- Orders: http://localhost:7086/api

> 💡 **Dica:** Todos os 4 backends podem ser executados simultaneamente! Use o sistema de toggle no frontend para alternar entre eles em tempo real.

## 📁 Estrutura do Projeto

```
/
├── frontend/                            # 🎨 Frontend (Cliente)
│   ├── admin/                           # Painel administrativo
│   │   ├── index.html                   # Dashboard admin
│   │   ├── produtos.html                # CRUD produtos
│   │   ├── clientes.html                # Gestão clientes
│   │   ├── agendamentos.html            # Gestão agendamentos
│   │   └── pedidos.html                 # Gestão pedidos
│   ├── categorias/                      # Páginas de categorias
│   │   ├── racoes-alimentacao/
│   │   ├── acessorios-brinquedos/
│   │   └── higiene-cuidados/
│   ├── servicos/                        # Serviços e agendamento
│   │   ├── index.html                   # Lista de serviços
│   │   └── agendamento.html             # Formulário de agendamento
│   ├── css/
│   │   └── style.css                    # Estilos customizados
│   ├── js/
│   │   ├── api-config.js                # Configuração da API
│   │   ├── auth.js                      # AuthManager (autenticação)
│   │   ├── carrinho.js                  # CarrinhoManager (carrinho)
│   │   ├── produtos.js                  # Gestão de produtos
│   │   ├── cadastro.js                  # Lógica de cadastro
│   │   ├── agendamento.js               # Lógica de agendamento
│   │   └── script.js                    # Scripts gerais
│   ├── index.html                       # Página inicial
│   ├── login.html                       # Página de login
│   ├── cadastro.html                    # Cadastro de cliente
│   ├── carrinho.html                    # Carrinho de compras
│   ├── checkout.html                    # Finalização de compra
│   └── meus-pedidos.html                # Histórico de pedidos
│
├── backend-springboot/                  # ☕ Backend (API REST)
│   ├── src/
│   │   └── main/
│   │       ├── java/com/petshop/
│   │       │   ├── PetshopApplication.java
│   │       │   ├── config/              # Configurações (CORS, DataInitializer)
│   │       │   ├── controller/          # Controllers REST (7)
│   │       │   ├── dto/                 # Data Transfer Objects (6+)
│   │       │   ├── model/               # Entidades JPA (9)
│   │       │   ├── repository/          # Repositórios Spring Data (9)
│   │       │   └── service/             # Lógica de negócio (5)
│   │       └── resources/
│   │           └── application.properties
│   ├── pom.xml                          # Maven dependencies
│   ├── README.md                        # 📚 Documentação completa da API
│   └── target/                          # Build artifacts (ignorado)
│
├── docs/                                # 📖 Documentação
│   ├── ACESSIBILIDADE.md                # Conformidade WCAG 2.1
│   ├── GUIA_CSS_BOOTSTRAP.md            # Guia de estilos
│   ├── GUIA_JAVASCRIPT.md               # Guia de JavaScript
│   └── INTEGRACAO_FRONTEND_BACKEND.md   # Guia de integração
│
├── tests/                               # 🧪 Testes
│   ├── teste-backend.html               # Teste de integração com API
│   └── README.md                        # Documentação de testes
│
├── mobile/                              # 📱 App Mobile (React Native/Expo)
│   ├── App.tsx                          # Componente raiz
│   ├── src/
│   │   ├── screens/                     # Telas do app
│   │   ├── components/                  # Componentes reutilizáveis
│   │   ├── contexts/                    # Contextos (Auth, Cart)
│   │   ├── services/                    # API e Storage
│   │   └── navigation/                  # Configuração de rotas
│   ├── Dockerfile                       # Build para Docker
│   └── README.md                        # Documentação do mobile
│
├── .gitignore                           # Arquivos ignorados pelo Git
└── README.md                            # 📄 Este arquivo
    │       │   ├── repository/
    │       │   │   ├── UsuarioRepository.java
    │       │   │   ├── ClienteRepository.java
    │       │   │   ├── PetRepository.java
    │       │   │   ├── CategoriaRepository.java
    │       │   │   ├── ProdutoRepository.java
    │       │   │   ├── ServicoRepository.java
    │       │   │   ├── AgendamentoRepository.java
    │       │   │   └── PedidoRepository.java
    │       │   ├── service/
    │       │   │   ├── AuthService.java
    │       │   │   ├── ClienteService.java
    │       │   │   ├── ProdutoService.java
    │       │   │   ├── PedidoService.java
    │       │   │   └── AgendamentoService.java
    │       │   └── dto/
    │       │       ├── LoginDTO.java
    │       │       ├── PedidoRequestDTO.java
    │       │       ├── PedidoResponseDTO.java
    │       │       ├── ItemPedidoRequestDTO.java
    │       │       └── ItemPedidoDTO.java
    │       └── resources/
    │           └── application.properties
    └── target/
```

## 🚀 Como Executar

### Pré-requisitos

**Para Backend Spring Boot:**
- **Java 21** (OpenJDK ou Oracle JDK)
- **Maven 3.8+**

**Para Backend ASP.NET Core:**
- **.NET SDK 8.0+**

**Para Frontend Web:**
- **Navegador moderno** (Chrome, Firefox, Edge)
- **Live Server** (VS Code) ou servidor HTTP local

**Para App Mobile:**
- **Node.js 18+**
- **Expo CLI** (`npm install -g expo-cli`)
- **Expo Go** (app no celular) ou emulador

**Para Docker (Recomendado):**
- **Docker** e **Docker Compose**

### 🐳 Executar com Docker (Recomendado)

```bash
# Subir todos os serviços
docker-compose up -d

# Serviços disponíveis:
# - Frontend Web:    http://localhost:80
# - App Mobile Web:  http://localhost:8081
# - Backend Spring:  http://localhost:8080
# - Backend ASP.NET: http://localhost:5000
# - PostgreSQL:      localhost:5432

# Ver logs
docker-compose logs -f

# Parar serviços
docker-compose down
```

### 1. Iniciar um dos Backends (ou todos simultaneamente)

#### Opção A: Backend Spring Boot (Monolito Java)

```bash
# Navegar para o diretório do backend
cd backend-springboot

# Compilar o projeto
mvn clean compile

# Executar o servidor Spring Boot
mvn spring-boot:run
```

**Disponível em:** http://localhost:8080  
**Swagger UI:** http://localhost:8080/swagger-ui.html  
**H2 Console:** http://localhost:8080/h2-console

#### Opção B: Backend ASP.NET Core (Monolito C#)

```bash
# Navegar para o diretório do projeto ASP.NET
cd backend-aspnet/PetshopApi

# Restaurar dependências (primeira vez)
dotnet restore

# Executar o servidor ASP.NET Core
dotnet run
```

**Disponível em:** http://localhost:5000  
**Swagger UI:** http://localhost:5000

#### Opção C: Azure Functions C# (Microsserviços)

```bash
# Navegar para o diretório das functions
cd functions

# Linux/Mac
./start-all.sh

# Windows PowerShell
./start-all.ps1
```

**Serviços disponíveis:**
- Auth: http://localhost:7071/api
- Customers: http://localhost:7072/api
- Pets: http://localhost:7073/api
- Catalog: http://localhost:7074/api
- Scheduling: http://localhost:7075/api
- Orders: http://localhost:7076/api

**Logs:** Verifique a pasta `functions/logs/` para logs de cada serviço

#### Opção D: Azure Functions Java (Microsserviços)

```bash
# Navegar para o diretório das functions Java
cd functions-java

# Linux/Mac
./start-all-java.sh

# Windows PowerShell
./start-all-java.ps1
```

**Serviços disponíveis:**
- Auth: http://localhost:7081/api
- Customers: http://localhost:7082/api
- Pets: http://localhost:7083/api
- Catalog: http://localhost:7084/api
- Scheduling: http://localhost:7085/api
- Orders: http://localhost:7086/api

**Logs:** Verifique a pasta `java-func-logs/` para logs de cada serviço

> **💡 Dica:** Você pode executar **todos os 4 backends simultaneamente** (portas diferentes) e usar o sistema de toggle no frontend para alternar entre eles em tempo real!

### 2. Iniciar o Frontend

#### Opção A: Live Server (VS Code) - Recomendado
1. Instale a extensão "Live Server" no VS Code
2. Abra a pasta `frontend/` no VS Code
3. Clique com botão direito em `index.html`
4. Selecione "Open with Live Server"

#### Opção B: Servidor HTTP Python
```bash
# Na pasta frontend
cd frontend
python3 -m http.server 5500
```
Acesse: **http://localhost:5500**

#### Opção C: Abrir diretamente
- Navegue até `frontend/` e abra `index.html` no navegador
- **Nota:** Algumas funcionalidades podem não funcionar devido a restrições CORS

### 3. Acessar o Sistema

**Frontend:** http://localhost:5500 (Live Server) ou http://localhost:5500 (Python)  

**Backend Spring Boot:**
- API: http://localhost:8080  
- Swagger UI: http://localhost:8080/swagger-ui.html  
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:petshopdb`
  - Username: `sa`
  - Password: (deixar em branco)

**Backend ASP.NET Core:**
- API: http://localhost:5000
- Swagger UI: http://localhost:5000

**Azure Functions C#:**
- Auth: http://localhost:7071/api
- Customers: http://localhost:7072/api
- Pets: http://localhost:7073/api
- Catalog: http://localhost:7074/api
- Scheduling: http://localhost:7075/api
- Orders: http://localhost:7076/api

**Azure Functions Java:**
- Auth: http://localhost:7081/api
- Customers: http://localhost:7082/api
- Pets: http://localhost:7083/api
- Catalog: http://localhost:7084/api
- Scheduling: http://localhost:7085/api
- Orders: http://localhost:7086/api

### 4. Sistema de Toggle entre Backends

O frontend possui um **toggle visual** que permite alternar dinamicamente entre os 4 backends disponíveis:

- 🟢 **Spring Boot** (http://localhost:8080/api) - Monolito Java
- 🟣 **ASP.NET Core** (http://localhost:5000/api) - Monolito C#
- 🔵 **Azure Functions C#** (http://localhost:707X/api) - Microsserviços C#
- 🟡 **Azure Functions Java** (http://localhost:708X/api) - Microsserviços Java

**Como usar:**
1. Inicie um ou mais backends (podem ser todos simultaneamente)
2. Abra o frontend no navegador
3. Clique no botão do backend desejado no toggle (canto superior direito)
4. A escolha é salva automaticamente no localStorage
5. Todas as requisições serão direcionadas ao backend selecionado

**Configuração Técnica:**
- Arquivo: `frontend/js/api-config.js`
- Valores possíveis: `SPRINGBOOT`, `ASPNET`, `FUNCTIONS`, `FUNCTIONS_JAVA`
- Armazenamento: `localStorage.getItem('backend-selecionado')`
- Normalização automática: Converte PascalCase → camelCase

**Documentação completa:** Veja [BACKEND_TOGGLE_README.md](frontend/BACKEND_TOGGLE_README.md)

### 5. Credenciais Padrão

**Admin:**
- Username: `admin`
- Senha: `admin123`

**Nota:** Para acessar o painel admin, faça login com essas credenciais.

## 🛠️ Tecnologias Utilizadas

### Backend Spring Boot
- **Java 21** - Linguagem de programação
- **Spring Boot 3.2.0** - Framework Java
- **Spring Data JPA** - Persistência de dados
- **H2 Database** - Banco de dados em memória
- **Spring Security Crypto** - BCrypt para hash de senhas
- **Swagger/OpenAPI** - Documentação automática da API
- **Maven** - Gerenciamento de dependências
- **Lombok** - Redução de boilerplate

### Backend ASP.NET Core
- **.NET 8.0** - Framework multiplataforma da Microsoft
- **ASP.NET Core Web API** - Framework para criar APIs REST
- **Entity Framework Core** - ORM para acesso ao banco de dados
- **SQLite** - Banco de dados baseado em arquivo
- **BCrypt.NET** - Biblioteca para hash de senhas
- **Swagger/OpenAPI** - Documentação interativa da API

### Azure Functions (C# e Java)
- **Azure Functions v4** - Plataforma serverless compute
- **HTTP Triggers** - Endpoints REST API
- **Timer Triggers** - Jobs agendados (limpeza, notificações)
- **Polly** (C#) - Resilience policies (retry, circuit breaker, timeout)
- **Resilience4j** (Java) - Padrões de resiliência
- **SQL Server** - Banco compartilhado via Docker
- **Maven** (Java) - Build e gerenciamento de dependências
- **Shared Libraries** - Módulos compartilhados com models, DTOs e utils

### Frontend
- **HTML5** - Estrutura semântica das páginas
- **CSS3** - Estilos customizados, animações e transições
- **Bootstrap 5.3.0** - Framework CSS responsivo (via CDN)
- **JavaScript (Vanilla)** - Interatividade e integração com API
- **Bootstrap Icons** - Ícones
- **Unsplash** - Imagens externas de alta qualidade

### Arquitetura
- **API REST** - Comunicação cliente-servidor
- **SPA parcial** - JavaScript carrega dados dinamicamente
- **localStorage** - Persistência de carrinho e autenticação no client-side
- **CORS habilitado** - Permite chamadas cross-origin
- **DTO Pattern** - Separação entre entidades e dados da API
- **Multi-Backend** - 4 backends intercambiáveis com contrato de API idêntico
- **Banco Compartilhado** - Todos os backends conectam ao mesmo banco de dados
- **Microsserviços** - Arquitetura distribuída com Azure Functions

### App Mobile
- **React Native** - Framework para apps nativos
- **Expo** - Plataforma para desenvolvimento React Native
- **TypeScript** - Tipagem estática para JavaScript
- **React Navigation** - Navegação entre telas
- **React Native Paper** - Material Design components
- **Axios** - Cliente HTTP
- **Expo SecureStore** - Armazenamento seguro de tokens

## 📝 Características Técnicas

### Backend (Spring Boot)

#### Segurança
- ✅ **BCrypt** - Hash de senhas com força 10
- ✅ **Tokens** - Autenticação baseada em tokens (Base64: username:timestamp)
- ✅ **Validação de token** - Tokens expiram após 24 horas
- ✅ **Roles** - ADMIN e CLIENTE com permissões diferentes
- ✅ **CORS** - Configuração para aceitar requisições do frontend

#### Persistência
- ✅ **JPA/Hibernate** - ORM para mapeamento objeto-relacional
- ✅ **H2 Database** - Banco em memória para desenvolvimento
- ✅ **Cascade** - Operações em cascata para relacionamentos
- ✅ **Validações** - Bean Validation com anotações (@NotNull, @Size, @Email)

#### API REST
- ✅ **Controllers** - 7 controllers REST
- ✅ **Services** - Camada de negócio separada
- ✅ **Repositories** - Spring Data JPA
- ✅ **DTOs** - Separação de entidades e dados da API
- ✅ **Swagger** - Documentação automática da API
- ✅ **ResponseEntity** - Controle de status HTTP

### Frontend (JavaScript)

#### Autenticação
- ✅ **AuthManager** - Classe centralizada para autenticação
- ✅ **localStorage** - Persistência de token e dados do usuário
- ✅ **Proteção de rotas** - Páginas protegidas redirecionam para login
- ✅ **Navbar dinâmica** - Mostra estado de autenticação
- ✅ **Relative paths** - Navegação funciona em qualquer subdiretório

#### Carrinho de Compras
- ✅ **CarrinhoManager** - Classe centralizada para gestão do carrinho
- ✅ **Validação de estoque** - Impede adicionar mais que o disponível
- ✅ **Contador no navbar** - Badge atualizado em tempo real
- ✅ **Persistência** - Carrinho salvo em localStorage
- ✅ **Toast notifications** - Feedback visual ao usuário

#### Integração com API
- ✅ **Fetch API** - Requisições HTTP assíncronas
- ✅ **async/await** - Código mais limpo
- ✅ **Error handling** - try/catch em todas as chamadas
- ✅ **Loading states** - Spinners durante carregamento
- ✅ **Dynamic rendering** - Conteúdo carregado do backend

### 🎯 Convenções de Código (CRÍTICO)

#### Serialização JSON
Todos os backends utilizam **camelCase** para campos JSON nas respostas da API:

- ✅ ASP.NET: `JsonNamingPolicy.CamelCase` em `Program.cs`
- ✅ Spring Boot: `spring.jackson.property-naming-strategy=LOWER_CAMEL_CASE`
- ✅ Frontend: `normalizeResponse()` em `js/api-config.js` converte PascalCase → camelCase
- ✅ Datas: Formato ISO 8601 (`yyyy-MM-dd` ou `yyyy-MM-ddTHH:mm:ss`)

#### Enums de Status (OBRIGATÓRIO)

**Todos os enums de status usam SCREAMING_SNAKE_CASE** em todos os 4 backends. Esta convenção é **crítica** para garantir consistência ao alternar backends com banco de dados compartilhado.

**StatusPedido:**
- `PENDENTE` - Pedido criado, aguardando confirmação
- `CONFIRMADO` - Pagamento confirmado
- `PROCESSANDO` - Em preparação
- `ENVIADO` - Pedido despachado para entrega
- `ENTREGUE` - Pedido entregue ao cliente
- `CANCELADO` - Pedido cancelado

**StatusAgendamento:**
- `PENDENTE` - Agendamento criado, aguardando confirmação
- `CONFIRMADO` - Agendamento confirmado
- `EM_ANDAMENTO` - Serviço em execução
- `CONCLUIDO` - Serviço finalizado
- `CANCELADO` - Agendamento cancelado

> ⚠️ **Importante:** Qualquer mudança em enums deve ser replicada em **todos os 4 backends** simultaneamente para manter a consistência do banco compartilhado.

## 📊 Estatísticas do Projeto

### Frontend
- **Páginas HTML**: 15+
- **Arquivos JavaScript**: 6+
- **Arquivos CSS**: 1 (+ Bootstrap CDN)
- **Linhas de Código JS**: ~2000+
- **Linhas de CSS**: ~300

### Backend (Total - 4 Implementações)

#### Spring Boot (Monolito Java)
- **Entidades JPA**: 9
- **Controllers REST**: 7
- **Services**: 5
- **Repositories**: 9
- **DTOs**: 6+
- **Endpoints API**: ~40
- **Linhas de Código**: ~3000+

#### ASP.NET Core (Monolito C#)
- **Entidades EF**: 9
- **Controllers REST**: 7
- **DTOs**: 6+
- **Endpoints API**: ~40
- **Linhas de Código**: ~2800+

#### Azure Functions C# (Microsserviços)
- **Serviços**: 6 (Auth, Customers, Pets, Catalog, Scheduling, Orders)
- **HTTP Triggers**: ~40
- **Timer Triggers**: 3+
- **Shared Models**: 9 entidades
- **Linhas de Código**: ~2500+

#### Azure Functions Java (Microsserviços)
- **Serviços**: 6 (Auth, Customers, Pets, Catalog, Scheduling, Orders)
- **HTTP Triggers**: ~40
- **Timer Triggers**: 3+
- **Shared Models**: 9 entidades
- **Linhas de Código**: ~2500+

#### Totais Consolidados
- **Total de Endpoints API**: ~160 (40 × 4 backends)
- **Total de Linhas de Código Backend**: ~11000+
- **Total de Linhas de Código (Full Stack)**: ~13000+

### App Mobile
- **Telas**: 12+
- **Componentes React Native**: 15+
- **Contextos**: 2 (Auth, Cart)
- **Linhas de TypeScript**: ~1500+

### Funcionalidades
- ✅ Sistema de autenticação completo
- ✅ Carrinho de compras funcional
- ✅ Checkout integrado com backend
- ✅ Histórico de pedidos
- ✅ Agendamento de serviços
- ✅ Painel administrativo com CRUDs
- ✅ Validação de estoque
- ✅ Cálculo automático de totais
- ✅ Proteção de rotas por role

## 🔐 Segurança

- ✅ **BCrypt** - Senhas nunca armazenadas em texto plano
- ✅ **Tokens** - Autenticação baseada em tokens com validade
- ✅ **Validação** - Input validation no backend e frontend
- ✅ **CORS** - Configurado corretamente para produção
- ✅ **SQL Injection** - Protegido via JPA/Hibernate
- ✅ **XSS** - Prevenido via escape de HTML no frontend
- ✅ **OWASP Dependency Check** - Scan automatizado de vulnerabilidades
- ✅ **NVD NIST API** - Análise de CVEs com chave de API ([veja configuração](.github/NVD_API_KEY_SETUP.md))

## 🎯 Fluxos Principais

### Fluxo de Compra
1. Cliente navega pelos produtos
2. Adiciona produtos ao carrinho (validação de estoque)
3. Visualiza carrinho e ajusta quantidades
4. Clica em "Finalizar Compra"
5. Sistema verifica autenticação (redireciona para login se necessário)
6. Preenche dados de entrega e pagamento
7. Confirma pedido
8. Backend cria pedido e itens
9. Modal de sucesso exibido
10. Carrinho limpo automaticamente
11. Cliente pode ver pedido em "Meus Pedidos"

### Fluxo de Cadastro
1. Cliente acessa página de cadastro
2. Preenche dados pessoais e do pet
3. Sistema valida e cria cliente + pet
4. Solicita criação de usuário (OBRIGATÓRIO)
5. Auto-sugere username baseado no nome
6. Valida senha (mínimo 6 caracteres)
7. Backend cria usuário com senha hash BCrypt
8. Redireciona para login
9. Cliente faz login e pode usar o sistema

### Fluxo de Agendamento
1. Cliente acessa página de serviços
2. Clica em "Agendar" ou acessa diretamente
3. Sistema verifica autenticação
4. Seleciona serviço(s), data, horário
5. Preenche dados do pet
6. Backend valida e cria agendamento
7. Confirmação exibida

## 📚 Documentação Adicional

### 📖 Guias Disponíveis
- **[docs/ACESSIBILIDADE.md](docs/ACESSIBILIDADE.md)** - Conformidade WCAG 2.1
- **[docs/GUIA_CSS_BOOTSTRAP.md](docs/GUIA_CSS_BOOTSTRAP.md)** - Guia de estilos e componentes
- **[docs/GUIA_JAVASCRIPT.md](docs/GUIA_JAVASCRIPT.md)** - Guia de JavaScript e boas práticas
- **[docs/INTEGRACAO_FRONTEND_BACKEND.md](docs/INTEGRACAO_FRONTEND_BACKEND.md)** - Guia de integração
- **[docs/DEPLOY.md](docs/DEPLOY.md)** - 🚀 Guia completo de deploy em produção
- **[backend-springboot/README.md](backend-springboot/README.md)** - Documentação completa da API
- **[tests/README.md](tests/README.md)** - Guia de testes e validações

### 🔗 Recursos Online
- **Swagger UI** - http://localhost:8080/swagger-ui.html (API interativa)
- **H2 Console** - http://localhost:8080/h2-console (Banco de dados)

## 🚧 Melhorias Futuras

### Backend
- [ ] Implementar JWT (JSON Web Tokens)
- [ ] Implementar refresh tokens
- [ ] Migrar para PostgreSQL em produção
- [ ] Adicionar paginação nas listagens
- [ ] Implementar filtros avançados
- [ ] Upload de imagens de produtos
- [ ] Notificações por email
- [ ] Relatórios e dashboards
- [ ] Testes unitários e integração
- [ ] Docker e Docker Compose
- [ ] CI/CD com GitHub Actions

### Frontend
- [ ] Adicionar filtros e busca nas páginas admin
- [ ] Gráficos no dashboard admin (Chart.js)
- [ ] Botões de alteração de status (pedidos/agendamentos)
- [ ] Sistema de avaliações de produtos
- [ ] Lista de desejos (wishlist)
- [ ] Histórico de navegação
- [ ] Comparador de produtos
- [ ] Chat de suporte

### Infraestrutura
- [ ] Deploy em cloud (ver [docs/DEPLOY.md](docs/DEPLOY.md))
- [ ] Monitoramento e logs (Sentry, New Relic)
- [ ] Cache com Redis
- [ ] CDN para imagens
- [ ] Backup automatizado
- [ ] Arquitetura de microsserviços

## 📄 Licença

Projeto educacional - Fundamentos de Sistemas Web - PUCRS Online

---

**Desenvolvido por:** Andreas Paulus Scherdien Berwaldt  
**Instituição:** PUCRS Online  
**Disciplina:** Fundamentos de Sistemas Web  
**Data:** Novembro de 2025
