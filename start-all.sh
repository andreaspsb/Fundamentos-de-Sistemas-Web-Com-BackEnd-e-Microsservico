#!/bin/bash

# ================================================
# Petshop - Script de Gerenciamento Completo
# ================================================
# ABORDAGEM HÍBRIDA:
#   - Docker Compose: 3 backends containerizados + 2 frontends + 6 microsserviços Java
#   - Docker Compose Dev: Infraestrutura local (Azurite, RabbitMQ, SQL Server, Redis)
#   - Local: Azure Functions C# via func start (portas 7071-7076)
#
# Uso:
#   ./start-all.sh              - Inicia backends, frontends e microsserviços
#   ./start-all.sh stop         - Para todos os serviços
#   ./start-all.sh restart      - Reinicia todos os serviços
#   ./start-all.sh status       - Mostra status dos containers e funções
#   ./start-all.sh logs         - Mostra logs dos serviços Docker
#   ./start-all.sh build        - Reconstrói as imagens Docker
#   ./start-all.sh dev          - Inicia infraestrutura de desenvolvimento (Azurite, RabbitMQ, etc.)
#   ./start-all.sh dev-stop     - Para infraestrutura de desenvolvimento
#   ./start-all.sh full         - Inicia TUDO (infraestrutura + backends + frontends)
# ================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="docker-compose.yml"
COMPOSE_DEV_FILE="docker-compose.dev.yml"
FUNCTIONS_DIR="$SCRIPT_DIR/functions"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}"
    echo "╔══════════════════════════════════════════════════════════════════╗"
    echo "║                 🐾 PETSHOP - SISTEMA COMPLETO 🐾                  ║"
    echo "╠══════════════════════════════════════════════════════════════════╣"
    echo "║  2 Frontends + 4 Backends (2 monolíticos + 12 microsserviços)   ║"
    echo "║  Banco de Dados: Azure SQL Database                              ║"
    echo "╚══════════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

print_services() {
    echo -e "${GREEN}"
    echo "┌──────────────────────────────────────────────────────────────────┐"
    echo "│                      SERVIÇOS DISPONÍVEIS                        │"
    echo "├──────────────────────────────────────────────────────────────────┤"
    echo "│  📱 FRONTENDS:                                                   │"
    echo "│     • Web (Nginx):        http://localhost:80                    │"
    echo "│     • Mobile (Expo):      http://localhost:8081                  │"
    echo "├──────────────────────────────────────────────────────────────────┤"
    echo "│  🖥️  BACKENDS MONOLÍTICOS:                                       │"
    echo "│     • Spring Boot:        http://localhost:8080                  │"
    echo "│     • ASP.NET Core:       http://localhost:5000                  │"
    echo "├──────────────────────────────────────────────────────────────────┤"
    echo "│  ⚡ MICROSSERVIÇOS C# (Azure Functions - Local):                 │"
    echo "│     • Auth:               http://localhost:7071                  │"
    echo "│     • Catalog:            http://localhost:7072                  │"
    echo "│     • Customers:          http://localhost:7073                  │"
    echo "│     • Orders:             http://localhost:7074                  │"
    echo "│     • Pets:               http://localhost:7075                  │"
    echo "│     • Scheduling:         http://localhost:7076                  │"
    echo "├──────────────────────────────────────────────────────────────────┤"
    echo "│  ☕ MICROSSERVIÇOS JAVA (Docker - Spring Boot):                  │"
    echo "│     • Auth:               http://localhost:7081                  │"
    echo "│     • Catalog:            http://localhost:7082                  │"
    echo "│     • Customers:          http://localhost:7083                  │"
    echo "│     • Orders:             http://localhost:7084                  │"
    echo "│     • Pets:               http://localhost:7085                  │"
    echo "│     • Scheduling:         http://localhost:7086                  │"
    echo "├──────────────────────────────────────────────────────────────────┤"
    echo "│  🗄️  BANCO DE DADOS:                                             │"
    echo "│     • Azure SQL Database (configurar em .env)                    │"
    echo "│     • Dev: H2 (Java) / SQLite (C#) - em memória                  │"
    echo "└──────────────────────────────────────────────────────────────────┘"
    echo -e "${NC}"
}

start_services() {
    print_header
    echo -e "${YELLOW}🚀 Iniciando todos os serviços...${NC}\n"
    
    # 1. Iniciar serviços Docker
    echo -e "${CYAN}📦 Iniciando serviços Docker (backends + frontends + microsserviços Java)...${NC}"
    docker compose -f $COMPOSE_FILE up -d
    
    # 2. Iniciar Azure Functions C# (se existir o script)
    if [ -f "$FUNCTIONS_DIR/start-all.sh" ]; then
        echo -e "\n${CYAN}⚡ Iniciando Azure Functions C# (microsserviços)...${NC}"
        cd "$FUNCTIONS_DIR"
        ./start-all.sh &
        cd "$SCRIPT_DIR"
        echo -e "${GREEN}   Azure Functions C# iniciadas em background${NC}"
    else
        echo -e "\n${YELLOW}⚠️  Script functions/start-all.sh não encontrado${NC}"
        echo -e "${YELLOW}   Microsserviços C# não serão iniciados automaticamente${NC}"
    fi
    
    echo -e "\n${GREEN}✅ Serviços iniciados com sucesso!${NC}\n"
    print_services
    
    echo -e "${YELLOW}⏳ Aguarde alguns segundos para os serviços ficarem prontos...${NC}"
    echo -e "${BLUE}💡 Use './start-all.sh status' para verificar o status${NC}\n"
}

stop_services() {
    print_header
    echo -e "${YELLOW}🛑 Parando todos os serviços...${NC}\n"
    
    # 1. Parar Azure Functions C# (se existir o script)
    if [ -f "$FUNCTIONS_DIR/stop-all.sh" ]; then
        echo -e "${CYAN}⚡ Parando Azure Functions C#...${NC}"
        cd "$FUNCTIONS_DIR"
        ./stop-all.sh 2>/dev/null || true
        cd "$SCRIPT_DIR"
    fi
    
    # 2. Parar serviços Docker
    echo -e "${CYAN}📦 Parando serviços Docker...${NC}"
    docker compose -f $COMPOSE_FILE down
    
    echo -e "\n${GREEN}✅ Todos os serviços foram parados!${NC}\n"
}

restart_services() {
    print_header
    echo -e "${YELLOW}🔄 Reiniciando todos os serviços...${NC}\n"
    
    stop_services
    sleep 2
    start_services
}

show_status() {
    print_header
    echo -e "${YELLOW}📊 Status dos serviços:${NC}\n"
    
    echo -e "${CYAN}=== Containers Docker (Aplicação) ===${NC}"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(petshop|func|NAMES)" || echo "Nenhum container de aplicação rodando"
    
    echo ""
    show_dev_status
    
    echo -e "${CYAN}=== Azure Functions C# (processos func) ===${NC}"
    if pgrep -f "func start" > /dev/null 2>&1; then
        ps aux | grep "[f]unc start" | awk '{print $11, $12, $13}' | head -10
        echo -e "${GREEN}Azure Functions C# estão rodando${NC}"
    else
        echo -e "${YELLOW}Azure Functions C# não estão rodando${NC}"
    fi
    
    echo ""
}

show_logs() {
    print_header
    echo -e "${YELLOW}📋 Logs dos serviços (Ctrl+C para sair):${NC}\n"
    
    docker compose -f $COMPOSE_FILE logs -f
}

show_dev_logs() {
    print_header
    echo -e "${YELLOW}📋 Logs da infraestrutura de desenvolvimento (Ctrl+C para sair):${NC}\n"
    
    docker compose -f "$SCRIPT_DIR/$COMPOSE_DEV_FILE" logs -f
}

build_services() {
    print_header
    echo -e "${YELLOW}🔨 Reconstruindo imagens Docker...${NC}\n"
    
    docker compose -f $COMPOSE_FILE build
    
    echo -e "\n${GREEN}✅ Imagens reconstruídas!${NC}\n"
}

# ================================================
# Funções de Infraestrutura de Desenvolvimento
# ================================================

print_dev_services() {
    echo -e "${GREEN}"
    echo "┌──────────────────────────────────────────────────────────────────┐"
    echo "│              INFRAESTRUTURA DE DESENVOLVIMENTO                   │"
    echo "├──────────────────────────────────────────────────────────────────┤"
    echo "│  📦 AZURITE (Azure Storage Emulator):                            │"
    echo "│     • Blob:               http://localhost:10000                 │"
    echo "│     • Queue:              http://localhost:10001                 │"
    echo "│     • Table:              http://localhost:10002                 │"
    echo "├──────────────────────────────────────────────────────────────────┤"
    echo "│  🐰 RABBITMQ (Messaging alternativo):                            │"
    echo "│     • AMQP:               amqp://localhost:5672                  │"
    echo "│     • Management UI:      http://localhost:15672                 │"
    echo "│     • Credenciais:        guest / guest                          │"
    echo "├──────────────────────────────────────────────────────────────────┤"
    echo "│  🗄️  SQL SERVER (Edge para dev local):                           │"
    echo "│     • Port:               localhost:1433                         │"
    echo "│     • User:               sa                                     │"
    echo "│     • Password:           conforme MSSQL_SA_PASSWORD no .env     │"
    echo "├──────────────────────────────────────────────────────────────────┤"
    echo "│  🔴 REDIS (Cache):                                               │"
    echo "│     • Port:               localhost:6379                         │"
    echo "├──────────────────────────────────────────────────────────────────┤"
    echo "│  📧 MAILHOG (Email testing):                                     │"
    echo "│     • SMTP:               localhost:1025                         │"
    echo "│     • Web UI:             http://localhost:8025                  │"
    echo "└──────────────────────────────────────────────────────────────────┘"
    echo -e "${NC}"
}

start_dev_infrastructure() {
    print_header
    echo -e "${YELLOW}🔧 Iniciando infraestrutura de desenvolvimento...${NC}\n"
    
    # Verificar se docker-compose.dev.yml existe
    if [ ! -f "$SCRIPT_DIR/$COMPOSE_DEV_FILE" ]; then
        echo -e "${RED}❌ Arquivo $COMPOSE_DEV_FILE não encontrado!${NC}"
        echo -e "${YELLOW}   Este arquivo contém a configuração de Azurite, RabbitMQ, SQL Server, etc.${NC}"
        exit 1
    fi
    
    # Iniciar infraestrutura
    echo -e "${CYAN}📦 Iniciando containers de infraestrutura...${NC}"
    docker compose -f "$SCRIPT_DIR/$COMPOSE_DEV_FILE" up -d
    
    echo -e "\n${GREEN}✅ Infraestrutura de desenvolvimento iniciada!${NC}\n"
    print_dev_services
    
    echo -e "${YELLOW}⏳ Aguarde alguns segundos para os serviços ficarem prontos...${NC}"
    echo -e "${BLUE}💡 Connection Strings:${NC}"
    echo -e "   • Azurite: ${CYAN}UseDevelopmentStorage=true${NC}"
    echo -e "   • RabbitMQ: ${CYAN}amqp://guest:guest@localhost:5672${NC}"
    echo -e "   • SQL Server: ${CYAN}Server=localhost,1433;User Id=sa;Password=<senha>;TrustServerCertificate=true${NC}"
    echo -e "   • Redis: ${CYAN}localhost:6379${NC}"
    echo ""
}

stop_dev_infrastructure() {
    print_header
    echo -e "${YELLOW}🛑 Parando infraestrutura de desenvolvimento...${NC}\n"
    
    if [ ! -f "$SCRIPT_DIR/$COMPOSE_DEV_FILE" ]; then
        echo -e "${RED}❌ Arquivo $COMPOSE_DEV_FILE não encontrado!${NC}"
        exit 1
    fi
    
    docker compose -f "$SCRIPT_DIR/$COMPOSE_DEV_FILE" down
    
    echo -e "\n${GREEN}✅ Infraestrutura de desenvolvimento parada!${NC}\n"
}

start_full() {
    print_header
    echo -e "${YELLOW}🚀 Iniciando TUDO (infraestrutura + backends + frontends)...${NC}\n"
    
    # 1. Iniciar infraestrutura de desenvolvimento
    if [ -f "$SCRIPT_DIR/$COMPOSE_DEV_FILE" ]; then
        echo -e "${CYAN}🔧 Iniciando infraestrutura de desenvolvimento...${NC}"
        docker compose -f "$SCRIPT_DIR/$COMPOSE_DEV_FILE" up -d
        echo -e "${GREEN}   Infraestrutura iniciada${NC}\n"
    fi
    
    # 2. Iniciar todos os serviços de aplicação
    start_services
    
    echo -e "${GREEN}✅ Sistema completo iniciado!${NC}\n"
    print_dev_services
}

show_dev_status() {
    echo -e "${CYAN}=== Infraestrutura de Desenvolvimento ===${NC}"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(azurite|rabbitmq|sqlserver|redis|mailhog|NAMES)" || echo "Nenhum container de infraestrutura rodando"
    echo ""
}

# Main
case "${1:-start}" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs
        ;;
    build)
        build_services
        ;;
    dev)
        start_dev_infrastructure
        ;;
    dev-stop)
        stop_dev_infrastructure
        ;;
    dev-logs)
        show_dev_logs
        ;;
    full)
        start_full
        ;;
    *)
        echo "Uso: $0 {start|stop|restart|status|logs|build|dev|dev-stop|dev-logs|full}"
        echo ""
        echo "Comandos de Aplicação:"
        echo "  start     - Inicia backends, frontends e microsserviços"
        echo "  stop      - Para todos os serviços de aplicação"
        echo "  restart   - Reinicia todos os serviços de aplicação"
        echo "  status    - Mostra status de todos os containers e funções"
        echo "  logs      - Mostra logs dos serviços de aplicação"
        echo "  build     - Reconstrói as imagens Docker"
        echo ""
        echo "Comandos de Infraestrutura (Dev):"
        echo "  dev       - Inicia infraestrutura de desenvolvimento (Azurite, RabbitMQ, SQL, Redis)"
        echo "  dev-stop  - Para infraestrutura de desenvolvimento"
        echo "  dev-logs  - Mostra logs da infraestrutura"
        echo ""
        echo "Comando Completo:"
        echo "  full      - Inicia TUDO (infraestrutura + backends + frontends)"
        exit 1
        ;;
esac
