# 🚀 Guia de Deploy - Pet Shop Full Stack

Guia completo para fazer deploy do sistema Pet Shop em produção.

## 📋 Índice

- [Pré-requisitos](#pré-requisitos)
- [Deploy com Docker](#deploy-com-docker) 🐳 **NOVO**
  - [VPS (DigitalOcean, Linode, etc)](#vps-digitalocean-linode-etc)
  - [AWS EC2](#aws-ec2)
  - [Google Cloud VM](#google-cloud-vm)
  - [Azure VM](#azure-vm)
- [Deploy do Backend (PaaS)](#deploy-do-backend-paas)
  - [Heroku](#heroku)
  - [Railway](#railway)
  - [AWS Elastic Beanstalk](#aws-elastic-beanstalk)
  - [Render](#render)
- [Deploy do Frontend](#deploy-do-frontend)
  - [Vercel](#vercel)
  - [Netlify](#netlify)
  - [GitHub Pages](#github-pages)
- [Configuração de Banco de Dados](#configuração-de-banco-de-dados)
  - [PostgreSQL](#postgresql)
  - [MySQL](#mysql)
- [Configurações de Produção](#configurações-de-produção)
- [Checklist de Deploy](#checklist-de-deploy)

---

## 🎯 Pré-requisitos

- Conta nos serviços de hospedagem escolhidos
- Git instalado e configurado
- Código no GitHub/GitLab
- Variáveis de ambiente configuradas

---

## 🐳 Deploy com Docker

**Recomendado para:** Maior controle, escalabilidade, portabilidade  
**Custo:** Variável ($5-50/mês dependendo do provedor)  
**Complexidade:** Média  

### Por que usar Docker em produção?

- ✅ **Portabilidade:** Funciona em qualquer servidor
- ✅ **Consistência:** Mesmo ambiente dev/staging/prod
- ✅ **Isolamento:** Containers separados por serviço
- ✅ **Escalabilidade:** Fácil adicionar instâncias
- ✅ **Rollback:** Reverter para versão anterior rapidamente

---

### 1. VPS (DigitalOcean, Linode, etc)

**Custo estimado:** $6-12/mês (2GB RAM, 1 vCPU)  
**Recomendado:** DigitalOcean, Linode, Vultr, Hetzner

#### Passo a Passo:

**1. Criar Droplet/VPS:**
- OS: Ubuntu 24.04 LTS
- RAM: Mínimo 2GB (recomendado 4GB)
- Storage: 50GB

**2. Acessar via SSH:**
```bash
ssh root@seu-ip
```

**3. Instalar Docker e Docker Compose:**
```bash
# Atualizar sistema
apt update && apt upgrade -y

# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Instalar Docker Compose
apt install docker-compose-plugin -y

# Verificar instalação
docker --version
docker compose version
```

**4. Configurar Firewall:**
```bash
# Instalar UFW
apt install ufw -y

# Permitir portas
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP
ufw allow 443/tcp   # HTTPS

# Ativar
ufw enable
ufw status
```

**5. Clonar repositório:**
```bash
# Criar usuário para deploy (mais seguro que root)
adduser deploy
usermod -aG sudo deploy
usermod -aG docker deploy

# Mudar para usuário deploy
su - deploy

# Clonar projeto
cd /opt
sudo mkdir petshop && sudo chown deploy:deploy petshop
cd petshop
git clone https://github.com/andreaspsb/Fundamentos-de-Sistemas-Web-Com-BackEnd.git .
```

**6. Configurar variáveis de ambiente:**
```bash
# Copiar exemplo
cp .env.example .env

# Editar com valores de produção
nano .env
```

```env
# Banco de Dados
POSTGRES_DB=petshop
POSTGRES_USER=petshop
POSTGRES_PASSWORD=SuaSenhaForte123!@#

# Spring Boot
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/petshop
JAVA_OPTS=-Xms512m -Xmx1024m

# ASP.NET Core
ASPNETCORE_ENVIRONMENT=Production
CONNECTION_STRING=Host=postgres;Port=5432;Database=petshop;Username=petshop;Password=SuaSenhaForte123!@#

# CORS (seu domínio)
CORS_ALLOWED_ORIGINS=https://seudominio.com,https://www.seudominio.com
```

**7. Iniciar containers:**
```bash
# Build e start
docker compose up -d

# Verificar logs
docker compose logs -f

# Verificar status
docker compose ps
```

**8. Configurar Nginx reverso proxy (opcional mas recomendado):**

Se quiser usar domínio próprio com SSL:

```bash
# Instalar Nginx no host
sudo apt install nginx certbot python3-certbot-nginx -y

# Criar configuração
sudo nano /etc/nginx/sites-available/petshop
```

```nginx
server {
    listen 80;
    server_name seudominio.com www.seudominio.com;

    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
# Ativar site
sudo ln -s /etc/nginx/sites-available/petshop /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx

# Configurar SSL (Let's Encrypt)
sudo certbot --nginx -d seudominio.com -d www.seudominio.com
```

**9. Deploy automático com GitHub Actions:**

Criar workflow `.github/workflows/deploy-vps.yml`:

```yaml
name: Deploy to VPS

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
    - name: Deploy via SSH
      uses: appleboy/ssh-action@master
      with:
        host: ${{ secrets.VPS_HOST }}
        username: ${{ secrets.VPS_USER }}
        key: ${{ secrets.VPS_SSH_KEY }}
        script: |
          cd /opt/petshop
          git pull origin main
          docker compose down
          docker compose build --no-cache
          docker compose up -d
          docker compose ps
```

**Adicionar secrets no GitHub:**
- Settings → Secrets → Actions
- `VPS_HOST`: IP do servidor
- `VPS_USER`: deploy
- `VPS_SSH_KEY`: Chave privada SSH

---

### 2. AWS EC2

**Custo estimado:** $8-15/mês (t3.small)

#### Passo a Passo:

**1. Criar instância EC2:**
- AMI: Ubuntu Server 24.04 LTS
- Instance type: t3.small (2 vCPU, 2GB RAM)
- Storage: 30GB GP3
- Security Group: Portas 22, 80, 443

**2. Conectar via SSH:**
```bash
chmod 400 sua-chave.pem
ssh -i sua-chave.pem ubuntu@ec2-xx-xx-xx-xx.compute.amazonaws.com
```

**3. Seguir passos 3-7 do VPS acima**

**4. Configurar RDS (opcional):**

Para banco gerenciado:
- Criar RDS PostgreSQL
- Atualizar `DATABASE_URL` no `.env`
- Security Group: Permitir conexão do EC2

**5. Elastic IP:**
```bash
# Alocar IP fixo no console AWS
# Associar ao EC2
```

---

### 3. Google Cloud VM

**Custo estimado:** $7-14/mês (e2-small)

#### Passo a Passo:

**1. Criar VM Instance:**
```bash
gcloud compute instances create petshop-vm \
  --zone=us-central1-a \
  --machine-type=e2-small \
  --image-family=ubuntu-2404-lts \
  --image-project=ubuntu-os-cloud \
  --boot-disk-size=30GB
```

**2. Conectar:**
```bash
gcloud compute ssh petshop-vm --zone=us-central1-a
```

**3. Seguir passos 3-7 do VPS**

**4. Configurar Firewall:**
```bash
gcloud compute firewall-rules create allow-http \
  --allow tcp:80 \
  --target-tags http-server

gcloud compute firewall-rules create allow-https \
  --allow tcp:443 \
  --target-tags https-server
```

---

### 4. Azure VM

**Custo estimado:** $10-20/mês (B2s)

#### Passo a Passo:

**1. Criar VM:**
```bash
az vm create \
  --resource-group petshop-rg \
  --name petshop-vm \
  --image Ubuntu2404 \
  --size Standard_B2s \
  --admin-username azureuser \
  --generate-ssh-keys
```

**2. Abrir portas:**
```bash
az vm open-port --port 80 --resource-group petshop-rg --name petshop-vm
az vm open-port --port 443 --resource-group petshop-rg --name petshop-vm
```

**3. Conectar:**
```bash
ssh azureuser@<public-ip>
```

**4. Seguir passos 3-7 do VPS**

---

### Manutenção e Monitoramento

**Backup do banco:**
```bash
# Criar backup
docker compose exec postgres pg_dump -U petshop petshop > backup-$(date +%Y%m%d).sql

# Backup automático (cron)
crontab -e
# Adicionar:
0 2 * * * cd /opt/petshop && docker compose exec -T postgres pg_dump -U petshop petshop > /opt/backups/backup-$(date +\%Y\%m\%d).sql
```

**Ver logs:**
```bash
# Todos os serviços
docker compose logs -f

# Serviço específico
docker compose logs -f petshop-springboot
```

**Atualizar aplicação:**
```bash
cd /opt/petshop
git pull
docker compose down
docker compose build --no-cache
docker compose up -d
```

**Limpar recursos:**
```bash
# Remover containers parados
docker container prune -f

# Remover imagens não usadas
docker image prune -a -f

# Liberar espaço
docker system prune -a --volumes -f
```

---

## ☕ Deploy do Backend (PaaS)

### 1. Heroku

**Vantagens:** Fácil deploy, PostgreSQL grátis, CI/CD automático  
**Desvantagens:** Sleep após 30 min de inatividade (plano free)

#### Passo a Passo:

1. **Instalar Heroku CLI:**
```bash
# Linux
curl https://cli-assets.heroku.com/install.sh | sh

# macOS
brew tap heroku/brew && brew install heroku

# Windows
# Baixar instalador em: https://devcenter.heroku.com/articles/heroku-cli
```

2. **Login no Heroku:**
```bash
heroku login
```

3. **Criar aplicação:**
```bash
cd backend-springboot
heroku create petshop-backend
```

4. **Adicionar PostgreSQL:**
```bash
heroku addons:create heroku-postgresql:mini
```

5. **Configurar variáveis de ambiente:**
```bash
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set JWT_SECRET=seu_secret_super_seguro_aqui
```

6. **Criar `Procfile` na raiz do backend:**
```
web: java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/petshop-backend-0.0.1-SNAPSHOT.jar
```

7. **Criar `system.properties`:**
```
java.runtime.version=21
```

8. **Fazer deploy:**
```bash
git add .
git commit -m "Configuração para Heroku"
git push heroku main
```

9. **Abrir aplicação:**
```bash
heroku open
```

---

### 2. Railway

**Vantagens:** Deploy super fácil, $5 grátis/mês, PostgreSQL incluído  
**Desvantagens:** Limite de $5 no plano free

#### Passo a Passo:

1. Acesse [railway.app](https://railway.app)
2. Conecte com GitHub
3. Clique em "New Project" → "Deploy from GitHub repo"
4. Selecione o repositório
5. Railway detecta automaticamente o Spring Boot
6. Adicione PostgreSQL: "New" → "Database" → "PostgreSQL"
7. Configure variáveis de ambiente:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DATABASE_URL` (gerado automaticamente)
8. Deploy automático a cada push!

**URL gerada:** `https://petshop-backend-production.up.railway.app`

---

### 3. AWS Elastic Beanstalk

**Vantagens:** Escalável, profissional, integração AWS  
**Desvantagens:** Complexo, pode ter custos

#### Passo a Passo:

1. **Instalar EB CLI:**
```bash
pip install awsebcli
```

2. **Inicializar:**
```bash
cd backend-springboot
eb init -p java-21 petshop-backend --region us-east-1
```

3. **Criar ambiente:**
```bash
eb create petshop-prod --database.engine postgres
```

4. **Fazer deploy:**
```bash
mvn clean package
eb deploy
```

5. **Abrir aplicação:**
```bash
eb open
```

---

### 4. Render

**Vantagens:** Simples, PostgreSQL grátis, SSL automático  
**Desvantagens:** Cold start no plano free

#### Passo a Passo:

1. Acesse [render.com](https://render.com)
2. Conecte com GitHub
3. "New" → "Web Service"
4. Selecione o repositório
5. Configure:
   - **Name:** `petshop-backend`
   - **Root Directory:** `backend-springboot`
   - **Build Command:** `mvn clean package -DskipTests`
   - **Start Command:** `java -jar target/petshop-backend-0.0.1-SNAPSHOT.jar`
   - **Instance Type:** Free
6. Adicione PostgreSQL: "New" → "PostgreSQL" → "Free"
7. Conecte banco ao web service
8. Deploy automático!

---

## 🎨 Deploy do Frontend

### 1. Vercel

**Vantagens:** Deploy instantâneo, CDN global, domínio grátis  
**Melhor para:** Frontend estático

#### Passo a Passo:

1. Instalar Vercel CLI:
```bash
npm install -g vercel
```

2. Fazer deploy:
```bash
cd frontend
vercel --prod
```

**OU via Interface Web:**
1. Acesse [vercel.com](https://vercel.com)
2. Conecte com GitHub
3. "New Project" → Selecione repositório
4. Configure:
   - **Framework Preset:** Other
   - **Root Directory:** `frontend`
   - **Build Command:** (deixar vazio)
   - **Output Directory:** `.`
5. Deploy!

**URL gerada:** `https://petshop-frontend.vercel.app`

#### Configurar variáveis de ambiente:
```bash
# Criar arquivo frontend/.env.production
VITE_API_URL=https://petshop-backend-production.up.railway.app/api
```

---

### 2. Netlify

**Vantagens:** Simples, formulários grátis, redirects fáceis  
**Melhor para:** Sites estáticos e SPAs

#### Passo a Passo:

1. **Via Drag & Drop:**
   - Acesse [netlify.com](https://netlify.com)
   - Arraste a pasta `frontend/` para o site

2. **Via GitHub:**
   - "New site from Git" → GitHub
   - Selecione repositório
   - Configure:
     - **Base directory:** `frontend`
     - **Build command:** (vazio)
     - **Publish directory:** `frontend`
   - Deploy!

3. **Criar `frontend/netlify.toml`:**
```toml
[build]
  base = "frontend"
  publish = "."

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

**URL gerada:** `https://petshop-frontend.netlify.app`

---

### 3. GitHub Pages

**Vantagens:** Grátis, integrado com GitHub  
**Desvantagens:** Apenas sites estáticos

#### Passo a Passo:

1. **Criar branch `gh-pages`:**
```bash
git checkout -b gh-pages
```

2. **Mover conteúdo do frontend para raiz:**
```bash
git filter-branch --subdirectory-filter frontend -- --all
```

3. **Push:**
```bash
git push origin gh-pages
```

4. **Configurar no GitHub:**
   - Settings → Pages
   - Source: `gh-pages` branch
   - Save

**URL:** `https://andreaspsb.github.io/Fundamentos-de-Sistemas-Web-Com-BackEnd/`

---

## 🗄️ Configuração de Banco de Dados

### PostgreSQL (Recomendado para Produção)

#### 1. Atualizar `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### 2. Criar `application-prod.properties`:
```properties
# Banco de dados
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Porta
server.port=${PORT:8080}

# CORS (ajustar com sua URL do frontend)
cors.allowed-origins=${FRONTEND_URL:https://petshop-frontend.vercel.app}
```

#### 3. Atualizar `CorsConfig.java`:
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

#### 4. Configurar variáveis de ambiente:
```bash
# Heroku
heroku config:set DATABASE_URL=postgres://user:pass@host:5432/dbname
heroku config:set FRONTEND_URL=https://petshop-frontend.vercel.app

# Railway (automático)
# Render (automático)
```

---

### MySQL

#### 1. Adicionar dependência:
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### 2. Configurar `application-prod.properties`:
```properties
spring.datasource.url=jdbc:mysql://${DB_HOST}:3306/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

---

## ⚙️ Configurações de Produção

### 1. Atualizar `api-config.js` (Frontend)

```javascript
const API_CONFIG = {
  BASE_URL: window.location.hostname === 'localhost' 
    ? 'http://localhost:8080/api'
    : 'https://petshop-backend-production.up.railway.app/api',
  // ... resto do código
};
```

### 2. Implementar JWT (Recomendado)

#### Adicionar dependência:
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

### 3. Configurar HTTPS

Todos os serviços mencionados (Heroku, Railway, Render, Vercel, Netlify) fornecem **SSL/HTTPS automático e gratuito**.

### 4. Variáveis de Ambiente

**Backend:**
```bash
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=postgres://...
JWT_SECRET=seu_secret_super_seguro
FRONTEND_URL=https://seu-frontend.vercel.app
PORT=8080
```

**Frontend (se usar build):**
```bash
VITE_API_URL=https://seu-backend.railway.app/api
```

---

## ✅ Checklist de Deploy

### Antes do Deploy:

- [ ] Remover todos os `console.log()` do JavaScript
- [ ] Configurar `application-prod.properties`
- [ ] Atualizar CORS com URLs de produção
- [ ] Trocar H2 por PostgreSQL
- [ ] Adicionar validações e tratamento de erros
- [ ] Implementar JWT (recomendado)
- [ ] Testar localmente com perfil `prod`
- [ ] Criar backup do código

### Backend:

- [ ] Deploy do backend funcionando
- [ ] Banco de dados conectado
- [ ] Dados iniciais criados (DataInitializer)
- [ ] Endpoints testados via Swagger
- [ ] CORS configurado corretamente
- [ ] Variáveis de ambiente configuradas
- [ ] Logs funcionando

### Frontend:

- [ ] Deploy do frontend funcionando
- [ ] API_CONFIG apontando para backend de produção
- [ ] Login funcionando
- [ ] Carrinho funcionando
- [ ] Checkout criando pedidos
- [ ] Admin acessível apenas com login
- [ ] Links relativos funcionando

### Pós-Deploy:

- [ ] Testar fluxo completo: cadastro → login → compra
- [ ] Testar admin panel
- [ ] Verificar performance
- [ ] Configurar domínio personalizado (opcional)
- [ ] Configurar monitoramento (opcional)
- [ ] Documentar URLs de produção no README

---

## 🔧 Troubleshooting

### Backend não inicia:
```bash
# Ver logs
heroku logs --tail
# ou
railway logs
```

### CORS Error:
- Verificar `CorsConfig.java`
- Adicionar URL do frontend em `allowed-origins`
- Verificar se HTTPS/HTTP estão corretos

### Banco não conecta:
- Verificar `DATABASE_URL` nas variáveis de ambiente
- Verificar credenciais
- Testar conexão manualmente

### Frontend não encontra API:
- Verificar `api-config.js`
- Verificar se backend está rodando
- Abrir console do navegador e verificar erros

---

## 📚 Recursos Adicionais

- [Spring Boot em Produção](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)
- [Heroku Java Support](https://devcenter.heroku.com/articles/getting-started-with-java)
- [Railway Docs](https://docs.railway.app/)
- [Vercel Docs](https://vercel.com/docs)
- [Netlify Docs](https://docs.netlify.com/)

---

## 🎉 Deploy Completo!

Após seguir este guia, seu sistema estará rodando em produção:

- **Backend:** `https://petshop-backend.railway.app`
- **Frontend:** `https://petshop-frontend.vercel.app`
- **Swagger:** `https://petshop-backend.railway.app/swagger-ui.html`

**Parabéns! 🚀**

---

**Desenvolvido por:** Andreas Paulus Scherdien Berwaldt  
**Data:** Novembro de 2025  
**Projeto:** Fundamentos de Sistemas Web - PUCRS Online
