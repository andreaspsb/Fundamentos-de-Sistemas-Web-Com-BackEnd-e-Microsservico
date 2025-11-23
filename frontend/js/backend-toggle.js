/**
 * Backend Toggle Component
 * Componente para alternar entre backends (Spring Boot / ASP.NET Core)
 */

class BackendToggle {
  constructor() {
    this.inicializar();
  }
  
  /**
   * Inicializa o componente de toggle
   */
  inicializar() {
    // Criar elemento do toggle
    this.criarElemento();
    
    // Atualizar estado inicial
    this.atualizarEstado();
    
    // Adicionar listeners
    this.adicionarListeners();
    
    console.log('✅ Backend Toggle inicializado');
  }
  
  /**
   * Cria o elemento HTML do toggle
   */
  criarElemento() {
    const container = document.createElement('div');
    container.className = 'backend-selector';
    container.innerHTML = `
      <p class="backend-selector-label">Backend:</p>
      <div class="backend-toggle-wrapper">
        <button class="backend-option" data-backend="SPRINGBOOT">
          Spring Boot
        </button>
        <button class="backend-option" data-backend="ASPNET">
          ASP.NET Core
        </button>
      </div>
      <div class="backend-info">
        <span class="backend-status-indicator"></span>
        <span class="backend-info-text">localhost:<span class="backend-port">8080</span></span>
      </div>
    `;
    
    document.body.appendChild(container);
    this.elemento = container;
  }
  
  /**
   * Adiciona event listeners aos botões
   */
  adicionarListeners() {
    const botoes = this.elemento.querySelectorAll('.backend-option');
    
    botoes.forEach(botao => {
      botao.addEventListener('click', () => {
        const backend = botao.dataset.backend;
        this.alternarBackend(backend);
      });
    });
  }
  
  /**
   * Alterna para o backend especificado
   */
  alternarBackend(backend) {
    if (alternarBackend(backend)) {
      this.atualizarEstado();
      this.mostrarNotificacao(backend);
    }
  }
  
  /**
   * Atualiza o estado visual do toggle
   */
  atualizarEstado() {
    const backendInfo = getBackendInfo();
    const botoes = this.elemento.querySelectorAll('.backend-option');
    
    // Atualizar botões
    botoes.forEach(botao => {
      const isAtivo = botao.dataset.backend === backendInfo.key;
      
      if (isAtivo) {
        botao.classList.add('active');
        botao.classList.add(backendInfo.key.toLowerCase());
      } else {
        botao.classList.remove('active', 'springboot', 'aspnet');
      }
    });
    
    // Atualizar informações de porta
    const portaElement = this.elemento.querySelector('.backend-port');
    if (portaElement) {
      portaElement.textContent = backendInfo.port;
    }
  }
  
  /**
   * Mostra notificação de troca de backend
   */
  mostrarNotificacao(backend) {
    const backendInfo = BACKENDS[backend];
    const notificacao = document.createElement('div');
    notificacao.className = 'backend-notification';
    notificacao.innerHTML = `
      <strong>Backend alterado!</strong><br>
      Agora usando: ${backendInfo.name}<br>
      <small>${backendInfo.url}</small>
    `;
    notificacao.style.cssText = `
      position: fixed;
      top: 80px;
      right: 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 15px 20px;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
      z-index: 10000;
      animation: slideIn 0.3s ease-out, fadeOut 0.3s ease-out 2.7s forwards;
      font-size: 14px;
      min-width: 250px;
    `;
    
    document.body.appendChild(notificacao);
    
    // Remover após 3 segundos
    setTimeout(() => {
      notificacao.remove();
    }, 3000);
  }
}

// Inicializar quando o DOM estiver pronto
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    window.backendToggle = new BackendToggle();
  });
} else {
  window.backendToggle = new BackendToggle();
}

console.log('✅ Backend Toggle component carregado!');
