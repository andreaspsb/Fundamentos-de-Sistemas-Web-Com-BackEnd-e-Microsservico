# Multi-stage build para otimizar tamanho da imagem

# Stage 1: Build
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /app

# Copiar apenas arquivos de projeto primeiro (melhor cache)
COPY PetshopApi/*.csproj ./PetshopApi/
RUN dotnet restore ./PetshopApi/PetshopApi.csproj

# Copiar código fonte
COPY PetshopApi/ ./PetshopApi/

# Build e publicação
WORKDIR /app/PetshopApi
RUN dotnet publish -c Release -o /app/publish

# Stage 2: Runtime
FROM mcr.microsoft.com/dotnet/aspnet:8.0-alpine

# Metadata
LABEL maintainer="petshop@example.com"
LABEL description="Pet Shop Backend - ASP.NET Core"
LABEL version="1.0.0"

# Criar usuário não-root por segurança
RUN addgroup -S dotnet && adduser -S dotnet -G dotnet
USER dotnet:dotnet

WORKDIR /app

# Copiar arquivos publicados do stage anterior
COPY --from=build /app/publish .

# Variáveis de ambiente
ENV ASPNETCORE_ENVIRONMENT=Production
ENV ASPNETCORE_URLS=http://+:5000
ENV DOTNET_EnableDiagnostics=0

# Expor porta
EXPOSE 5000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:5000/health || exit 1

# Comando de execução
ENTRYPOINT ["dotnet", "PetshopApi.dll"]
