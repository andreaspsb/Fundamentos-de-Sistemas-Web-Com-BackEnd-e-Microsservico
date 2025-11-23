# 🌐 Configuração CORS - Cross-Origin Resource Sharing

## 📋 Visão Geral

Este documento descreve a configuração de CORS (Cross-Origin Resource Sharing) implementada nos backends do projeto Pet Shop, seguindo as melhores práticas de segurança.

## 🎯 Objetivos

- ✅ **Desenvolvimento**: Permitir acesso de qualquer origem local para facilitar testes
- ✅ **Produção**: Restringir acesso apenas a domínios específicos e confiáveis
- ✅ **Segurança**: Controlar métodos HTTP, headers e credenciais permitidos
- ✅ **Performance**: Cache de preflight requests para reduzir latência

---

## 🔧 Implementação

### Backend ASP.NET Core

#### Arquivo: `Program.cs`

```csharp
// Configure CORS with environment-specific settings
builder.Services.AddCors(options =>
{
    // Development: Allow all origins (for local testing)
    options.AddPolicy("Development", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });

    // Production: Restrict to specific origins
    options.AddPolicy("Production", policy =>
    {
        var allowedOrigins = builder.Configuration
            .GetSection("Cors:AllowedOrigins")
            .Get<string[]>() ?? new[] 
            { 
                "https://petshop.com",
                "https://www.petshop.com",
                "https://api.petshop.com"
            };

        policy.WithOrigins(allowedOrigins)
              .WithMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
              .WithHeaders("Content-Type", "Authorization", "X-Requested-With")
              .AllowCredentials()
              .SetIsOriginAllowedToAllowWildcardSubdomains()
              .WithExposedHeaders("X-Pagination", "X-Total-Count")
              .SetPreflightMaxAge(TimeSpan.FromMinutes(10));
    });
});

// Apply CORS policy based on environment
var corsPolicy = app.Environment.IsDevelopment() ? "Development" : "Production";
app.UseCors(corsPolicy);
```

#### Arquivo: `appsettings.json` (Produção)

```json
{
  "Cors": {
    "AllowedOrigins": [
      "https://petshop.com",
      "https://www.petshop.com",
      "https://api.petshop.com"
    ]
  }
}
```

#### Arquivo: `appsettings.Development.json` (Desenvolvimento)

```json
{
  "Cors": {
    "AllowedOrigins": [
      "http://localhost:5173",
      "http://localhost:3000",
      "http://localhost:8080",
      "http://127.0.0.1:5173",
      "http://127.0.0.1:3000",
      "http://127.0.0.1:8080"
    ]
  }
}
```

---

### Backend Spring Boot

#### Arquivo: `WebConfig.java`

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:https://petshop.com,https://www.petshop.com}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "X-Requested-With")
                .allowCredentials(true)
                .exposedHeaders("X-Pagination", "X-Total-Count")
                .maxAge(3600); // Cache preflight por 1 hora
    }
}
```

#### Arquivo: `application-dev.properties` (Desenvolvimento)

```properties
# CORS - Allow local development origins
cors.allowed-origins=http://localhost:5173,http://localhost:3000,http://localhost:8080,http://127.0.0.1:5173,http://127.0.0.1:3000,http://127.0.0.1:8080
```

#### Arquivo: `application-prod.properties` (Produção)

```properties
# CORS - Restrict to production domains only
cors.allowed-origins=https://petshop.com,https://www.petshop.com,https://api.petshop.com
```

---

## 🚀 Como Usar

### Desenvolvimento Local

#### ASP.NET Core
```bash
# Executa automaticamente com política "Development"
dotnet run
```

#### Spring Boot
```bash
# Executa com perfil de desenvolvimento
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Ou via java
java -jar -Dspring.profiles.active=dev petshop.jar
```

### Produção

#### ASP.NET Core
```bash
# Define ambiente como Production
export ASPNETCORE_ENVIRONMENT=Production
dotnet run

# Ou publica e executa
dotnet publish -c Release
cd bin/Release/net8.0/publish
dotnet PetshopApi.dll
```

#### Spring Boot
```bash
# Executa com perfil de produção
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Ou via java
java -jar -Dspring.profiles.active=prod petshop.jar
```

---

## ⚙️ Configuração de Origens Permitidas

### Desenvolvimento

As seguintes origens são permitidas em desenvolvimento:

| Origem | Uso Comum |
|--------|-----------|
| `http://localhost:5173` | Vite (Vue, React) |
| `http://localhost:3000` | React, Next.js |
| `http://localhost:8080` | Angular, Spring Boot |
| `http://127.0.0.1:*` | Alternativa a localhost |

### Produção

Em produção, **SEMPRE** configure origens específicas:

```json
// ASP.NET Core: appsettings.json
{
  "Cors": {
    "AllowedOrigins": [
      "https://seudominio.com",
      "https://www.seudominio.com"
    ]
  }
}
```

```properties
# Spring Boot: application-prod.properties
cors.allowed-origins=https://seudominio.com,https://www.seudominio.com
```

---

## 🔒 Configurações de Segurança

### Métodos HTTP Permitidos

```
✅ GET     - Leitura de dados
✅ POST    - Criação de recursos
✅ PUT     - Atualização completa
✅ DELETE  - Remoção de recursos
✅ PATCH   - Atualização parcial
✅ OPTIONS - Preflight requests (obrigatório para CORS)
```

### Headers Permitidos

```
✅ Content-Type      - Tipo de conteúdo (application/json)
✅ Authorization     - Token de autenticação
✅ X-Requested-With  - Identificação de requisições AJAX
```

### Headers Expostos

Estes headers podem ser lidos pelo JavaScript no frontend:

```
✅ X-Pagination   - Informações de paginação
✅ X-Total-Count  - Total de registros
```

### Credenciais (Cookies)

```csharp
// ASP.NET Core
.AllowCredentials()  // Permite envio de cookies

// Spring Boot
.allowCredentials(true)  // Permite envio de cookies
```

⚠️ **IMPORTANTE**: Quando `AllowCredentials()` está habilitado, **não é possível** usar `AllowAnyOrigin()`. É necessário especificar origens exatas.

---

## 🧪 Testando CORS

### Teste Manual com cURL

```bash
# Preflight request (OPTIONS)
curl -X OPTIONS http://localhost:5000/api/produtos \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: GET" \
  -v

# Deve retornar:
# Access-Control-Allow-Origin: http://localhost:5173
# Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
```

### Teste no Navegador

```javascript
// No console do navegador
fetch('http://localhost:5000/api/produtos', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log('Sucesso:', data))
.catch(error => console.error('Erro CORS:', error));
```

### Erros Comuns

#### ❌ Erro: "CORS policy: No 'Access-Control-Allow-Origin' header"

**Causa**: Origem não permitida na configuração

**Solução**: Adicionar origem na lista de origens permitidas

#### ❌ Erro: "Credential is not supported if the CORS header 'Access-Control-Allow-Origin' is '*'"

**Causa**: Tentativa de usar `AllowAnyOrigin()` com `AllowCredentials()`

**Solução**: Especificar origens exatas em vez de usar wildcard

#### ❌ Erro: "Method PUT is not allowed by Access-Control-Allow-Methods"

**Causa**: Método HTTP não permitido

**Solução**: Adicionar método na lista de métodos permitidos

---

## 📊 Comparação: Antes vs Depois

### ❌ Antes (Inseguro)

```csharp
// ASP.NET Core
policy.AllowAnyOrigin()
      .AllowAnyMethod()
      .AllowAnyHeader();
```

```java
// Spring Boot
registry.addMapping("/**")
        .allowedOrigins("*")
        .allowedMethods("*")
        .allowedHeaders("*");
```

**Problemas:**
- ⚠️ Qualquer site pode acessar sua API
- ⚠️ Vulnerável a ataques CSRF
- ⚠️ Não é possível usar credenciais (cookies)
- ⚠️ Sem controle de cache

### ✅ Depois (Seguro)

```csharp
// ASP.NET Core (Produção)
policy.WithOrigins(allowedOrigins)
      .WithMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
      .WithHeaders("Content-Type", "Authorization", "X-Requested-With")
      .AllowCredentials()
      .SetPreflightMaxAge(TimeSpan.FromMinutes(10));
```

```java
// Spring Boot (Produção)
registry.addMapping("/**")
        .allowedOrigins("https://petshop.com")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        .allowedHeaders("Content-Type", "Authorization", "X-Requested-With")
        .allowCredentials(true)
        .maxAge(3600);
```

**Benefícios:**
- ✅ Apenas domínios confiáveis podem acessar
- ✅ Controle fino sobre métodos e headers
- ✅ Suporte a credenciais (cookies)
- ✅ Cache de preflight (melhor performance)
- ✅ Proteção contra CSRF

---

## 🎓 Melhores Práticas

### ✅ DO

1. **Use configurações diferentes para dev/prod**
   ```
   Development: Permissivo (facilitar testes)
   Production: Restritivo (segurança)
   ```

2. **Especifique origens exatas em produção**
   ```
   ✅ https://meusite.com
   ❌ https://*.meusite.com (wildcards só com SetIsOriginAllowedToAllowWildcardSubdomains)
   ```

3. **Liste apenas métodos HTTP necessários**
   ```
   ✅ GET, POST, PUT, DELETE, PATCH, OPTIONS
   ❌ Não use "*" ou AllowAnyMethod em produção
   ```

4. **Use HTTPS em produção**
   ```
   ✅ https://meusite.com
   ❌ http://meusite.com
   ```

5. **Configure cache de preflight**
   ```csharp
   .SetPreflightMaxAge(TimeSpan.FromMinutes(10))  // ASP.NET
   ```
   ```java
   .maxAge(3600)  // Spring Boot (1 hora)
   ```

### ❌ DON'T

1. **Não use `AllowAnyOrigin()` em produção**
2. **Não use `AllowAnyMethod()` em produção**
3. **Não use `AllowAnyHeader()` em produção**
4. **Não exponha headers desnecessários**
5. **Não configure CORS no frontend** (não funciona!)

---

## 📚 Recursos Adicionais

### Documentação Oficial

- [ASP.NET Core CORS](https://learn.microsoft.com/en-us/aspnet/core/security/cors)
- [Spring Boot CORS](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html)
- [MDN - CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)

### Ferramentas de Teste

- [CORS Tester](https://www.test-cors.org/)
- [Postman](https://www.postman.com/) - Testar APIs
- Browser DevTools - Console e Network tab

---

## 🔍 Troubleshooting

### Verificar Configuração Atual

#### ASP.NET Core
```bash
# Ver variáveis de ambiente
dotnet run --environment Development

# Ver configuração carregada
dotnet user-secrets list
```

#### Spring Boot
```bash
# Ver perfil ativo
java -jar petshop.jar --spring.profiles.active=dev

# Ver todas as propriedades
java -jar petshop.jar --debug
```

### Logs Úteis

```csharp
// ASP.NET Core: Adicionar logs
app.UseCors(policy => 
{
    policy.WithOrigins("http://localhost:5173");
    Console.WriteLine("CORS policy applied for localhost:5173");
});
```

```java
// Spring Boot: Adicionar logs
@Override
public void addCorsMappings(CorsRegistry registry) {
    logger.info("Configuring CORS with origins: " + Arrays.toString(allowedOrigins));
    registry.addMapping("/**").allowedOrigins(allowedOrigins);
}
```

---

## ✅ Checklist de Implementação

- [x] Configuração CORS implementada em ASP.NET Core
- [x] Configuração CORS implementada em Spring Boot
- [x] Políticas diferentes para dev/prod criadas
- [x] Origens permitidas configuradas via arquivos de configuração
- [x] Métodos HTTP específicos definidos
- [x] Headers permitidos e expostos configurados
- [x] Cache de preflight configurado
- [x] Credenciais habilitadas com origens específicas
- [x] Anotações `@CrossOrigin` removidas dos controllers (Spring Boot)
- [x] Documentação criada
- [x] Testes realizados em ambos backends

---

## 📝 Notas de Versão

**Versão 2.0** (22 Nov 2025)
- ✅ Implementação de políticas por ambiente
- ✅ Configuração via arquivos de configuração
- ✅ Remoção de `@CrossOrigin` redundantes
- ✅ Headers expostos adicionados
- ✅ Cache de preflight configurado
- ✅ Suporte a credenciais com origens específicas

**Versão 1.0** (Original)
- ⚠️ Configuração permissiva (`AllowAnyOrigin`)
- ⚠️ Sem diferenciação entre ambientes
- ⚠️ Sem cache de preflight

---

**Autor:** GitHub Copilot  
**Data:** 22 de Novembro de 2025  
**Projeto:** Pet Shop Full Stack
