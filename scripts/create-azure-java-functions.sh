#!/bin/bash

# ================================================
# Script de Criação de Recursos Azure - Java Functions
# ================================================
# Este script cria todos os recursos necessários no Azure para
# hospedar os 6 microsserviços Java do Petshop como Azure Functions.
#
# Recursos criados:
#   - Resource Group
#   - Storage Account (para WebJobs)
#   - App Service Plan (Consumption/Premium)
#   - 6 Function Apps (uma para cada microsserviço)
#
# Uso:
#   ./create-azure-java-functions.sh              # Criar recursos
#   ./create-azure-java-functions.sh --delete     # Remover recursos
#   ./create-azure-java-functions.sh --status     # Ver status dos recursos
# ================================================

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ================================================
# CONFIGURAÇÕES - Usando recursos existentes
# ================================================
RESOURCE_GROUP="petshop-rg"
LOCATION="brazilsouth"
STORAGE_ACCOUNT="petshopfuncsstorage"  # Já existe

# Nomes das Function Apps Java (sufixo -java para diferenciar das C#)
FUNC_AUTH="func-petshop-auth-java"
FUNC_CUSTOMERS="func-petshop-customers-java"
FUNC_PETS="func-petshop-pets-java"
FUNC_CATALOG="func-petshop-catalog-java"
FUNC_SCHEDULING="func-petshop-scheduling-java"
FUNC_ORDERS="func-petshop-orders-java"

# Azure SQL Server existente
SQL_SERVER="petshop-db"
SQL_DATABASE="petshop-db"

# App Service Plan para Java Functions (Linux)
JAVA_PLAN="petshop-java-functions-plan"

# Lista de todas as funções
ALL_FUNCTIONS=("$FUNC_AUTH" "$FUNC_CUSTOMERS" "$FUNC_PETS" "$FUNC_CATALOG" "$FUNC_SCHEDULING" "$FUNC_ORDERS")
FUNCTION_NAMES=("Auth" "Customers" "Pets" "Catalog" "Scheduling" "Orders")

# ================================================
# FUNÇÕES AUXILIARES
# ================================================

print_header() {
    echo -e "${BLUE}"
    echo "╔══════════════════════════════════════════════════════════════════╗"
    echo "║         ☕ AZURE - JAVA FUNCTIONS PROVISIONING ☕               ║"
    echo "╠══════════════════════════════════════════════════════════════════╣"
    echo "║  6 Microsserviços: Auth, Customers, Pets, Catalog, Scheduling,  ║"
    echo "║                    Orders                                        ║"
    echo "╚══════════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

print_config() {
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}                    CONFIGURAÇÃO DOS RECURSOS                       ${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  📍 Resource Group:     ${GREEN}${RESOURCE_GROUP}${NC} (existente)"
    echo -e "  🌎 Localização:        ${GREEN}${LOCATION}${NC}"
    echo -e "  📦 Storage Account:    ${GREEN}${STORAGE_ACCOUNT}${NC} (existente)"
    echo ""
    echo -e "  ${YELLOW}Function Apps Java (novas):${NC}"
    for i in "${!ALL_FUNCTIONS[@]}"; do
        echo -e "    • ${FUNCTION_NAMES[$i]}: ${GREEN}${ALL_FUNCTIONS[$i]}${NC}"
    done
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════════${NC}"
}

check_login() {
    echo -e "${YELLOW}🔐 Verificando login no Azure...${NC}"
    if ! az account show &>/dev/null; then
        echo -e "${RED}❌ Você não está logado no Azure. Execute: az login${NC}"
        exit 1
    fi
    
    ACCOUNT=$(az account show --query name -o tsv)
    echo -e "${GREEN}✅ Logado como: ${ACCOUNT}${NC}"
    echo ""
}

# ================================================
# CRIAÇÃO DE RECURSOS
# ================================================

create_resource_group() {
    echo -e "${YELLOW}📁 Verificando Resource Group: ${RESOURCE_GROUP}...${NC}"
    
    if az group show --name "$RESOURCE_GROUP" &>/dev/null; then
        echo -e "${GREEN}   ✅ Resource Group existe${NC}"
    else
        echo -e "${RED}   ❌ Resource Group não encontrado!${NC}"
        echo -e "${YELLOW}   Execute primeiro o deploy dos backends principais.${NC}"
        exit 1
    fi
}

create_storage_account() {
    echo -e "${YELLOW}📦 Verificando Storage Account: ${STORAGE_ACCOUNT}...${NC}"
    
    if az storage account show --name "$STORAGE_ACCOUNT" --resource-group "$RESOURCE_GROUP" &>/dev/null; then
        echo -e "${GREEN}   ✅ Storage Account existe${NC}"
    else
        echo -e "${RED}   ❌ Storage Account não encontrado!${NC}"
        exit 1
    fi
    
    # Obter connection string
    STORAGE_CONNECTION=$(az storage account show-connection-string \
        --name "$STORAGE_ACCOUNT" \
        --resource-group "$RESOURCE_GROUP" \
        --query connectionString -o tsv)
    
    echo -e "${GREEN}   📝 Connection string obtida${NC}"
}

create_java_plan() {
    echo -e "${YELLOW}📋 Criando App Service Plan Linux para Java: ${JAVA_PLAN}...${NC}"
    
    if az appservice plan show --name "$JAVA_PLAN" --resource-group "$RESOURCE_GROUP" &>/dev/null; then
        echo -e "${GREEN}   App Service Plan já existe${NC}"
    else
        # B1 = Basic tier, mais barato que Consumption para múltiplas funções
        # EP1 = Elastic Premium (melhor para Functions, mas mais caro)
        az appservice plan create \
            --name "$JAVA_PLAN" \
            --resource-group "$RESOURCE_GROUP" \
            --location "$LOCATION" \
            --sku B1 \
            --is-linux \
            --output none
        echo -e "${GREEN}   ✅ App Service Plan criado (B1 Linux)${NC}"
    fi
}

create_function_app() {
    local func_name=$1
    local display_name=$2
    
    echo -e "${YELLOW}⚡ Criando Function App: ${func_name} (${display_name})...${NC}"
    
    if az functionapp show --name "$func_name" --resource-group "$RESOURCE_GROUP" &>/dev/null; then
        echo -e "${GREEN}   Function App já existe${NC}"
    else
        az functionapp create \
            --name "$func_name" \
            --resource-group "$RESOURCE_GROUP" \
            --storage-account "$STORAGE_ACCOUNT" \
            --plan "$JAVA_PLAN" \
            --runtime java \
            --runtime-version 21.0 \
            --os-type Linux \
            --functions-version 4 \
            --output none
        
        echo -e "${GREEN}   ✅ Function App criado${NC}"
    fi
    
    # Obter FQDN do SQL Server
    SQL_FQDN=$(az sql server show --name "$SQL_SERVER" --resource-group "$RESOURCE_GROUP" --query fullyQualifiedDomainName -o tsv 2>/dev/null || echo "")
    
    # Configurar variáveis de ambiente
    echo -e "${CYAN}   Configurando Application Settings...${NC}"
    
    SETTINGS=(
        "JWT_SECRET=petshop-jwt-secret-key-producao-azure-2024"
        "SPRING_PROFILES_ACTIVE=prod"
        "FUNCTIONS_EXTENSION_VERSION=~4"
        "FUNCTIONS_WORKER_RUNTIME=java"
        "WEBSITE_RUN_FROM_PACKAGE=1"
    )
    
    # Adicionar config do SQL se disponível
    if [ -n "$SQL_FQDN" ]; then
        SETTINGS+=(
            "SPRING_DATASOURCE_URL=jdbc:sqlserver://${SQL_FQDN}:1433;database=${SQL_DATABASE};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30"
            "SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.microsoft.sqlserver.jdbc.SQLServerDriver"
            "SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.SQLServerDialect"
        )
        echo -e "${CYAN}   📊 SQL Server configurado: ${SQL_FQDN}${NC}"
    fi
    
    az functionapp config appsettings set \
        --name "$func_name" \
        --resource-group "$RESOURCE_GROUP" \
        --settings "${SETTINGS[@]}" \
        --output none
    
    echo -e "${GREEN}   ✅ Configurações aplicadas${NC}"
    echo -e "${YELLOW}   ⚠️  Lembre-se de configurar SPRING_DATASOURCE_USERNAME e PASSWORD!${NC}"
}

create_all_resources() {
    print_header
    check_login
    print_config
    
    echo ""
    echo -e "${YELLOW}🚀 Iniciando criação dos recursos...${NC}"
    echo ""
    
    create_resource_group
    create_storage_account
    create_java_plan
    
    echo ""
    echo -e "${YELLOW}📦 Criando 6 Function Apps...${NC}"
    echo ""
    
    for i in "${!ALL_FUNCTIONS[@]}"; do
        create_function_app "${ALL_FUNCTIONS[$i]}" "${FUNCTION_NAMES[$i]}"
    done
    
    echo ""
    echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}              ✅ TODOS OS RECURSOS CRIADOS COM SUCESSO!             ${NC}"
    echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
    echo ""
    
    # Exibir URLs
    echo -e "${CYAN}📍 URLs dos microsserviços:${NC}"
    for i in "${!ALL_FUNCTIONS[@]}"; do
        local url="https://${ALL_FUNCTIONS[$i]}.azurewebsites.net"
        echo -e "  • ${FUNCTION_NAMES[$i]}: ${GREEN}${url}${NC}"
    done
    
    echo ""
    echo -e "${YELLOW}📝 Próximos passos:${NC}"
    echo ""
    echo "1. Configure a connection string do Azure SQL em cada Function App:"
    echo "   (O banco petshop-db já existe no resource group)"
    echo ""
    echo "2. Deploy das funções:"
    echo "   cd functions-java"
    echo "   mvn clean package -DskipTests"
    echo "   cd func-petshop-auth-java && mvn azure-functions:deploy"
    echo "   # Repetir para as outras 5 funções"
    echo ""
    echo "3. Ou use o script de deploy (a ser criado):"
    echo "   ./scripts/deploy-java-functions.sh"
    echo ""
    
    # Salvar configuração para uso posterior
    save_config
}

save_config() {
    CONFIG_FILE="$(dirname "$0")/../.azure-java-functions.env"
    cat > "$CONFIG_FILE" << EOF
# Azure Java Functions Configuration
# Gerado em: $(date)
# NÃO FAÇA COMMIT DESTE ARQUIVO!

RESOURCE_GROUP=${RESOURCE_GROUP}
LOCATION=${LOCATION}
STORAGE_ACCOUNT=${STORAGE_ACCOUNT}
APP_SERVICE_PLAN=${APP_SERVICE_PLAN}

# Function App Names
FUNC_AUTH=${FUNC_AUTH}
FUNC_CUSTOMERS=${FUNC_CUSTOMERS}
FUNC_PETS=${FUNC_PETS}
FUNC_CATALOG=${FUNC_CATALOG}
FUNC_SCHEDULING=${FUNC_SCHEDULING}
FUNC_ORDERS=${FUNC_ORDERS}

# URLs
URL_AUTH=https://${FUNC_AUTH}.azurewebsites.net
URL_CUSTOMERS=https://${FUNC_CUSTOMERS}.azurewebsites.net
URL_PETS=https://${FUNC_PETS}.azurewebsites.net
URL_CATALOG=https://${FUNC_CATALOG}.azurewebsites.net
URL_SCHEDULING=https://${FUNC_SCHEDULING}.azurewebsites.net
URL_ORDERS=https://${FUNC_ORDERS}.azurewebsites.net
EOF
    
    echo -e "${GREEN}📝 Configuração salva em: ${CONFIG_FILE}${NC}"
    echo -e "${YELLOW}   (Adicione ao .gitignore se ainda não estiver)${NC}"
}

# ================================================
# STATUS DOS RECURSOS
# ================================================

show_status() {
    print_header
    check_login
    
    echo -e "${YELLOW}📊 Status dos recursos no Azure:${NC}"
    echo ""
    
    # Verificar Resource Group
    echo -e "${CYAN}📁 Resource Group: ${RESOURCE_GROUP}${NC}"
    if az group show --name "$RESOURCE_GROUP" &>/dev/null; then
        echo -e "   ${GREEN}✅ Existe${NC}"
        
        # Listar Function Apps
        echo ""
        echo -e "${CYAN}⚡ Function Apps:${NC}"
        az functionapp list \
            --resource-group "$RESOURCE_GROUP" \
            --query "[].{Name:name, State:state, URL:defaultHostName}" \
            --output table 2>/dev/null || echo "   Nenhum Function App encontrado"
        
        # Listar Storage Accounts
        echo ""
        echo -e "${CYAN}📦 Storage Accounts:${NC}"
        az storage account list \
            --resource-group "$RESOURCE_GROUP" \
            --query "[].{Name:name, Location:location, SKU:sku.name}" \
            --output table 2>/dev/null || echo "   Nenhum Storage Account encontrado"
            
    else
        echo -e "   ${RED}❌ Não existe${NC}"
    fi
}

# ================================================
# REMOÇÃO DE RECURSOS
# ================================================

delete_resources() {
    print_header
    check_login
    
    echo -e "${RED}⚠️  ATENÇÃO: Esta ação irá REMOVER TODOS os recursos do grupo ${RESOURCE_GROUP}${NC}"
    echo ""
    
    # Verificar se existe
    if ! az group show --name "$RESOURCE_GROUP" &>/dev/null; then
        echo -e "${YELLOW}Resource Group não existe. Nada a remover.${NC}"
        exit 0
    fi
    
    # Listar o que será removido
    echo -e "${YELLOW}Recursos que serão removidos:${NC}"
    az resource list --resource-group "$RESOURCE_GROUP" --query "[].{Name:name, Type:type}" --output table
    echo ""
    
    read -p "Tem certeza que deseja REMOVER TODOS estes recursos? (digite 'SIM' para confirmar): " confirm
    
    if [ "$confirm" = "SIM" ]; then
        echo ""
        echo -e "${YELLOW}🗑️  Removendo Resource Group e todos os recursos...${NC}"
        az group delete --name "$RESOURCE_GROUP" --yes --no-wait
        echo -e "${GREEN}✅ Remoção iniciada (pode levar alguns minutos)${NC}"
    else
        echo -e "${YELLOW}Operação cancelada.${NC}"
    fi
}

# ================================================
# MAIN
# ================================================

case "${1:-}" in
    --delete|delete|-d)
        delete_resources
        ;;
    --status|status|-s)
        show_status
        ;;
    --help|-h)
        echo "Uso: $0 [opção]"
        echo ""
        echo "Opções:"
        echo "  (sem opção)    Criar todos os recursos"
        echo "  --status, -s   Mostrar status dos recursos"
        echo "  --delete, -d   Remover todos os recursos"
        echo "  --help, -h     Mostrar esta ajuda"
        ;;
    *)
        create_all_resources
        ;;
esac
