# Fundamentos-de-Sistemas-Web

Site completo de um Pet Shop desenvolvido com HTML5, CSS3, Bootstrap 5 e JavaScript, focado em fundamentos de sistemas web.

## 📋 Descrição do Projeto

Este projeto consiste em um site institucional e de e-commerce para um pet shop, apresentando produtos e serviços para animais de estimação. O site foi desenvolvido utilizando HTML5 semântico, Bootstrap 5 para layout responsivo, CSS3 customizado para estilização avançada e JavaScript para interatividade e funções temporais, priorizando a estrutura, acessibilidade e experiência do usuário.

## 🎯 Funcionalidades

### Página Principal (`index.html`)
- Header com gradiente animado
- Navbar responsiva com menu collapse para mobile
- **Carrossel automático** de promoções com 3 slides (rotação a cada 4 segundos usando `setInterval()`)
- 4 cards de categorias com efeitos hover
- Links para todas as seções do site
- Footer completo com informações de contato, horário e links rápidos

### Categorias de Produtos

O site apresenta **3 categorias de produtos**, cada uma com sua própria página:

#### 1. Rações e Alimentação (`/categorias/racoes-alimentacao/`)
**Produtos em destaque:**
- **Ração Premium para Cães Adultos** - R$ 150,00
  - Ração seca de alta qualidade para cães adultos de todos os portes
  - Fórmula balanceada com vitaminas e minerais essenciais
  - Embalagem de 10kg

- **Ração Hipoalergênica para Gatos** - R$ 95,00
  - Ração especial para gatos com sensibilidade alimentar
  - Ingredientes selecionados que não causam alergias
  - Embalagem de 3kg

#### 2. Acessórios e Brinquedos (`/categorias/acessorios-brinquedos/`)
**Produtos em destaque:**
- **Kit Coleira e Guia Resistente** - R$ 45,00
  - Conjunto de coleira ajustável e guia de 1,5m em nylon resistente
  - Ideal para passeios seguros
  - Disponível em várias cores, Tamanho M

- **Cama Ortopédica para Cães** - R$ 180,00
  - Cama confortável com espuma ortopédica de alta densidade
  - Perfeita para cães idosos ou com problemas articulares
  - Capa removível e lavável, Tamanho G

#### 3. Higiene e Cuidados (`/categorias/higiene-cuidados/`)
**Produtos em destaque:**
- **Kit Xampu e Condicionador para Peles Sensíveis** - R$ 65,00
  - Conjunto completo para banho de pets com pele sensível
  - Fórmula hipoalergênica e pH balanceado
  - Fragrância suave, Frascos de 500ml cada

- **Antipulgas e Carrapatos** - R$ 85,00
  - Proteção eficaz contra pulgas e carrapatos por até 3 meses
  - Aplicação tópica fácil e segura
  - Para cães de 10 a 25kg, Embalagem com 3 pipetas

### Serviços (`/servicos/`)

O pet shop oferece serviços de banho e tosa com sistema de agendamento online:

- **Banho** - R$ 50,00
  - Banho completo com shampoo adequado à pelagem do seu pet
  - Secagem e escovação profissional

- **Tosa** - R$ 40,00
  - Tosa higiênica ou completa conforme solicitado
  - Corte especializado para cada raça

- **Combo Banho + Tosa** - R$ 80,00
  - Pacote completo com desconto
  - Banho + tosa + escovação

**Opções de entrega:**
- **Tele-busca e entrega** - R$ 20,00 (busca e entrega do pet em domicílio)
- **No local** - Grátis (você leva e busca o pet)

### Agendamento Online (`/servicos/agendamento.html`)
- Formulário completo de agendamento
- Seleção interativa de serviços (cards clicáveis)
- Escolha do método de entrega
- Validação de data (não permite domingos)
- Validação de horário (8h às 18h)
- Campos para dados do cliente e pet
- Máscara de telefone automática
- Cálculo automático do valor total
- Confirmação com `setTimeout()` (1.5s)
- **Pré-seleção via URL**: Links da página de serviços passam parâmetros (ex: `?servico=banho`)

### Cadastro (`/cadastro.html`)
- Formulário extenso com mais de 20 campos
- **Dados do Cliente**: nome, CPF, telefone, email, data de nascimento, gênero, endereço completo
- **Dados do Pet**: nome, tipo, raça, idade, peso, gênero, castrado, necessidades especiais
- Máscaras automáticas (CPF: 000.000.000-00, Telefone: (00) 00000-0000)
- Validação HTML5 + Bootstrap + JavaScript
- **Demonstração de funções temporais**:
  - `setTimeout(2000)`: Simulação de loading ao enviar
  - `setInterval(1000)`: Contador regressivo de 8 segundos após sucesso
- Checkboxes para termos e newsletter
- Toast notification animado

## 📁 Estrutura do Projeto

```
.
├── index.html                           # Página principal com carrossel
├── cadastro.html                        # Formulário de cadastro completo
├── README.md                            # Documentação do projeto
├── ACESSIBILIDADE.md                    # Documentação de acessibilidade WCAG 2.1
├── css/
│   └── style.css                        # Estilos customizados
├── js/
│   ├── script.js                        # JavaScript do carrossel
│   ├── cadastro.js                      # Validação e funções temporais do cadastro
│   └── agendamento.js                   # Interatividade do agendamento
├── categorias/
│   ├── racoes-alimentacao/
│   │   ├── index.html                   # Página da categoria
│   │   └── imagens/                     # Pasta para imagens
│   │       └── README.md
│   ├── acessorios-brinquedos/
│   │   ├── index.html
│   │   └── imagens/
│   │       └── README.md
│   └── higiene-cuidados/
│       ├── index.html
│       └── imagens/
│           └── README.md
└── servicos/
    ├── index.html                       # Página de serviços
    └── agendamento.html                 # Formulário de agendamento
```

## 🚀 Como Executar

### Método 1: Servidor HTTP Python (Recomendado)

```bash
# Na raiz do projeto
python3 -m http.server 8000
```

Acesse no navegador: `http://localhost:8000/`

### Método 2: Abrir diretamente no navegador

Abra o arquivo `index.html` diretamente no navegador (duplo clique ou arrastar para o navegador).

**Nota:** Algumas funcionalidades podem não funcionar corretamente sem um servidor HTTP.

## 🛠️ Tecnologias Utilizadas

- **HTML5** - Estrutura semântica das páginas
- **CSS3** - Estilos customizados, animações e transições
- **Bootstrap 5.3.3** - Framework CSS responsivo (via CDN)
- **JavaScript (Vanilla)** - Interatividade e funções temporais
- **Unsplash** - Imagens externas de alta qualidade
- **Markdown** - Documentação

## 📝 Características Técnicas

### HTML5 Semântico
- ✅ Tags apropriadas (`<header>`, `<nav>`, `<main>`, `<section>`, `<article>`, `<footer>`)
- ✅ Navegação relativa consistente entre páginas
- ✅ Estrutura de pastas organizada por categoria
- ✅ Uso de `index.html` em diretórios para URLs limpas
- ✅ Meta tags viewport para responsividade
- ✅ Formulários com tipos de input HTML5 (email, tel, date, time, number, url, etc.)

### Bootstrap 5
- ✅ Grid system responsivo (container, row, col)
- ✅ Navbar com collapse para mobile
- ✅ Cards com imagens
- ✅ Carousel com controles e indicadores
- ✅ Formulários com validação visual
- ✅ Alerts e badges
- ✅ Utilitários de espaçamento e tipografia

### CSS Customizado
- ✅ Variáveis CSS (`:root`) para cores
- ✅ Animações (@keyframes: fadeInUp, gradientShift)
- ✅ Hover effects em cards e botões
- ✅ Transições suaves (transition: all 0.3s ease)
- ✅ Scrollbar customizada
- ✅ Estados de seleção para cards interativos
- ✅ Gradientes animados no header
- ✅ Media queries para responsividade

### JavaScript
- ✅ **Funções temporais**: `setTimeout()` e `setInterval()`
- ✅ Manipulação do DOM
- ✅ Event listeners (click, input, submit)
- ✅ Validação customizada de formulários
- ✅ Máscaras de entrada (CPF, telefone)
- ✅ URLSearchParams para pré-seleção
- ✅ Scroll automático
- ✅ Cálculos dinâmicos de preço

### Acessibilidade (WCAG 2.1 Level AA)
- ✅ `role="navigation"` em navbars
- ✅ `aria-label` em elementos interativos
- ✅ `aria-hidden="true"` em emojis decorativos
- ✅ `aria-current="page"` em links ativos
- ✅ `role="contentinfo"` em footers
- ✅ Atributo `alt` descritivo em todas as imagens
- ✅ Labels associados a inputs
- ✅ Navegação por teclado funcional
- ✅ Contraste adequado de cores

## 📷 Imagens

O projeto utiliza **imagens externas da Unsplash** via CDN, garantindo alta qualidade e performance:

### Carrossel (Página Principal)
- Promoção de rações
- Banho e tosa
- Acessórios e brinquedos

### Produtos (12 imagens no total)
- **Rações e Alimentação**: 2 produtos
- **Acessórios e Brinquedos**: 2 produtos  
- **Higiene e Cuidados**: 2 produtos
- **Serviços**: 3 cards de serviços

**Todas as imagens possuem:**
- ✅ Atributo `alt` descritivo
- ✅ Dimensões otimizadas via parâmetros URL
- ✅ `object-fit: cover` para proporção consistente
- ✅ Altura fixa para uniformidade

## 🔗 Navegação

Todas as páginas possuem um menu de navegação responsivo e consistente com links para:
- **Home** - Página inicial com carrossel
- **Rações e Alimentação** - Categoria de produtos
- **Acessórios e Brinquedos** - Categoria de produtos
- **Higiene e Cuidados** - Categoria de produtos
- **Serviços** - Serviços de banho e tosa
- **Cadastro** - Formulário de cadastro completo

### Footer (Rodapé)
Todas as páginas possuem footer completo com:
- **Informações do estabelecimento**: Endereço, horário de funcionamento
- **Contato**: Telefone clicável, WhatsApp funcional, E-mail
- **Links rápidos**: Navegação rápida para principais páginas
- **Contexto acadêmico**: PUCRS Online - Fundamentos de Sistemas Web
- **Autoria**: Andreas Paulus Scherdien Berwaldt

## 🎓 Requisitos Atendidos

### Funções Temporais JavaScript
- ✅ `setInterval()` - Carrossel automático (4 segundos)
- ✅ `setInterval()` - Contador regressivo no cadastro (8 segundos)
- ✅ `setTimeout()` - Simulação de loading no cadastro (2 segundos)
- ✅ `setTimeout()` - Delay no processamento do agendamento (1.5 segundos)

### Formulários Completos
- ✅ **Cadastro**: 20+ campos com validação completa
- ✅ **Agendamento**: Seleção interativa, validação de data/horário
- ✅ Máscaras de entrada automáticas
- ✅ Feedback visual de erros e sucesso
- ✅ Validação HTML5 + Bootstrap + JavaScript

### Rodapé (Footer)
- ✅ Informações de contato do estabelecimento
- ✅ Endereço completo e horário de funcionamento
- ✅ Links de navegação rápida
- ✅ Contexto acadêmico (PUCRS Online)
- ✅ Autoria do desenvolvedor
- ✅ Links clicáveis (tel:, mailto:, WhatsApp)

### Acessibilidade
- ✅ ARIA labels e roles
- ✅ Atributo `alt` em todas as imagens
- ✅ Navegação por teclado
- ✅ Contraste adequado
- ✅ HTML semântico
- ✅ WCAG 2.1 Level AA compliant

## 📊 Estatísticas do Projeto

- **Páginas HTML**: 7
- **Arquivos CSS**: 1 (+ Bootstrap CDN)
- **Arquivos JavaScript**: 3
- **Imagens**: 12 (via Unsplash)
- **Linhas de Código JS**: ~800
- **Linhas de CSS**: ~300
- **Funções Temporais**: 4 implementações diferentes

## 📄 Licença

Projeto educacional - Fundamentos de Sistemas Web - PUCRS Online

---

**Desenvolvido por:** Andreas Paulus Scherdien Berwaldt  
**Instituição:** PUCRS Online  
**Disciplina:** Fundamentos de Sistemas Web  
**Data:** Novembro de 2025
