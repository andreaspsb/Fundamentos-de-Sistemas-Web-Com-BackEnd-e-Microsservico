# 🔧 Guia de Atualização - Backend Toggle

> **📌 Nota:** Este sistema suporta **4 backends intercambiáveis**:
> - Spring Boot (porta 8080) - Monolito Java
> - ASP.NET Core (porta 5000) - Monolito C#
> - C# Azure Functions (portas 7071-7076) - Microsserviços C#
> - Java Azure Functions (portas 7081-7086) - Microsserviços Java
>
> Todos compartilham o mesmo banco de dados. O toggle permite alternar entre eles dinamicamente.

## ⚡ Adição Rápida do Toggle nas Páginas

Para adicionar o seletor de backend em qualquer página HTML do projeto, siga estes passos:

### 1️⃣ Adicionar CSS no `<head>`

Localize a tag `<head>` e adicione **logo após o Bootstrap CSS**:

```html
<!-- Backend Toggle CSS -->
<link rel="stylesheet" href="css/backend-toggle.css">
```

### 2️⃣ Adicionar JavaScript antes do `</body>`

Localize onde o `api-config.js` é carregado e adicione **logo após**:

```html
<script src="js/api-config.js"></script>
<script src="js/backend-toggle.js"></script>
```

## 📄 Páginas que Precisam de Atualização

### ✅ Já Atualizado
- `index.html`

### ⚠️ Páginas Pendentes

Execute este comando para adicionar automaticamente em todas as páginas HTML:

```bash
# Ir para o diretório do projeto
cd /home/andreas/repositoriosgit/Fundamentos-de-Sistemas-Web-Com-BackEnd/frontend

# Listar todas as páginas HTML
find . -name "*.html" -type f
```

**Lista de páginas:**
- [ ] `cadastro.html`
- [ ] `carrinho.html`
- [ ] `checkout.html`
- [ ] `login.html`
- [ ] `meus-pedidos.html`
- [ ] `admin/*.html`
- [ ] `categorias/**/*.html`
- [ ] `servicos/*.html`

## 🤖 Script Automático de Atualização

Crie um script para adicionar o toggle em todas as páginas de uma vez:

```bash
#!/bin/bash
# update-toggle.sh

PAGES=(
  "cadastro.html"
  "carrinho.html"
  "checkout.html"
  "login.html"
  "meus-pedidos.html"
)

for page in "${PAGES[@]}"; do
  echo "Atualizando $page..."
  
  # Adicionar CSS (se ainda não existe)
  if ! grep -q "backend-toggle.css" "$page"; then
    sed -i '/<link.*bootstrap.*css/a\    <!-- Backend Toggle CSS -->\n    <link rel="stylesheet" href="css/backend-toggle.css">' "$page"
  fi
  
  # Adicionar JS (se ainda não existe)
  if ! grep -q "backend-toggle.js" "$page"; then
    sed -i 's|<script src="js/api-config.js"></script>|<script src="js/api-config.js"></script>\n    <script src="js/backend-toggle.js"></script>|' "$page"
  fi
  
  echo "✅ $page atualizado!"
done

echo ""
echo "🎉 Todas as páginas foram atualizadas!"
```

## 📋 Checklist de Verificação

Após atualizar uma página, verifique:

- [ ] O CSS `backend-toggle.css` está sendo carregado
- [ ] O JS `backend-toggle.js` está sendo carregado **APÓS** `api-config.js`
- [ ] O toggle aparece no canto superior direito
- [ ] Não há erros no console do navegador (F12)
- [ ] Clicar nos botões alterna entre backends
- [ ] Notificação aparece ao trocar backend

## 🎯 Template Completo

Use este template para páginas novas:

```html
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Minha Página - Pet Shop</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Backend Toggle CSS -->
    <link rel="stylesheet" href="css/backend-toggle.css">
    
    <!-- CSS Customizado -->
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <!-- Conteúdo da página -->
    
    <!-- Scripts -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="js/api-config.js"></script>
    <script src="js/backend-toggle.js"></script>
    <!-- Outros scripts da página -->
</body>
</html>
```

## 🔍 Teste Rápido

Após atualizar as páginas, teste:

1. Abra a página no navegador
2. Pressione F12 para abrir o console
3. Verifique se não há erros
4. Verifique se aparece:
   ```
   ✅ API Config carregado!
   🎯 Backend atual: Spring Boot (http://localhost:8080/api)
   ✅ Backend Toggle inicializado
   ```
5. Verifique se o toggle mostra **4 opções de backend** (Spring Boot, ASP.NET Core, C# Functions, Java Functions)
6. Teste alternar entre os backends disponíveis
7. Verifique se notificações aparecem ao trocar

## 🆘 Troubleshooting

### Toggle não aparece
```javascript
// No console do navegador:
console.log(window.backendToggle);
// Deve retornar: BackendToggle {elemento: div.backend-selector}
```

### Erro 404 nos arquivos
- Verifique se os caminhos estão corretos relativos à página
- Para páginas em subpastas, ajuste o caminho:
  ```html
  <!-- Para categorias/racoes-alimentacao/index.html -->
  <link rel="stylesheet" href="../../css/backend-toggle.css">
  <script src="../../js/backend-toggle.js"></script>
  ```

### CSS não aplicado
- Forçar reload sem cache: `Ctrl + Shift + R` (Linux/Windows) ou `Cmd + Shift + R` (Mac)
- Verificar caminho relativo do CSS

## 💡 Dicas

1. **Ordem importa:** `api-config.js` deve vir ANTES de `backend-toggle.js`
2. **Cache:** Limpe o cache do navegador após modificações
3. **Consistência:** Use o mesmo padrão em todas as páginas
4. **Teste:** Sempre teste após adicionar em uma nova página
