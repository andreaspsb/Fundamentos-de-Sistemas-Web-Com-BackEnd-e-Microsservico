# ⚡ Guia de Início Rápido - Pet Shop Full Stack

## 🚀 3 Passos para Rodar o Projeto

### Passo 1: Escolha seu Backend (4 Opções)

#### Opção A: Spring Boot (Monolito Java) 🟢

```bash
cd backend-springboot
mvn spring-boot:run
```

✅ **Disponível em:** http://localhost:8080  
✅ **Swagger:** http://localhost:8080/swagger-ui.html

#### Opção B: ASP.NET Core (Monolito C#) 🟣

```bash
cd backend-aspnet/PetshopApi
dotnet run
```

✅ **Disponível em:** http://localhost:5000  
✅ **Swagger:** http://localhost:5000

#### Opção C: Azure Functions C# (Microsserviços) 🔵

```bash
cd functions
./start-all.sh      # Linux/Mac
# ou
./start-all.ps1     # Windows
```

✅ **Serviços disponíveis:**
- Auth: http://localhost:7071/api
- Customers: http://localhost:7072/api
- Pets: http://localhost:7073/api
- Catalog: http://localhost:7074/api
- Scheduling: http://localhost:7075/api
- Orders: http://localhost:7076/api

#### Opção D: Azure Functions Java (Microsserviços) 🟡

```bash
cd functions-java
./start-all-java.sh      # Linux/Mac
# ou
./start-all-java.ps1     # Windows
```

✅ **Serviços disponíveis:**
- Auth: http://localhost:7081/api
- Customers: http://localhost:7082/api
- Pets: http://localhost:7083/api
- Catalog: http://localhost:7084/api
- Scheduling: http://localhost:7085/api
- Orders: http://localhost:7086/api

> 💡 **Dica:** Execute todos os 4 backends simultaneamente (portas diferentes) e use o toggle no frontend para alternar entre eles!

---

### Passo 2: Inicie o Frontend

```bash
cd frontend
python3 -m http.server 5500
```

Ou use **Live Server** no VS Code (clique direito em `index.html` → "Open with Live Server")

✅ **Disponível em:** http://localhost:5500

---

### Passo 3: Acesse e Explore! 🎉

1. **Abra:** http://localhost:5500
2. **Veja o toggle** no canto superior direito
3. **Faça login:**
   - Username: `admin`
   - Senha: `admin123`
4. **Explore** todas as funcionalidades!

---

## 🎯 O que Você Pode Fazer

### Como Cliente
- ✅ Ver catálogo de produtos por categoria
- ✅ Adicionar produtos ao carrinho
- ✅ Finalizar compras
- ✅ Ver histórico de pedidos
- ✅ Agendar serviços de banho e tosa

### Como Admin
- ✅ Gerenciar produtos, clientes, pets
- ✅ Ver e gerenciar pedidos
- ✅ Ver e gerenciar agendamentos
- ✅ Dashboard com estatísticas

---

## 🔄 Sistema de Toggle

**Alternar entre os 4 backends dinamicamente:**

1. Clique no toggle no canto superior direito
2. Escolha entre:
   - 🟢 **Spring Boot** (Monolito Java - porta 8080)
   - 🟣 **ASP.NET Core** (Monolito C# - porta 5000)
   - 🔵 **C# Functions** (Microsserviços - portas 7071-7076)
   - 🟡 **Java Functions** (Microsserviços - portas 7081-7086)
3. Pronto! Todas as requisições agora usam o backend selecionado

**Comparação de Backends:**

| Funcionalidade | Spring Boot | ASP.NET Core | C# Functions | Java Functions |
|----------------|-------------|--------------|--------------|----------------|
| Produtos | ✅ | ✅ | ✅ | ✅ |
| Categorias | ✅ | ✅ | ✅ | ✅ |
| Clientes | ✅ | ✅ | ✅ | ✅ |
| Pets | ✅ | ✅ | ✅ | ✅ |
| Serviços | ✅ | ✅ | ✅ | ✅ |
| Pedidos | ✅ | ✅ | ✅ | ✅ |
| Agendamentos | ✅ | ✅ | ✅ | ✅ |
| **Arquitetura** | Monolito | Monolito | Microsserviços | Microsserviços |
| **Portas** | 8080 | 5000 | 7071-7076 | 7081-7086 |

---

## 🆘 Problemas Comuns

### Backend não conecta
```bash
# Verifique se a porta está em uso
# Spring Boot:
lsof -i :8080

# ASP.NET:
lsof -i :5000

# Ou teste diretamente no navegador
http://localhost:8080/api/produtos
http://localhost:5000/api/produtos
```

### Frontend não carrega dados
1. Verifique se o backend está rodando
2. Veja o console do navegador (F12)
3. Confirme o backend selecionado no toggle

### Erro de CORS
- ✅ Já está configurado em ambos backends
- Se persistir, use Live Server ou servidor HTTP local

---

## 📚 Documentação Completa

- [README.md](README.md) - Documentação completa
- [backend-springboot/README.md](backend-springboot/README.md) - API Spring Boot
- [backend-aspnet/README.md](backend-aspnet/README.md) - API ASP.NET Core
- [frontend/BACKEND_TOGGLE_README.md](frontend/BACKEND_TOGGLE_README.md) - Sistema de toggle
- [REVISAO_PROJETO.md](REVISAO_PROJETO.md) - Análise técnica completa

---

## 🎓 Requisitos

**Spring Boot:**
- Java 21+
- Maven 3.8+

**ASP.NET Core:**
- .NET SDK 8.0+

**Frontend:**
- Qualquer navegador moderno
- Python 3 (para servidor HTTP) OU Live Server (VS Code)

---

## ⚡ Comandos Rápidos

```bash
# Backend Spring Boot
cd backend-springboot && mvn spring-boot:run

# Backend ASP.NET Core
cd backend-aspnet/PetshopApi && dotnet run

# Frontend (Python)
cd frontend && python3 -m http.server 5500

# Ou use Ctrl+P no VS Code e digite:
> Live Server: Open with Live Server
```

---

## 🎯 Endpoints Principais

### Produtos
- `GET /api/produtos` - Listar todos
- `GET /api/produtos/disponiveis` - Apenas disponíveis
- `GET /api/produtos/categoria/{id}` - Por categoria

### Autenticação
- `POST /api/auth/login` - Fazer login
- `POST /api/auth/registrar` - Criar conta

### Pedidos
- `GET /api/pedidos` - Listar todos
- `POST /api/pedidos` - Criar pedido
- `GET /api/pedidos/cliente/{id}` - Por cliente

---

## 🎉 Pronto!

Agora você tem um sistema completo funcionando com:
- ✅ Quatro backends intercambiáveis (2 monolitos + 2 microsserviços)
- ✅ Frontend moderno e responsivo
- ✅ Sistema de toggle dinâmico entre 4 opções
- ✅ Dados iniciais já populados
- ✅ Swagger para testar APIs
- ✅ Banco de dados compartilhado entre todos os backends

**Explore e divirta-se! 🐾**
