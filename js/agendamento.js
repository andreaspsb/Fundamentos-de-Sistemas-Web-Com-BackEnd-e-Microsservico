// ========================================
// JavaScript para Agendamento de Serviços
// ========================================

document.addEventListener('DOMContentLoaded', function() {
  const form = document.getElementById('agendamentoForm');
  
  if (!form) return;
  
  console.log('📅 Formulário de agendamento carregado!');
  
  // Configurar data mínima (hoje)
  configurarDataMinima();
  
  // Adicionar interatividade aos cards de serviço
  configurarCardsServicos();
  
  // Adicionar interatividade aos cards de método
  configurarCardsMetodo();
  
  // Configurar máscara de telefone
  configurarMascaraTelefone();
  
  // Pré-selecionar serviço da URL
  preencherServicoURL();
  
  // Configurar botão "Ver Resumo"
  const btnResumo = document.querySelector('button[onclick="calcularResumo()"]');
  if (btnResumo) {
    btnResumo.removeAttribute('onclick');
    btnResumo.addEventListener('click', function(e) {
      e.preventDefault();
      e.stopPropagation();
      calcularResumo();
    });
    console.log('✅ Botão Ver Resumo configurado');
  }
  
  // Validação do formulário
  form.addEventListener('submit', function(event) {
    event.preventDefault();
    event.stopPropagation();
    
    if (validarFormulario()) {
      processarAgendamento();
    } else {
      mostrarErrosValidacao();
    }
    
    form.classList.add('was-validated');
  }, false);
  
  // Limpar ao resetar
  form.addEventListener('reset', function() {
    form.classList.remove('was-validated');
    document.getElementById('sucessoMsg').style.display = 'none';
    document.getElementById('resumoAgendamento').style.display = 'none';
    limparSelecaoCards();
    console.log('🔄 Formulário resetado');
  });
});

/**
 * Configura a data mínima como hoje
 */
function configurarDataMinima() {
  const dataInput = document.getElementById('dataAgendamento');
  const hoje = new Date().toISOString().split('T')[0];
  dataInput.min = hoje;
  
  // Definir data máxima (30 dias a partir de hoje)
  const dataMaxima = new Date();
  dataMaxima.setDate(dataMaxima.getDate() + 30);
  dataInput.max = dataMaxima.toISOString().split('T')[0];
  
  console.log('📆 Calendário configurado - Período disponível: hoje até 30 dias');
}

/**
 * Configura interatividade dos cards de serviço
 */
function configurarCardsServicos() {
  const cards = ['cardBanho', 'cardTosa', 'cardCompleto'];
  const checkboxes = ['servicoBanho', 'servicoTosa', 'servicoCompleto'];
  
  console.log('🔧 Configurando cards de serviços...');
  
  cards.forEach((cardId, index) => {
    const card = document.getElementById(cardId);
    const checkbox = document.getElementById(checkboxes[index]);
    
    if (!card) {
      console.error(`❌ Card não encontrado: ${cardId}`);
      return;
    }
    
    if (!checkbox) {
      console.error(`❌ Checkbox não encontrado: ${checkboxes[index]}`);
      return;
    }
    
    console.log(`✅ Configurando: ${cardId}`);
    
    card.addEventListener('click', function(e) {
      console.log('🖱️ Click detectado no card:', cardId);
      console.log('Estado atual do checkbox:', checkbox.checked);
      
      // Se já estava selecionado, desmarcar
      if (checkbox.checked) {
        checkbox.checked = false;
        atualizarVisualCard(card, false);
        console.log(`🛁 Serviço ${checkbox.value}: desmarcado`);
        return;
      }
      
      // Desmarcar todos os outros serviços primeiro
      const todosCheckboxes = ['servicoBanho', 'servicoTosa', 'servicoCompleto'];
      const todosCards = ['cardBanho', 'cardTosa', 'cardCompleto'];
      
      todosCheckboxes.forEach((id, i) => {
        const cb = document.getElementById(id);
        const c = document.getElementById(todosCards[i]);
        if (cb && c) {
          cb.checked = false;
          atualizarVisualCard(c, false);
        }
      });
      
      // Marcar apenas o selecionado
      checkbox.checked = true;
      atualizarVisualCard(card, true);
      console.log(`🛁 Serviço ${checkbox.value}: selecionado`);
      console.log('Novo estado do checkbox:', checkbox.checked);
      
      // Esconder mensagem de erro ao selecionar um serviço
      const servicoError = document.getElementById('servicoError');
      if (servicoError) {
        servicoError.classList.remove('d-block');
        servicoError.style.display = 'none';
      }
    });
  });
}

/**
 * Configura interatividade dos cards de método
 */
function configurarCardsMetodo() {
  const cards = ['cardTelebusca', 'cardLocal'];
  const radios = ['metodoTelebusca', 'metodoLocal'];
  
  cards.forEach((cardId, index) => {
    const card = document.getElementById(cardId);
    const radio = document.getElementById(radios[index]);
    
    card.addEventListener('click', function(e) {
      if (e.target.type !== 'radio') {
        radio.checked = true;
      }
      
      // Atualizar todos os cards
      cards.forEach((id, i) => {
        const c = document.getElementById(id);
        atualizarVisualCard(c, radios[i] === radios[index]);
      });
      
      console.log(`🚗 Método selecionado: ${radio.value}`);
      
      // Esconder mensagem de erro ao selecionar um método
      const metodoError = document.getElementById('metodoError');
      if (metodoError) {
        metodoError.classList.remove('d-block');
        metodoError.style.display = 'none';
      }
    });
  });
}

/**
 * Configura máscara de telefone
 */
function configurarMascaraTelefone() {
  const telefoneInput = document.getElementById('telefone');
  
  if (!telefoneInput) return;
  
  telefoneInput.addEventListener('input', function(e) {
    let valor = e.target.value.replace(/\D/g, ''); // Remove tudo que não é dígito
    
    if (valor.length <= 10) {
      // Formato: (00) 0000-0000
      valor = valor.replace(/^(\d{2})(\d{4})(\d{0,4}).*/, '($1) $2-$3');
    } else {
      // Formato: (00) 00000-0000
      valor = valor.replace(/^(\d{2})(\d{5})(\d{0,4}).*/, '($1) $2-$3');
    }
    
    e.target.value = valor;
  });
  
  console.log('📱 Máscara de telefone configurada');
}

/**
 * Pré-seleciona serviço baseado no parâmetro da URL
 */
function preencherServicoURL() {
  const urlParams = new URLSearchParams(window.location.search);
  const servico = urlParams.get('servico');
  
  if (!servico) return;
  
  console.log('🔗 Parâmetro de URL detectado:', servico);
  
  // Mapear valores da URL para IDs dos elementos
  const mapeamento = {
    'banho': { checkbox: 'servicoBanho', card: 'cardBanho' },
    'tosa': { checkbox: 'servicoTosa', card: 'cardTosa' },
    'completo': { checkbox: 'servicoCompleto', card: 'cardCompleto' }
  };
  
  const elemento = mapeamento[servico];
  
  if (elemento) {
    const checkbox = document.getElementById(elemento.checkbox);
    const card = document.getElementById(elemento.card);
    
    if (checkbox && card) {
      // Desmarcar todos primeiro
      ['servicoBanho', 'servicoTosa', 'servicoCompleto'].forEach(id => {
        const cb = document.getElementById(id);
        if (cb) cb.checked = false;
      });
      
      ['cardBanho', 'cardTosa', 'cardCompleto'].forEach(id => {
        const c = document.getElementById(id);
        if (c) atualizarVisualCard(c, false);
      });
      
      // Marcar o serviço selecionado
      checkbox.checked = true;
      atualizarVisualCard(card, true);
      
      // Scroll suave até o serviço selecionado
      setTimeout(() => {
        card.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }, 500);
      
      console.log('✅ Serviço pré-selecionado:', servico);
    }
  }
}

/**
 * Atualiza visual do card (selecionado/não selecionado)
 */
function atualizarVisualCard(card, selecionado) {
  if (selecionado) {
    card.classList.add('selected');
    card.style.borderColor = '#0d6efd';
    card.style.borderWidth = '3px';
    card.style.backgroundColor = '#e7f3ff';
  } else {
    card.classList.remove('selected');
    card.style.borderColor = '';
    card.style.borderWidth = '';
    card.style.backgroundColor = '';
  }
}

/**
 * Limpa seleção visual dos cards
 */
function limparSelecaoCards() {
  const allCards = document.querySelectorAll('.servico-card, .metodo-card');
  allCards.forEach(card => {
    card.classList.remove('selected');
    card.style.borderColor = '';
    card.style.borderWidth = '';
    card.style.backgroundColor = '';
  });
}

/**
 * Valida o formulário
 */
function validarFormulario() {
  const form = document.getElementById('agendamentoForm');
  let valido = true;
  
  // Validar serviços
  const servicosSelecionados = document.querySelectorAll('input[name="servicos"]:checked');
  const servicoError = document.getElementById('servicoError');
  if (servicosSelecionados.length === 0) {
    servicoError.classList.add('d-block');
    servicoError.style.display = 'block';
    valido = false;
    setTimeout(() => {
      servicoError.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
  } else {
    servicoError.classList.remove('d-block');
    servicoError.style.display = 'none';
  }
  
  // Validar método
  const metodoSelecionado = document.querySelector('input[name="metodo"]:checked');
  const metodoError = document.getElementById('metodoError');
  if (!metodoSelecionado) {
    metodoError.classList.add('d-block');
    metodoError.style.display = 'block';
    valido = false;
    setTimeout(() => {
      metodoError.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
  } else {
    metodoError.classList.remove('d-block');
    metodoError.style.display = 'none';
  }
  
  // Validar data (não pode ser domingo)
  const dataInput = document.getElementById('dataAgendamento');
  if (dataInput.value) {
    const data = new Date(dataInput.value + 'T00:00:00');
    if (data.getDay() === 0) {
      alert('⚠️ Não atendemos aos domingos. Por favor, selecione outra data.');
      valido = false;
    }
  }
  
  return valido && form.checkValidity();
}

/**
 * Mostra erros de validação
 */
function mostrarErrosValidacao() {
  const invalidFields = document.querySelectorAll('.form-control:invalid, .form-select:invalid');
  
  console.warn('⚠️ Formulário com erros de validação');
  
  if (invalidFields.length > 0) {
    invalidFields[0].scrollIntoView({ behavior: 'smooth', block: 'center' });
    setTimeout(() => invalidFields[0].focus(), 500);
  }
}

/**
 * Calcula e mostra o resumo do agendamento
 */
function calcularResumo() {
  console.log('🧮 Calculando resumo...');
  
  // Coletar serviços
  const servicosSelecionados = Array.from(
    document.querySelectorAll('input[name="servicos"]:checked')
  ).map(cb => cb.value);
  
  console.log('Serviços selecionados:', servicosSelecionados);
  
  if (servicosSelecionados.length === 0) {
    alert('⚠️ Por favor, selecione pelo menos um serviço.');
    return;
  }
  
  // Calcular valores
  let total = 0;
  let servicosTexto = [];
  
  if (servicosSelecionados.includes('completo')) {
    total = 80;
    servicosTexto.push('Banho + Tosa (Combo)');
  } else {
    if (servicosSelecionados.includes('banho')) {
      total += 50;
      servicosTexto.push('Banho');
    }
    if (servicosSelecionados.includes('tosa')) {
      total += 40;
      servicosTexto.push('Tosa');
    }
  }
  
  console.log('Serviços texto:', servicosTexto);
  console.log('Total parcial:', total);
  
  // Adicionar taxa de tele-busca
  const metodo = document.querySelector('input[name="metodo"]:checked');
  console.log('Método selecionado:', metodo ? metodo.value : 'nenhum');
  
  if (metodo && metodo.value === 'telebusca') {
    total += 20;
  }
  
  // Montar resumo
  const data = document.getElementById('dataAgendamento').value;
  const horario = document.getElementById('horarioAgendamento').value;
  const nomePet = document.getElementById('nomePet').value;
  const metodoTexto = metodo ? (metodo.value === 'telebusca' ? 'Tele-busca' : 'Entrega no local') : 'Não selecionado';
  
  const resumoHTML = `
    <p><strong>Serviço(s):</strong> ${servicosTexto.join(' + ')}</p>
    <p><strong>Método:</strong> ${metodoTexto} ${metodo && metodo.value === 'telebusca' ? '(+ R$ 20,00)' : ''}</p>
    <p><strong>Pet:</strong> ${nomePet || 'Não informado'}</p>
    <p><strong>Data:</strong> ${data ? new Date(data + 'T00:00:00').toLocaleDateString('pt-BR') : 'Não selecionada'}</p>
    <p><strong>Horário:</strong> ${horario || 'Não selecionado'}</p>
  `;
  
  document.getElementById('resumoConteudo').innerHTML = resumoHTML;
  document.getElementById('valorTotal').textContent = `R$ ${total.toFixed(2).replace('.', ',')}`;
  document.getElementById('resumoAgendamento').style.display = 'block';
  
  // Scroll para o resumo com delay
  setTimeout(() => {
    document.getElementById('resumoAgendamento').scrollIntoView({ behavior: 'smooth', block: 'center' });
  }, 150);
  
  console.log('💰 Resumo calculado - Total: R$', total);
}

/**
 * Processa o agendamento
 */
function processarAgendamento() {
  const formData = new FormData(document.getElementById('agendamentoForm'));
  const dados = Object.fromEntries(formData.entries());
  
  // Coletar serviços (checkboxes)
  const servicos = Array.from(
    document.querySelectorAll('input[name="servicos"]:checked')
  ).map(cb => cb.value);
  
  dados.servicos = servicos;
  
  console.log('═══════════════════════════════════════');
  console.log('📅 PROCESSANDO AGENDAMENTO');
  console.log('═══════════════════════════════════════');
  console.log('Dados:', dados);
  
  // Mostrar loading
  const submitBtn = document.querySelector('button[type="submit"]');
  submitBtn.disabled = true;
  submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Confirmando...';
  
  // Simular processamento com setTimeout (1.5 segundos)
  setTimeout(() => {
    submitBtn.disabled = false;
    submitBtn.innerHTML = '<i class="bi bi-check-circle"></i> Confirmar Agendamento';
    
    // Resetar formulário ANTES de mostrar a mensagem
    document.getElementById('agendamentoForm').reset();
    document.getElementById('agendamentoForm').classList.remove('was-validated');
    document.getElementById('resumoAgendamento').style.display = 'none';
    limparSelecaoCards();
    
    // Mostrar mensagem de sucesso
    const sucessoMsg = document.getElementById('sucessoMsg');
    sucessoMsg.style.display = 'block';
    
    console.log('✅ Mensagem de sucesso exibida!');
    
    // Scroll para mensagem de sucesso com delay maior
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
      console.log('🔝 Scroll para topo da página');
    }, 300);
    
    console.log('✅ Agendamento confirmado!');
    console.log('═══════════════════════════════════════');
  }, 1500);
}
