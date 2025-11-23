# Guia de Testes e Cobertura de Código

## Resumo da Cobertura de Testes

### Spring Boot (Backend Java)
- **Total de Testes**: 69
- **Status**: ✅ Todos passando
- **Framework**: JUnit 5 + Mockito + MockMvc
- **Cobertura de Código**: JaCoCo configurado

#### Distribuição dos Testes
- `JwtUtilTest`: 9 testes (geração, validação, expiração de tokens JWT)
- `AuthServiceTest`: 7 testes (autenticação, validação de credenciais)
- `AuthControllerTest`: 5 testes (endpoints de login, cadastro, validação)
- `ProdutoServiceTest`: 18 testes (CRUD, gerenciamento de estoque, validações)
- `ProdutoControllerTest`: 14 testes (endpoints REST de produtos)
- `ClienteServiceTest`: 16 testes (CRUD, validação de CPF/email, formatação)

### ASP.NET (Backend C#)
- **Total de Testes**: 48
- **Status**: ✅ Todos passando
- **Framework**: xUnit + Moq + EntityFrameworkCore.InMemory
- **Cobertura de Código**: Coverlet configurado (29.41% linha)

#### Distribuição dos Testes
- `JwtServiceTests`: 9 testes (geração, validação, claims JWT)
- `AuthControllerTests`: 9 testes (registro, login, validação de duplicatas)
- `ProdutosControllerTests`: 18 testes (CRUD, busca, estoque, ativação)
- `ClientesControllerTests`: 12 testes (CRUD, busca por CPF, validação)

---

## Executando os Testes

### Spring Boot

```bash
cd backend-springboot

# Executar todos os testes
mvn test

# Executar testes com relatório de cobertura
mvn clean test

# Executar testes de uma classe específica
mvn test -Dtest=ProdutoServiceTest
mvn test -Dtest=ClienteServiceTest
```

### ASP.NET

```bash
cd backend-aspnet/PetshopApi.Tests

# Executar todos os testes
dotnet test

# Executar com cobertura de código
dotnet test /p:CollectCoverage=true /p:CoverletOutputFormat=opencover

# Executar testes de uma classe específica
dotnet test --filter "FullyQualifiedName~ProdutosControllerTests"
dotnet test --filter "FullyQualifiedName~ClientesControllerTests"
```

---

## Relatórios de Cobertura

### Spring Boot (JaCoCo)

Após executar `mvn clean test`, o relatório é gerado em:

```
backend-springboot/target/site/jacoco/index.html
```

**Abrir o relatório:**
```bash
# Linux/Mac
xdg-open backend-springboot/target/site/jacoco/index.html

# Windows
start backend-springboot/target/site/jacoco/index.html
```

**Arquivos gerados:**
- `index.html` - Relatório principal navegável
- `jacoco.xml` - Formato XML (para CI/CD)
- `jacoco.csv` - Formato CSV (para análise)

**Configuração no pom.xml:**
- Plugin JaCoCo configurado com mínimo de 50% de cobertura por pacote
- Relatório gerado automaticamente na fase `test`

### ASP.NET (Coverlet)

Após executar o comando de cobertura, o relatório é gerado em:

```
backend-aspnet/PetshopApi.Tests/coverage.opencover.xml
```

**Visualizar cobertura no terminal:**
Ao executar com Coverlet, a tabela de cobertura é exibida automaticamente:

```
+------------+--------+--------+--------+
| Module     | Line   | Branch | Method |
+------------+--------+--------+--------+
| PetshopApi | 29.41% | 20.13% | 38.79% |
+------------+--------+--------+--------+
```

**Para gerar relatório HTML (requer .NET Runtime completo):**
```bash
# Instalar ReportGenerator
dotnet tool install -g dotnet-reportgenerator-globaltool

# Gerar relatório
reportgenerator \
  -reports:backend-aspnet/PetshopApi.Tests/coverage.opencover.xml \
  -targetdir:backend-aspnet/PetshopApi.Tests/coverage-report \
  -reporttypes:Html
```

---

## Padrões de Teste Utilizados

### Spring Boot

#### Testes de Service (Mockito)
```java
@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {
    @Mock
    private ProdutoRepository produtoRepository;
    
    @InjectMocks
    private ProdutoService produtoService;
    
    @Test
    void testSalvarComSucesso() {
        // Arrange
        Produto produto = new Produto();
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        
        // Act
        Produto resultado = produtoService.salvar(produto);
        
        // Assert
        assertNotNull(resultado);
        verify(produtoRepository).save(produto);
    }
}
```

#### Testes de Controller (MockMvc)
```java
@WebMvcTest(controllers = ProdutoController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    ))
class ProdutoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ProdutoService produtoService;
    
    @Test
    void testBuscarPorId() throws Exception {
        when(produtoService.buscarPorId(1L)).thenReturn(produto);
        
        mockMvc.perform(get("/api/produtos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }
}
```

### ASP.NET

#### Testes de Controller (In-Memory Database)
```csharp
public class ProdutosControllerTests : IDisposable
{
    private readonly PetshopContext _context;
    private readonly ProdutosController _controller;
    
    public ProdutosControllerTests()
    {
        var options = new DbContextOptionsBuilder<PetshopContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
            .Options;
        
        _context = new PetshopContext(options);
        // Seed data...
        _controller = new ProdutosController(_context);
    }
    
    [Fact]
    public async Task GetById_WithValidId_ReturnsOkWithProduto()
    {
        // Act
        var result = await _controller.GetById(1);
        
        // Assert
        var okResult = Assert.IsType<OkObjectResult>(result.Result);
        Assert.NotNull(okResult.Value);
    }
    
    public void Dispose()
    {
        _context.Database.EnsureDeleted();
        _context.Dispose();
    }
}
```

---

## Cenários de Teste Cobertos

### Autenticação e JWT
- ✅ Geração de tokens JWT válidos
- ✅ Validação de tokens expirados
- ✅ Extração de claims (username, roles)
- ✅ Login com credenciais válidas/inválidas
- ✅ Cadastro de novos usuários
- ✅ Validação de duplicatas (username, email)

### Produtos
- ✅ CRUD completo (Create, Read, Update, Delete)
- ✅ Listagem com filtros (categoria, disponibilidade, estoque baixo)
- ✅ Busca por nome/descrição
- ✅ Gerenciamento de estoque (adicionar, atualizar, reduzir)
- ✅ Ativação/desativação de produtos
- ✅ Validações (categoria inexistente, estoque insuficiente)
- ✅ Tratamento de erros (produto não encontrado)

### Clientes
- ✅ CRUD completo
- ✅ Busca por CPF
- ✅ Validação de duplicatas (CPF, email)
- ✅ Formatação de CPF/telefone (remoção de caracteres não numéricos)
- ✅ Validação de formatos (CPF 11 dígitos, telefone 10-11 dígitos)
- ✅ Tratamento de erros (cliente não encontrado)

---

## Melhorias Futuras

### Aumentar Cobertura
- [ ] Adicionar testes para `PedidoController`/`PedidosController`
- [ ] Adicionar testes para `AgendamentoController`/`AgendamentosController`
- [ ] Aumentar cobertura do ASP.NET para 50%+
- [ ] Testes de integração end-to-end

### Integração Contínua
- [ ] Configurar GitHub Actions para executar testes automaticamente
- [ ] Gerar relatórios de cobertura no CI/CD
- [ ] Adicionar badges de status dos testes no README
- [ ] Bloquear merge de PRs com cobertura abaixo do mínimo

### Testes Adicionais
- [ ] Testes de performance
- [ ] Testes de carga (stress testing)
- [ ] Testes de segurança (SQL injection, XSS)
- [ ] Testes de validação de entrada

---

## Comandos Úteis

### Maven (Spring Boot)
```bash
# Limpar e recompilar
mvn clean compile

# Executar apenas testes unitários
mvn test

# Pular testes durante build
mvn package -DskipTests

# Ver relatório de cobertura
mvn jacoco:report
```

### .NET (ASP.NET)
```bash
# Restaurar dependências
dotnet restore

# Compilar projeto
dotnet build

# Executar testes com verbosidade
dotnet test --verbosity detailed

# Executar testes em modo watch (reroda ao detectar mudanças)
dotnet watch test
```

---

## Estrutura dos Arquivos de Teste

### Spring Boot
```
backend-springboot/src/test/java/com/petshop/
├── controller/
│   ├── AuthControllerTest.java
│   └── ProdutoControllerTest.java
├── security/
│   └── JwtUtilTest.java
└── service/
    ├── AuthServiceTest.java
    ├── ClienteServiceTest.java
    └── ProdutoServiceTest.java
```

### ASP.NET
```
backend-aspnet/PetshopApi.Tests/
├── Controllers/
│   ├── AuthControllerTests.cs
│   ├── ClientesControllerTests.cs
│   └── ProdutosControllerTests.cs
└── Services/
    └── JwtServiceTests.cs
```

---

## Notas Importantes

1. **Mock vs In-Memory Database**:
   - Spring Boot: Usa Mockito para services, MockMvc para controllers
   - ASP.NET: Usa EntityFrameworkCore.InMemory para testes reais de banco

2. **Isolamento de Testes**:
   - Cada teste é independente
   - Spring Boot: @DirtiesContext quando necessário
   - ASP.NET: Database recriado com Guid único para cada teste

3. **Exclusão de Filtros de Segurança**:
   - Necessário excluir `JwtAuthenticationFilter` nos testes de controller
   - Permite testar lógica de negócio sem autenticação

4. **Cobertura de Código**:
   - JaCoCo (Spring Boot): Configurado para mínimo 50% linha
   - Coverlet (ASP.NET): Atualmente em 29.41%, precisa melhorar

## Total Geral
- **117 testes automatizados** (69 Spring Boot + 48 ASP.NET)
- **100% de sucesso** em ambos os backends
- Cobertura configurada e funcional
