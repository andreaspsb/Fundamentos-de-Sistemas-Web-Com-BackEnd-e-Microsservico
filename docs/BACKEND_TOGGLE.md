# 🔄 Backend Toggle - Alternância entre Backends

## 📋 Visão Geral

O sistema agora suporta **quatro backends diferentes** que podem ser alternados dinamicamente:

- **Spring Boot** (Java Monolito) - porta 8080
- **ASP.NET Core** (C# Monolito) - porta 5000
- **C# Azure Functions** (Microsserviços) - portas 7071-7076
- **Java Azure Functions** (Microsserviços) - portas 7081-7086

Todos os backends implementam a mesma API REST e compartilham o mesmo banco de dados em produção.

## 🎯 Como Usar

### 1. Adicionar o Toggle às Páginas

Para habilitar o seletor de backend em qualquer página HTML, adicione estas linhas **ANTES** de outros scripts:

```html
<!-- CSS do Toggle -->
<link rel="stylesheet" href="css/backend-toggle.css">

<!-- Scripts da API (deve vir primeiro) -->
<script src="js/api-config.js"></script>

<!-- Toggle do Backend -->
<script src="js/backend-toggle.js"></script>
```

### 2. Aparecimento Visual

Um painel flutuante aparecerá **no canto superior direito** da página com:

- ✅ Botões para alternar entre Spring Boot e ASP.NET Core
- 🔵 Indicador de status do backend ativo
- 📡 Porta sendo utilizada

### 3. Alternando Backend

**Via Interface Gráfica:**
- Clique no botão do backend desejado
- Uma notificação confirma a alteração
- Todas as requisições subsequentes usarão o novo backend

**Via JavaScript (console ou código):**
```javascript
// Alternar para Spring Boot
alternarBackend('SPRINGBOOT');

// Alternar para ASP.NET Core
alternarBackend('ASPNET');

// Alternar para C# Functions
alternarBackend('FUNCTIONS');

// Alternar para Java Functions
alternarBackend('FUNCTIONS_JAVA');

// Verificar backend atual
console.log(getBackendInfo());
// { key: 'SPRINGBOOT', name: 'Spring Boot', url: 'http://localhost:8080/api', port: 8080 }
```

## 🔧 Backends Disponíveis

| Chave | Nome | URL Base | Portas | Tipo |
|-------|------|----------|--------|------|
| `SPRINGBOOT` | Spring Boot | http://localhost:8080/api | 8080 | Monolito |
| `ASPNET` | ASP.NET Core | http://localhost:5000/api | 5000 | Monolito |
| `FUNCTIONS` | C# Functions | http://localhost:707X/api | 7071-7076 | Microsserviços |
| `FUNCTIONS_JAVA` | Java Functions | http://localhost:708X/api | 7081-7086 | Microsserviços |

> **Nota:** Para microsserviços, cada serviço tem sua própria porta:
> - Auth, Customers, Pets, Catalog, Scheduling, Orders
> - O roteamento é feito automaticamente baseado no endpoint

## 🔧 Persistência

- A escolha do backend é **salva automaticamente** no `localStorage`
- Persiste entre recarregamentos de página
- Persiste entre diferentes páginas do site
- Pode ser limpa apagando o `localStorage` do navegador

## 📦 Estrutura de Arquivos

```
frontend/
├── css/
│   └── backend-toggle.css      # Estilos do componente
├── js/
│   ├── api-config.js            # Configuração da API (modificado)
│   └── backend-toggle.js        # Lógica do toggle
└── [páginas HTML]               # Adicionar imports
```

## 🎨 Personalização

### Modificar Posição do Toggle

Edite em `css/backend-toggle.css`:

```css
.backend-selector {
  top: 20px;    /* Distância do topo */
  right: 20px;  /* Distância da direita */
  
  /* Para colocar no canto esquerdo: */
  /* left: 20px; */
  /* right: auto; */
}
```

### Ocultar o Toggle em Páginas Específicas

Adicione no `<head>` da página:

```html
<style>
  .backend-selector { display: none; }
</style>
```

### Modificar Backends Disponíveis

Edite em `js/api-config.js`:

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
  // Adicione novos backends aqui
  NODEJS: {
    name: 'Node.js',
    url: 'http://localhost:3000/api',
    port: 3000
  }
};
```

## 🚀 Testando a Alternância

### Teste Manual

1. **Inicie ambos os backends:**
   ```bash
   # Terminal 1 - Spring Boot
   cd backend-springboot
   ./mvnw spring-boot:run
   
   # Terminal 2 - ASP.NET Core
   cd backend-aspnet/PetshopApi
   dotnet run
   ```

2. **Abra qualquer página do frontend:**
   - Exemplo: `frontend/index.html`

3. **Use o toggle:**
   - Clique em "Spring Boot" → requisições vão para porta 8080
   - Clique em "ASP.NET Core" → requisições vão para porta 5000

4. **Verifique no console do navegador (F12):**
   ```
   🌐 GET: http://localhost:8080/api/produtos
   // ou
   🌐 GET: http://localhost:5000/api/produtos
   ```

### Teste via Console

```javascript
// Ver backend atual
getBackendInfo()

// Fazer requisição de teste
ApiService.get('/produtos')
  .then(produtos => console.log('Produtos:', produtos))
  .catch(error => console.error(error))

// Alternar e testar novamente
alternarBackend('ASPNET')
ApiService.get('/produtos')
  .then(produtos => console.log('Produtos:', produtos))
```

## ⚠️ Detecção de Erros

Se um backend não estiver rodando, você verá uma mensagem específica:

```
⚠️ Não foi possível conectar ao servidor Spring Boot. 
Verifique se o backend está rodando em http://localhost:8080/api
```

ou

```
⚠️ Não foi possível conectar ao servidor ASP.NET Core. 
Verifique se o backend está rodando em http://localhost:5000/api
```

## 📊 Compatibilidade dos Backends

Ambos os backends implementam **EXATAMENTE** a mesma API:

| Endpoint | Spring Boot | ASP.NET Core |
|----------|-------------|--------------|
| GET /produtos | ✅ | ✅ |
| GET /categorias | ✅ | ✅ |
| GET /servicos | ✅ | ✅ |
| POST /auth/login | ✅ | ✅ |
| GET /clientes | ✅ | ✅ |
| GET /pets | ✅ | ✅ |

### Dados Iniciais Idênticos

Ambos inicializam com:
- 3 categorias (Rações, Higiene, Acessórios)
- 6 produtos com as mesmas imagens do Unsplash
- 3 serviços (Banho R$50, Tosa R$40, Banho+Tosa R$80)
- 1 usuário admin (username: admin, senha: admin123)

## 🔍 Debug

### Ativar Logs Detalhados

Já está ativo! Veja no console do navegador:

```javascript
// Logs de requisições
🌐 GET: http://localhost:5000/api/produtos
✅ Resposta: [{...}, {...}]

// Log de troca de backend
🔄 Backend alterado para: ASP.NET Core (http://localhost:5000/api)
```

### Verificar Estado do LocalStorage

```javascript
// Ver backend salvo
localStorage.getItem('backend-selecionado')

// Resetar para padrão
localStorage.removeItem('backend-selecionado')
location.reload()
```

## 📱 Responsividade

O toggle é **totalmente responsivo**:

- **Desktop:** Painel horizontal no canto superior direito
- **Mobile:** Painel vertical compacto
- **Impressão:** Oculto automaticamente

## 🎯 Próximos Passos

Agora você pode:

1. ✅ Desenvolver em qualquer backend
2. ✅ Testar ambos os backends sem alterar código
3. ✅ Comparar performance entre implementações
4. ✅ Demonstrar diferentes tecnologias mantendo o mesmo frontend

## 🆘 Troubleshooting

### Toggle não aparece
- Verifique se os arquivos CSS e JS estão sendo carregados
- Veja se há erros no console do navegador (F12)

### Requisições vão para porta errada
- Limpe o localStorage: `localStorage.clear()`
- Recarregue a página

### Backend não responde
- Verifique se o backend está rodando
- Teste diretamente no navegador: `http://localhost:8080/api/produtos`
- Verifique CORS no backend
