using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;
using Petshop.Shared.Enums;
using Petshop.Shared.Models;

namespace Petshop.Shared.Data;

public class PetshopDbContext : DbContext
{
    public PetshopDbContext(DbContextOptions<PetshopDbContext> options) : base(options)
    {
    }

    public DbSet<Usuario> Usuarios { get; set; }
    public DbSet<Cliente> Clientes { get; set; }
    public DbSet<Pet> Pets { get; set; }
    public DbSet<Categoria> Categorias { get; set; }
    public DbSet<Produto> Produtos { get; set; }
    public DbSet<Servico> Servicos { get; set; }
    public DbSet<Agendamento> Agendamentos { get; set; }
    public DbSet<Pedido> Pedidos { get; set; }
    public DbSet<ItemPedido> ItensPedido { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // Configurar enums para serem armazenados como STRING (compatível com Spring Boot)
        modelBuilder.Entity<Agendamento>()
            .Property(a => a.Status)
            .HasConversion<string>()
            .HasMaxLength(20);

        modelBuilder.Entity<Pedido>()
            .Property(p => p.Status)
            .HasConversion<string>()
            .HasMaxLength(20);

        // Usuario - Cliente (1:1 opcional)
        modelBuilder.Entity<Usuario>()
            .HasOne(u => u.Cliente)
            .WithMany()
            .HasForeignKey(u => u.ClienteId)
            .OnDelete(DeleteBehavior.SetNull);

        // Cliente - Pets (1:N)
        modelBuilder.Entity<Pet>()
            .HasOne(p => p.Cliente)
            .WithMany(c => c.Pets)
            .HasForeignKey(p => p.ClienteId)
            .OnDelete(DeleteBehavior.Cascade);

        // Categoria - Produtos (1:N)
        modelBuilder.Entity<Produto>()
            .HasOne(p => p.Categoria)
            .WithMany(c => c.Produtos)
            .HasForeignKey(p => p.CategoriaId)
            .OnDelete(DeleteBehavior.Restrict);

        // Agendamento - Cliente (N:1)
        modelBuilder.Entity<Agendamento>()
            .HasOne(a => a.Cliente)
            .WithMany(c => c.Agendamentos)
            .HasForeignKey(a => a.ClienteId)
            .OnDelete(DeleteBehavior.Cascade);

        // Agendamento - Pet (N:1)
        modelBuilder.Entity<Agendamento>()
            .HasOne(a => a.Pet)
            .WithMany(p => p.Agendamentos)
            .HasForeignKey(a => a.PetId)
            .OnDelete(DeleteBehavior.Restrict);

        // Agendamento - Servicos (M:N)
        modelBuilder.Entity<Agendamento>()
            .HasMany(a => a.Servicos)
            .WithMany(s => s.Agendamentos)
            .UsingEntity(j => j.ToTable("agendamento_servicos"));

        // Pedido - Cliente (N:1)
        modelBuilder.Entity<Pedido>()
            .HasOne(p => p.Cliente)
            .WithMany(c => c.Pedidos)
            .HasForeignKey(p => p.ClienteId)
            .OnDelete(DeleteBehavior.Cascade);

        // Pedido - Itens (1:N)
        modelBuilder.Entity<ItemPedido>()
            .HasOne(i => i.Pedido)
            .WithMany(p => p.Itens)
            .HasForeignKey(i => i.PedidoId)
            .OnDelete(DeleteBehavior.Cascade);

        // ItemPedido - Produto (N:1)
        modelBuilder.Entity<ItemPedido>()
            .HasOne(i => i.Produto)
            .WithMany(p => p.ItensPedido)
            .HasForeignKey(i => i.ProdutoId)
            .OnDelete(DeleteBehavior.Restrict);

        // Índices únicos
        modelBuilder.Entity<Usuario>()
            .HasIndex(u => u.Username)
            .IsUnique();

        modelBuilder.Entity<Usuario>()
            .HasIndex(u => u.Email)
            .IsUnique();

        modelBuilder.Entity<Cliente>()
            .HasIndex(c => c.Cpf)
            .IsUnique();

        modelBuilder.Entity<Cliente>()
            .HasIndex(c => c.Email)
            .IsUnique();
    }
}
