#!/bin/bash

# 🔍 Script de Verificação de Saúde - Azure Deployment
# Verifica se todos os serviços do Pet Shop estão funcionando

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# URLs dos serviços (ajuste conforme necessário)
BACKEND_SPRING="https://petshop-backend-spring.azurewebsites.net"
BACKEND_ASPNET="https://petshop-backend-aspnet.azurewebsites.net"
FRONTEND="https://yellow-field-047215b0f.3.azurestaticapps.net"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "   🔍 VERIFICAÇÃO DE SAÚDE - AZURE PET SHOP"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Função para testar endpoint
check_endpoint() {
    local name=$1
    local url=$2
    local expected_code=${3:-200}
    
    echo -n "Verificando ${name}... "
    
    response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 10 --max-time 30 "${url}" 2>/dev/null || echo "000")
    
    if [ "$response" -eq "$expected_code" ] || [ "$response" -eq 200 ]; then
        echo -e "${GREEN}✅ OK${NC} (HTTP $response)"
        return 0
    elif [ "$response" -eq "000" ]; then
        echo -e "${RED}❌ FALHOU${NC} (Timeout ou erro de conexão)"
        return 1
    else
        echo -e "${YELLOW}⚠️ ATENÇÃO${NC} (HTTP $response)"
        return 1
    fi
}

# Função para testar JSON response
check_json_endpoint() {
    local name=$1
    local url=$2
    
    echo -n "Verificando ${name}... "
    
    response=$(curl -s --connect-timeout 10 --max-time 30 "${url}" 2>/dev/null || echo "ERROR")
    
    if echo "$response" | grep -q "ERROR"; then
        echo -e "${RED}❌ FALHOU${NC} (Timeout ou erro de conexão)"
        return 1
    elif echo "$response" | grep -qE '\{|\['; then
        echo -e "${GREEN}✅ OK${NC} (JSON válido retornado)"
        return 0
    else
        echo -e "${YELLOW}⚠️ ATENÇÃO${NC} (Resposta não parece JSON)"
        echo "    Resposta: ${response:0:100}"
        return 1
    fi
}

# Contador de erros
errors=0

echo -e "${BLUE}▶ Backend Spring Boot:${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

check_endpoint "Health Check" "${BACKEND_SPRING}/actuator/health" 200 || ((errors++))
check_json_endpoint "API Produtos" "${BACKEND_SPRING}/api/produtos" || ((errors++))
check_endpoint "Swagger UI" "${BACKEND_SPRING}/swagger-ui/index.html" 200 || ((errors++))

echo ""
echo -e "${BLUE}▶ Backend ASP.NET Core:${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

check_json_endpoint "Health Check" "${BACKEND_ASPNET}/health" || ((errors++))
check_json_endpoint "API Produtos" "${BACKEND_ASPNET}/api/produtos" || ((errors++))
check_endpoint "Swagger UI" "${BACKEND_ASPNET}/swagger" 200 || ((errors++))

echo ""
echo -e "${BLUE}▶ Frontend (Azure Static Web Apps):${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

check_endpoint "Página Inicial" "${FRONTEND}" 200 || ((errors++))
check_endpoint "Página de Login" "${FRONTEND}/login.html" 200 || ((errors++))
check_endpoint "Página de Cadastro" "${FRONTEND}/cadastro.html" 200 || ((errors++))

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ $errors -eq 0 ]; then
    echo -e "${GREEN}✅ TODOS OS SERVIÇOS ESTÃO FUNCIONANDO!${NC}"
    echo ""
    echo "🎉 Seu Pet Shop está 100% operacional no Azure!"
    echo ""
    echo "📍 URLs de Acesso:"
    echo "   • Frontend: ${FRONTEND}"
    echo "   • Backend Spring Boot: ${BACKEND_SPRING}"
    echo "   • Backend ASP.NET Core: ${BACKEND_ASPNET}"
else
    echo -e "${RED}❌ ENCONTRADOS ${errors} ERRO(S)${NC}"
    echo ""
    echo "🔧 Próximos passos para resolver:"
    echo ""
    echo "1. Verifique se os App Services estão 'Running' no Azure Portal"
    echo "2. Verifique os logs: Portal Azure → App Services → Log stream"
    echo "3. Confirme as variáveis de ambiente (Configuration)"
    echo "4. Verifique o firewall do Azure SQL Database"
    echo "5. Consulte: .github/AZURE_SQL_FIREWALL.md"
    echo ""
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "   ✨ Verificação concluída em $(date '+%Y-%m-%d %H:%M:%S')"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

exit 0
