# ⚡ Quick Start - Backend Toggle

## 🚀 Início Rápido em 3 Passos

### 1️⃣ Iniciar os Backends (escolha 1 ou mais dos 4 disponíveis)

**Terminal 1 - Spring Boot (Monolito):**
```bash
cd backend-springboot
./mvnw spring-boot:run
```
Aguarde até ver: `Started PetshopApplication in X seconds`

**Terminal 2 - ASP.NET Core (Monolito):**
```bash
cd backend-aspnet/PetshopApi
dotnet run
```
Aguarde até ver: `Now listening on: http://localhost:5000`

**Terminal 3 - C# Azure Functions (Microsserviços) [Opcional]:**
```bash
cd functions
./start-all.sh      # Linux/Mac
# ou
./start-all.ps1     # Windows
```
Aguarde até ver as 6 funções iniciadas (portas 7071-7076)

**Terminal 4 - Java Azure Functions (Microsserviços) [Opcional]:**
```bash
cd functions-java
./start-all-java.sh      # Linux/Mac
# ou
./start-all-java.ps1     # Windows
```
Aguarde até ver as 6 funções Java iniciadas (portas 7081-7086)

### 2️⃣ Abrir o Frontend

```bash
cd frontend
# Abra qualquer arquivo HTML no navegador
firefox demo-toggle.html
```

### 3️⃣ Usar o Toggle

1. Veja o painel no canto superior direito
2. Clique em um dos 4 backends disponíveis:
   - **Spring Boot** (porta 8080) - Monolito Java
   - **ASP.NET Core** (porta 5000) - Monolito C#
   - **C# Functions** (portas 7071-7076) - Microsserviços C#
   - **Java Functions** (portas 7081-7086) - Microsserviços Java
3. Veja a notificação de confirmação
4. Pronto! Todas as requisições agora usam o backend selecionado

## 🎯 Teste Rápido

Abra o console do navegador (F12) e execute:

```javascript
// Ver backend atual
getBackendInfo()

// Testar Monolitos
alternarBackend('ASPNET')
ApiService.get('/produtos').then(console.table)

alternarBackend('SPRINGBOOT')
ApiService.get('/produtos').then(console.table)

// Testar Microsserviços (se disponíveis)
alternarBackend('FUNCTIONS')
ApiService.get('/produtos').then(produtos => {
  console.log('✅ C# Functions (porta 7074 - Catalog):', produtos.length, 'produtos')
  console.table(produtos)
})

alternarBackend('FUNCTIONS_JAVA')
ApiService.get('/auth/login', {
  method: 'POST',
  body: JSON.stringify({ username: 'admin', password: 'admin123' })
}).then(response => {
  console.log('✅ Java Functions (porta 7081 - Auth):', response)
})
```

## 📄 Páginas de Demonstração

- **demo-toggle.html** - Demo interativa com console e exemplos dos 4 backends
- **index.html** - Página principal com produtos
- **login.html** - Teste login com os 4 backends
- **carrinho.html** - Teste carrinho de compras

## ⚠️ Troubleshooting

### Toggle não aparece?
- Recarregue com Ctrl+Shift+R (força reload sem cache)
- Verifique console (F12) por erros

### Erro "Failed to fetch"?
- Verifique se os backends estão rodando
- Teste direto no navegador:
  - **Spring Boot:** http://localhost:8080/api/produtos
  - **ASP.NET:** http://localhost:5000/api/produtos
  - **C# Functions (Catalog):** http://localhost:7074/api/produtos
  - **Java Functions (Catalog):** http://localhost:7084/api/produtos

### Requisições vão para porta errada?
```javascript
// Limpar localStorage e recarregar
localStorage.clear()
location.reload()
```

## 📚 Documentação Completa

- [BACKEND_TOGGLE.md](../docs/BACKEND_TOGGLE.md) - Documentação detalhada
- [TOGGLE_IMPLEMENTATION_SUMMARY.md](../TOGGLE_IMPLEMENTATION_SUMMARY.md) - Resumo completo

## 🎓 Dados de Teste

**Todos os 4 backends compartilham o mesmo banco de dados** e têm dados idênticos:

**Login:**
- Usuário: `admin`
- Senha: `admin123`

**Produtos:** 6 produtos em 3 categorias
**Serviços:** Banho (R$50), Tosa (R$40), Banho+Tosa (R$80)

**Observação:** Dados criados em um backend são visíveis em todos os outros.

## 💡 Dicas

- Use F12 para ver os logs detalhados das requisições
- O backend selecionado persiste entre recarregamentos
- Você pode alternar entre os 4 backends a qualquer momento
- Todos os backends retornam dados idênticos (mesmo banco de dados compartilhado)
- Microsserviços (Functions) distribuem requisições automaticamente entre as funções apropriadas

---

**Pronto para começar!** 🚀
