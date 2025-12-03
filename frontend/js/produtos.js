// ========================================
// JavaScript para Produtos com integração Backend
// ========================================

document.addEventListener('DOMContentLoaded', async function() {
  console.log('🛍️ Página de produtos carregada!');
  
  // Identificar qual categoria está sendo exibida
  const path = window.location.pathname;
  let categoriaAtual = null;
  
  if (path.includes('racoes-alimentacao')) {
    categoriaAtual = 'Rações e Alimentação';
  } else if (path.includes('higiene-cuidados')) {
    categoriaAtual = 'Higiene e Cuidados';
  } else if (path.includes('acessorios-brinquedos')) {
    categoriaAtual = 'Acessórios e Brinquedos';
  }
  
  if (categoriaAtual) {
    await carregarProdutosDaCategoria(categoriaAtual);
  }
});

/**
 * Carrega produtos de uma categoria específica do backend
 */
async function carregarProdutosDaCategoria(nomeCategoria) {
  try {
    console.log(`📦 Carregando produtos da categoria: ${nomeCategoria}`);
    
    // 1. Buscar todas as categorias
    const categorias = await ApiService.get(API_CONFIG.ENDPOINTS.CATEGORIAS_ATIVAS);
    console.log('✅ Categorias carregadas:', categorias);
    
    // 2. Encontrar a categoria atual
    const categoria = categorias.find(cat => cat.nome === nomeCategoria);
    
    if (!categoria) {
      console.warn(`⚠️ Categoria "${nomeCategoria}" não encontrada`);
      return;
    }
    
    console.log(`📁 Categoria encontrada:`, categoria);
    
    // 3. Buscar produtos da categoria
    const produtos = await ApiService.get(
      API_CONFIG.ENDPOINTS.PRODUTOS_BY_CATEGORIA(categoria.id)
    );
    
    console.log(`✅ ${produtos.length} produtos carregados:`, produtos);
    
    // 4. Renderizar produtos na página
    renderizarProdutos(produtos);
    
  } catch (error) {
    console.error('❌ Erro ao carregar produtos:', error);
    mostrarErroCarregamento();
  }
}

/**
 * Renderiza produtos na página
 */
function renderizarProdutos(produtos) {
  const container = document.querySelector('main .row.g-4');
  
  if (!container) {
    console.error('❌ Container de produtos não encontrado');
    return;
  }
  
  // Limpar produtos existentes
  container.innerHTML = '';
  
  if (produtos.length === 0) {
    container.innerHTML = `
      <div class="col-12">
        <div class="alert alert-info">
          <i class="bi bi-info-circle"></i> Nenhum produto disponível no momento.
        </div>
      </div>
    `;
    return;
  }
  
  // Renderizar cada produto
  produtos.forEach(produto => {
    const produtoHTML = `
      <div class="col-md-6">
        <div class="card produto-card shadow-sm h-100">
          <img src="${produto.urlImagem || 'https://via.placeholder.com/600x300?text=Sem+Imagem'}" 
               class="card-img-top" 
               alt="${produto.nome}" 
               style="height: 300px; object-fit: cover;">
          <div class="card-body">
            <h3 class="card-title text-primary">${produto.nome}</h3>
            <p class="card-text">${produto.descricao || 'Sem descrição'}</p>
            <div class="d-flex justify-content-between align-items-center mt-3">
              <span class="badge bg-success fs-5">${formatarMoeda(produto.preco)}</span>
              <div>
                <small class="text-muted d-block mb-2">
                  ${produto.quantidadeEstoque > 0 
                    ? `✅ ${produto.quantidadeEstoque} em estoque` 
                    : '❌ Fora de estoque'}
                </small>
                <button class="btn btn-primary" 
                        onclick="adicionarAoCarrinho(${produto.id}, '${produto.nome.replace(/'/g, "\\'")}', ${produto.preco}, ${produto.quantidadeEstoque}, '${produto.urlImagem || ''}')"
                        ${produto.quantidadeEstoque === 0 ? 'disabled' : ''}>
                  <i class="bi bi-cart-plus"></i> Adicionar ao Carrinho
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
    
    container.insertAdjacentHTML('beforeend', produtoHTML);
  });
  
  console.log(`✅ ${produtos.length} produtos renderizados`);
}

/**
 * Adiciona produto ao carrinho
 */
async function adicionarAoCarrinho(produtoId, produtoNome, produtoPreco, quantidadeEstoque, urlImagem) {
  console.log(`🛒 Adicionando ao carrinho: ${produtoNome} (ID: ${produtoId})`);
  
  try {
    // Buscar dados completos do produto se necessário
    if (!urlImagem) {
      const produto = await ApiService.get(`${API_CONFIG.ENDPOINTS.PRODUTOS}/${produtoId}`);
      urlImagem = produto.urlImagem;
      quantidadeEstoque = produto.quantidadeEstoque;
    }
    
    // Usar CarrinhoManager
    if (typeof CarrinhoManager !== 'undefined') {
      const produto = {
        id: produtoId,
        nome: produtoNome,
        preco: produtoPreco,
        quantidadeEstoque: quantidadeEstoque || 999,
        urlImagem: urlImagem
      };
      
      CarrinhoManager.adicionarProduto(produto, 1);
    } else {
      // Fallback para o método antigo
      let carrinho = carregarLocalStorage('carrinho') || [];
      
      const itemExistente = carrinho.find(item => item.produtoId === produtoId);
      
      if (itemExistente) {
        itemExistente.quantidade++;
      } else {
        carrinho.push({
          produtoId: produtoId,
          nome: produtoNome,
          preco: produtoPreco,
          quantidade: 1
        });
      }
      
      salvarLocalStorage('carrinho', carrinho);
      mostrarNotificacao(`✅ ${produtoNome} adicionado ao carrinho!`, 'success');
    }
    
  } catch (error) {
    console.error('❌ Erro ao adicionar ao carrinho:', error);
    mostrarNotificacao('❌ Erro ao adicionar ao carrinho', 'danger');
  }
}

/**
 * Atualiza contador do carrinho no menu
 */
/**
 * Mostra notificação usando toast do Bootstrap
 */
function mostrarNotificacao(mensagem, tipo = 'info') {
  // Criar elemento do toast se não existir
  let toastContainer = document.querySelector('.toast-container');
  
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
    document.body.appendChild(toastContainer);
  }
  
  const toastId = 'toast-' + Date.now();
  const toastHTML = `
    <div id="${toastId}" class="toast align-items-center text-bg-${tipo} border-0" role="alert">
      <div class="d-flex">
        <div class="toast-body">
          ${mensagem}
        </div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
      </div>
    </div>
  `;
  
  toastContainer.insertAdjacentHTML('beforeend', toastHTML);
  
  const toastElement = document.getElementById(toastId);
  const toast = new bootstrap.Toast(toastElement, { delay: 3000 });
  toast.show();
  
  // Remover do DOM após fechar
  toastElement.addEventListener('hidden.bs.toast', function() {
    toastElement.remove();
  });
}

/**
 * Mostra mensagem de erro ao carregar produtos
 */
function mostrarErroCarregamento() {
  const container = document.querySelector('main .row.g-4');
  
  if (container) {
    container.innerHTML = `
      <div class="col-12">
        <div class="alert alert-danger">
          <h4 class="alert-heading"><i class="bi bi-exclamation-triangle"></i> Erro ao Carregar Produtos</h4>
          <p>Não foi possível carregar os produtos. Verifique se o servidor está rodando.</p>
          <hr>
          <p class="mb-0">
            <button class="btn btn-danger" onclick="location.reload()">
              <i class="bi bi-arrow-clockwise"></i> Tentar Novamente
            </button>
          </p>
        </div>
      </div>
    `;
  }
}

// Tornar funções disponíveis globalmente
window.adicionarAoCarrinho = adicionarAoCarrinho;
