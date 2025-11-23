# 🚀 Azure Portal - Guia Rápido (Usando Workflows Existentes)

**Este projeto já possui workflows GitHub Actions configurados!** Este guia mostra como configurar o Azure pelo portal web e integrar com os workflows existentes.

---

## ✅ Workflows Já Prontos

Seu projeto já tem:
- ✅ **ci-tests.yml** - Testes automáticos (Spring Boot + ASP.NET Core + Playwright)
- ✅ **cd-azure.yml** - Deploy automático para Azure
- ✅ **security-scan.yml** - Scan de segurança e dependências

**Você só precisa:**
1. Criar os recursos no Azure Portal
2. Configurar os secrets no GitHub
3. Fazer push e o deploy será automático! 🎉

---

## 📋 Checklist Rápido

- [ ] Ativar Azure for Students
- [ ] Criar Resource Group
- [ ] Criar App Service (Backend)
- [ ] Criar Static Web App (Frontend)
- [ ] Criar Banco de Dados (Azure ou externo)
- [ ] Configurar secrets no GitHub
- [ ] Ajustar nome do App Service no workflow
- [ ] Push para main → Deploy automático!

---

## 🎓 Passo 1: Ativar Azure for Students

1. **Acesse:** https://azure.microsoft.com/pt-br/free/students/
2. **Login** com sua Conta Microsoft
3. Use **email institucional** (.edu.br) para verificação
4. **Aguarde aprovação** (instantâneo ou 1-3 dias)
5. **Acesse o Portal:** https://portal.azure.com
6. **Verifique créditos:** Subscriptions → Azure for Students → $100 disponível

---

## 🏗️ Passo 2: Criar Recursos no Portal Azure

### 2.1 Criar Resource Group

1. Portal Azure → Pesquise **"Resource groups"**
2. **+ Create**
3. **Preencha:**
   - Subscription: `Azure for Students`
   - Resource group: `petshop-rg`
   - Region: `Brazil South`
4. **Review + create** → **Create**

---

### 2.2 Criar App Service Plan

1. Pesquise **"App Service plans"**
2. **+ Create**
3. **Preencha:**
   - Subscription: `Azure for Students`
   - Resource Group: `petshop-rg`
   - Name: `petshop-plan`
   - Operating System: `Linux`
   - Region: `Brazil South`
   - Pricing Tier: `B1 Basic` (~$13/mês) ou `F1 Free` (limitado)
4. **Review + create** → **Create**

---

### 2.3 Criar Web App (Backend Spring Boot)

1. Pesquise **"App Services"**
2. **+ Create** → **Web App**
3. **Preencha:**
   - Subscription: `Azure for Students`
   - Resource Group: `petshop-rg`
   - **Name:** `petshop-backend-spring` ⚠️ **ANOTE ESSE NOME!**
   - Publish: `Code`
   - Runtime stack: `Java 21`
   - Java web server: `Java SE (Embedded Web Server)`
   - Operating System: `Linux`
   - Region: `Brazil South`
   - App Service Plan: `petshop-plan`
4. **Review + create** → **Create**
5. **Aguarde** ~2 minutos

**URL gerada:** `https://petshop-backend-spring.azurewebsites.net`

---

### 2.4 (Opcional) Criar Web App para Backend ASP.NET Core

**Se quiser hospedar ambos os backends:**

1. Pesquise **"App Services"**
2. **+ Create** → **Web App**
3. **Preencha:**
   - Subscription: `Azure for Students`
   - Resource Group: `petshop-rg`
   - **Name:** `petshop-backend-aspnet` ⚠️ **ANOTE ESSE NOME!**
   - Publish: `Code`
   - Runtime stack: `.NET 8 (LTS)`
   - Operating System: `Linux`
   - Region: `Brazil South`
   - App Service Plan: `petshop-plan` (mesmo plano)
4. **Review + create** → **Create**
5. **Aguarde** ~2 minutos

**URL gerada:** `https://petshop-backend-aspnet.azurewebsites.net`

**⚠️ Nota sobre custos:** Hospedar 2 backends no mesmo App Service Plan não aumenta o custo, mas consome mais recursos. Você pode começar apenas com Spring Boot e adicionar o ASP.NET Core depois.

---

### 2.5 Configurar Variáveis de Ambiente (Backend Spring Boot)

1. Vá no seu **App Service** → `petshop-backend-spring`
2. Menu lateral → **Configuration**
3. Aba **Application settings** → **+ New application setting**

**Adicione cada variável:**

| Name | Value | Descrição |
|------|-------|-----------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Ativa perfil de produção |
| `DATABASE_URL` | `jdbc:postgresql://...` | URL do banco (veja passo 2.6) |
| `DB_USERNAME` | `petshop_admin` | Usuário do banco |
| `DB_PASSWORD` | `SuaSenhaForte123!@#` | Senha do banco |
| `FRONTEND_URL` | `https://petshop-frontend-xxx.azurestaticapps.net` | URL do frontend (veja passo 2.5) |
| `JWT_SECRET` | `seu-secret-aleatorio-256-bits` | Secret do JWT |

4. Clique em **Save** → **Continue** (confirmar restart)

---

### 2.5.1 (Opcional) Configurar Variáveis de Ambiente (Backend ASP.NET Core)

**Se você criou o Web App ASP.NET Core no passo 2.4:**

1. Vá no seu **App Service** → `petshop-backend-aspnet`
2. Menu lateral → **Configuration**
3. Aba **Application settings** → **+ New application setting**

**Adicione cada variável:**

| Name | Value | Descrição |
|------|-------|-----------|
| `ASPNETCORE_ENVIRONMENT` | `Production` | Ativa perfil de produção |
| `DATABASE_URL` | `Host=...;Port=5432;Database=...` | Connection string formato ADO.NET |
| `JWT_SECRET` | `seu-secret-aleatorio-256-bits` | Secret do JWT |
| `FRONTEND_URL` | `https://petshop-frontend-xxx.azurestaticapps.net` | URL do frontend |

**⚠️ Importante:** O formato da connection string é diferente:

**Spring Boot (JDBC):**
```
jdbc:postgresql://petshop-db.postgres.database.azure.com:5432/petshopdb?sslmode=require
```

**ASP.NET Core (ADO.NET):**
```
Host=petshop-db.postgres.database.azure.com;Port=5432;Database=petshopdb;Username=petshop_admin;Password=SuaSenhaForte123!@#;SSL Mode=Require
```

4. Clique em **Save** → **Continue** (confirmar restart)

---

### 2.6 Criar Static Web App (Frontend)

1. Pesquise **"Static Web Apps"**
2. **+ Create**
3. **Preencha:**
   - Subscription: `Azure for Students`
   - Resource Group: `petshop-rg`
   - Name: `petshop-frontend`
   - Plan type: `Free` ✅
   - Region: `East US 2`
   - **Deployment source:** `GitHub`
4. **Autentique com GitHub** → Autorize Azure
5. **Selecione:**
   - Organization: `andreaspsb`
   - Repository: `Fundamentos-de-Sistemas-Web-Com-BackEnd`
   - Branch: `main`
6. **Build Details:**
   - Build Presets: `Custom`
   - App location: `/frontend`
   - Api location: (vazio)
   - Output location: (vazio)
7. **Review + create** → **Create**

**URL gerada:** `https://petshop-frontend-<hash>.azurestaticapps.net`

**⚠️ Importante:** Anote essa URL e atualize o `FRONTEND_URL` nos Backends (passos 2.5 e 2.5.1)

---

### 2.7 Banco de Dados - Escolha uma opção:

#### Opção A: Neon.tech - Gratuito (3GB) ⭐ **ALTAMENTE RECOMENDADO**

1. Acesse https://neon.tech
2. **Sign in with GitHub**
3. **Create Project** → Nome: `petshop-db`
4. **Copie a Connection String** (formato JDBC):
   ```
   jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech/petshopdb?sslmode=require&user=petshop_admin&password=xxx
   ```
5. Use essa string no `DATABASE_URL` do backend

**✅ Vantagens:**
- ✅ **100% GRATUITO** - não consome seus $100
- ✅ 3 GB de storage
- ✅ 0.5 GB RAM
- ✅ Sem custo oculto
- ✅ Conecta de qualquer lugar (não precisa configurar firewall)
- ✅ Backups automáticos

#### Opção B: PostgreSQL no Azure ⚠️ **PAGO** (~$12/mês)

> **⚠️ ATENÇÃO:** Azure for Students **NÃO inclui** banco de dados gratuito. PostgreSQL consome ~$12/mês dos seus $100 de crédito.

**Se mesmo assim quiser usar Azure PostgreSQL:**

1. Pesquise **"Azure Database for PostgreSQL flexible servers"**
2. **+ Create**
3. **Preencha:**
   - Resource Group: `petshop-rg`
   - Server name: `petshop-db`
   - Region: `Brazil South`
   - PostgreSQL version: `16`
   - Compute + storage: `Burstable, B1ms` (tier mais barato)
   - Admin username: `petshop_admin`
   - Password: `SuaSenhaForte123!@#`
4. **Networking:** Allow public access from any Azure service
5. **Review + create** → **Create** (leva ~5 min)
6. **Criar database:** Databases → + Add → Nome: `petshopdb`

**Connection String:**
```
jdbc:postgresql://petshop-db.postgres.database.azure.com:5432/petshopdb?sslmode=require
```

**💸 Custo estimado:** ~$12-15/mês (consome 12-15% do seu crédito mensal)

---

## 🔐 Passo 3: Configurar Secrets no GitHub

### 3.1 Obter AZURE_CREDENTIALS

**Via Portal Azure - Cloud Shell:**

1. No Portal Azure, clique no ícone **>_** (Cloud Shell) no topo
2. Se pedir, selecione **Bash**
3. Execute:
   ```bash
   az ad sp create-for-rbac \
     --name "github-petshop-deploy" \
     --role contributor \
     --scopes /subscriptions/$(az account show --query id -o tsv)/resourceGroups/petshop-rg \
     --sdk-auth
   ```
4. **Copie TODO O JSON** retornado (incluindo chaves e vírgulas)

### 3.2 Obter AZURE_STATIC_WEB_APPS_API_TOKEN

1. Vá no seu **Static Web App** → `petshop-frontend`
2. Menu → **Overview**
3. Procure **"Manage deployment token"** → Copie o token

### 3.3 Adicionar Secrets no GitHub

1. Acesse seu repositório: https://github.com/andreaspsb/Fundamentos-de-Sistemas-Web-Com-BackEnd
2. **Settings** → **Secrets and variables** → **Actions**
3. **New repository secret**

**Adicione esses 2 secrets obrigatórios:**

| Name | Value |
|------|-------|
| `AZURE_CREDENTIALS` | JSON completo do passo 3.1 |
| `AZURE_STATIC_WEB_APPS_API_TOKEN` | Token do passo 3.2 |

> 💡 **Novidade:** O workflow agora suporta **deploy simultâneo** de Spring Boot + ASP.NET Core!

**Secrets opcionais (para scans de qualidade):**

| Name | Value | Onde obter |
|------|-------|------------|
| `SONAR_TOKEN` | Token SonarCloud | https://sonarcloud.io |
| `CODECOV_TOKEN` | Token Codecov | https://codecov.io |

---

## ⚙️ Passo 4: Ajustar Nomes dos App Services nos Workflows

### 4.1 Atualizar variáveis do workflow consolidado

Edite o arquivo `.github/workflows/cd-azure.yml`:

**Localize a seção `env:` no início do arquivo e ajuste:**

```yaml
env:
  AZURE_WEBAPP_NAME_SPRING: petshop-backend-spring    # ⬅️ Nome do passo 2.3
  AZURE_WEBAPP_NAME_ASPNET: petshop-backend-aspnet    # ⬅️ Nome do passo 2.4 (se criou)
  AZURE_STATIC_WEB_APP_NAME: petshop-frontend         # ⬅️ Nome do Static Web App
  JAVA_VERSION: '21'
  DOTNET_VERSION: '8.0.x'
  NODE_VERSION: '18'
```

**🎯 Estrutura do novo workflow:**

```yaml
jobs:
  build-backend:           # ☕ Compila Spring Boot → JAR
  build-backend-aspnet:    # 🟣 Compila ASP.NET Core → DLL
  build-frontend:          # 🎨 Prepara frontend estático
  deploy-backend-spring:   # ☁️ Deploy Spring Boot
  deploy-backend-aspnet:   # ☁️ Deploy ASP.NET Core (opcional)
  deploy-frontend:         # ☁️ Deploy frontend
  smoke-tests:             # ✅ Testa os 3 serviços
```

### 4.2 Desabilitar deploy do ASP.NET Core (se não criou o Web App)

**Se você NÃO criou o App Service ASP.NET Core no passo 2.4:**

O workflow tenta fazer deploy do ASP.NET por padrão. Para desabilitar:

**Opção 1 - Remover condição OR:**

Edite `.github/workflows/cd-azure.yml`, linha ~141:

```yaml
# Antes:
if: vars.DEPLOY_ASPNET == 'true' || true

# Depois:
if: vars.DEPLOY_ASPNET == 'true'
```

**Opção 2 - Usar variável no GitHub:**

1. Vá em **Settings → Secrets and variables → Variables**
2. **New repository variable**
3. Name: `DEPLOY_ASPNET`, Value: `false`

**Opção 3 - Comentar jobs:**

Comente as seções `build-backend-aspnet` e `deploy-backend-aspnet` no workflow.

jobs:
  build-and-deploy:
    name: Build and Deploy ASP.NET Core
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout código
      uses: actions/checkout@v4
    
    - name: Setup .NET ${{ env.DOTNET_VERSION }}
      uses: actions/setup-dotnet@v4
      with:
        dotnet-version: ${{ env.DOTNET_VERSION }}
    
    - name: Restore dependencies
      working-directory: ./backend-aspnet/PetshopApi
      run: dotnet restore
    
    - name: Build
      working-directory: ./backend-aspnet/PetshopApi
      run: dotnet build --configuration Release --no-restore
    
    - name: Publish
      working-directory: ./backend-aspnet/PetshopApi
      run: dotnet publish -c Release -o ./publish
    
    - name: Login no Azure
      uses: azure/login@v1
      with:
        creds: ${{ secrets.AZURE_CREDENTIALS }}
    
    - name: Deploy para Azure Web App
      uses: azure/webapps-deploy@v2
      with:
        app-name: ${{ env.AZURE_WEBAPP_NAME_ASPNET }}
        package: './backend-aspnet/PetshopApi/publish'
    
    - name: Verificar saúde da aplicação
      run: |
        sleep 30
        curl -f https://${{ env.AZURE_WEBAPP_NAME_ASPNET }}.azurewebsites.net/health || echo "Aguardando iniciar..."
```

### 4.3 Fazer commit das mudanças

```bash
git add .github/workflows/cd-azure.yml
git commit -m "chore: atualizar workflow Azure com suporte dual backend"
git push origin main
```

---

## 🚀 Passo 5: Deploy Automático!

1. **Faça push para main:**
   ```bash
   git push origin main
   ```

2. **Acompanhe o deploy:**
   - GitHub → Actions → https://github.com/andreaspsb/Fundamentos-de-Sistemas-Web-Com-BackEnd/actions
   - Você verá o workflow **CD - Deploy Azure** rodando com **7 jobs:**
     - ☕ build-backend (Spring Boot)
     - 🟣 build-backend-aspnet (ASP.NET Core)
     - 🎨 build-frontend
     - ☁️ deploy-backend-spring
     - ☁️ deploy-backend-aspnet
     - ☁️ deploy-frontend
     - ✅ smoke-tests (testa tudo)

3. **Aguarde ~5-10 minutos** para o primeiro deploy

4. **Ao final, você verá as URLs no log do workflow:**
   ```
   ✅ Deploy realizado com sucesso!
   
   🌐 URLs do Projeto:
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   Frontend:
     🎨 https://petshop-frontend.azurestaticapps.net
   
   Backend Spring Boot:
     ☕ https://petshop-backend-spring.azurewebsites.net
     📚 https://petshop-backend-spring.azurewebsites.net/swagger-ui.html
     🔌 https://petshop-backend-spring.azurewebsites.net/api/produtos
   
   Backend ASP.NET Core (se deployado):
     🟣 https://petshop-backend-aspnet.azurewebsites.net
     📚 https://petshop-backend-aspnet.azurewebsites.net/swagger
     🔌 https://petshop-backend-aspnet.azurewebsites.net/api/produtos
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   ```

---

## 🎯 O que acontece no Workflow Consolidado?

### CD - Deploy Azure (cd-azure.yml) - Agora com 7 jobs!

**Jobs paralelos (executam simultaneamente):**
- ✅ **build-backend**: Compila Spring Boot → gera JAR
- ✅ **build-backend-aspnet**: Compila ASP.NET Core → gera DLL
- ✅ **build-frontend**: Prepara arquivos estáticos

**Jobs sequenciais (aguardam os builds):**
- ✅ **deploy-backend-spring**: Deploy JAR no App Service
- ✅ **deploy-backend-aspnet**: Deploy ASP.NET no App Service (opcional)
- ✅ **deploy-frontend**: Deploy frontend no Static Web App

**Job final:**
- ✅ **smoke-tests**: Testa saúde dos 3 serviços (Spring Boot + ASP.NET + Frontend)

### CI - Tests (ci-tests.yml)
- ✅ Roda testes do Spring Boot
- ✅ Roda testes do ASP.NET Core
- ✅ Roda testes E2E (Playwright)
- ✅ Gera relatório de cobertura
- ✅ Upload para Codecov

### Security Scan (security-scan.yml)
- ✅ Notificação de sucesso

### Security Scan (security-scan.yml)
- ✅ Verifica dependências vulneráveis
- ✅ CodeQL analysis
- ✅ OWASP Dependency Check
- ✅ Scan de secrets

---

## 🔍 Monitoramento e Logs

### Ver Logs do Backend:

1. Portal Azure → seu **App Service**
2. Menu → **Log stream**
3. Veja logs em tempo real

### Application Insights (Gratuito):

1. App Service → **Application Insights**
2. **Turn on** → Create new: `petshop-insights`
3. Acesse métricas:
   - Performance
   - Failures
   - Live Metrics
   - Application Map

---

## 💰 Custos Estimados (Azure for Students - $100 grátis)

### ⭐ Opção RECOMENDADA: Neon.tech (Banco Externo Gratuito)

| Recurso | Tier | Custo/mês |
|---------|------|-----------|
| **App Service B1** (1 ou 2 backends) | Pago | ~$13 |
| **Neon.tech PostgreSQL** (3GB) | **Free** | **$0** ✅ |
| **Static Web Apps** | Free | $0 |
| **Application Insights** | Free | $0 |
| **TOTAL** | | **~$13/mês** |

**💰 Duração:** ~7-8 meses com $100 de crédito!

---

### ⚠️ Opção CARA: PostgreSQL no Azure (NÃO recomendado)

| Recurso | Tier | Custo/mês |
|---------|------|-----------|
| **App Service B1** (1 ou 2 backends) | Pago | ~$13 |
| **PostgreSQL B1ms Azure** | **Pago** | **~$12** 💸 |
| **Static Web Apps** | Free | $0 |
| **Application Insights** | Free | $0 |
| **TOTAL** | | **~$25/mês** |

**💸 Duração:** ~4 meses com $100 de crédito

---

### 📊 Comparação

| Cenário | Banco | Custo/mês | Duração com $100 | Recomendação |
|---------|-------|-----------|------------------|--------------|
| **1 Backend + Neon.tech** | Neon.tech | ~$13 | 7-8 meses | ⭐⭐⭐⭐⭐ |
| **2 Backends + Neon.tech** | Neon.tech | ~$13 | 7-8 meses | ⭐⭐⭐⭐⭐ |
| **1 Backend + Azure PostgreSQL** | Azure | ~$25 | 4 meses | ⭐⭐ |
| **2 Backends + Azure PostgreSQL** | Azure | ~$25 | 4 meses | ⭐⭐ |

> **💡 Dica:** Azure for Students **NÃO inclui** bancos de dados gratuitos. Use Neon.tech para economizar ~$12/mês!

> **🎯 Observação:** 2 Web Apps no mesmo App Service Plan têm o **mesmo custo** de 1, pois compartilham o plano (CPU/RAM)

---

## ✅ Checklist Final

### Recursos Azure:
- [ ] Resource Group criado
- [ ] App Service Plan criado
- [ ] Web App Spring Boot (Backend) criada
- [ ] Web App ASP.NET Core (Opcional) criada
- [ ] Static Web App (Frontend) criada
- [ ] Banco de dados configurado (Azure ou Neon.tech)
- [ ] Variáveis de ambiente configuradas (Spring Boot)
- [ ] Variáveis de ambiente configuradas (ASP.NET Core - se aplicável)
- [ ] Application Insights ativo

### GitHub:
- [ ] `AZURE_CREDENTIALS` adicionado
- [ ] `AZURE_STATIC_WEB_APPS_API_TOKEN` adicionado
- [ ] Nome do Spring Boot App atualizado no workflow
- [ ] Workflow ASP.NET Core criado (se aplicável)
- [ ] Push para main feito
- [ ] Workflows executaram com sucesso
  - [ ] CI - Tests ✅
  - [ ] CD - Deploy Spring Boot ✅
  - [ ] CD - Deploy ASP.NET Core ✅ (se aplicável)
  - [ ] Security Scan ✅

### Testes:
- [ ] Frontend acessível
- [ ] Backend API respondendo
- [ ] Swagger funcionando
- [ ] Login funcionando
- [ ] Carrinho funcionando

---

## 🆘 Problemas Comuns

### ⚠️ Erro: "Site Disabled (CODE: 403)" - App Service Parado

**Sintomas:**
- Deploy falha com erro 403
- Portal mostra "Site has been disabled"
- App Service aparece como "Stopped"

**Causa:** O App Service foi parado (manual ou automático) e precisa ser reiniciado.

**Solução Completa:**

1. **Iniciar o App Service:**
   - Portal Azure → App Services → `petshop-backend-aspnet`
   - Na página Overview, clique em **"Start"** no topo
   - Aguarde status mudar para "Running" (~30 segundos)

2. **Configurar Firewall do Azure SQL Database:**
   - Portal Azure → SQL databases → `petshop-db`
   - Menu lateral: **"Networking"** ou **"Firewalls and virtual networks"**
   - ✅ Marque: **"Allow Azure services and resources to access this server"**
   - Clique em **"Save"**
   
   **Importante:** Sem essa configuração, o App Service não consegue conectar ao banco!

3. **Adicionar Connection String no App Service:**
   - Portal Azure → App Services → `petshop-backend-aspnet`
   - Menu lateral: **"Configuration"** ou **"Environment variables"**
   - Seção **"Connection strings"** → **"+ New connection string"**
   - Preencha:
     ```
     Name: DefaultConnection
     Value: Server=tcp:petshop-db.database.windows.net,1433;Initial Catalog=petshop-db;Persist Security Info=False;User ID=petshop_admin;Password=SUA_SENHA;MultipleActiveResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;
     Type: SQLAzure
     ```
   - **⚠️ Substitua `SUA_SENHA` pela senha real do banco!**

4. **Adicionar Variáveis de Ambiente:**
   - Na mesma tela (Configuration), vá em **"Application settings"**
   - Adicione:
     ```
     ASPNETCORE_ENVIRONMENT = Production
     JWT_SECRET_KEY = [gere uma chave secreta forte - 32+ caracteres]
     ```
   - Clique em **"Save"** → **"Continue"**
   - O app será reiniciado automaticamente

5. **Verificar se funcionou:**
   - Abra: `https://petshop-backend-aspnet.azurewebsites.net/health`
   - Deve retornar: `{"status":"Healthy"}`

**📋 Guia detalhado:** Veja `.github/AZURE_SQL_FIREWALL.md` para instruções passo a passo com screenshots.

---

### Workflow falha: "Login failed"
**Solução:** Verifique se o secret `AZURE_CREDENTIALS` está correto (JSON completo).

### Backend retorna 503/502
**Solução:** Aguarde 2-3 minutos após deploy. App está iniciando.

### Frontend não conecta ao backend
**Solução:** 
1. Verifique `FRONTEND_URL` nas variáveis do backend
2. Atualize `frontend/js/api-config.js` com URL correta do backend:
   ```javascript
   const API_CONFIG = {
     BASE_URL: 'https://petshop-backend-spring.azurewebsites.net/api',
     // ou
     BASE_URL: 'https://petshop-backend-aspnet.azurewebsites.net/api',
   };
   ```

### CORS Error
**Solução:** Adicione URL do frontend em `FRONTEND_URL` (pode ser múltiplas separadas por vírgula)

### Erro de conexão com Azure SQL Database
**Solução:** 
1. Certifique-se que o firewall permite "Azure services"
2. Use o formato correto de connection string para SQL Server:
   ```
   Server=tcp:SEU_SERVIDOR.database.windows.net,1433;Initial Catalog=SEU_DB;...
   ```
3. Verifique usuário e senha
4. Para ASP.NET Core, adicione na seção "Connection strings" (não "Application settings")

### ASP.NET Core não inicia
**Solução:**
1. Verifique se criou `appsettings.Production.json`
2. Verifique se adicionou pacote `Microsoft.EntityFrameworkCore.SqlServer`
3. Verifique se a connection string está em "Connection strings" (não em "Application settings")
4. Veja logs: App Service → Log stream

---

## 📚 Próximos Passos

1. ✅ **Domínio personalizado** (opcional)
2. ✅ **Monitoramento avançado** com alertas
3. ✅ **Backup automático** do banco
4. ✅ **CI/CD para ASP.NET Core** (criar segundo Web App)
5. ✅ **Cache com Redis** (opcional)

---

## 🎉 Pronto!

Seu projeto está deployado no Azure com CI/CD completo!

**Links úteis:**
- Portal Azure: https://portal.azure.com
- GitHub Actions: https://github.com/andreaspsb/Fundamentos-de-Sistemas-Web-Com-BackEnd/actions
- Documentação completa: `AZURE_SETUP.md`

---

**Desenvolvido por:** Andreas Paulus Scherdien Berwaldt  
**Instituição:** PUCRS Online  
**Data:** Novembro de 2025
