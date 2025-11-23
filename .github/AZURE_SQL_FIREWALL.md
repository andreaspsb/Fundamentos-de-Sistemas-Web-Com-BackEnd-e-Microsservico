# Configurar Firewall do Azure SQL Database

## 🔥 Permitir acesso dos App Services ao Banco de Dados

### No Portal Azure:

1. **Acesse o Azure SQL Database:**
   - Vá para: https://portal.azure.com
   - Procure por "SQL databases"
   - Clique em `petshop-db`

2. **Configurar Firewall:**
   - No menu lateral esquerdo, clique em **"Networking"** ou **"Firewalls and virtual networks"**
   
3. **Adicionar regras:**
   
   **Opção A - Permitir serviços Azure (RECOMENDADO):**
   ```
   ✅ Marque: "Allow Azure services and resources to access this server"
   ```
   
   **Opção B - Adicionar IP específico (para teste local):**
   ```
   Nome: Meu-IP-Local
   IP Inicial: [seu IP atual]
   IP Final: [seu IP atual]
   ```

4. **Salvar:**
   - Clique em **"Save"** no topo da página
   - Aguarde a confirmação (pode levar ~1 minuto)

---

## 🔑 Configurar Connection String no App Service ASP.NET

### No Portal Azure:

1. **Acesse o App Service:**
   - Procure por "App Services"
   - Clique em `petshop-backend-aspnet`

2. **Configurar Connection String:**
   - No menu lateral esquerdo, vá em **"Configuration"** ou **"Environment variables"**
   - Na seção **"Connection strings"**, clique em **"+ New connection string"**

3. **Adicionar Connection String:**
   ```
   Name: DefaultConnection
   Value: Server=tcp:petshop-db.database.windows.net,1433;Initial Catalog=petshop-db;Persist Security Info=False;User ID=petshop_admin;Password=SUA_SENHA_AQUI;MultipleActiveResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;
   Type: SQLAzure
   ```
   
   **⚠️ IMPORTANTE:** Substitua `SUA_SENHA_AQUI` pela senha real que você definiu ao criar o banco de dados!

4. **Adicionar outras variáveis de ambiente (Application settings):**
   
   Clique na aba **"Application settings"** e adicione:
   
   ```
   Name: ASPNETCORE_ENVIRONMENT
   Value: Production
   ```
   
   ```
   Name: JWT_SECRET_KEY
   Value: [gere uma chave secreta forte - mínimo 32 caracteres]
   ```

5. **Salvar:**
   - Clique em **"Save"** no topo
   - Confirme clicando em **"Continue"**
   - O App Service será **reiniciado automaticamente**

---

## 🚀 Iniciar o App Service (se estiver parado)

### No Portal Azure:

1. **Acesse o App Service:**
   - Procure por "App Services"
   - Clique em `petshop-backend-aspnet`

2. **Verificar Status:**
   - No topo da página **"Overview"**, você verá o status:
     - ✅ **Running** → Está rodando
     - ⚠️ **Stopped** → Está parado

3. **Iniciar o App Service:**
   - Se estiver **Stopped**, clique no botão **"Start"** no topo
   - Aguarde ~30 segundos até o status mudar para **"Running"**

4. **Verificar logs (se continuar falhando):**
   - No menu lateral, vá em **"Log stream"**
   - Deixe aberto para ver os logs em tempo real
   - Faça um novo deploy e observe os erros

---

## 🔍 Verificar se funcionou

### Teste direto no navegador:

```
https://petshop-backend-aspnet.azurewebsites.net/health
```

Resposta esperada:
```json
{
  "status": "Healthy"
}
```

### Teste da API:

```
https://petshop-backend-aspnet.azurewebsites.net/api/produtos
```

---

## ❌ Troubleshooting - Erros Comuns

### Erro: "Cannot open server"
**Causa:** Firewall não permite conexão do App Service ao SQL  
**Solução:** Marque "Allow Azure services..." nas configurações de Networking

### Erro: "Login failed for user 'petshop_admin'"
**Causa:** Senha errada na connection string  
**Solução:** Verifique a senha no Configuration → Connection strings

### Erro: "The site has been disabled"
**Causa:** App Service está stopped ou plano gratuito expirou  
**Solução:** 
1. Verifique no Overview se está "Running"
2. Clique em "Start" se necessário
3. Verifique se o plano "Free F1" está disponível na sua assinatura

### App Service não inicia após configurar
**Causa:** Pode haver erro no código ou na configuração  
**Solução:**
1. Vá em "Log stream" para ver os logs em tempo real
2. Verifique se a connection string está correta
3. Verifique se o ASPNETCORE_ENVIRONMENT está como "Production"

---

## 📋 Checklist Final

Antes de fazer um novo deploy, confirme:

- [ ] Firewall do SQL permite "Azure services"
- [ ] Connection string configurada em Configuration → Connection strings
- [ ] ASPNETCORE_ENVIRONMENT = Production
- [ ] JWT_SECRET_KEY definida
- [ ] App Service está "Running" (não "Stopped")
- [ ] Secret AZURE_CREDENTIALS está configurado no GitHub
- [ ] Teste manual funcionou: https://petshop-backend-aspnet.azurewebsites.net/health

---

## 🎯 Próximos Passos

Depois de configurar tudo:

1. Volte ao GitHub
2. Vá em **Actions**
3. Execute o workflow **"CD - Deploy Azure"** manualmente
4. Acompanhe os logs para ver se o deploy funciona

Se tudo estiver certo, o deploy deve funcionar! 🚀
