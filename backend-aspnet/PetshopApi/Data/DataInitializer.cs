using PetshopApi.Models;
using BC = BCrypt.Net.BCrypt;

namespace PetshopApi.Data;

public class DataInitializer
{
    public static void Initialize(PetshopContext context)
    {
        context.Database.EnsureCreated();

        // Se já existem dados, não inicializa
        if (context.Usuarios.Any())
        {
            return;
        }

        // Criar Categorias (idênticas ao Spring Boot)
        var racoes = new Categoria 
        { 
            Nome = "Rações e Alimentação", 
            Descricao = "Rações, petiscos e suplementos para cães e gatos",
            Ativo = true
        };
        context.Categorias.Add(racoes);
        context.SaveChanges();

        var higiene = new Categoria 
        { 
            Nome = "Higiene e Cuidados", 
            Descricao = "Produtos de higiene, beleza e primeiros socorros para pets",
            Ativo = true
        };
        context.Categorias.Add(higiene);
        context.SaveChanges();

        var acessorios = new Categoria 
        { 
            Nome = "Acessórios e Brinquedos", 
            Descricao = "Coleiras, camas, roupas, brinquedos e itens para passeios",
            Ativo = true
        };
        context.Categorias.Add(acessorios);
        context.SaveChanges();

        // Criar Produtos - Rações e Alimentação (idênticos ao Spring Boot)
        var racaoPremium = new Produto
        {
            Nome = "Ração Premium para Cães Adultos",
            Descricao = "Ração seca de alta qualidade para cães adultos de todos os portes. Fórmula balanceada com vitaminas e minerais essenciais. Embalagem de 10kg.",
            Preco = 150.00,
            QuantidadeEstoque = 50,
            UrlImagem = "https://images.unsplash.com/photo-1589924691995-400dc9ecc119?w=600&h=300&fit=crop",
            CategoriaId = racoes.Id,
            Ativo = true
        };
        context.Produtos.Add(racaoPremium);

        var racaoGatos = new Produto
        {
            Nome = "Ração Hipoalergênica para Gatos",
            Descricao = "Ração especial para gatos com sensibilidade alimentar. Ingredientes selecionados que não causam alergias. Embalagem de 3kg.",
            Preco = 95.00,
            QuantidadeEstoque = 40,
            UrlImagem = "https://images.unsplash.com/photo-1606214174585-fe31582dc6ee?w=600&h=300&fit=crop",
            CategoriaId = racoes.Id,
            Ativo = true
        };
        context.Produtos.Add(racaoGatos);

        // Criar Produtos - Higiene e Cuidados
        var kitBanho = new Produto
        {
            Nome = "Kit Xampu e Condicionador para Peles Sensíveis",
            Descricao = "Conjunto completo para banho de pets com pele sensível. Fórmula hipoalergênica e pH balanceado. Fragrância suave. Frascos de 500ml cada.",
            Preco = 65.00,
            QuantidadeEstoque = 60,
            UrlImagem = "https://images.unsplash.com/photo-1556229010-6c3f2c9ca5f8?w=600&h=300&fit=crop",
            CategoriaId = higiene.Id,
            Ativo = true
        };
        context.Produtos.Add(kitBanho);

        var antipulgas = new Produto
        {
            Nome = "Antipulgas e Carrapatos",
            Descricao = "Proteção eficaz contra pulgas e carrapatos por até 3 meses. Aplicação tópica fácil e segura. Para cães de 10 a 25kg. Embalagem com 3 pipetas.",
            Preco = 85.00,
            QuantidadeEstoque = 35,
            UrlImagem = "https://images.unsplash.com/photo-1623387641168-d9803ddd3f35?w=600&h=300&fit=crop",
            CategoriaId = higiene.Id,
            Ativo = true
        };
        context.Produtos.Add(antipulgas);

        // Criar Produtos - Acessórios e Brinquedos
        var kitColeira = new Produto
        {
            Nome = "Kit Coleira e Guia Resistente",
            Descricao = "Conjunto de coleira ajustável e guia de 1,5m em nylon resistente. Ideal para passeios seguros. Disponível em várias cores. Tamanho M.",
            Preco = 45.00,
            QuantidadeEstoque = 80,
            UrlImagem = "https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=600&h=300&fit=crop",
            CategoriaId = acessorios.Id,
            Ativo = true
        };
        context.Produtos.Add(kitColeira);

        var camaOrtopedica = new Produto
        {
            Nome = "Cama Ortopédica para Cães",
            Descricao = "Cama confortável com espuma ortopédica de alta densidade. Perfeita para cães idosos ou com problemas articulares. Capa removível e lavável. Tamanho G.",
            Preco = 180.00,
            QuantidadeEstoque = 25,
            UrlImagem = "https://images.unsplash.com/photo-1615751072497-5f5169febe17?w=600&h=300&fit=crop",
            CategoriaId = acessorios.Id,
            Ativo = true
        };
        context.Produtos.Add(camaOrtopedica);
        context.SaveChanges();

        // Criar Serviços (idênticos ao Spring Boot)
        var banho = new Servico 
        { 
            Nome = "Banho", 
            Descricao = "Banho completo com produtos de qualidade", 
            Preco = 50.00,
            Ativo = true
        };
        context.Servicos.Add(banho);

        var tosa = new Servico 
        { 
            Nome = "Tosa", 
            Descricao = "Tosa higiênica ou completa", 
            Preco = 40.00,
            Ativo = true
        };
        context.Servicos.Add(tosa);

        var completo = new Servico 
        { 
            Nome = "Banho + Tosa", 
            Descricao = "Pacote completo com desconto - banho e tosa", 
            Preco = 80.00,
            Ativo = true
        };
        context.Servicos.Add(completo);
        context.SaveChanges();

        // Criar usuários
        var adminUser = new Usuario
        {
            Username = "admin",
            Email = "admin@petshop.com",
            Senha = BC.HashPassword("admin123"),
            Role = "ADMIN",
            Ativo = true
        };

        var clienteUser = new Usuario
        {
            Username = "maria.silva",
            Email = "maria.silva@email.com",
            Senha = BC.HashPassword("senha123"),
            Role = "CLIENTE",
            Ativo = true
        };

        context.Usuarios.AddRange(adminUser, clienteUser);
        context.SaveChanges();

        // Criar cliente de exemplo
        var cliente = new Cliente
        {
            Nome = "Maria Silva",
            Cpf = "12345678901",
            Telefone = "11987654321",
            Email = "maria.silva@email.com",
            DataNascimento = new DateTime(1985, 5, 15),
            Sexo = "F",
            Endereco = "Rua das Flores",
            Numero = "123",
            Complemento = "Apto 45",
            Bairro = "Jardim das Rosas",
            Cidade = "São Paulo"
        };

        context.Clientes.Add(cliente);
        context.SaveChanges();

        // Associar usuário ao cliente
        clienteUser.ClienteId = cliente.Id;
        context.SaveChanges();

        // Criar pet de exemplo
        var pet = new Pet
        {
            Nome = "Rex",
            Tipo = "cao",
            Raca = "Labrador",
            Idade = 3,
            Peso = 25.5,
            Sexo = "M",
            Castrado = true,
            ClienteId = cliente.Id,
            TemAlergia = false,
            PrecisaMedicacao = false,
            ComportamentoAgressivo = false
        };

        context.Pets.Add(pet);
        context.SaveChanges();

        Console.WriteLine("✅ Dados iniciais carregados com sucesso!");
        Console.WriteLine("   - 3 Categorias criadas");
        Console.WriteLine("   - 6 Produtos criados");
        Console.WriteLine("   - 3 Serviços criados");
        Console.WriteLine("   - 2 Usuários criados (admin/admin123 e maria.silva/senha123)");
    }
}
