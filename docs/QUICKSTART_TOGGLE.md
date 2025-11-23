# ⚡ Quick Start - Backend Toggle

## 🚀 Início Rápido em 3 Passos

### 1️⃣ Iniciar os Backends

**Terminal 1:**
```bash
cd backend-springboot
./mvnw spring-boot:run
```
Aguarde até ver: `Started PetshopApplication in X seconds`

**Terminal 2:**
```bash
cd backend-aspnet/PetshopApi
dotnet run
```
Aguarde até ver: `Now listening on: http://localhost:5000`

### 2️⃣ Abrir o Frontend

```bash
cd frontend
# Abra qualquer arquivo HTML no navegador
firefox demo-toggle.html
```

### 3️⃣ Usar o Toggle

1. Veja o painel no canto superior direito
2. Clique em "Spring Boot" ou "ASP.NET Core"
3. Veja a notificação de confirmação
4. Pronto! Todas as requisições agora usam o backend selecionado

## 🎯 Teste Rápido

Abra o console do navegador (F12) e execute:

```javascript
// Ver backend atual
getBackendInfo()

// Alternar para ASP.NET
alternarBackend('ASPNET')

// Fazer requisição
ApiService.get('/produtos').then(console.table)

// Alternar para Spring Boot
alternarBackend('SPRINGBOOT')

// Fazer requisição novamente
ApiService.get('/produtos').then(console.table)
```

## 📄 Páginas de Demonstração

- **demo-toggle.html** - Demo interativa com console e exemplos
- **index.html** - Página principal com produtos
- **login.html** - Teste login com ambos os backends
- **carrinho.html** - Teste carrinho de compras

## ⚠️ Troubleshooting

### Toggle não aparece?
- Recarregue com Ctrl+Shift+R (força reload sem cache)
- Verifique console (F12) por erros

### Erro "Failed to fetch"?
- Verifique se os backends estão rodando
- Teste direto no navegador:
  - Spring Boot: http://localhost:8080/api/produtos
  - ASP.NET: http://localhost:5000/api/produtos

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

Ambos os backends têm os mesmos dados:

**Login:**
- Usuário: `admin`
- Senha: `admin123`

**Produtos:** 6 produtos em 3 categorias
**Serviços:** Banho (R$50), Tosa (R$40), Banho+Tosa (R$80)

## 💡 Dicas

- Use F12 para ver os logs detalhados das requisições
- O backend selecionado persiste entre recarregamentos
- Você pode alternar a qualquer momento
- Ambos os backends retornam dados idênticos

---

**Pronto para começar!** 🚀
