# Instruções para Copilot

## Estrutura do Projeto

### Backends:
- `backend-aspnet/` - API em C# .NET (referência principal)
- `backend-springboot/` - API em Java Spring
- `functions/` - C# Azure Functions
- `functions-java/` - Java Azure Functions

### Frontends:
- `frontend/` - Interface em JavaScript Vanilla e Bootstrap
- `mobile/` - Interface em React Native

### Banco de Dados
- Servidor: `petshop-db.database.windows.net`
- Banco: `petshop-db`
- Scripts SQL: `scripts/`

### Migrations
- Usar **EF Core** (`backend-aspnet/`) como fonte de verdade para o schema
- Gerar migrations com: `dotnet ef migrations add <Nome>`
- Exportar SQL para `scripts/` com: `dotnet ef migrations script`
- Nomear scripts: `001-descricao.sql`, `002-descricao.sql`
- Outros backends (Spring Boot, Functions) aplicam os scripts SQL diretamente
- **Nunca** usar migrations automáticas do Flyway/Liquibase nos outros backends

### Entidades Principais
- Clientes, Pets, Produtos, Serviços, Agendamentos, Pedidos

## Regras de Consistência

1. **Alterações em backend** → Verificar se aplica aos outros 3 backends
2. **Alterações em frontend** → Verificar se aplica ao mobile também
3. **Alterações no banco** → Deve ser compatível com todos os backends
4. **Alterações na API** → Deve ser compatível com todos os frontends
5. **Alterações full-stack** → Coordenar mudanças entre frontend e backend
6. **Antes de enviar PR** → Testar em todos os backends/frontends afetados
7. **Diferenças intencionais** → Documentar o motivo

## Padrões de Código

- URLs em kebab-case: `/api/pets`, `/api/clientes/{id}`
- Campos JSON idênticos em todos os backends
- Endpoints da API idênticos em todos os backends
- Interface do usuário consistente em todos os frontends

## Ordem de Implementação

1. Atualizar schema do banco (se necessário)
2. Implementar em `backend-aspnet/` (referência)
3. Replicar para `backend-springboot/`
4. Replicar para Azure Functions (se aplicável)
5. Atualizar `frontend/`
6. Atualizar `mobile/`

## Testes

- Testar alterações em todos os backends/frontends relevantes
- Utilizar testes automatizados sempre que possível

