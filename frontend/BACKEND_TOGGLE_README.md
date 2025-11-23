# 🔄 Backend Toggle - Alternância Dinâmica de Backends

## 🎯 O que foi implementado?

Um **sistema de alternância dinâmica** que permite ao frontend se comunicar com **dois backends diferentes**:

- **🟢 Spring Boot** (Java) - `http://localhost:8080/api`
- **🟣 ASP.NET Core** (C#/.NET) - `http://localhost:5000/api`

## ✨ Características

- ✅ **Toggle visual** no canto superior direito de cada página
- ✅ **Persistência** da escolha via `localStorage`
- ✅ **Notificações** ao trocar de backend
- ✅ **Detecção automática** de backend offline
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

### 1. Iniciar os Backends

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

// Alternar para ASP.NET
alternarBackend('ASPNET')

// Fazer uma requisição de teste
ApiService.get('/produtos')
  .then(produtos => console.table(produtos))

// Alternar de volta
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
- Spring Boot: verificar `@CrossOrigin` nos controllers
- ASP.NET: verificar `builder.Services.AddCors()` no `Program.cs`

## 📖 Documentação Completa

- [BACKEND_TOGGLE.md](../docs/BACKEND_TOGGLE.md) - Documentação detalhada
- [ATUALIZACAO_TOGGLE.md](../docs/ATUALIZACAO_TOGGLE.md) - Guia de atualização

## 🎯 Compatibilidade dos Backends

Ambos implementam **exatamente a mesma API**:

| Endpoint | Método | Spring Boot | ASP.NET |
|----------|--------|-------------|---------|
| `/produtos` | GET | ✅ | ✅ |
| `/produtos/{id}` | GET | ✅ | ✅ |
| `/categorias` | GET | ✅ | ✅ |
| `/servicos` | GET | ✅ | ✅ |
| `/auth/login` | POST | ✅ | ✅ |
| `/clientes` | GET/POST | ✅ | ✅ |
| `/pets` | GET/POST | ✅ | ✅ |

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
- 🔄 **Alternância dinâmica** entre backends
- 💾 **Persistência** da escolha do usuário
- 🎯 **100% compatível** com ambos os backends
- 📱 **Responsivo** em todos os dispositivos
- 🎨 **Interface moderna** e intuitiva

## 🎓 Lições Aprendidas

1. **Frontend agnóstico:** Um frontend bem arquitetado funciona com qualquer backend
2. **Padrão de configuração:** Centralizar configurações facilita manutenção
3. **DRY (Don't Repeat Yourself):** O script automático evitou edição manual de 16 arquivos
4. **Feedback visual:** Notificações melhoram a experiência do usuário
5. **Debugging:** Logs detalhados facilitam troubleshooting

---

**Desenvolvido para demonstrar a flexibilidade e interoperabilidade entre diferentes tecnologias de backend.**
