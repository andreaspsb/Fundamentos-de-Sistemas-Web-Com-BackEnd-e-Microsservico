// @ts-check
const { defineConfig, devices } = require('@playwright/test');

/**
 * Configuração do Playwright para testes do Pet Shop
 * Testa todos os 4 backends: ASP.NET, Spring Boot, C# Functions, Java Functions
 * 
 * Pré-requisitos:
 * - Executar ./start-all.sh para iniciar todos os backends
 * - Frontend é iniciado automaticamente pelo Playwright
 * 
 * @see https://playwright.dev/docs/test-configuration
 */
module.exports = defineConfig({
  testDir: './tests/e2e',
  
  /* Tempo máximo de execução por teste */
  timeout: 30 * 1000,
  
  /* Configuração de expect */
  expect: {
    timeout: 10000
  },
  
  /* Executar testes sequencialmente (um backend por vez) */
  fullyParallel: false,
  
  /* Falhar o build em CI se testes foram deixados com .only */
  forbidOnly: !!process.env.CI,
  
  /* Retry em caso de falha */
  retries: process.env.CI ? 1 : 0,
  
  /* Workers: 1 para execução sequencial entre backends */
  workers: 1,
  
  /* Reporter para saída de testes */
  reporter: [
    ['html'],
    ['list']
  ],
  
  /* Configurações compartilhadas entre projetos */
  use: {
    /* URL base do frontend */
    baseURL: 'http://localhost:5500',
    
    /* Coletar trace on first retry */
    trace: 'on-first-retry',
    
    /* Screenshots on failure */
    screenshot: 'only-on-failure',
    
    /* Video on first retry */
    video: 'retain-on-failure',
    
    /* Browser: apenas Chromium inicialmente */
    ...devices['Desktop Chrome'],
  },

  /* 
   * Projetos: um por backend
   * Cada projeto configura PETSHOP_BACKEND via env var
   * Os testes usam esse valor para configurar o localStorage
   */
  projects: [
    {
      name: 'aspnet',
      use: {
        ...devices['Desktop Chrome'],
      },
      metadata: {
        backend: 'ASPNET',
        backendName: 'ASP.NET Core',
        port: 5000,
      },
    },
    {
      name: 'springboot',
      use: {
        ...devices['Desktop Chrome'],
      },
      metadata: {
        backend: 'SPRINGBOOT',
        backendName: 'Spring Boot',
        port: 8080,
      },
    },
    {
      name: 'functions',
      use: {
        ...devices['Desktop Chrome'],
      },
      metadata: {
        backend: 'FUNCTIONS',
        backendName: 'C# Functions',
        port: 7071,
      },
    },
    {
      name: 'functions-java',
      use: {
        ...devices['Desktop Chrome'],
      },
      metadata: {
        backend: 'FUNCTIONS_JAVA',
        backendName: 'Java Functions',
        port: 7081,
      },
    },
  ],

  /* Servidor de desenvolvimento local - apenas frontend */
  webServer: {
    command: 'cd frontend && python3 -m http.server 5500',
    url: 'http://localhost:5500',
    reuseExistingServer: true,
    timeout: 120 * 1000,
  },
});
