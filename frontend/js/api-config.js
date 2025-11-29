// ========================================
// Configuração da API - Pet Shop Backend
// ========================================

// Detectar se está em produção (Azure Static Web Apps)
const isProduction = window.location.hostname.includes('azurestaticapps.net') || 
                     window.location.hostname.includes('azure') ||
                     window.location.protocol === 'https:' && !window.location.hostname.includes('localhost');

// URLs de produção no Azure
const AZURE_URLS = {
  SPRINGBOOT: 'https://petshop-backend-spring.azurewebsites.net/api',
  ASPNET: 'https://petshop-backend-aspnet.azurewebsites.net/api'
};

// Definir backends disponíveis
const BACKENDS = {
  SPRINGBOOT: {
    name: 'Spring Boot',
    url: isProduction ? AZURE_URLS.SPRINGBOOT : 'http://localhost:8080/api',
    port: 8080,
    type: 'monolith',
    color: '#6DB33F'
  },
  ASPNET: {
    name: 'ASP.NET Core',
    url: isProduction ? AZURE_URLS.ASPNET : 'http://localhost:5000/api',
    port: 5000,
    type: 'monolith',
    color: '#512BD4'
  },
  FUNCTIONS: {
    name: 'Azure Functions',
    url: 'http://localhost:7071/api',
    port: 7071,
    type: 'microservices',
    color: '#0078D4',
    // URLs dos microsserviços individuais
    services: {
      auth: 'http://localhost:7071/api',
      customers: 'http://localhost:7072/api',
      pets: 'http://localhost:7073/api',
      catalog: 'http://localhost:7074/api',
      scheduling: 'http://localhost:7075/api',
      orders: 'http://localhost:7076/api'
    }
  }
};

// Carregar backend selecionado do localStorage ou usar Spring Boot como padrão
const getBackendAtual = () => {
  const backendSalvo = localStorage.getItem('backend-selecionado');
  return backendSalvo || 'SPRINGBOOT';
};

const API_CONFIG = {
  // BASE_URL será atualizado dinamicamente
  get BASE_URL() {
    const backendAtual = getBackendAtual();
    return BACKENDS[backendAtual].url;
  },
  
  // Para microsserviços, retorna a URL do serviço específico
  getServiceUrl(service) {
    const backendAtual = getBackendAtual();
    const backend = BACKENDS[backendAtual];
    
    // Se for microsserviços, retorna URL do serviço específico
    if (backend.type === 'microservices' && backend.services && backend.services[service]) {
      return backend.services[service];
    }
    
    // Caso contrário, retorna URL base (monolito)
    return backend.url;
  },
  ENDPOINTS: {
    // Autenticação
    LOGIN: '/auth/login',
    REGISTRAR: '/auth/registrar',
    VALIDAR_TOKEN: '/auth/validar',
    
    // Clientes
    CLIENTES: '/clientes',
    CLIENTE_BY_ID: (id) => `/clientes/${id}`,
    CLIENTE_BY_CPF: (cpf) => `/clientes/cpf/${cpf}`,
    
    // Pets
    PETS: '/pets',
    PET_BY_ID: (id) => `/pets/${id}`,
    PETS_BY_CLIENTE: (clienteId) => `/pets/cliente/${clienteId}`,
    
    // Serviços
    SERVICOS: '/servicos',
    SERVICOS_ATIVOS: '/servicos/ativos',
    SERVICO_BY_ID: (id) => `/servicos/${id}`,
    
    // Agendamentos
    AGENDAMENTOS: '/agendamentos',
    AGENDAMENTO_BY_ID: (id) => `/agendamentos/${id}`,
    AGENDAMENTO_DISPONIBILIDADE: '/agendamentos/disponibilidade',
    
    // Categorias
    CATEGORIAS: '/categorias',
    CATEGORIAS_ATIVAS: '/categorias/ativas',
    CATEGORIA_BY_ID: (id) => `/categorias/${id}`,
    
    // Produtos
    PRODUTOS: '/produtos',
    PRODUTOS_DISPONIVEIS: '/produtos/disponiveis',
    PRODUTO_BY_ID: (id) => `/produtos/${id}`,
    PRODUTOS_BY_CATEGORIA: (categoriaId) => `/produtos/categoria/${categoriaId}/disponiveis`,
    PRODUTOS_BUSCAR: '/produtos/buscar',
    
    // Pedidos
    PEDIDOS: '/pedidos',
    PEDIDO_BY_ID: (id) => `/pedidos/${id}`,
    PEDIDO_ADICIONAR_ITEM: (id) => `/pedidos/${id}/itens`,
    PEDIDO_REMOVER_ITEM: (pedidoId, itemId) => `/pedidos/${pedidoId}/itens/${itemId}`,
    PEDIDO_CONFIRMAR: (id) => `/pedidos/${id}/confirmar`,
    PEDIDOS_BY_CLIENTE: (clienteId) => `/pedidos/cliente/${clienteId}`
  }
};

/**
 * Classe para fazer requisições à API
 */
class ApiService {
  /**
   * Faz requisição GET
   */
  static async get(endpoint, params = {}) {
    try {
      const url = new URL(`${API_CONFIG.BASE_URL}${endpoint}`);
      
      // Adicionar parâmetros de query string
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined) {
          url.searchParams.append(key, params[key]);
        }
      });
      
      console.log(`🌐 GET: ${url}`);
      
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        }
      });
      
      return await this.handleResponse(response);
    } catch (error) {
      console.error('❌ Erro na requisição GET:', error);
      throw error;
    }
  }
  
  /**
   * Faz requisição POST
   */
  static async post(endpoint, data) {
    try {
      const url = `${API_CONFIG.BASE_URL}${endpoint}`;
      console.log(`🌐 POST: ${url}`, data);
      
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(data)
      });
      
      return await this.handleResponse(response);
    } catch (error) {
      console.error('❌ Erro na requisição POST:', error);
      throw error;
    }
  }
  
  /**
   * Faz requisição PUT
   */
  static async put(endpoint, data) {
    try {
      const url = `${API_CONFIG.BASE_URL}${endpoint}`;
      console.log(`🌐 PUT: ${url}`, data);
      
      const response = await fetch(url, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(data)
      });
      
      return await this.handleResponse(response);
    } catch (error) {
      console.error('❌ Erro na requisição PUT:', error);
      throw error;
    }
  }
  
  /**
   * Faz requisição DELETE
   */
  static async delete(endpoint) {
    try {
      const url = `${API_CONFIG.BASE_URL}${endpoint}`;
      console.log(`🌐 DELETE: ${url}`);
      
      const response = await fetch(url, {
        method: 'DELETE',
        headers: {
          'Accept': 'application/json'
        }
      });
      
      return await this.handleResponse(response);
    } catch (error) {
      console.error('❌ Erro na requisição DELETE:', error);
      throw error;
    }
  }
  
  /**
   * Trata a resposta da API
   */
  static async handleResponse(response) {
    const contentType = response.headers.get('content-type');
    
    // Se a resposta for 204 No Content, retornar null
    if (response.status === 204) {
      console.log('✅ Resposta: 204 No Content');
      return null;
    }
    
    // Tentar parsear JSON
    let data = null;
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      data = await response.text();
    }
    
    // Se a resposta não for OK (status 200-299)
    if (!response.ok) {
      console.error('❌ Erro na resposta da API:', {
        status: response.status,
        statusText: response.statusText,
        data: data
      });
      
      console.log('🔍 Tipo do data:', typeof data);
      console.log('🔍 Data completo:', JSON.stringify(data, null, 2));
      
      // Criar erro customizado
      const error = new Error(data.message || data.error || 'Erro na requisição');
      error.status = response.status;
      error.data = data;
      throw error;
    }
    
    console.log('✅ Resposta:', data);
    return data;
  }
}

/**
 * Exibe mensagem de erro amigável ao usuário
 */
function mostrarErroAPI(error, mensagemPadrao = 'Ocorreu um erro. Tente novamente.') {
  let mensagem = mensagemPadrao;
  
  console.log('🔍 Debug erro completo:', error);
  console.log('🔍 error.data:', error.data);
  
  if (error.data && error.data.message) {
    mensagem = error.data.message;
  } else if (error.data && error.data.errors) {
    // Erros de validação - pode ser objeto ou array
    if (typeof error.data.errors === 'object' && !Array.isArray(error.data.errors)) {
      // Formato: { campo: "mensagem", campo2: "mensagem2" }
      mensagem = 'Erros de validação:\n\n';
      Object.keys(error.data.errors).forEach(campo => {
        mensagem += `• ${campo}: ${error.data.errors[campo]}\n`;
      });
    } else if (Array.isArray(error.data.errors)) {
      // Formato: [{field: "campo", message: "mensagem"}]
      mensagem = 'Erros de validação:\n\n';
      error.data.errors.forEach(err => {
        mensagem += `• ${err.field}: ${err.message}\n`;
      });
    }
  } else if (error.data && error.data.error) {
    mensagem = error.data.error;
  } else if (error.data && typeof error.data === 'object') {
    // Tentar extrair mensagem do objeto de erro
    mensagem = JSON.stringify(error.data);
  } else if (error.message) {
    mensagem = error.message;
  }
  
  // Verificar se o backend está rodando
  if (error.message && error.message.includes('Failed to fetch')) {
    const backendAtual = getBackendAtual();
    const backend = BACKENDS[backendAtual];
    mensagem = `⚠️ Não foi possível conectar ao servidor ${backend.name}. Verifique se o backend está rodando em ${backend.url}`;
  }
  
  alert(mensagem);
  console.error('❌ Erro detalhado:', error);
}

/**
 * Formata data para o formato brasileiro
 */
function formatarData(dataISO) {
  if (!dataISO) return '';
  const data = new Date(dataISO);
  return data.toLocaleDateString('pt-BR');
}

/**
 * Formata valor monetário
 */
function formatarMoeda(valor) {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  }).format(valor);
}

/**
 * Remove formatação de CPF para enviar ao backend
 */
function limparCPF(cpf) {
  return cpf.replace(/\D/g, '');
}

/**
 * Remove formatação de telefone para enviar ao backend
 */
function limparTelefone(telefone) {
  return telefone.replace(/\D/g, '');
}

/**
 * Converte data do input (yyyy-mm-dd) para LocalDate do backend
 */
function formatarDataParaBackend(dataInput) {
  // O backend espera yyyy-MM-dd, que é o mesmo formato do input type="date"
  return dataInput;
}

/**
 * Salva dados no localStorage
 */
function salvarLocalStorage(chave, dados) {
  try {
    localStorage.setItem(chave, JSON.stringify(dados));
    console.log(`💾 Dados salvos no localStorage: ${chave}`);
  } catch (error) {
    console.error('❌ Erro ao salvar no localStorage:', error);
  }
}

/**
 * Recupera dados do localStorage
 */
function carregarLocalStorage(chave) {
  try {
    const dados = localStorage.getItem(chave);
    if (dados) {
      console.log(`📂 Dados carregados do localStorage: ${chave}`);
      return JSON.parse(dados);
    }
    return null;
  } catch (error) {
    console.error('❌ Erro ao carregar do localStorage:', error);
    return null;
  }
}

/**
 * Remove dados do localStorage
 */
function limparLocalStorage(chave) {
  try {
    localStorage.removeItem(chave);
    console.log(`🗑️ Dados removidos do localStorage: ${chave}`);
  } catch (error) {
    console.error('❌ Erro ao remover do localStorage:', error);
  }
}

/**
 * Alterna o backend entre Spring Boot e ASP.NET Core
 */
function alternarBackend(novoBackend) {
  if (!BACKENDS[novoBackend]) {
    console.error(`❌ Backend inválido: ${novoBackend}`);
    return false;
  }
  
  localStorage.setItem('backend-selecionado', novoBackend);
  const backend = BACKENDS[novoBackend];
  console.log(`🔄 Backend alterado para: ${backend.name} (${backend.url})`);
  
  // Atualizar indicador visual se existir
  atualizarIndicadorBackend();
  
  return true;
}

/**
 * Obtém informações do backend atual
 */
function getBackendInfo() {
  const backendAtual = getBackendAtual();
  return {
    key: backendAtual,
    ...BACKENDS[backendAtual]
  };
}

/**
 * Atualiza o indicador visual do backend (se existir na página)
 */
function atualizarIndicadorBackend() {
  const indicador = document.getElementById('backend-status');
  if (indicador) {
    const backend = getBackendInfo();
    indicador.textContent = `Backend: ${backend.name}`;
    indicador.className = 'backend-status';
    indicador.dataset.backend = backend.key;
  }
}

// Exportar para uso global
window.BACKENDS = BACKENDS;
window.API_CONFIG = API_CONFIG;
window.ApiService = ApiService;
window.mostrarErroAPI = mostrarErroAPI;
window.formatarData = formatarData;
window.formatarMoeda = formatarMoeda;
window.limparCPF = limparCPF;
window.limparTelefone = limparTelefone;
window.formatarDataParaBackend = formatarDataParaBackend;
window.salvarLocalStorage = salvarLocalStorage;
window.carregarLocalStorage = carregarLocalStorage;
window.limparLocalStorage = limparLocalStorage;
window.alternarBackend = alternarBackend;
window.getBackendInfo = getBackendInfo;
window.getBackendAtual = getBackendAtual;

console.log('✅ API Config carregado!');
console.log(`🎯 Backend atual: ${getBackendInfo().name} (${getBackendInfo().url})`);
