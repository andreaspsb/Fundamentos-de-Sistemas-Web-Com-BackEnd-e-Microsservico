// ========================================
// Gerenciamento de Autenticação
// ========================================

/**
 * Classe para gerenciar autenticação
 */
class AuthManager {
  
  static TOKEN_KEY = 'petshop_auth_token';
  static USER_KEY = 'petshop_user_data';

  /**
   * Helper para resolver caminhos relativos baseado na localização atual
   */
  static getRelativePath(targetFile) {
    const currentPath = window.location.pathname;
    
    // Se estiver em /categorias/xxx/
    if (currentPath.match(/\/categorias\/[^/]+\//)) {
      return `../../${targetFile}`;
    }
    // Se estiver em /servicos/ ou /admin/
    else if (currentPath.includes('/servicos/') || currentPath.includes('/admin/')) {
      return `../${targetFile}`;
    }
    // Se estiver na raiz
    else {
      return targetFile;
    }
  }

  /**
   * Faz login do usuário
   */
  static async login(username, senha) {
    try {
      const response = await ApiService.post(API_CONFIG.ENDPOINTS.LOGIN, {
        username,
        senha
      });

      // Normalizar resposta (APIs podem retornar PascalCase ou camelCase)
      const normalizedResponse = {
        token: response.token || response.Token,
        username: response.username || response.Username,
        email: response.email || response.Email,
        role: response.role || response.Role,
        clienteId: response.clienteId || response.ClienteId,
        clienteNome: response.clienteNome || response.ClienteNome
      };

      // Salvar token e dados do usuário
      this.saveToken(normalizedResponse.token);
      this.saveUserData(normalizedResponse);

      console.log('✅ Login realizado com sucesso:', normalizedResponse.username);
      return normalizedResponse;
    } catch (error) {
      console.error('❌ Erro no login:', error);
      throw error;
    }
  }

  /**
   * Faz logout do usuário
   */
  static logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    console.log('👋 Logout realizado');
    window.location.href = this.getRelativePath('login.html');
  }

  /**
   * Verifica se usuário está autenticado
   */
  static isAuthenticated() {
    return this.getToken() !== null;
  }

  /**
   * Retorna o token salvo
   */
  static getToken() {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Salva o token
   */
  static saveToken(token) {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * Retorna dados do usuário logado
   */
  static getUserData() {
    const data = localStorage.getItem(this.USER_KEY);
    return data ? JSON.parse(data) : null;
  }

  /**
   * Salva dados do usuário
   */
  static saveUserData(userData) {
    localStorage.setItem(this.USER_KEY, JSON.stringify(userData));
  }

  /**
   * Verifica se usuário é admin
   */
  static isAdmin() {
    const user = this.getUserData();
    return user && user.role === 'ADMIN';
  }

  /**
   * Verifica se usuário é cliente
   */
  static isCliente() {
    const user = this.getUserData();
    return user && user.role === 'CLIENTE';
  }

  /**
   * Valida token com o backend
   */
  static async validateToken() {
    try {
      const token = this.getToken();
      if (!token) return false;

      const response = await fetch(API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.VALIDAR_TOKEN, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      const data = await response.json();
      return data.valido;
    } catch (error) {
      console.error('❌ Erro ao validar token:', error);
      return false;
    }
  }

  /**
   * Protege uma página (redireciona se não autenticado)
   */
  static async protectPage(requiredRole = null) {
    if (!this.isAuthenticated()) {
      console.warn('⚠️ Página protegida - redirecionando para login');
      window.location.href = this.getRelativePath('login.html');
      return false;
    }

    // Validar token com backend
    const tokenValido = await this.validateToken();
    if (!tokenValido) {
      console.warn('⚠️ Token inválido - redirecionando para login');
      this.logout();
      return false;
    }

    // Verificar role se especificada
    if (requiredRole) {
      const user = this.getUserData();
      if (user.role !== requiredRole) {
        console.warn('⚠️ Acesso negado - role inadequada');
        alert('Você não tem permissão para acessar esta página');
        window.location.href = this.getRelativePath('index.html');
        return false;
      }
    }

    return true;
  }

  /**
   * Atualiza navbar com informações do usuário
   */
  static updateNavbar() {
    const navbar = document.querySelector('.navbar-nav.ms-auto');
    
    if (!navbar) return;

    // Se não estiver logado, adicionar botão de Login
    if (!this.isAuthenticated()) {
      const loginItem = document.createElement('li');
      loginItem.className = 'nav-item';
      
      const loginPath = this.getRelativePath('login.html');
      
      loginItem.innerHTML = `
        <a class="nav-link" href="${loginPath}">
          <button class="btn btn-outline-primary btn-sm">
            🔐 Entrar
          </button>
        </a>
      `;
      navbar.appendChild(loginItem);
      return;
    }

    // Se estiver logado, adicionar dropdown do usuário
    const user = this.getUserData();

    // Adicionar item de usuário
    const userItem = document.createElement('li');
    userItem.className = 'nav-item dropdown';
    
    const adminPath = this.getRelativePath('admin/index.html');
    const agendamentosPath = this.getRelativePath('meus-agendamentos.html');
    const pedidosPath = this.getRelativePath('meus-pedidos.html');
    
    userItem.innerHTML = `
      <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
        👤 ${user.username}
      </a>
      <ul class="dropdown-menu dropdown-menu-end">
        <li><span class="dropdown-item-text"><small>Role: ${user.role}</small></span></li>
        <li><hr class="dropdown-divider"></li>
        ${user.role === 'ADMIN' ? `<li><a class="dropdown-item" href="${adminPath}">🛠️ Admin Panel</a></li>` : ''}
        <li><a class="dropdown-item" href="${agendamentosPath}">📅 Meus Agendamentos</a></li>
        <li><a class="dropdown-item" href="${pedidosPath}">📦 Meus Pedidos</a></li>
        <li><hr class="dropdown-divider"></li>
        <li><a class="dropdown-item" href="#" id="logoutBtn">🚪 Sair</a></li>
      </ul>
    `;
    
    navbar.appendChild(userItem);

    // Adicionar evento de logout
    document.getElementById('logoutBtn').addEventListener('click', (e) => {
      e.preventDefault();
      if (confirm('Deseja realmente sair?')) {
        this.logout();
      }
    });
  }
}

// Expor para uso global
window.AuthManager = AuthManager;
