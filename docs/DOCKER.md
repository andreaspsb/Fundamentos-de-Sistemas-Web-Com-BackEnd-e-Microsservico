# 🐳 Guia Docker - Pet Shop

Este documento explica como usar Docker e Docker Compose para executar o projeto completo.

> **⚙️ Sobre os backends:** Este projeto possui **4 backends intercambiáveis** (Spring Boot, ASP.NET Core, C# Functions, Java Functions). O `docker-compose.yml` inclui os 2 backends monolíticos (Spring Boot e ASP.NET). As Azure Functions (C# e Java) são executadas localmente com `func start` - veja [docs/MICROSSERVICOS.md](MICROSSERVICOS.md) para detalhes.

## 📋 Pré-requisitos

- **Docker** 20.10+
- **Docker Compose** 2.0+

### Instalar Docker

**Linux (Ubuntu/Debian):**
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

**macOS:**
```bash
brew install --cask docker
```

**Windows:**
- Baixar [Docker Desktop](https://www.docker.com/products/docker-desktop/)

---

## 🚀 Início Rápido

### 1. Clonar o repositório
```bash
git clone https://github.com/seu-usuario/petshop.git
cd petshop
```

### 2. Executar com Docker Compose
```bash
# Iniciar todos os serviços
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar todos os serviços
docker-compose down
```

### 3. Acessar a aplicação

- **Frontend**: http://localhost
- **Backend Spring Boot**: http://localhost:8080
- **Backend ASP.NET**: http://localhost:5000
- **Swagger Spring Boot**: http://localhost:8080/swagger-ui.html
- **Swagger ASP.NET**: http://localhost:5000/swagger

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────┐
│           Nginx (Port 80)                   │
│         Frontend + Reverse Proxy            │
└─────────┬───────────────────────┬───────────┘
          │                       │
          ├───────────────────────┼──────────────┐
          │                       │              │
          ▼                       ▼              ▼
┌──────────────────┐  ┌──────────────────┐   ┌─────────────┐
│  Spring Boot     │  │   ASP.NET Core   │   │ PostgreSQL  │
│   (Port 8080)    │  │    (Port 5000)   │   │ (Port 5432) │
└──────────────────┘  └──────────────────┘   └─────────────┘
         │                      │                    │
         └──────────────────────┴────────────────────┘
                    petshop-network
```

---

## 📦 Serviços

### 1. PostgreSQL (`postgres`)
- **Imagem**: `postgres:16-alpine`
- **Porta**: 5432
- **Database**: petshop
- **Credenciais**: petshop / petshop123

### 2. Backend Spring Boot (`petshop-springboot`)
- **Build**: Multi-stage com Maven
- **Porta**: 8080
- **Health**: `/actuator/health`
- **Swagger**: `/swagger-ui.html`

### 3. Backend ASP.NET Core (`petshop-aspnet`)
- **Build**: Multi-stage com .NET SDK
- **Porta**: 5000
- **Health**: `/health`
- **Swagger**: `/swagger`

### 4. Frontend Nginx (`petshop-frontend`)
- **Imagem**: `nginx:alpine`
- **Porta**: 80
- **Serve**: Arquivos estáticos + Reverse proxy

---

## 🛠️ Comandos Úteis

### Gerenciamento Básico

```bash
# Iniciar serviços (modo daemon)
docker-compose up -d

# Iniciar serviços (modo interativo)
docker-compose up

# Parar serviços
docker-compose stop

# Parar e remover containers
docker-compose down

# Parar, remover e limpar volumes
docker-compose down -v
```

### Logs

```bash
# Ver logs de todos os serviços
docker-compose logs -f

# Ver logs de um serviço específico
docker-compose logs -f petshop-springboot
docker-compose logs -f petshop-aspnet
docker-compose logs -f postgres

# Ver últimas 100 linhas
docker-compose logs --tail=100 petshop-springboot
```

### Build

```bash
# Rebuild todos os containers
docker-compose build

# Rebuild sem cache
docker-compose build --no-cache

# Rebuild um serviço específico
docker-compose build petshop-springboot
```

### Status e Informações

```bash
# Ver status dos containers
docker-compose ps

# Ver uso de recursos
docker stats

# Inspecionar um container
docker inspect petshop-springboot

# Ver configuração final do compose
docker-compose config
```

### Executar Comandos nos Containers

```bash
# Shell no container Spring Boot
docker-compose exec petshop-springboot sh

# Shell no container ASP.NET
docker-compose exec petshop-aspnet sh

# Acessar PostgreSQL
docker-compose exec postgres psql -U petshop -d petshop

# Ver logs de um arquivo específico
docker-compose exec petshop-springboot cat /var/log/app.log
```

---

## 🔧 Ambientes

### Desenvolvimento

Usar `docker-compose.dev.yml` para desenvolvimento:

```bash
# Iniciar em modo desenvolvimento
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up

# Inclui:
# - Hot reload (se configurado)
# - Logs em DEBUG
# - Adminer (interface web PostgreSQL) em http://localhost:8082
# - Portas de debug expostas
```

### Produção

Usar apenas `docker-compose.yml`:

```bash
# Build otimizado para produção
docker-compose build --no-cache

# Iniciar
docker-compose up -d

# Verificar health
docker-compose ps
```

---

## 🔐 Variáveis de Ambiente

### Criar arquivo .env

```bash
cp .env.example .env
```

### Editar .env

```env
# Banco de Dados
POSTGRES_DB=petshop
POSTGRES_USER=petshop
POSTGRES_PASSWORD=senha_forte_aqui

# Spring Boot
SPRING_PROFILES_ACTIVE=prod
JAVA_OPTS=-Xms512m -Xmx1024m

# ASP.NET Core
ASPNETCORE_ENVIRONMENT=Production

# CORS
CORS_ALLOWED_ORIGINS=https://meusite.com,https://www.meusite.com
```

---

## 🗄️ Banco de Dados

### Backup

```bash
# Criar backup
docker-compose exec postgres pg_dump -U petshop petshop > backup.sql

# Criar backup comprimido
docker-compose exec postgres pg_dump -U petshop petshop | gzip > backup.sql.gz
```

### Restore

```bash
# Restaurar backup
docker-compose exec -T postgres psql -U petshop petshop < backup.sql

# Restaurar backup comprimido
gunzip < backup.sql.gz | docker-compose exec -T postgres psql -U petshop petshop
```

### Acessar via Adminer (Dev)

```bash
# Iniciar com Adminer
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# Acessar: http://localhost:8082
# Server: postgres
# Username: petshop
# Password: petshop123
# Database: petshop
```

---

## 🔍 Troubleshooting

### Container não inicia

```bash
# Ver logs de erro
docker-compose logs petshop-springboot

# Verificar health
docker-compose ps

# Tentar iniciar manualmente
docker-compose up petshop-springboot
```

### Porta já em uso

```bash
# Verificar quem está usando a porta
lsof -i :8080
lsof -i :5000
lsof -i :80

# Mudar porta no docker-compose.yml
services:
  petshop-springboot:
    ports:
      - "8081:8080"  # Muda porta externa para 8081
```

### Limpar tudo e recomeçar

```bash
# Parar tudo
docker-compose down -v

# Remover imagens antigas
docker-compose down --rmi all -v

# Rebuild completo
docker-compose build --no-cache

# Iniciar novamente
docker-compose up -d
```

### Problemas de permissão

```bash
# Adicionar usuário ao grupo docker
sudo usermod -aG docker $USER

# Logout e login novamente
# Ou executar:
newgrp docker
```

### Container em loop de restart

```bash
# Ver logs
docker-compose logs --tail=50 petshop-springboot

# Verificar health check
docker inspect petshop-springboot | grep -A 10 Health

# Desabilitar temporariamente health check
# Comentar no docker-compose.yml:
# healthcheck:
#   disable: true
```

---

## 🚀 Deploy em Produção

### 1. Preparar ambiente

```bash
# Criar .env de produção
cp .env.example .env.prod

# Editar com valores de produção
nano .env.prod
```

### 2. Build de produção

```bash
# Build sem cache
docker-compose build --no-cache

# Tag das imagens
docker tag petshop-springboot:latest seu-registry/petshop-springboot:1.0.0
docker tag petshop-aspnet:latest seu-registry/petshop-aspnet:1.0.0
```

### 3. Push para registry

```bash
# Login no registry
docker login seu-registry.com

# Push das imagens
docker push seu-registry/petshop-springboot:1.0.0
docker push seu-registry/petshop-aspnet:1.0.0
```

### 4. Deploy no servidor

```bash
# No servidor de produção
git pull
docker-compose -f docker-compose.yml --env-file .env.prod up -d
```

---

## 📊 Monitoramento

### Health Checks

```bash
# Spring Boot
curl http://localhost:8080/actuator/health

# ASP.NET
curl http://localhost:5000/health

# Nginx
curl http://localhost/health
```

### Logs em tempo real

```bash
# Todos os serviços
docker-compose logs -f --tail=100

# Apenas erros
docker-compose logs -f | grep -i error
```

### Uso de recursos

```bash
# Ver estatísticas
docker stats

# Ver volumes
docker volume ls

# Ver espaço usado
docker system df
```

---

## 🧹 Limpeza

```bash
# Remover containers parados
docker-compose down

# Remover volumes não usados
docker volume prune

# Remover imagens não usadas
docker image prune -a

# Limpeza completa (cuidado!)
docker system prune -a --volumes
```

---

## 📚 Recursos Adicionais

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Multi-stage Builds](https://docs.docker.com/build/building/multi-stage/)

---

## 🆘 Suporte

Se encontrar problemas:

1. Verificar logs: `docker-compose logs -f`
2. Verificar health: `docker-compose ps`
3. Limpar e reconstruir: `docker-compose down -v && docker-compose build --no-cache`
4. Abrir issue no GitHub

---

**Última atualização:** Novembro 2025
