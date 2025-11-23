# 🔐 Autenticação JWT - JSON Web Tokens

## 📋 Visão Geral

Este documento descreve a implementação completa de autenticação JWT (JSON Web Tokens) nos backends do projeto Pet Shop, substituindo o sistema anterior baseado em tokens Base64.

## 🎯 Por que JWT?

### ❌ Sistema Anterior (Base64)
- Token simples: `Base64(username:timestamp)`
- Facilmente decodificável
- Sem assinatura digital
- Vulnerável a falsificação
- Sem informações de contexto

### ✅ Sistema JWT (Atual)
- **Assinado digitalmente** com HMAC SHA-256
- **Autocontido** - contém informações do usuário
- **Verificável** - impossível falsificar sem a chave secreta
- **Com expiração** - tokens expiram automaticamente
- **Padrão da indústria** - amplamente suportado

---

## 🏗️ Arquitetura JWT

### Estrutura de um Token JWT

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsImV4cCI6MTcwMDAwMDAwMH0.signature
     [Header - Algoritmo]          [Payload - Dados do Usuário]         [Signature - Assinatura]
```

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload (Claims):**
```json
{
  "sub": "admin",              // Subject (username)
  "role": "ADMIN",             // Role do usuário
  "clienteId": "123",          // ID do cliente (se aplicável)
  "iat": 1700000000,           // Issued At (quando foi criado)
  "exp": 1700086400            // Expiration (quando expira)
}
```

**Signature:**
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

---

## 🔧 Implementação

### Backend Spring Boot

#### 1. Dependências Maven (pom.xml)

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

#### 2. Classe JwtUtil

**Arquivo:** `src/main/java/com/petshop/security/JwtUtil.java`

```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}") // 24 horas
    private Long expiration;
    
    // Gerar token
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    // Validar token
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    // Extrair username
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // Extrair role
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
}
```

#### 3. Filtro de Autenticação

**Arquivo:** `src/main/java/com/petshop/security/JwtAuthenticationFilter.java`

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            String username = jwtUtil.extractUsername(jwt);
            String role = jwtUtil.extractRole(jwt);
            
            if (jwtUtil.validateToken(jwt)) {
                request.setAttribute("username", username);
                request.setAttribute("role", role);
                request.setAttribute("authenticated", true);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

#### 4. Configuração

**Arquivo:** `application.properties`

```properties
# JWT Configuration
jwt.secret=petshop-secret-key-must-be-at-least-256-bits-long-for-HS256-algorithm
jwt.expiration=86400000
```

---

### Backend ASP.NET Core

#### 1. Pacotes NuGet (.csproj)

```xml
<PackageReference Include="Microsoft.AspNetCore.Authentication.JwtBearer" Version="8.0.11" />
<PackageReference Include="System.IdentityModel.Tokens.Jwt" Version="8.2.1" />
```

#### 2. Serviço JWT

**Arquivo:** `Security/JwtService.cs`

```csharp
public class JwtService
{
    private readonly IConfiguration _configuration;
    
    public string GenerateToken(string username, string role, long? clienteId = null)
    {
        var key = Encoding.UTF8.GetBytes(_configuration["Jwt:SecretKey"]);
        var expirationMinutes = int.Parse(_configuration["Jwt:ExpirationMinutes"] ?? "1440");
        
        var tokenDescriptor = new SecurityTokenDescriptor
        {
            Subject = new ClaimsIdentity(new[]
            {
                new Claim(ClaimTypes.Name, username),
                new Claim(ClaimTypes.Role, role),
                new Claim("clienteId", clienteId?.ToString() ?? "")
            }),
            Expires = DateTime.UtcNow.AddMinutes(expirationMinutes),
            SigningCredentials = new SigningCredentials(
                new SymmetricSecurityKey(key),
                SecurityAlgorithms.HmacSha256Signature
            ),
            Issuer = _configuration["Jwt:Issuer"],
            Audience = _configuration["Jwt:Audience"]
        };
        
        var tokenHandler = new JwtSecurityTokenHandler();
        var token = tokenHandler.CreateToken(tokenDescriptor);
        return tokenHandler.WriteToken(token);
    }
    
    public ClaimsPrincipal? ValidateToken(string token)
    {
        var key = Encoding.UTF8.GetBytes(_configuration["Jwt:SecretKey"]);
        var tokenHandler = new JwtSecurityTokenHandler();
        
        var validationParameters = new TokenValidationParameters
        {
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(key),
            ValidateIssuer = true,
            ValidIssuer = _configuration["Jwt:Issuer"],
            ValidateAudience = true,
            ValidAudience = _configuration["Jwt:Audience"],
            ValidateLifetime = true,
            ClockSkew = TimeSpan.Zero
        };
        
        return tokenHandler.ValidateToken(token, validationParameters, out _);
    }
}
```

#### 3. Middleware JWT

**Arquivo:** `Middleware/JwtMiddleware.cs`

```csharp
public class JwtMiddleware
{
    private readonly RequestDelegate _next;
    
    public async Task Invoke(HttpContext context, JwtService jwtService)
    {
        var token = context.Request.Headers["Authorization"]
            .FirstOrDefault()?.Split(" ").Last();
        
        if (token != null)
        {
            var principal = jwtService.ValidateToken(token);
            if (principal != null)
            {
                context.User = principal;
                context.Items["Username"] = principal.Identity?.Name;
                context.Items["Role"] = principal.FindFirst(ClaimTypes.Role)?.Value;
            }
        }
        
        await _next(context);
    }
}
```

#### 4. Configuração (Program.cs)

```csharp
// Register JWT Service
builder.Services.AddSingleton<JwtService>();

// Configure JWT Authentication
var jwtSecretKey = builder.Configuration["Jwt:SecretKey"];
var key = Encoding.UTF8.GetBytes(jwtSecretKey);

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(key),
        ValidateIssuer = true,
        ValidIssuer = builder.Configuration["Jwt:Issuer"],
        ValidateAudience = true,
        ValidAudience = builder.Configuration["Jwt:Audience"],
        ValidateLifetime = true
    };
});

// ...

// No pipeline
app.UseMiddleware<JwtMiddleware>();
app.UseAuthentication();
app.UseAuthorization();
```

#### 5. Configuração (appsettings.json)

```json
{
  "Jwt": {
    "SecretKey": "petshop-secret-key-must-be-at-least-256-bits-long-for-HS256-algorithm",
    "Issuer": "PetshopApi",
    "Audience": "PetshopFrontend",
    "ExpirationMinutes": "1440"
  }
}
```

---

## 🚀 Como Usar

### 1. Login e Obtenção do Token

**Request:**
```http
POST /api/auth/login HTTP/1.1
Content-Type: application/json

{
  "username": "admin",
  "senha": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "email": "admin@petshop.com",
  "role": "ADMIN",
  "clienteId": null
}
```

### 2. Usar Token nas Requisições

**Todas as requisições protegidas devem incluir o header Authorization:**

```http
GET /api/produtos HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. Validar Token

**Request:**
```http
GET /api/auth/validar HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "valido": true
}
```

---

## 🧪 Testando JWT

### Teste com cURL

```bash
# 1. Login
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","senha":"admin123"}' \
  | jq -r '.token')

# 2. Usar token
curl -X GET http://localhost:8080/api/produtos \
  -H "Authorization: Bearer $TOKEN"

# 3. Validar token
curl -X GET http://localhost:8080/api/auth/validar \
  -H "Authorization: Bearer $TOKEN"
```

### Teste no Swagger

1. Acesse `/swagger-ui.html` (Spring Boot) ou `/` (ASP.NET)
2. Clique no botão **🔒 Authorize**
3. Digite: `Bearer {seu_token_aqui}`
4. Clique em **Authorize**
5. Agora todas as requisições incluirão o token automaticamente

### Decodificar Token (Debug)

Visite https://jwt.io e cole seu token para ver o conteúdo:

```json
{
  "sub": "admin",
  "role": "ADMIN",
  "iat": 1700000000,
  "exp": 1700086400
}
```

---

## 🔒 Segurança

### Chave Secreta

⚠️ **IMPORTANTE**: A chave secreta deve ter **no mínimo 256 bits (32 caracteres)** para HS256.

**❌ Não fazer:**
```properties
jwt.secret=123456  # Muito curta e insegura!
```

**✅ Fazer:**
```properties
jwt.secret=petshop-secret-key-must-be-at-least-256-bits-long-for-HS256-algorithm
```

**🔐 Em produção:**
```bash
# Usar variável de ambiente
export JWT_SECRET=$(openssl rand -base64 32)
```

### Expiração

Configure tempo de expiração adequado:

```properties
# Desenvolvimento: 24 horas
jwt.expiration=86400000

# Produção: 1 hora (mais seguro)
jwt.expiration=3600000
```

### HTTPS

⚠️ **Sempre use HTTPS em produção** para evitar interceptação do token.

```csharp
// ASP.NET: Forçar HTTPS
app.UseHttpsRedirection();

options.RequireHttpsMetadata = true; // true em produção
```

---

## 📊 Comparação: Base64 vs JWT

| Característica | Base64 (Antigo) | JWT (Novo) |
|----------------|-----------------|------------|
| **Segurança** | ❌ Baixa | ✅ Alta |
| **Assinatura** | ❌ Não | ✅ Sim |
| **Falsificável** | ✅ Sim | ❌ Não |
| **Autocontido** | ❌ Não | ✅ Sim |
| **Expiração** | ⚠️ Manual | ✅ Automática |
| **Padrão** | ❌ Customizado | ✅ RFC 7519 |
| **Suporte** | ❌ Limitado | ✅ Universal |
| **Verificação** | ❌ No servidor | ✅ Stateless |

---

## 🎓 Melhores Práticas

### ✅ DO

1. **Use chaves secretas fortes** (mínimo 256 bits)
2. **Configure expiração adequada** (1-24 horas)
3. **Use HTTPS em produção**
4. **Armazene token no localStorage ou sessionStorage**
5. **Inclua apenas informações necessárias no payload**
6. **Valide token em cada requisição protegida**
7. **Implemente refresh tokens** para sessões longas
8. **Rotacione chaves secretas periodicamente**

### ❌ DON'T

1. **Não armazene senhas no token**
2. **Não use tokens muito longos** (+ de 1 semana)
3. **Não compartilhe chave secreta**
4. **Não armazene dados sensíveis no payload** (é decodificável!)
5. **Não envie token via query string** (use header)
6. **Não ignore erros de validação**
7. **Não use HTTP em produção**

---

## 🔄 Fluxo de Autenticação

```mermaid
sequenceDiagram
    participant Frontend
    participant API
    participant JWT Service
    participant Database

    Frontend->>API: POST /api/auth/login
    API->>Database: Buscar usuário
    Database-->>API: Dados do usuário
    API->>JWT Service: Gerar token
    JWT Service-->>API: Token JWT
    API-->>Frontend: { token, username, role }
    
    Note over Frontend: Armazena token no localStorage
    
    Frontend->>API: GET /api/produtos<br/>Authorization: Bearer {token}
    API->>JWT Service: Validar token
    JWT Service-->>API: Token válido + claims
    API->>Database: Buscar produtos
    Database-->>API: Lista de produtos
    API-->>Frontend: Produtos JSON
```

---

## 🧩 Integração com Frontend

### Armazenar Token

```javascript
// Após login bem-sucedido
const response = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username, senha })
});

const data = await response.json();
localStorage.setItem('token', data.token);
localStorage.setItem('username', data.username);
localStorage.setItem('role', data.role);
```

### Usar Token

```javascript
// Em todas as requisições protegidas
const token = localStorage.getItem('token');

const response = await fetch('/api/produtos', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

### Interceptor Global (Recomendado)

```javascript
// Adicionar token automaticamente
const originalFetch = window.fetch;
window.fetch = function(...args) {
  const token = localStorage.getItem('token');
  if (token && args[1]) {
    args[1].headers = {
      ...args[1].headers,
      'Authorization': `Bearer ${token}`
    };
  }
  return originalFetch.apply(this, args);
};
```

---

## 📚 Recursos Adicionais

### Documentação Oficial

- [JWT.io](https://jwt.io) - Introdução e debugger
- [RFC 7519](https://datatracker.ietf.org/doc/html/rfc7519) - Especificação JWT
- [JJWT (Java)](https://github.com/jwtk/jjwt) - Biblioteca Java
- [Microsoft JWT](https://learn.microsoft.com/en-us/aspnet/core/security/authentication/) - ASP.NET Core

### Ferramentas

- [JWT.io Debugger](https://jwt.io) - Decodificar tokens
- [Postman](https://www.postman.com/) - Testar APIs com JWT
- [Browser DevTools](https://developer.chrome.com/docs/devtools/) - Inspecionar requisições

---

## ✅ Checklist de Implementação

- [x] Dependências JWT adicionadas (Spring Boot + ASP.NET)
- [x] Classe JwtUtil/JwtService criada
- [x] Filtro/Middleware de autenticação criado
- [x] Configuração JWT nos arquivos de configuração
- [x] AuthController atualizado para gerar JWT
- [x] Endpoint /validar implementado
- [x] Swagger configurado para JWT
- [x] Ambos backends compilando sem erros
- [ ] Frontend atualizado para usar JWT
- [ ] Refresh tokens implementados (futuro)
- [ ] Testes automatizados (futuro)

---

## 🔮 Próximos Passos

### Refresh Tokens

Implementar sistema de refresh tokens para sessões longas sem precisar fazer login novamente:

```
Access Token: 1 hora (curto)
Refresh Token: 7 dias (longo)
```

### Rate Limiting

Adicionar limitação de requisições para prevenir ataques de força bruta:

```
Max 5 tentativas de login por minuto
```

### Revogação de Tokens

Implementar lista negra de tokens para logout forçado:

```
Redis/Cache para tokens revogados
```

---

## 📝 Notas de Versão

**Versão 2.0** (22 Nov 2025)
- ✅ Implementação completa de JWT
- ✅ Substituição do sistema Base64
- ✅ Suporte em Spring Boot e ASP.NET Core
- ✅ Middleware/Filtro de autenticação
- ✅ Swagger configurado para JWT
- ✅ Documentação completa

**Versão 1.0** (Original)
- ⚠️ Sistema Base64 inseguro
- ⚠️ Sem assinatura digital
- ⚠️ Vulnerável a falsificação

---

**Autor:** GitHub Copilot  
**Data:** 22 de Novembro de 2025  
**Projeto:** Pet Shop Full Stack
