# Integração Frontend + Backend - Pet Shop

> **📌 Nota sobre Backends:** Este projeto suporta **4 backends intercambiáveis** que compartilham o mesmo banco de dados:
> - **Spring Boot** (porta 8080) - Monolito Java
> - **ASP.NET Core** (porta 5000) - Monolito C#
> - **C# Azure Functions** (portas 7071-7076) - Microsserviços C#
> - **Java Azure Functions** (portas 7081-7086) - Microsserviços Java
>
> Os exemplos abaixo usam Spring Boot, mas você pode testar com qualquer um dos 4 backends.
> Use o **toggle visual** no frontend para alternar entre eles dinamicamente.

## 📋 Arquivos Criados/Atualizados

### Novos Arquivos
1. **`js/api-config.js`** - Configuração e classe de serviço para comunicação com API
2. **`js/produtos.js`** - Integração de produtos com backend
3. **`backend-springboot/src/main/java/com/petshop/config/DataInitializer.java`** - Inicialização de dados

### Arquivos Atualizados
1. **`js/cadastro.js`** - Integração do formulário de cadastro com backend
2. **`cadastro.html`** - Inclusão do script `api-config.js`

## 🚀 Como Testar

### 1. Iniciar o Backend (escolha 1 ou mais dos 4 disponíveis)

**Opção A - Spring Boot (Monolito):**
```bash
cd backend-springboot
mvn spring-boot:run
```
Aguarde até ver a mensagem:
```
✅ Dados iniciais carregados com sucesso!
   - 3 Categorias criadas
   - 6 Produtos criados
   - 3 Serviços criados
```

**Opção B - ASP.NET Core (Monolito):**
```bash
cd backend-aspnet/PetshopApi
dotnet run
```
Aguarde até ver: `Now listening on: http://localhost:5000`

**Opção C - C# Azure Functions (Microsserviços):**
```bash
cd functions
./start-all.sh      # Linux/Mac
# ou
./start-all.ps1     # Windows
```
Aguarde até ver as 6 funções iniciadas (portas 7071-7076)

**Opção D - Java Azure Functions (Microsserviços):**
```bash
cd functions-java
./start-all-java.sh      # Linux/Mac
# ou
./start-all-java.ps1     # Windows
```
Aguarde até ver as 6 funções Java iniciadas (portas 7081-7086)

### 2. Acessar o Frontend

Abra os arquivos HTML diretamente no navegador ou use um servidor HTTP local:

```bash
# Opção 1: Python
python3 -m http.server 8000

# Opção 2: Node.js (npx)
npx http-server -p 8000

# Acesse: http://localhost:8000
```

### 3. Testar Cadastro de Cliente e Pet

1. Acesse: `http://localhost:8000/cadastro.html`
2. Preencha todos os campos do formulário:
   - Dados do Cliente (nome, CPF, email, telefone, etc.)
   - Dados do Pet (nome, tipo, raça, idade, peso, etc.)
3. Clique em **Cadastrar**
4. Observe no console do navegador (F12):
   - Requisição POST para `/api/clientes`
   - Requisição POST para `/api/pets`
   - IDs dos registros criados
5. Verifique a mensagem de sucesso com os dados cadastrados

**Validações Importantes:**
- CPF único (não pode duplicar)
- Email único (não pode duplicar)
- Todos os campos obrigatórios devem ser preenchidos

### 4. Testar Visualização de Produtos

1. Acesse as páginas de categorias:
   - `http://localhost:8000/categorias/racoes-alimentacao/`
   - `http://localhost:8000/categorias/higiene-cuidados/`
   - `http://localhost:8000/categorias/acessorios-brinquedos/`

2. Os produtos serão carregados automaticamente do backend

3. Observe no console:
   - Requisição GET para `/api/categorias/ativas`
   - Requisição GET para `/api/produtos/categoria/{id}/disponiveis`
   - Lista de produtos renderizados

4. Clique em **Adicionar ao Carrinho**:
   - Produto será salvo no localStorage
   - Notificação de sucesso aparecerá

### 5. Verificar Dados no H2 Console

1. Acesse: `http://localhost:8080/h2-console`
2. Configurações:
   - **JDBC URL**: `jdbc:h2:mem:petshopdb`
   - **User Name**: `sa`
   - **Password**: (deixe em branco)
3. Execute queries SQL:

```sql
-- Ver todos os clientes
SELECT * FROM clientes;

-- Ver todos os pets
SELECT * FROM pets;

-- Ver produtos com estoque
SELECT p.nome, p.preco, p.quantidade_estoque, c.nome as categoria
FROM produtos p
JOIN categorias c ON p.categoria_id = c.id;

-- Ver serviços disponíveis
SELECT * FROM servicos WHERE ativo = true;
```

### 6. Testar API via Swagger (Monolitos)

**Spring Boot:**
1. Acesse: `http://localhost:8080/swagger-ui.html`

**ASP.NET Core:**
1. Acesse: `http://localhost:5000/swagger`

**Endpoints Comuns (disponíveis em todos os backends):**
- **GET /api/clientes** - Listar todos os clientes
- **GET /api/produtos/disponiveis** - Listar produtos disponíveis
- **GET /api/servicos/ativos** - Listar serviços ativos
- **GET /api/categorias/ativas** - Listar categorias ativas

**Microsserviços (Azure Functions):**
- Não possuem Swagger UI centralizado
- Cada função expõe seus endpoints individuais
- Teste diretamente via navegador ou curl nas portas específicas (7071-7076 para C#, 7081-7086 para Java)

## 🔍 Console do Navegador

Abra o DevTools (F12) para ver logs detalhados:

> **💡 Nota:** As URLs nos logs variam conforme o backend selecionado no toggle:
> - Spring Boot: `http://localhost:8080/api/...`
> - ASP.NET Core: `http://localhost:5000/api/...`
> - C# Functions: `http://localhost:7071-7076/api/...` (roteado por função)
> - Java Functions: `http://localhost:7081-7086/api/...` (roteado por função)

### Exemplo de Log de Cadastro Bem-Sucedido (Spring Boot):
```
✅ API Config carregado!
🎯 Backend atual: Spring Boot (http://localhost:8080/api)
📋 Formulário de cadastro carregado!
✨ Máscaras de formatação aplicadas
═══════════════════════════════════════
🚀 INICIANDO PROCESSAMENTO DO CADASTRO
═══════════════════════════════════════
👤 Cadastrando cliente... {nome: "João Silva", cpf: "12345678900", ...}
🌐 POST: http://localhost:8080/api/clientes
✅ Resposta: {id: 1, nome: "João Silva", ...}
✅ Cliente cadastrado: {id: 1, ...}
🐾 Cadastrando pet... {nome: "Rex", tipo: "cachorro", ...}
🌐 POST: http://localhost:8080/api/pets
✅ Resposta: {id: 1, nome: "Rex", ...}
✅ Pet cadastrado: {id: 1, ...}
💾 Dados salvos no localStorage: ultimoCliente
💾 Dados salvos no localStorage: ultimoPet
✅ Cadastro finalizado com sucesso!
```

### Exemplo de Log de Produtos:
```
🛍️ Página de produtos carregada!
📦 Carregando produtos da categoria: Rações e Alimentação
🌐 GET: http://localhost:8080/api/categorias/ativas
✅ Categorias carregadas: [{id: 1, nome: "Rações e Alimentação", ...}, ...]
📁 Categoria encontrada: {id: 1, nome: "Rações e Alimentação"}
🌐 GET: http://localhost:8080/api/produtos/categoria/1/disponiveis
✅ 2 produtos carregados: [{id: 1, nome: "Ração Premium...", ...}, ...]
✅ 2 produtos renderizados
```

## 🎯 Dados Iniciais Disponíveis

> **📌 Importante:** Todos os 4 backends compartilham o **mesmo banco de dados** (Azure SQL em produção, H2/SQLite em desenvolvimento). Os dados criados em um backend são visíveis em todos os outros.

### Categorias:
1. Rações e Alimentação
2. Higiene e Cuidados
3. Acessórios e Brinquedos

### Produtos (6 produtos):
1. **Ração Premium para Cães Adultos** - R$ 150,00 (50 em estoque)
2. **Ração Hipoalergênica para Gatos** - R$ 95,00 (40 em estoque)
3. **Kit Xampu e Condicionador** - R$ 65,00 (60 em estoque)
4. **Antipulgas e Carrapatos** - R$ 85,00 (35 em estoque)
5. **Kit Coleira e Guia Resistente** - R$ 45,00 (80 em estoque)
6. **Cama Ortopédica para Cães** - R$ 180,00 (25 em estoque)

### Serviços (3 serviços):
1. **Banho** - R$ 50,00
2. **Tosa** - R$ 40,00
3. **Banho + Tosa** (Combo) - R$ 80,00

## 🛠️ Próximos Passos

### Funcionalidades Pendentes:
1. **Agendamentos** - Integrar formulário de agendamento com backend
2. **Carrinho de Compras** - Criar página de carrinho e checkout
3. **Listagem de Clientes** - Página administrativa
4. **Busca de Produtos** - Implementar busca e filtros
5. **Histórico de Pedidos** - Visualizar pedidos do cliente

### Para Adicionar aos HTMLs de Produtos:

Adicione o script `produtos.js` antes do fechamento do `</body>`:

```html
<!-- JavaScript Customizado -->
<script src="../../js/api-config.js"></script>
<script src="../../js/script.js"></script>
<script src="../../js/produtos.js"></script>
```

## 📝 Notas Importantes

1. **CORS**: O backend está configurado para aceitar requisições de qualquer origem (`*`)
2. **Banco de Dados**: H2 in-memory - os dados são perdidos ao reiniciar o servidor
3. **LocalStorage**: Dados do carrinho são salvos no navegador
4. **Validações**: O backend valida CPF e email únicos
5. **Formato de Dados**: Datas em formato ISO (yyyy-MM-dd), CPF e telefone sem formatação

## 🐛 Solução de Problemas

### Erro: "Failed to fetch"
- Verifique qual backend está selecionado no toggle
- Verifique se o backend correspondente está rodando:
  - Spring Boot: `http://localhost:8080`
  - ASP.NET Core: `http://localhost:5000`
  - C# Functions: portas 7071-7076
  - Java Functions: portas 7081-7086
- Verifique se não há firewall bloqueando
- Teste alternar para outro backend no toggle

### Erro: "CPF já cadastrado"
- Use um CPF diferente para cada teste
- Ou reinicie o servidor para limpar o banco
- Lembre-se: dados são compartilhados entre os 4 backends

### Produtos não aparecem
- Abra o console (F12) para ver erros
- Verifique se o DataInitializer foi executado no backend escolhido
- Verifique a URL da categoria no código
- Teste alternar entre backends usando o toggle

### Erro 400 (Bad Request)
- Verifique os dados enviados no console
- Confirme que todos os campos obrigatórios foram preenchidos
- Verifique se o formato dos dados está correto (JSON camelCase)
- Verifique formato de data (yyyy-MM-dd)
