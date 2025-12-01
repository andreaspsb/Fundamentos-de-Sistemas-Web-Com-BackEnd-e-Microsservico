# Petshop Functions - Java/Spring Boot

Backend em microsserviços usando Azure Functions com Spring Boot e Spring Cloud Function.

## Arquitetura

Este projeto implementa a quarta versão do backend da Petshop, usando:

- **Java 17** com **Spring Boot 3.2.12**
- **Spring Cloud Function** com adaptador Azure
- **Azure Functions** para hospedagem serverless (Consumption Plan Windows)
- **H2 Database** em modo TCP (desenvolvimento) para compartilhamento entre microsserviços
- **Azure SQL Database** para produção
- **JWT** para autenticação
- **Resilience4j** para circuit breaker e retry em comunicação entre serviços

## Estrutura do Projeto

```
functions-java/
├── pom.xml                           # POM pai multi-módulo
├── start-all-java.sh                 # Script para iniciar todos os serviços
├── stop-all-java.sh                  # Script para parar todos os serviços
├── petshop-functions-shared/         # Biblioteca compartilhada
│   └── src/main/java/.../shared/
│       ├── model/                    # Entidades JPA
│       ├── dto/                      # DTOs para requisições/respostas
│       ├── repository/               # Repositórios Spring Data
│       ├── security/                 # JWT e autorização
│       ├── config/                   # DataInitializer
│       ├── messages/                 # Mensagens Service Bus
│       └── serviceclients/           # Clientes HTTP com Resilience4j
├── func-petshop-auth-java/           # Autenticação (porta 7081)
├── func-petshop-customers-java/      # Clientes (porta 7082)
├── func-petshop-pets-java/           # Pets (porta 7083)
├── func-petshop-catalog-java/        # Catálogo (porta 7084)
├── func-petshop-scheduling-java/     # Agendamentos (porta 7085)
└── func-petshop-orders-java/         # Pedidos (porta 7086)
```

## Pré-requisitos

1. **Java 17** ou superior
2. **Maven 3.9+**
3. **Azure Functions Core Tools v4**
   ```bash
   npm install -g azure-functions-core-tools@4 --unsafe-perm true
   ```

## Como Executar (Desenvolvimento)

### Opção 1: Script Automatizado

```bash
cd functions-java
chmod +x start-all-java.sh stop-all-java.sh
./start-all-java.sh
```

Para parar:
```bash
./stop-all-java.sh
```

### Opção 2: Manual

1. **Build do projeto:**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Iniciar servidor H2:**
   ```bash
   java -cp ~/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar \
        org.h2.tools.Server -tcp -tcpAllowOthers -ifNotExists
   ```

3. **Iniciar cada função (em terminais separados):**
   ```bash
   cd func-petshop-auth-java/target/azure-functions/func-petshop-auth-java
   func start --port 7081
   
   # Repetir para cada função nas portas 7082-7086
   ```

## Endpoints

### Auth (porta 7081)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /api/auth/login | Login |
| POST | /api/auth/register | Registro |
| GET | /api/auth/validate | Validar token |
| GET | /api/auth/me | Dados do usuário atual |
| POST | /api/auth/change-password | Alterar senha |

### Clientes (porta 7082)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/clientes | Listar (Admin) |
| GET | /api/clientes/{id} | Buscar por ID |
| GET | /api/clientes/cpf/{cpf} | Buscar por CPF (Admin) |
| POST | /api/clientes | Criar |
| PUT | /api/clientes/{id} | Atualizar |
| DELETE | /api/clientes/{id} | Excluir (Admin) |

### Pets (porta 7083)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/pets | Listar |
| GET | /api/pets/{id} | Buscar por ID |
| GET | /api/pets/cliente/{clienteId} | Pets do cliente |
| POST | /api/pets | Criar |
| PUT | /api/pets/{id} | Atualizar |
| DELETE | /api/pets/{id} | Excluir |

### Catálogo (porta 7084)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/categorias | Listar categorias |
| GET | /api/produtos | Listar produtos |
| GET | /api/produtos/categoria/{id} | Produtos por categoria |
| GET | /api/produtos/buscar?nome=X | Buscar produtos |
| GET | /api/servicos | Listar serviços |
| POST/PUT/DELETE | ... | CRUD (Admin) |

### Agendamentos (porta 7085)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/agendamentos | Listar |
| GET | /api/agendamentos/{id} | Buscar por ID |
| GET | /api/agendamentos/data/{data} | Por data (Admin) |
| POST | /api/agendamentos | Criar |
| PUT | /api/agendamentos/{id} | Atualizar |
| DELETE | /api/agendamentos/{id} | Cancelar |

### Pedidos (porta 7086)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/pedidos | Listar |
| GET | /api/pedidos/{id} | Buscar por ID |
| GET | /api/pedidos/status/{status} | Por status (Admin) |
| GET | /api/pedidos/estatisticas | Estatísticas (Admin) |
| POST | /api/pedidos | Criar |
| PUT | /api/pedidos/{id}/status | Atualizar status (Admin) |
| DELETE | /api/pedidos/{id} | Cancelar |

## Autenticação

Use o header `Authorization: Bearer <token>` nas requisições autenticadas.

**Usuário padrão:**
- Username: `admin`
- Password: `admin123`
- Role: `Admin`

## Configuração de Banco de Dados

### Desenvolvimento (H2)
Configuração padrão usando H2 em modo TCP para compartilhamento entre microsserviços:
```properties
spring.datasource.url=jdbc:h2:tcp://localhost/~/petshop-functions;AUTO_SERVER=TRUE
```

### Produção (PostgreSQL)
```properties
spring.datasource.url=jdbc:postgresql://host:5432/petshop
spring.datasource.username=usuario
spring.datasource.password=senha
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Produção (SQL Server)
```properties
spring.datasource.url=jdbc:sqlserver://host:1433;database=petshop
spring.datasource.username=usuario
spring.datasource.password=senha
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect
```

## Deploy no Azure

1. **Login no Azure:**
   ```bash
   az login
   ```

2. **Deploy de cada função:**
   ```bash
   cd func-petshop-auth-java
   mvn azure-functions:deploy
   ```

3. **Configurar variáveis de ambiente no Azure Portal:**
   - JWT_SECRET
   - SPRING_DATASOURCE_URL
   - SPRING_DATASOURCE_USERNAME
   - SPRING_DATASOURCE_PASSWORD

## Comparação com Outros Backends

| Feature | ASP.NET | Spring Boot | Functions C# | Functions Java |
|---------|---------|-------------|--------------|----------------|
| Framework | .NET 8 | Spring Boot 3.2 | .NET 8 | Spring Boot 3.2 |
| Arquitetura | Monolítico | Monolítico | Microsserviços | Microsserviços |
| Hospedagem | App Service | App Service | Azure Functions | Azure Functions |
| Banco Dev | SQLite | H2 | SQLite | H2 (TCP) |
| Auth | JWT | JWT | JWT | JWT |
| Portas | 5000 | 8080 | 7071-7076 | 7081-7086 |

## Estrutura de Resposta

### Sucesso
```json
{
  "id": 1,
  "nome": "Exemplo",
  ...
}
```

### Erro
```json
{
  "error": "Mensagem de erro"
}
```

## Tecnologias Utilizadas

- Java 17
- Spring Boot 3.2.12
- Spring Cloud Function 4.1.3
- Spring Data JPA
- Azure Functions Java Library 3.1.0
- H2 Database 2.2.224
- PostgreSQL Driver
- SQL Server Driver
- JWT (jjwt 0.12.5)
- Resilience4j 2.2.0
- Azure Service Bus
