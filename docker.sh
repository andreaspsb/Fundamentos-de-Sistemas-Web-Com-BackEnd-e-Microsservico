#!/bin/bash

# Script auxiliar para gerenciar Docker Compose do Pet Shop
# Uso: ./docker.sh [comando]

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para exibir mensagens coloridas
info() {
    echo -e "${BLUE}ℹ ${1}${NC}"
}

success() {
    echo -e "${GREEN}✓ ${1}${NC}"
}

warning() {
    echo -e "${YELLOW}⚠ ${1}${NC}"
}

error() {
    echo -e "${RED}✗ ${1}${NC}"
    exit 1
}

# Verificar se Docker está instalado
check_docker() {
    if ! command -v docker &> /dev/null; then
        error "Docker não está instalado. Instale em: https://docs.docker.com/get-docker/"
    fi
    
    # Verificar Docker Compose (plugin ou standalone)
    if docker compose version &> /dev/null 2>&1; then
        DOCKER_COMPOSE="docker compose"
    elif command -v $DOCKER_COMPOSE &> /dev/null; then
        DOCKER_COMPOSE="$DOCKER_COMPOSE"
    else
        error "Docker Compose não está instalado."
    fi
    
    success "Docker instalado: $(docker --version)"
}

# Criar arquivo .env se não existir
setup_env() {
    if [ ! -f .env ]; then
        warning ".env não encontrado. Criando a partir de .env.example..."
        cp .env.example .env
        warning "⚠️  IMPORTANTE: Edite o arquivo .env com suas configurações!"
        warning "⚠️  Especialmente mude as senhas antes de usar em produção!"
    else
        success ".env já existe"
    fi
}

# Iniciar serviços
start() {
    info "Iniciando serviços do Pet Shop..."
    check_docker
    setup_env
    
    if [ "$1" == "dev" ]; then
        info "Modo desenvolvimento ativado (inclui Adminer)"
        $DOCKER_COMPOSE -f $DOCKER_COMPOSE.yml -f $DOCKER_COMPOSE.dev.yml up -d
    else
        $DOCKER_COMPOSE up -d
    fi
    
    success "Serviços iniciados!"
    echo ""
    info "Aguardando serviços ficarem saudáveis..."
    sleep 10
    
    $DOCKER_COMPOSE ps
    
    echo ""
    success "🎉 Pet Shop está rodando!"
    echo ""
    echo "📱 Acessos:"
    echo "  Frontend:              http://localhost"
    echo "  Backend Spring Boot:   http://localhost:8080"
    echo "  Backend ASP.NET:       http://localhost:5000"
    echo "  Swagger Spring Boot:   http://localhost:8080/swagger-ui.html"
    echo "  Swagger ASP.NET:       http://localhost:5000/swagger"
    
    if [ "$1" == "dev" ]; then
        echo "  Adminer (DB):          http://localhost:8082"
    fi
    
    echo ""
    info "Para ver os logs: ./docker.sh logs"
}

# Parar serviços
stop() {
    info "Parando serviços..."
    $DOCKER_COMPOSE stop
    success "Serviços parados!"
}

# Parar e remover containers
down() {
    info "Parando e removendo containers..."
    
    if [ "$1" == "volumes" ]; then
        warning "Removendo volumes (dados do banco serão perdidos)..."
        $DOCKER_COMPOSE down -v
    else
        $DOCKER_COMPOSE down
    fi
    
    success "Containers removidos"
}

# Ver logs
logs() {
    if [ -z "$1" ]; then
        info "Mostrando logs de todos os serviços..."
        $DOCKER_COMPOSE logs -f --tail=100
    else
        info "Mostrando logs de: $1"
        $DOCKER_COMPOSE logs -f --tail=100 "$1"
    fi
}

# Rebuild containers
rebuild() {
    info "Reconstruindo containers..."
    
    if [ "$1" == "no-cache" ]; then
        warning "Rebuild sem cache (mais lento, mas garante atualização)"
        $DOCKER_COMPOSE build --no-cache
    else
        $DOCKER_COMPOSE build
    fi
    
    success "Rebuild concluído"
}

# Status dos serviços
status() {
    info "Status dos serviços:"
    $DOCKER_COMPOSE ps
    
    echo ""
    info "Health checks:"
    
    # Spring Boot
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        success "Spring Boot: Saudável"
    else
        error "Spring Boot: Indisponível"
    fi
    
    # ASP.NET
    if curl -s http://localhost:5000/health > /dev/null 2>&1; then
        success "ASP.NET: Saudável"
    else
        error "ASP.NET: Indisponível"
    fi
    
    # Frontend
    if curl -s http://localhost > /dev/null 2>&1; then
        success "Frontend: Saudável"
    else
        error "Frontend: Indisponível"
    fi
}

# Backup do banco
backup() {
    info "Criando backup do banco de dados..."
    
    BACKUP_FILE="backup-$(date +%Y%m%d-%H%M%S).sql"
    $DOCKER_COMPOSE exec -T postgres pg_dump -U petshop petshop > "$BACKUP_FILE"
    
    success "Backup criado: $BACKUP_FILE"
}

# Restore do banco
restore() {
    if [ -z "$1" ]; then
        error "Uso: ./docker.sh restore <arquivo-backup.sql>"
    fi
    
    if [ ! -f "$1" ]; then
        error "Arquivo não encontrado: $1"
    fi
    
    warning "Isso irá sobrescrever os dados atuais. Continuar? (s/n)"
    read -r response
    
    if [ "$response" != "s" ]; then
        info "Operação cancelada"
        exit 0
    fi
    
    info "Restaurando backup..."
    $DOCKER_COMPOSE exec -T postgres psql -U petshop petshop < "$1"
    success "Backup restaurado com sucesso"
}

# Limpar tudo
clean() {
    warning "Isso irá remover TUDO (containers, volumes, imagens). Continuar? (s/n)"
    read -r response
    
    if [ "$response" != "s" ]; then
        info "Operação cancelada"
        exit 0
    fi
    
    info "Removendo tudo..."
    $DOCKER_COMPOSE down -v --rmi all
    success "Limpeza concluída"
}

# Menu de ajuda
help() {
    echo "🐳 Docker Helper - Pet Shop"
    echo ""
    echo "Uso: ./docker.sh [comando] [opções]"
    echo ""
    echo "Comandos:"
    echo "  start [dev]     - Iniciar serviços (adicione 'dev' para modo desenvolvimento)"
    echo "  stop            - Parar serviços"
    echo "  restart         - Reiniciar serviços"
    echo "  down [volumes]  - Parar e remover containers (adicione 'volumes' para remover dados)"
    echo "  logs [serviço]  - Ver logs (opcional: especificar serviço)"
    echo "  status          - Ver status e health dos serviços"
    echo "  rebuild [no-cache] - Reconstruir containers"
    echo "  backup          - Criar backup do banco de dados"
    echo "  restore <file>  - Restaurar backup do banco"
    echo "  clean           - Remover TUDO (containers, volumes, imagens)"
    echo "  help            - Exibir esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  ./docker.sh start           # Iniciar em produção"
    echo "  ./docker.sh start dev       # Iniciar em desenvolvimento"
    echo "  ./docker.sh logs            # Ver todos os logs"
    echo "  ./docker.sh logs postgres   # Ver logs do PostgreSQL"
    echo "  ./docker.sh rebuild no-cache # Rebuild sem cache"
    echo ""
}

# Main
case "$1" in
    start)
        start "$2"
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        start "$2"
        ;;
    down)
        down "$2"
        ;;
    logs)
        logs "$2"
        ;;
    status)
        status
        ;;
    rebuild)
        rebuild "$2"
        ;;
    backup)
        backup
        ;;
    restore)
        restore "$2"
        ;;
    clean)
        clean
        ;;
    help|--help|-h|"")
        help
        ;;
    *)
        error "Comando desconhecido: $1\nUse './docker.sh help' para ver os comandos disponíveis"
        ;;
esac
