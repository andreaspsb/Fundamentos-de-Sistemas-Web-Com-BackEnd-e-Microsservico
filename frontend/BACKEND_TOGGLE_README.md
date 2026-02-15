# 🔄 Backend Toggle - Alternância Dinâmica de Backends

## 🎯 O que foi implementado?

Um **sistema de alternância dinâmica** que permite ao frontend se comunicar com **quatro backends diferentes**:

- **🟢 Spring Boot** (Java Monolito) - `http://localhost:8080/api`
- **🟣 ASP.NET Core** (C#/.NET Monolito) - `http://localhost:5000/api`
- **🔵 C# Azure Functions** (Microsserviços) - `http://localhost:7071-7076/api`
- **🟡 Java Azure Functions** (Microsserviços) - `http://localhost:7081-7086/api`

## ✨ Características

- ✅ **Toggle visual** com 4 opções de backend
- ✅ **Persistência** da escolha via `localStorage`
- ✅ **Notificações** ao trocar de backend
- ✅ **Detecção automática** de backend offline
- ✅ **Suporte a microsserviços** com múltiplas portas
- ✅ **Roteamento inteligente** para serviços específicos
- ✅ **Totalmente responsivo** (desktop, tablet, mobile)
- ✅ **16 páginas HTML** atualizadas automaticamente

## 📁 Arquivos Criados/Modificados

### Novos Arquivos
```
frontend/
├── css/
│   └── backend-toggle.css         # Estilos do componente visual
└── js/
    └── backend-toggle.js          # Lógica de alternância

docs/
├── BACKEND_TOGGLE.md              # Documentação completa
└── ATUALIZACAO_TOGGLE.md          # Guia de atualização manual

scripts/
└── update-toggle.sh               # Script de atualização automática
```

### Arquivos Modificados
```
frontend/
├── js/
│   └── api-config.js              # Adicionado suporte dinâmico a múltiplos backends
└── [16 páginas HTML]              # Todas atualizadas com o toggle
```

## 🚀 Como Usar

### 1. Iniciar os Backends (escolha até 4)

**Terminal 1 - Spring Boot:**
```bash
cd backend-springboot
./mvnw spring-boot:run
```

**Terminal 2 - ASP.NET Core:**
```bash
cd backend-aspnet/PetshopApi
dotnet run
```

**Terminal 3 - C# Functions (Opcional):**
```bash
cd functions
./start-all.sh      # Linux/Mac
# ou
./start-all.ps1     # Windows
```

**Terminal 4 - Java Functions (Opcional):**
```bash
cd functions-java
./start-all-java.sh      # Linux/Mac
# ou
./start-all-java.ps1     # Windows
```

### 2. Abrir o Frontend

Abra qualquer página HTML no navegador, por exemplo:
```bash
cd frontend
# Abrir index.html no navegador
```

### 3. Usar o Toggle

- Um painel aparecerá no **canto superior direito**
- Clique em **Spring Boot** ou **ASP.NET Core**
- Veja a notificação de confirmação
- Todas as requisições agora usam o backend selecionado

## 🎨 Interface Visual

```
┌─────────────────────────────────────────┐
│ Backend: [Spring Boot] [ASP.NET Core]  │
│          🟢 localhost:8080              │
└─────────────────────────────────────────┘
```

- **Verde**: Backend Spring Boot
- **Roxo**: Backend ASP.NET Core
- **Indicador pulsante**: Backend ativo

## 🧪 Testando

### Via Interface
1. Abra `frontend/index.html`
2. Abra o console do navegador (F12)
3. Veja os logs:
   ```
   ✅ API Config carregado!
   🎯 Backend atual: Spring Boot (http://localhost:8080/api)
   ✅ Backend Toggle inicializado
   ```
4. Clique no botão do outro backend
5. Observe a notificação e os logs de requisição

### Via Console JavaScript
```javascript
// Ver backend atual
getBackendInfo()
// { key: 'SPRINGBOOT', name: 'Spring Boot', url: 'http://localhost:8080/api', port: 8080 }

// Alternar para ASP.NET Core (Monolito)
alternarBackend('ASPNET')

// Alternar para C# Azure Functions (Microsserviços)
alternarBackend('FUNCTIONS')
// Nota: Requisições serão roteadas para funções específicas (7071-7076)

// Alternar para Java Azure Functions (Microsserviços)
alternarBackend('FUNCTIONS_JAVA')
// Nota: Requisições serão roteadas para funções Java (7081-7086)

// Fazer uma requisição de teste
ApiService.get('/produtos')
  .then(produtos => console.table(produtos))

// Testar autenticação em diferentes backends
alternarBackend('FUNCTIONS')
ApiService.post('/auth/login', { username: 'admin', password: 'admin123' })
  .then(response => console.log('Login em Functions:', response))

alternarBackend('FUNCTIONS_JAVA')
ApiService.post('/auth/login', { username: 'admin', password: 'admin123' })
  .then(response => console.log('Login em Functions Java:', response))

// Alternar de volta para monolito
alternarBackend('SPRINGBOOT')
```

## 📊 Páginas Atualizadas

| Página | Status | Caminho |
|--------|--------|---------|
| Home | ✅ | `index.html` |
| Login | ✅ | `login.html` |
| Cadastro | ✅ | `cadastro.html` |
| Carrinho | ✅ | `carrinho.html` |
| Checkout | ✅ | `checkout.html` |
| Meus Pedidos | ✅ | `meus-pedidos.html` |
| Rações | ✅ | `categorias/racoes-alimentacao/index.html` |
| Higiene | ✅ | `categorias/higiene-cuidados/index.html` |
| Acessórios | ✅ | `categorias/acessorios-brinquedos/index.html` |
| Serviços | ✅ | `servicos/index.html` |
| Agendamento | ✅ | `servicos/agendamento.html` |
| Admin Home | ✅ | `admin/index.html` |
| Admin Produtos | ✅ | `admin/produtos.html` |
| Admin Clientes | ✅ | `admin/clientes.html` |
| Admin Pedidos | ✅ | `admin/pedidos.html` |
| Admin Agendamentos | ✅ | `admin/agendamentos.html` |

**Total: 16 páginas** ✅

## 🔧 Manutenção

### Adicionar Toggle em Nova Página

```html
<!-- No <head> -->
<link rel="stylesheet" href="css/backend-toggle.css">

<!-- Antes do </body> -->
<script src="js/api-config.js"></script>
<script src="js/backend-toggle.js"></script>
```

### Atualizar Múltiplas Páginas de Uma Vez

```bash
./scripts/update-toggle.sh
```

### Adicionar Novo Backend

Edite `frontend/js/api-config.js`:

```javascript
const BACKENDS = {
  SPRINGBOOT: {
    name: 'Spring Boot',
    url: 'http://localhost:8080/api',
    port: 8080
  },
  ASPNET: {
    name: 'ASP.NET Core',
    url: 'http://localhost:5000/api',
    port: 5000
  },
  NODEJS: {  // Novo backend
    name: 'Node.js',
    url: 'http://localhost:3000/api',
    port: 3000
  }
};
```

Depois atualize o HTML em `backend-toggle.js`:

```javascript
<button class="backend-option" data-backend="NODEJS">
  Node.js
</button>
```

## 🐛 Troubleshooting

### Toggle não aparece
- Verifique se os arquivos CSS e JS estão sendo carregados (F12 > Network)
- Confirme que não há erros no console

### Requisições vão para porta errada
```javascript
// Limpar localStorage e recarregar
localStorage.clear()
location.reload()
```

### Backend não responde
- Verifique se o backend está rodando
- Teste diretamente: `http://localhost:8080/api/produtos`
- Verifique CORS no backend

### Erro de CORS
- **Spring Boot:** verificar `@CrossOrigin` nos controllers
- **ASP.NET:** verificar `builder.Services.AddCors()` no `Program.cs`
- **C# Functions:** verificar `local.settings.json` - seção `Host.CORS`
- **Java Functions:** verificar `local.settings.json` ou headers em `HttpResponseMessage`

## 📖 Documentação Completa

- [BACKEND_TOGGLE.md](../docs/BACKEND_TOGGLE.md) - Documentação detalhada
- [ATUALIZACAO_TOGGLE.md](../docs/ATUALIZACAO_TOGGLE.md) - Guia de atualização

## 🎯 Compatibilidade dos Backends

**Os 4 backends implementam exatamente o mesmo contrato de API**:

| Endpoint | Método | Spring Boot | ASP.NET | C# Functions | Java Functions |
|----------|--------|-------------|---------|--------------|----------------|
| `/produtos` | GET | ✅ | ✅ | ✅ | ✅ |
| `/produtos/{id}` | GET | ✅ | ✅ | ✅ | ✅ |
| `/categorias` | GET | ✅ | ✅ | ✅ | ✅ |
| `/servicos` | GET | ✅ | ✅ | ✅ | ✅ |
| `/auth/login` | POST | ✅ | ✅ | ✅ | ✅ |
| `/clientes` | GET/POST | ✅ | ✅ | ✅ | ✅ |
| `/pets` | GET/POST | ✅ | ✅ | ✅ | ✅ |

**Notas Importantes:**
- **Monolitos** (Spring Boot, ASP.NET): Todos os endpoints na mesma porta
- **Microsserviços** (Functions): Requisições roteadas automaticamente para a função apropriada
- **Banco de Dados Compartilhado**: Todos conectam ao mesmo banco (Azure SQL ou H2/SQLite local)

### Dados Iniciais Idênticos

- 3 categorias (Rações, Higiene, Acessórios)
- 6 produtos (mesmas imagens Unsplash)
- 3 serviços (Banho, Tosa, Banho+Tosa)
- 1 usuário admin (admin/admin123)

## 💡 Casos de Uso

1. **Desenvolvimento:** Trabalhar em um backend enquanto o outro serve de referência
2. **Testes:** Comparar comportamento entre implementações
3. **Performance:** Medir diferenças de velocidade
4. **Demonstração:** Mostrar que frontends podem ser agnósticos de tecnologia
5. **Aprendizado:** Comparar arquiteturas Java vs C#

## ✅ Resultado Final

- ✨ **16 páginas** com toggle funcional
- 🔄 **Alternância dinâmica** entre **4 backends diferentes**
- 💾 **Persistência** da escolha do usuário
- 🎯 **100% compatível** com todos os backends (monolitos e microsserviços)
- 📱 **Responsivo** em todos os dispositivos
- 🎨 **Interface moderna** e intuitiva

## 🎓 Lições Aprendidas

1. **Frontend agnóstico:** Um frontend bem arquitetado funciona com qualquer backend (monolito ou microsserviços)
2. **Padrão de configuração:** Centralizar configurações facilita manutenção de múltiplos backends
3. **DRY (Don't Repeat Yourself):** O script automático evitou edição manual de 16 arquivos
4. **Feedback visual:** Notificações melhoram a experiência do usuário
5. **Debugging:** Logs detalhados facilitam troubleshooting
6. **Interoperabilidade:** Banco de dados compartilhado entre 4 backends diferentes demonstra flexibilidade arquitetural

---

**Desenvolvido para demonstrar a flexibilidade e interoperabilidade entre diferentes tecnologias de backend (Java, C#) e arquiteturas (monolítica vs microsserviços).**
