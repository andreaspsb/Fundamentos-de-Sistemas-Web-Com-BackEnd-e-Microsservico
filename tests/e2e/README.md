# Testes Playwright - Pet Shop (Multi-Backend)

Suite de testes E2E usando Playwright para o sistema Pet Shop, testando todos os 4 backends.

## 🏗️ Arquitetura

Os testes são executados **sequencialmente** contra cada backend:

| Backend | Nome | Porta | Tipo |
|---------|------|-------|------|
| `aspnet` | ASP.NET Core | 5000 | Monolith |
| `springboot` | Spring Boot | 8080 | Monolith |
| `functions` | C# Functions | 7071 | Microservices |
| `functions-java` | Java Functions | 7081 | Microservices |

## 📋 Testes Implementados

### 1. **smoke.spec.js** - Testes de Fumaça
- ✅ Homepage carrega com navegação visível
- ✅ Backend responde ao health check
- ✅ Login com admin funciona

### 2. **auth.spec.js** - Autenticação
- ✅ Login e logout funcionam corretamente
- ✅ Sessão persiste após recarregar página
- ✅ Páginas protegidas redirecionam para login

### 3. **carrinho.spec.js** - Carrinho de Compras
- ✅ Adicionar produto ao carrinho atualiza contador
- ✅ Visualizar carrinho mostra produto adicionado
- ✅ Remover produto do carrinho

## 🚀 Pré-requisitos

### 1. Instalar dependências
```bash
npm install
npx playwright install chromium
```

### 2. Iniciar TODOS os backends
```bash
./start-all.sh
```

⚠️ **IMPORTANTE**: Todos os 4 backends devem estar rodando antes de executar os testes!

### 3. Verificar que backends estão ativos
```bash
./start-all.sh status
```

## ▶️ Executar Testes

### Todos os backends (recomendado)
```bash
npm test
```

Isso executa todos os testes em sequência: ASPNET → Spring Boot → Functions → Functions-Java

### Backend específico
```bash
# Apenas ASP.NET
npx playwright test --project=aspnet

# Apenas Spring Boot
npx playwright test --project=springboot

# Apenas C# Functions
npx playwright test --project=functions

# Apenas Java Functions
npx playwright test --project=functions-java
```

### Teste específico
```bash
# Apenas smoke tests no ASP.NET
npx playwright test smoke --project=aspnet

# Apenas auth tests em todos os backends
npx playwright test auth
```

### Modo interativo
```bash
npm run test:ui
```

### Modo debug
```bash
npm run test:debug
```

## 📊 Relatórios

### Ver relatório HTML
```bash
npm run test:report
```

### Arquivos gerados
- `playwright-report/` - Relatório HTML
- `test-results/` - Screenshots e traces de falhas

## 🔧 Estrutura de Arquivos

```
tests/e2e/
├── test-helpers.js    # Funções auxiliares (setupBackend, loginAsAdmin, etc.)
├── smoke.spec.js      # Testes de fumaça básicos
├── auth.spec.js       # Testes de autenticação
├── carrinho.spec.js   # Testes de carrinho de compras
└── README.md          # Esta documentação
```

## 🛠️ Helpers Disponíveis

```javascript
const {
  setupBackend,        // Configura localStorage para backend correto
  checkBackendHealth,  // Verifica se backend está rodando
  loginAsAdmin,        // Faz login como admin
  logout,              // Faz logout
  clearUserState,      // Limpa tokens e dados do usuário
  clearCart,           // Limpa carrinho
  getCartCount,        // Obtém contador do carrinho
  goToCategory,        // Navega para categoria de produtos
  addFirstProductToCart, // Adiciona primeiro produto ao carrinho
} = require('./test-helpers');
```

## 🐛 Troubleshooting

### Backend não está rodando
```
Error: Backend ASP.NET Core não está acessível
```
**Solução**: Execute `./start-all.sh` e verifique com `./start-all.sh status`

### Timeout em testes
```
Timeout exceeded
```
**Solução**: Verifique se os backends estão respondendo corretamente. Aumente timeout no `playwright.config.js` se necessário.

### Frontend não inicia
```
Error: Port 5500 is in use
```
**Solução**: Mate processos na porta 5500 ou configure `reuseExistingServer: true` no config.

## 📝 Convenções

1. **Cada teste deve funcionar em todos os 4 backends** - use `setupBackend()` no `beforeEach`
2. **Testes são independentes** - cada teste limpa seu estado
3. **Use os helpers** - evite código duplicado
4. **Logs informativos** - helpers usam `console.log` com emojis para debug

## 🔜 Próximos Passos

- [ ] Adicionar testes de checkout
- [ ] Adicionar testes de agendamento
- [ ] Adicionar testes de cadastro
- [ ] Adicionar testes mobile (viewports)
- [ ] Adicionar testes de acessibilidade

---

**Autor**: Sistema Pet Shop  
**Framework**: Playwright  
**Última atualização**: Dezembro 2025
