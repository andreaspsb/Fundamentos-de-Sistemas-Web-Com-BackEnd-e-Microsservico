# Petshop Backend - Spring Boot

## 🚀 Tecnologias

- **Java 21** - Linguagem de programação moderna e robusta
- **Spring Boot 3.2.0** - Framework Java para desenvolvimento rápido
- **Spring Data JPA** - Abstração para acesso a dados com JPA/Hibernate
- **H2 Database** - Banco de dados em memória para desenvolvimento
- **Spring Security Crypto** - BCrypt para hash de senhas
- **Swagger/OpenAPI** - Documentação automática e interativa da API
- **Maven** - Gerenciamento de dependências e build
- **Lombok** - Redução de código boilerplate

## 📦 Dependências Incluídas

- **Spring Web** - Para criar APIs REST
- **Spring Data JPA** - Para acesso ao banco de dados com repositórios
- **H2 Database** - Banco de dados em memória (desenvolvimento)
- **Validation** - Para validação de dados com Bean Validation
- **Lombok** - Para reduzir código boilerplate (getters, setters, construtores)
- **DevTools** - Hot reload durante desenvolvimento
- **Spring Security Crypto** - BCrypt para hash de senhas

## 🏗️ Estrutura do Projeto

```
src/main/java/com/petshop/
├── PetshopApplication.java    # Classe principal (@SpringBootApplication)
├── controller/                # Controllers REST (Endpoints da API)
│   ├── AuthController.java         # Autenticação e registro
│   ├── ClienteController.java      # CRUD de clientes
│   ├── PetController.java          # CRUD de pets
│   ├── CategoriaController.java    # CRUD de categorias
│   ├── ProdutoController.java      # CRUD de produtos
│   ├── ServicoController.java      # CRUD de serviços
│   └── AgendamentoController.java  # CRUD de agendamentos
├── service/                   # Lógica de negócio
│   ├── AuthService.java
│   ├── ClienteService.java
│   ├── ProdutoService.java
│   ├── PedidoService.java
│   └── AgendamentoService.java
├── repository/                # Repositórios Spring Data JPA
│   ├── UsuarioRepository.java
│   ├── ClienteRepository.java
│   ├── PetRepository.java
│   ├── CategoriaRepository.java
│   ├── ProdutoRepository.java
│   ├── ServicoRepository.java
│   ├── AgendamentoRepository.java
│   ├── PedidoRepository.java
│   └── ItemPedidoRepository.java
├── model/                     # Entidades JPA (9 totais)
│   ├── Usuario.java
│   ├── Cliente.java
│   ├── Pet.java
│   ├── Categoria.java
│   ├── Produto.java
│   ├── Servico.java
│   ├── Agendamento.java
│   ├── Pedido.java
│   └── ItemPedido.java
├── dto/                       # Data Transfer Objects
│   ├── LoginDTO.java
│   ├── RegisterDTO.java
│   ├── PedidoRequestDTO.java
│   ├── PedidoResponseDTO.java
│   ├── ItemPedidoRequestDTO.java
│   └── ItemPedidoDTO.java
└── config/                    # Configurações
    ├── CorsConfig.java             # Configuração CORS
    ├── DataInitializer.java        # Dados iniciais (seed)
    └── SwaggerConfig.java          # Configuração Swagger
```

## 🔧 Como Executar

### Pré-requisitos
- **Java 21** (OpenJDK ou Oracle JDK) instalado
- **Maven 3.8+** instalado

### Instalação e Execução

```bash
# Navegar até o diretório do projeto
cd backend-springboot

# Compilar o projeto (primeira vez)
mvn clean compile

# Executar a aplicação
mvn spring-boot:run
```

### Executar JAR compilado

```bash
# Compilar e gerar JAR
mvn clean package

# Executar o JAR
java -jar target/petshop-backend-0.0.1-SNAPSHOT.jar
```

A aplicação estará disponível em:
- **API Base**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:petshopdb`
  - Username: `sa`
  - Password: (deixar em branco)

## 📚 Endpoints Disponíveis

### Autenticação (`/api/auth`)

- `POST /api/auth/login` - Fazer login e obter token
  - Body: `{"username": "admin", "password": "admin123"}`
  - Retorna: `{"token": "...", "usuario": {...}}`

- `POST /api/auth/registrar` - Registrar novo usuário
  - Body: `{"username": "...", "password": "...", "email": "...", "clienteId": 1}`
  - Retorna: Dados do usuário criado

- `GET /api/auth/validar-token` - Validar token de autenticação
  - Header: `Authorization: Bearer {token}`
  - Retorna: `true` ou `false`

- `POST /api/auth/logout` - Fazer logout (invalida token)
  - Header: `Authorization: Bearer {token}`

### Clientes (`/api/clientes`)

- `GET /api/clientes` - Listar todos os clientes
- `GET /api/clientes/{id}` - Buscar cliente por ID
- `GET /api/clientes/cpf/{cpf}` - Buscar cliente por CPF
- `GET /api/clientes/email/{email}` - Buscar cliente por email
- `POST /api/clientes` - Criar novo cliente
- `PUT /api/clientes/{id}` - Atualizar cliente
- `DELETE /api/clientes/{id}` - Deletar cliente

### Pets (`/api/pets`)

- `GET /api/pets` - Listar todos os pets
- `GET /api/pets/{id}` - Buscar pet por ID
- `GET /api/pets/cliente/{clienteId}` - Listar pets de um cliente
- `POST /api/pets` - Criar novo pet
- `PUT /api/pets/{id}` - Atualizar pet
- `DELETE /api/pets/{id}` - Deletar pet

### Categorias (`/api/categorias`)

- `GET /api/categorias` - Listar todas as categorias
- `GET /api/categorias/{id}` - Buscar categoria por ID
- `GET /api/categorias/ativas` - Listar apenas categorias ativas
- `POST /api/categorias` - Criar nova categoria
- `PUT /api/categorias/{id}` - Atualizar categoria
- `DELETE /api/categorias/{id}` - Deletar categoria

### Produtos (`/api/produtos`)

- `GET /api/produtos` - Listar todos os produtos
- `GET /api/produtos/{id}` - Buscar produto por ID
- `GET /api/produtos/disponiveis` - Listar produtos disponíveis (ativos e com estoque)
- `GET /api/produtos/categoria/{categoriaId}` - Listar produtos por categoria
- `GET /api/produtos/categoria/{categoriaId}/disponiveis` - Produtos disponíveis por categoria
- `GET /api/produtos/buscar?termo={termo}` - Buscar produtos por nome
- `GET /api/produtos/estoque-baixo?quantidade={quantidade}` - Produtos com estoque baixo
- `POST /api/produtos` - Criar novo produto
- `PUT /api/produtos/{id}` - Atualizar produto
- `PATCH /api/produtos/{id}/estoque?quantidade={quantidade}` - Atualizar estoque
- `DELETE /api/produtos/{id}` - Deletar produto

### Serviços (`/api/servicos`)

- `GET /api/servicos` - Listar todos os serviços
- `GET /api/servicos/{id}` - Buscar serviço por ID
- `GET /api/servicos/disponiveis` - Listar apenas serviços ativos
- `POST /api/servicos` - Criar novo serviço
- `PUT /api/servicos/{id}` - Atualizar serviço
- `DELETE /api/servicos/{id}` - Deletar serviço

### Agendamentos (`/api/agendamentos`)

- `GET /api/agendamentos` - Listar todos os agendamentos
- `GET /api/agendamentos/{id}` - Buscar agendamento por ID
- `GET /api/agendamentos/cliente/{clienteId}` - Listar agendamentos de um cliente
- `GET /api/agendamentos/pet/{petId}` - Listar agendamentos de um pet
- `GET /api/agendamentos/data?inicio={data}&fim={data}` - Agendamentos por período
- `POST /api/agendamentos` - Criar novo agendamento
- `PUT /api/agendamentos/{id}` - Atualizar agendamento
- `PATCH /api/agendamentos/{id}/status` - Atualizar status do agendamento
- `DELETE /api/agendamentos/{id}` - Deletar agendamento

### Pedidos (`/api/pedidos`)

- `GET /api/pedidos` - Listar todos os pedidos
- `GET /api/pedidos/{id}` - Buscar pedido por ID
- `GET /api/pedidos/cliente/{clienteId}` - Listar pedidos de um cliente
- `POST /api/pedidos` - Criar novo pedido
- `POST /api/pedidos/{pedidoId}/itens` - Adicionar item ao pedido
- `DELETE /api/pedidos/{pedidoId}/itens/{itemId}` - Remover item do pedido
- `POST /api/pedidos/{id}/confirmar` - Confirmar pedido
- `PATCH /api/pedidos/{id}/status` - Atualizar status do pedido
- `POST /api/pedidos/{id}/cancelar` - Cancelar pedido
- `DELETE /api/pedidos/{id}` - Deletar pedido

## 📊 Dados Iniciais (DataInitializer)

O sistema cria automaticamente ao iniciar (via `DataInitializer.java`):

### Categorias (3)
1. **Rações e Alimentação** - Alimentos e rações de qualidade
2. **Acessórios e Brinquedos** - Coleiras, guias, brinquedos
3. **Higiene e Cuidados** - Produtos para higiene e saúde

### Produtos (6+)
Dois produtos por categoria com:
- Nome, descrição, preço
- Estoque (quantidades variadas)
- URLs de imagens (Unsplash)
- Status ativo

Exemplos:
- Ração Premium para Cães (R$ 120,00) - 50 unidades
- Shampoo para Pets (R$ 25,00) - 100 unidades
- Bola de Borracha (R$ 15,00) - 200 unidades

### Serviços (3)
1. **Banho** - R$ 50,00
2. **Tosa** - R$ 40,00
3. **Banho + Tosa (Combo)** - R$ 80,00

### Usuários (2)

**Admin:**
- Username: `admin`
- Senha: `admin123` (hash BCrypt)
- Role: `ADMIN`
- Email: `admin@petshop.com`

**Cliente de Teste:**
- Username: `maria.silva`
- Senha: `senha123` (hash BCrypt)
- Role: `CLIENTE`
- Email: `maria@email.com`

### Cliente e Pet de Exemplo
- **Cliente**: Maria Silva (CPF, telefone, endereço completo)
- **Pet**: Rex - Labrador Retriever, 3 anos, 30kg

## 🔐 Segurança

### Hash de Senhas
- ✅ **BCrypt** - Todas as senhas são hasheadas com BCrypt (força 10)
- ✅ **Nunca em texto plano** - Senhas nunca são armazenadas ou retornadas em texto plano
- ✅ **Salt automático** - BCrypt gera salt único para cada senha

### Autenticação
- ✅ **Token-based** - Sistema de tokens Base64 (username:timestamp:random)
- ✅ **Expiração** - Tokens expiram após 24 horas
- ✅ **Validação** - Endpoint dedicado para validar tokens

### Autorização
- ✅ **Roles** - `ADMIN` e `CLIENTE` com permissões diferentes
- ✅ **Proteção de endpoints** - Alguns endpoints verificam role
- ⚠️ **Nota**: Para produção, implementar JWT com Spring Security

### CORS
CORS configurado para aceitar requisições de qualquer origem durante desenvolvimento:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
```

⚠️ **Produção**: Configurar origens específicas ao invés de `"*"`

## ⚙️ Configurações (application.properties)

```properties
# Servidor
server.port=8080

# H2 Database (em memória)
spring.datasource.url=jdbc:h2:mem:petshopdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JSON Serialization
spring.jackson.property-naming-strategy=LOWER_CAMEL_CASE
spring.jackson.serialization.write-dates-as-timestamps=false

# Swagger/OpenAPI
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### Configurações Importantes

- **`ddl-auto=create-drop`** - Recria o banco a cada restart (dados não persistem)
- **`show-sql=true`** - Mostra SQL no console (útil para debug)
- **`LOWER_CAMEL_CASE`** - JSON responses em camelCase (consistência com outros backends)

## 💡 Exemplos de Uso

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Resposta:
```json
{
  "token": "YWRtaW46MTcwMzE4MjQwMDAwMDoxMjM0NTY=",
  "usuario": {
    "id": 1,
    "username": "admin",
    "email": "admin@petshop.com",
    "role": "ADMIN"
  }
}
```

### Listar Produtos Disponíveis
```bash
curl http://localhost:8080/api/produtos/disponiveis
```

### Criar Produto (requer autenticação)
```bash
curl -X POST http://localhost:8080/api/produtos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YWRtaW46MTcwMzE4MjQwMDAwMDoxMjM0NTY=" \
  -d '{
    "nome": "Ração Premium Plus",
    "descricao": "Ração super premium para cães adultos",
    "preco": 150.00,
    "estoque": 30,
    "urlImagem": "https://images.unsplash.com/photo-1589924691995-400dc9ecc119",
    "categoriaId": 1,
    "ativo": true
  }'
```

### Criar Agendamento
```bash
curl -X POST http://localhost:8080/api/agendamentos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "clienteId": 1,
    "petId": 1,
    "servicoId": 3,
    "dataHora": "2026-02-20T14:00:00",
    "observacoes": "Pet tem medo de barulho",
    "metodoEntrega": "BUSCAR"
  }'
```

## 🛠️ Troubleshooting

### Porta 8080 Já em Uso

**Problema**: `Port 8080 is already in use`

**Solução**:
```bash
# Windows - Encontrar processo usando porta 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>

# Ou altere a porta em application.properties:
server.port=8081
```

### H2 Console Não Carrega

**Problema**: Console H2 não abre em `/h2-console`

**Solução**: Verifique se está habilitado em `application.properties`:
```properties
spring.h2.console.enabled=true
```

### Erros de CORS

**Problema**: Frontend não consegue fazer requisições

**Solução**: Verifique se `CorsConfig.java` está configurado corretamente e se o backend está rodando.

### Dados Não Persistem

**Problema**: Dados são perdidos ao reiniciar

**Explicação**: H2 em memória (`jdbc:h2:mem:`) recria o banco a cada restart. Isso é intencional para desenvolvimento.

**Solução para Persistir**:
```properties
# Altere para arquivo em disco:
spring.datasource.url=jdbc:h2:file:./data/petshopdb
```

### Build Falha

**Problema**: `mvn clean package` falha

**Solução**:
```bash
# Limpar cache Maven
mvn clean

# Atualizar dependências
mvn dependency:resolve

# Forçar atualização
mvn clean install -U
```

### Swagger Não Aparece

**Problema**: Swagger UI não carrega

**Solução**: Acesse diretamente:
- http://localhost:8080/swagger-ui/index.html
- ou http://localhost:8080/swagger-ui.html

## 🤝 Compatibilidade com Outros Backends

Este backend Spring Boot mantém **contrato de API idêntico** aos outros 3 backends do projeto:

✅ **ASP.NET Core** (Monolito C#)  
✅ **Azure Functions C#** (Microsserviços)  
✅ **Azure Functions Java** (Microsserviços)  

### Garantias de Compatibilidade

- ✅ **Mesmos endpoints** - URLs e métodos HTTP idênticos
- ✅ **Mesmo esquema JSON** - Respostas em camelCase consistentes
- ✅ **Mesmos status codes** - 200, 201, 400, 404, 500, etc.
- ✅ **Enums consistentes** - `SCREAMING_SNAKE_CASE` (ex: `PENDENTE`, `CONFIRMADO`)
- ✅ **Mesmas validações** - Regras de negócio replicadas
- ✅ **Banco compartilhado** - Todos conectam ao mesmo banco em produção

### Frontend Toggle

O frontend possui sistema de toggle que permite alternar entre backends dinamicamente:

📍 **Configuração**: `frontend/js/api-config.js`  
🔄 **Valores**: `SPRINGBOOT`, `ASPNET`, `FUNCTIONS`, `FUNCTIONS_JAVA`  
💾 **Storage**: `localStorage.getItem('backend-selecionado')`

> 💡 **Teste**: Inicie todos os 4 backends simultaneamente e alterne entre eles usando o toggle no frontend. Os dados serão os mesmos pois todos compartilham o banco!

## 📖 Documentação Adicional

- **[README Principal](../README.md)** - Visão geral do projeto completo
- **[Backend ASP.NET](../backend-aspnet/README.md)** - Documentação do backend .NET
- **[Azure Functions C#](../functions/README.md)** - Documentação dos microsserviços C#
- **[Azure Functions Java](../functions-java/README.md)** - Documentação dos microsserviços Java
- **[Frontend](../frontend/BACKEND_TOGGLE_README.md)** - Sistema de toggle de backends
- **[Swagger UI](http://localhost:8080/swagger-ui.html)** - Documentação interativa (com backend rodando)

## 📄 Licença

Projeto educacional - Fundamentos de Sistemas Web - PUCRS Online
