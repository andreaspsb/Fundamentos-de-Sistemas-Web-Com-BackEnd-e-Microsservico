#!/bin/bash

# =============================================================================
# PetShop - Script para iniciar ambiente de desenvolvimento
# =============================================================================
# Este script inicia os serviços de infraestrutura necessários para
# desenvolvimento local usando Docker.
#
# Serviços:
#   - Azurite (Azure Storage Emulator) - Portas 10000-10002
#   - RabbitMQ (Message Broker) - Portas 5672, 15672
#   - SQL Server Edge - Porta 1433
#   - Redis (Cache) - Porta 6379
#   - MailHog (SMTP) - Portas 1025, 8025
#
# Uso:
#   ./start-dev-services.sh          # Inicia todos os serviços
#   ./start-dev-services.sh azurite  # Inicia apenas Azurite
#   ./start-dev-services.sh stop     # Para todos os serviços
#   ./start-dev-services.sh status   # Mostra status dos serviços
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.dev.yml"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}"
    echo "==================================================="
    echo "  PetShop - Ambiente de Desenvolvimento"
    echo "==================================================="
    echo -e "${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker não está instalado!"
        echo "Instale o Docker: https://docs.docker.com/get-docker/"
        exit 1
    fi

    if ! docker info &> /dev/null; then
        print_error "Docker não está rodando!"
        echo "Inicie o serviço Docker e tente novamente."
        exit 1
    fi
}

start_services() {
    local services="$1"
    
    print_header
    check_docker
    
    echo "Iniciando serviços de desenvolvimento..."
    echo ""
    
    if [ -z "$services" ]; then
        docker-compose -f "$COMPOSE_FILE" up -d
    else
        docker-compose -f "$COMPOSE_FILE" up -d $services
    fi
    
    echo ""
    print_success "Serviços iniciados!"
    echo ""
    show_endpoints
}

stop_services() {
    print_header
    check_docker
    
    echo "Parando serviços..."
    docker-compose -f "$COMPOSE_FILE" down
    
    print_success "Serviços parados!"
}

show_status() {
    print_header
    check_docker
    
    echo "Status dos serviços:"
    echo ""
    docker-compose -f "$COMPOSE_FILE" ps
}

show_endpoints() {
    echo -e "${BLUE}Endpoints disponíveis:${NC}"
    echo ""
    echo "  📦 Azurite (Azure Storage Emulator):"
    echo "     Blob:  http://localhost:10000"
    echo "     Queue: http://localhost:10001"
    echo "     Table: http://localhost:10002"
    echo ""
    echo "  🐰 RabbitMQ:"
    echo "     AMQP: amqp://localhost:5672"
    echo "     UI:   http://localhost:15672 (petshop/petshop123)"
    echo ""
    echo "  🗄️  SQL Server:"
    echo "     Host: localhost,1433"
    echo "     User: sa"
    echo "     Pass: PetShop@2024!"
    echo ""
    echo "  📮 MailHog (Email testing):"
    echo "     SMTP: localhost:1025"
    echo "     UI:   http://localhost:8025"
    echo ""
    echo "  💾 Redis:"
    echo "     Host: localhost:6379"
    echo ""
    echo -e "${YELLOW}Dica: Configure MessageBroker:Provider=AzureStorageQueue para usar Azurite${NC}"
}

show_help() {
    echo "Uso: $0 [comando] [serviços]"
    echo ""
    echo "Comandos:"
    echo "  start [serviços]  Inicia serviços (padrão: todos)"
    echo "  stop              Para todos os serviços"
    echo "  status            Mostra status dos serviços"
    echo "  logs [serviço]    Mostra logs de um serviço"
    echo "  help              Mostra esta ajuda"
    echo ""
    echo "Serviços disponíveis:"
    echo "  azurite    - Azure Storage Emulator"
    echo "  rabbitmq   - Message Broker"
    echo "  sqlserver  - SQL Server Edge"
    echo "  redis      - Cache"
    echo "  mailhog    - Email testing"
    echo ""
    echo "Exemplos:"
    echo "  $0                    # Inicia todos os serviços"
    echo "  $0 start azurite      # Inicia apenas Azurite"
    echo "  $0 stop               # Para todos"
    echo "  $0 logs rabbitmq      # Mostra logs do RabbitMQ"
}

# Main
case "${1:-start}" in
    start)
        start_services "${@:2}"
        ;;
    stop)
        stop_services
        ;;
    status)
        show_status
        ;;
    logs)
        docker-compose -f "$COMPOSE_FILE" logs -f "${2:-}"
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        # Se o primeiro argumento não é um comando, assume que são serviços
        start_services "$@"
        ;;
esac
