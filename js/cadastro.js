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
  
  console.log('📝 Dados do cadastro:', dados);
  
  // Simular envio com delay (função temporal)
  mostrarLoading();
  
  setTimeout(() => {
    finalizarCadastro(dados);
  }, 2000);
}

/**
 * Mostra indicador de loading
 */
function mostrarLoading() {
  const submitBtn = document.querySelector('button[type="submit"]');
  submitBtn.disabled = true;
  submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Processando...';
  
  console.log('⏳ Processando cadastro...');
}

/**
 * Finaliza o cadastro e mostra mensagem de sucesso
 */
function finalizarCadastro(dados) {
  const submitBtn = document.querySelector('button[type="submit"]');
  const form = document.getElementById('cadastroForm');
  const sucessoMsg = document.getElementById('sucessoMsg');
  
  // Restaurar botão
  submitBtn.disabled = false;
  submitBtn.innerHTML = '<i class="bi bi-check-circle"></i> Cadastrar';
  
  // Mostrar mensagem de sucesso
  sucessoMsg.style.display = 'block';
  sucessoMsg.scrollIntoView({ behavior: 'smooth', block: 'center' });
  
  // Resetar formulário
  form.reset();
  form.classList.remove('was-validated');
  
  console.log('✅ Cadastro finalizado com sucesso!');
  console.log(`Cliente: ${dados.nomeCliente}`);
  console.log(`Pet: ${dados.nomePet} (${dados.raca})`);
  
  // Ocultar mensagem após 8 segundos
  setTimeout(() => {
    sucessoMsg.style.display = 'none';
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
