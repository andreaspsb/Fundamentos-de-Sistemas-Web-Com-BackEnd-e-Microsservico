// ========================================
// JavaScript para Formulário de Cadastro
// ========================================

document.addEventListener('DOMContentLoaded', function() {
  const form = document.getElementById('cadastroForm');
  
  if (!form) return;
  
  console.log('📋 Formulário de cadastro carregado!');
  
  // Aplicar máscaras nos campos
  aplicarMascaras();
  
  // Validação do formulário
  form.addEventListener('submit', function(event) {
    event.preventDefault();
    event.stopPropagation();
    
    if (form.checkValidity()) {
      processarCadastro();
    } else {
      mostrarErrosValidacao();
    }
    
    form.classList.add('was-validated');
  }, false);
  
  // Limpar validação ao resetar
  form.addEventListener('reset', function() {
    form.classList.remove('was-validated');
    document.getElementById('sucessoMsg').style.display = 'none';
    console.log('🔄 Formulário resetado');
  });
});

/**
 * Aplica máscaras de formatação nos campos
 */
function aplicarMascaras() {
  // Máscara de CPF
  const cpfInput = document.getElementById('cpf');
  cpfInput.addEventListener('input', function(e) {
    let value = e.target.value.replace(/\D/g, '');
    if (value.length <= 11) {
      value = value.replace(/(\d{3})(\d)/, '$1.$2');
      value = value.replace(/(\d{3})(\d)/, '$1.$2');
      value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
      e.target.value = value;
    }
  });
  
  // Máscara de Telefone
  const telefoneInput = document.getElementById('telefone');
  telefoneInput.addEventListener('input', function(e) {
    let value = e.target.value.replace(/\D/g, '');
    if (value.length <= 11) {
      value = value.replace(/(\d{2})(\d)/, '($1) $2');
      value = value.replace(/(\d{5})(\d)/, '$1-$2');
      e.target.value = value;
    }
  });
  
  console.log('✨ Máscaras de formatação aplicadas');
}

/**
 * Mostra erros de validação
 */
function mostrarErrosValidacao() {
  const invalidFields = document.querySelectorAll('.form-control:invalid, .form-check-input:invalid, .form-select:invalid');
  
  console.warn('⚠️ Formulário com erros de validação');
  console.log(`Total de campos inválidos: ${invalidFields.length}`);
  
  // Scroll para o primeiro campo inválido
  if (invalidFields.length > 0) {
    invalidFields[0].scrollIntoView({ behavior: 'smooth', block: 'center' });
    
    // Usar setTimeout para garantir que o foco aconteça após o scroll
    setTimeout(() => {
      invalidFields[0].focus();
    }, 500);
  }
}

/**
 * Processa o cadastro após validação
 */
function processarCadastro() {
  const formData = new FormData(document.getElementById('cadastroForm'));
  const dados = Object.fromEntries(formData.entries());
  
  // Coletar checkboxes múltiplos (necessidades especiais)
  const necessidades = Array.from(
    document.querySelectorAll('input[name="necessidades"]:checked')
  ).map(cb => cb.value);
  
  dados.necessidades = necessidades;
  
  console.log('═══════════════════════════════════════');
  console.log('🚀 INICIANDO PROCESSAMENTO DO CADASTRO');
  console.log('═══════════════════════════════════════');
  console.log('📝 Dados coletados:', dados);
  console.log('⏳ Aguardando 2 segundos (setTimeout)...');
  console.log('═══════════════════════════════════════');
  
  // Mostrar loading no botão
  mostrarLoading();
  
  // Mostrar toast de processamento
  mostrarToastProcessamento();
  
  // Simular envio com delay de 2 segundos (demonstração de setTimeout)
  let tempoDecorrido = 0;
  const intervalo = setInterval(() => {
    tempoDecorrido++;
    const toastTimer = document.getElementById('toastTimer');
    if (toastTimer) {
      toastTimer.textContent = `${tempoDecorrido}s`;
    }
    console.log(`⏱️ Processando... ${tempoDecorrido} segundo(s) decorrido(s)`);
  }, 1000);
  
  setTimeout(() => {
    clearInterval(intervalo);
    console.log('═══════════════════════════════════════');
    console.log('✅ TIMEOUT DE 2 SEGUNDOS CONCLUÍDO!');
    console.log('═══════════════════════════════════════');
    ocultarToastProcessamento();
    finalizarCadastro(dados);
  }, 2000);
}

/**
 * Mostra toast de processamento
 */
function mostrarToastProcessamento() {
  const toastEl = document.getElementById('loadingToast');
  const toast = new bootstrap.Toast(toastEl, {
    autohide: false
  });
  toast.show();
}

/**
 * Oculta toast de processamento
 */
function ocultarToastProcessamento() {
  const toastEl = document.getElementById('loadingToast');
  const toast = bootstrap.Toast.getInstance(toastEl);
  if (toast) {
    toast.hide();
  }
}

/**
 * Mostra indicador de loading
 */
function mostrarLoading() {
  const submitBtn = document.querySelector('button[type="submit"]');
  const originalHTML = submitBtn.innerHTML;
  
  submitBtn.disabled = true;
  submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Processando...';
  
  console.log('⏳ Processando cadastro... (aguarde 2 segundos)');
  
  // Retornar HTML original para restauração posterior
  return originalHTML;
}

/**
 * Finaliza o cadastro e mostra mensagem de sucesso
 */
function finalizarCadastro(dados) {
  const submitBtn = document.querySelector('button[type="submit"]');
  const form = document.getElementById('cadastroForm');
  const sucessoMsg = document.getElementById('sucessoMsg');
  const countdownSpan = document.getElementById('countdown');
  
  console.log('🎯 Iniciando finalização do cadastro...');
  
  // Restaurar botão
  submitBtn.disabled = false;
  submitBtn.innerHTML = '<i class="bi bi-check-circle"></i> Cadastrar';
  
  // Resetar formulário
  form.reset();
  form.classList.remove('was-validated');
  
  // Mostrar mensagem de sucesso
  if (sucessoMsg) {
    sucessoMsg.style.display = 'block';
    sucessoMsg.style.visibility = 'visible';
    sucessoMsg.style.opacity = '1';
    
    console.log('✅ Mensagem de sucesso exibida!');
    console.log('📍 Elemento sucessoMsg:', sucessoMsg);
    console.log('📍 Display:', sucessoMsg.style.display);
  } else {
    console.error('❌ Elemento sucessoMsg não encontrado!');
  }
  
  // Scroll suave para a mensagem
  setTimeout(() => {
    if (sucessoMsg) {
      sucessoMsg.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }, 200);
  
  console.log('✅ Cadastro finalizado com sucesso!');
  console.log(`👤 Cliente: ${dados.nomeCliente}`);
  console.log(`🐾 Pet: ${dados.nomePet} (${dados.raca})`);
  console.log('⏰ Iniciando countdown de 8 segundos...');
  
  // Countdown de 8 segundos (demonstração de setInterval)
  let segundosRestantes = 8;
  const countdownInterval = setInterval(() => {
    segundosRestantes--;
    if (countdownSpan) {
      countdownSpan.textContent = segundosRestantes;
    }
    console.log(`⏰ Tempo restante: ${segundosRestantes}s`);
    
    if (segundosRestantes <= 0) {
      clearInterval(countdownInterval);
    }
  }, 1000);
  
  // Ocultar mensagem após 8 segundos (demonstração de setTimeout)
  setTimeout(() => {
    clearInterval(countdownInterval);
    console.log('⏰ 8 segundos transcorridos - ocultando mensagem');
    if (sucessoMsg) {
      sucessoMsg.style.display = 'none';
    }
    if (countdownSpan) {
      countdownSpan.textContent = '8'; // Reset para próxima vez
    }
  }, 8000);
}

/**
 * Validação customizada de CPF
 */
function validarCPF(cpf) {
  cpf = cpf.replace(/\D/g, '');
  
  if (cpf.length !== 11) return false;
  if (/^(\d)\1{10}$/.test(cpf)) return false;
  
  let soma = 0;
  let resto;
  
  for (let i = 1; i <= 9; i++) {
    soma += parseInt(cpf.substring(i - 1, i)) * (11 - i);
  }
  
  resto = (soma * 10) % 11;
  if (resto === 10 || resto === 11) resto = 0;
  if (resto !== parseInt(cpf.substring(9, 10))) return false;
  
  soma = 0;
  for (let i = 1; i <= 10; i++) {
    soma += parseInt(cpf.substring(i - 1, i)) * (12 - i);
  }
  
  resto = (soma * 10) % 11;
  if (resto === 10 || resto === 11) resto = 0;
  if (resto !== parseInt(cpf.substring(10, 11))) return false;
  
  return true;
}

/**
 * Auto-completar idade do pet com base na data
 */
document.addEventListener('DOMContentLoaded', function() {
  const tipoPetSelect = document.getElementById('tipoPet');
  
  if (tipoPetSelect) {
    tipoPetSelect.addEventListener('change', function() {
      console.log(`🐾 Tipo de pet selecionado: ${this.value}`);
    });
  }
});
