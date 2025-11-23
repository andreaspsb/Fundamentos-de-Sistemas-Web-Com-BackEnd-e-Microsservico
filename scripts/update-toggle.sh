#!/bin/bash

# ========================================
# Script de Atualização Automática
# Adiciona Backend Toggle em todas as páginas HTML
# ========================================

echo "🔧 Backend Toggle - Atualização Automática"
echo "=========================================="
echo ""

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Contador
UPDATED=0
SKIPPED=0
ERRORS=0

# Função para processar uma página
process_page() {
    local file="$1"
    local relative_path="${file#./}"
    
    echo -n "Processando $relative_path... "
    
    # Verificar se já tem o toggle
    if grep -q "backend-toggle.js" "$file"; then
        echo -e "${YELLOW}SKIP${NC} (já atualizado)"
        ((SKIPPED++))
        return
    fi
    
    # Verificar se tem api-config.js
    if ! grep -q "api-config.js" "$file"; then
        echo -e "${YELLOW}SKIP${NC} (sem api-config.js)"
        ((SKIPPED++))
        return
    fi
    
    # Criar backup
    cp "$file" "$file.bak"
    
    # Calcular o nível de profundidade para ajustar caminhos relativos
    depth=$(echo "$relative_path" | tr -cd '/' | wc -c)
    path_prefix=""
    for ((i=0; i<depth; i++)); do
        path_prefix="../$path_prefix"
    done
    
    # Adicionar CSS no <head>
    if ! grep -q "backend-toggle.css" "$file"; then
        # Encontrar linha do Bootstrap CSS e adicionar depois
        sed -i '/bootstrap.*\.css/a\    \n    <!-- Backend Toggle CSS -->\n    <link rel="stylesheet" href="'"${path_prefix}css/backend-toggle.css"'">' "$file"
    fi
    
    # Adicionar JS após api-config.js
    if ! grep -q "backend-toggle.js" "$file"; then
        sed -i 's|<script src="'"${path_prefix}"'js/api-config.js"></script>|<script src="'"${path_prefix}"'js/api-config.js"></script>\n    <script src="'"${path_prefix}"'js/backend-toggle.js"></script>|' "$file"
        
        # Se não encontrou com caminho relativo, tentar sem
        if ! grep -q "backend-toggle.js" "$file"; then
            sed -i 's|<script src="js/api-config.js"></script>|<script src="js/api-config.js"></script>\n    <script src="js/backend-toggle.js"></script>|' "$file"
        fi
    fi
    
    # Verificar se a atualização funcionou
    if grep -q "backend-toggle.js" "$file"; then
        echo -e "${GREEN}OK${NC}"
        ((UPDATED++))
        rm "$file.bak"
    else
        echo -e "${RED}ERROR${NC}"
        ((ERRORS++))
        mv "$file.bak" "$file"
    fi
}

# Diretório do frontend
FRONTEND_DIR="$(dirname "$0")/../frontend"
cd "$FRONTEND_DIR"

echo "Diretório: $(pwd)"
echo ""

# Encontrar todas as páginas HTML
echo "Buscando páginas HTML..."
mapfile -t HTML_FILES < <(find . -name "*.html" -type f | sort)

echo "Encontradas ${#HTML_FILES[@]} páginas"
echo ""

# Processar cada arquivo
for file in "${HTML_FILES[@]}"; do
    process_page "$file"
done

echo ""
echo "=========================================="
echo "📊 Resumo da Atualização"
echo "=========================================="
echo -e "${GREEN}✅ Atualizados:${NC} $UPDATED"
echo -e "${YELLOW}⏭️  Ignorados:${NC} $SKIPPED"
echo -e "${RED}❌ Erros:${NC} $ERRORS"
echo ""

if [ $ERRORS -gt 0 ]; then
    echo -e "${RED}⚠️  Algumas páginas apresentaram erros.${NC}"
    echo "   Verifique os backups (.bak) criados."
    exit 1
elif [ $UPDATED -gt 0 ]; then
    echo -e "${GREEN}🎉 Atualização concluída com sucesso!${NC}"
    echo ""
    echo "💡 Próximos passos:"
    echo "   1. Teste as páginas no navegador"
    echo "   2. Verifique se o toggle aparece"
    echo "   3. Teste a alternância entre backends"
    exit 0
else
    echo -e "${YELLOW}ℹ️  Nenhuma atualização necessária.${NC}"
    echo "   Todas as páginas já possuem o toggle."
    exit 0
fi
