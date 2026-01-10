# 🌐 Configuração CORS - Cross-Origin Resource Sharing

## 📋 Visão Geral

Este documento descreve a configuração de CORS (Cross-Origin Resource Sharing) implementada nos backends do projeto Pet Shop.

> ⚠️ **IMPORTANTE**: Cada tipo de backend tem seu próprio método de configuração CORS. Não existe uma forma única de centralizar tudo.

---

## 🎯 Origens Permitidas (TODAS)

```
# PRODUÇÃO
https://andreaspsb.github.io
https://yellow-field-047215b0f.3.azurestaticapps.net

# DESENVOLVIMENTO LOCAL
http://localhost:5500
http://127.0.0.1:5500
http://localhost:3000
http://127.0.0.1:3000
http://localhost:5173
http://127.0.0.1:5173
http://localhost:8080
http://127.0.0.1:8080
http://localhost:19006
http://127.0.0.1:19006
```

---

## 📊 Resumo por Backend

| Backend | Onde Configurar | Local vs Produção |
|---------|-----------------|-------------------|
| **ASP.NET Core** | `Program.cs` (código) | ✅ Mesmo código funciona em ambos |
| **Spring Boot** | `WebConfig.java` (código) | ✅ Mesmo código funciona em ambos |
| **C# Azure Functions** | Azure Portal/CLI | ⚠️ `host.json` só funciona local |
| **Java Azure Functions** | Azure Portal/CLI | ⚠️ `host.json` só funciona local |

---

## 🔧 Implementação por Backend

### 1. Backend ASP.NET Core

#### Arquivo: `backend-aspnet/PetshopApi/Program.cs`

CORS está configurado diretamente no código com origens hardcoded:

```csharp
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllEnvironments", policy =>
    {
        policy.WithOrigins(
                // PRODUÇÃO
                "https://andreaspsb.github.io",
                "https://yellow-field-047215b0f.3.azurestaticapps.net",
                // DESENVOLVIMENTO LOCAL
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:8080",
                "http://127.0.0.1:8080",
                "http://localhost:19006",
                "http://127.0.0.1:19006"
            )
            .AllowAnyMethod()
            .AllowAnyHeader()
            .AllowCredentials()
            .WithExposedHeaders("X-Pagination", "X-Total-Count")
            .SetPreflightMaxAge(TimeSpan.FromHours(1));
    });
});

// Aplicar a política
app.UseCors("AllEnvironments");
```

**Para adicionar nova origem**: Edite `Program.cs` e faça deploy.

---

### 2. Backend Spring Boot

#### Arquivo: `backend-springboot/src/main/java/com/petshop/config/WebConfig.java`

CORS está configurado diretamente no código com origens hardcoded:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @NonNull
    private static final String[] ALLOWED_ORIGINS = {
        // === PRODUÇÃO ===
        "https://andreaspsb.github.io",
        "https://yellow-field-047215b0f.3.azurestaticapps.net",
        
        // === DESENVOLVIMENTO LOCAL ===
        "http://localhost:5500",
        "http://127.0.0.1:5500",
        "http://localhost:3000",
        "http://127.0.0.1:3000",
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://localhost:8080",
        "http://127.0.0.1:8080",
        "http://localhost:19006",
        "http://127.0.0.1:19006"
    };

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(ALLOWED_ORIGINS)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("X-Pagination", "X-Total-Count")
                .maxAge(3600);
    }
}
```

**Para adicionar nova origem**: Edite `WebConfig.java` e faça deploy.

---

### 3. Azure Functions (C# e Java) - ⚠️ ATENÇÃO ESPECIAL

> **IMPORTANTE**: Para Azure Functions em produção, o CORS configurado no **Azure Portal/CLI tem precedência** sobre o `host.json`. O `host.json` só funciona para desenvolvimento local.

#### Desenvolvimento Local: `host.json`

Cada function tem seu próprio `host.json`. Exemplo:

```json
{
  "version": "2.0",
  "extensions": {
    "http": {
      "routePrefix": "api",
      "cors": {
        "allowedOrigins": [
          "https://andreaspsb.github.io",
          "https://yellow-field-047215b0f.3.azurestaticapps.net",
          "http://localhost:5500",
          "http://127.0.0.1:5500"
        ],
        "allowedMethods": ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
        "allowedHeaders": ["Content-Type", "Authorization", "Accept"],
        "supportCredentials": false
      }
    }
  }
}
```

#### Produção (Azure): CLI ou Portal

**O CORS em produção DEVE ser configurado via Azure CLI ou Portal!**

```bash
# Adicionar origens permitidas
az functionapp cors add \
  --name func-petshop-catalog-java \
  --resource-group petshop-rg \
  --allowed-origins "https://andreaspsb.github.io" "https://yellow-field-047215b0f.3.azurestaticapps.net"

# Verificar configuração atual
az functionapp cors show \
  --name func-petshop-catalog-java \
  --resource-group petshop-rg

# Remover todas as origens (limpar)
az functionapp cors remove \
  --name func-petshop-catalog-java \
  --resource-group petshop-rg \
  --allowed-origins "*"
```

#### Script para configurar TODAS as Functions de uma vez

```powershell
# PowerShell - Configurar CORS para todas as 12 Functions
$functions = @(
    "func-petshop-auth-java", "func-petshop-catalog-java", 
    "func-petshop-customers-java", "func-petshop-orders-java", 
    "func-petshop-pets-java", "func-petshop-scheduling-java",
    "func-petshop-auth", "func-petshop-catalog", 
    "func-petshop-customers", "func-petshop-orders", 
    "func-petshop-pets", "func-petshop-scheduling"
)

$origins = @(
    "https://andreaspsb.github.io",
    "https://yellow-field-047215b0f.3.azurestaticapps.net",
    "http://localhost:5500"
)

foreach ($fn in $functions) {
    # Limpar configuração existente
    az functionapp cors remove --name $fn --resource-group petshop-rg --allowed-origins "*" 2>$null
    
    # Adicionar novas origens
    az functionapp cors add --name $fn --resource-group petshop-rg --allowed-origins $origins
    
    Write-Host "✓ $fn configurado"
}
```

---

## ⚠️ Por que Azure Functions é diferente?

| Aspecto | Monólitos (ASP.NET/Spring) | Azure Functions |
|---------|----------------------------|-----------------|
| **Quem controla CORS** | Aplicação (código) | Azure Platform |
| **Onde configurar para produção** | Código fonte | Azure Portal/CLI |
| **`host.json` funciona em prod?** | N/A | ❌ NÃO |
| **Deploy atualiza CORS?** | ✅ Sim | ❌ Não - é config do Azure |

A Microsoft projetou Azure Functions para que configurações de infraestrutura (como CORS) sejam gerenciadas pela plataforma Azure, não pelo código deployado.

---

## 🧪 Testando CORS

### Teste com cURL

```bash
# Preflight request (OPTIONS)
curl -X OPTIONS https://func-petshop-catalog-java.azurewebsites.net/api/categorias \
  -H "Origin: https://andreaspsb.github.io" \
  -H "Access-Control-Request-Method: GET" \
  -v

# Deve retornar:
# Access-Control-Allow-Origin: https://andreaspsb.github.io
```

### Teste no Navegador

```javascript
// No console do navegador
fetch('https://func-petshop-catalog-java.azurewebsites.net/api/categorias/ativas', {
  method: 'GET',
  headers: { 'Content-Type': 'application/json' }
})
.then(r => r.json())
.then(console.log)
.catch(console.error);
```

---

## 🔧 Troubleshooting

### Erro: "No 'Access-Control-Allow-Origin' header"

1. **Para ASP.NET/Spring Boot**: Verifique se a origem está no código
2. **Para Azure Functions**: Execute o script de configuração CLI acima

### Erro: CORS funciona local mas não em produção

- **Azure Functions**: O `host.json` NÃO é usado em produção. Configure via CLI.
- **ASP.NET/Spring Boot**: Verifique se o código foi deployado corretamente.

### Como verificar CORS atual no Azure

```bash
# Ver configuração de uma Function específica
az functionapp cors show --name func-petshop-catalog-java --resource-group petshop-rg -o json
```

---

## ✅ Checklist de Configuração

### Monólitos (ASP.NET / Spring Boot)
- [x] Origens hardcoded no código fonte
- [x] Deploy automático via CI/CD atualiza CORS

### Azure Functions (C# e Java)
- [x] `host.json` configurado para desenvolvimento local
- [x] Azure CLI usado para configurar CORS em produção
- [ ] **LEMBRETE**: Após criar nova Function, executar script CLI

---

## 📝 Referência Rápida

| Precisa fazer | Backend | Comando/Ação |
|---------------|---------|--------------|
| Adicionar origem | ASP.NET | Editar `Program.cs` → deploy |
| Adicionar origem | Spring Boot | Editar `WebConfig.java` → deploy |
| Adicionar origem | Functions | `az functionapp cors add ...` |
| Verificar CORS | Functions | `az functionapp cors show ...` |
| Limpar CORS | Functions | `az functionapp cors remove ... --allowed-origins "*"` |

---

**Última atualização:** 10 de Janeiro de 2026
