#!/bin/bash

# ================================================
# Petshop - Script de Gerenciamento Completo
# ================================================
# ABORDAGEM HÍBRIDA:
#   - Docker Compose: 3 backends containerizados + 2 frontends + 6 microsserviços Java
#   - Local: Azure Functions C# via func start (portas 7071-7076)
#
# Uso:
#   ./start-all.sh          - Inicia todos os serviços
#   ./start-all.sh stop     - Para todos os serviços
#   ./start-all.sh restart  - Reinicia todos os serviços
#   ./start-all.sh status   - Mostra status dos containers e funções
#   ./start-all.sh logs     - Mostra logs dos serviços Docker
#   ./start-all.sh build    - Reconstrói as imagens Docker
# ================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="docker-compose.yml"
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
    
    echo -e "${CYAN}=== Containers Docker ===${NC}"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(petshop|func|NAMES)" || echo "Nenhum container rodando"
    
    echo ""
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

build_services() {
    print_header
    echo -e "${YELLOW}🔨 Reconstruindo imagens Docker...${NC}\n"
    
    docker compose -f $COMPOSE_FILE build
    
    echo -e "\n${GREEN}✅ Imagens reconstruídas!${NC}\n"
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
    *)
        echo "Uso: $0 {start|stop|restart|status|logs|build}"
        exit 1
        ;;
esac
