package com.petshop.functions.shared.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Data do pedido é obrigatória")
    @Column(name = "data_pedido", nullable = false)
    private LocalDateTime dataPedido;

    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor deve ser maior que zero")
    @Column(name = "valor_total", nullable = false)
    private Double valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.PENDENTE;

    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento; // credito, debito, pix, boleto

    @Column(length = 1000)
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    // Enum para status do pedido
    public enum StatusPedido {
        PENDENTE,
        CONFIRMADO,
        PROCESSANDO,
        ENVIADO,
        ENTREGUE,
        CANCELADO
    }

    // Construtores
    public Pedido() {
        this.dataPedido = LocalDateTime.now();
    }

    public Pedido(Cliente cliente) {
        this();
        this.cliente = cliente;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDateTime dataPedido) {
        this.dataPedido = dataPedido;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    // Métodos auxiliares
    public void adicionarItem(ItemPedido item) {
        itens.add(item);
        item.setPedido(this);
        calcularValorTotal();
    }

    public void removerItem(ItemPedido item) {
        itens.remove(item);
        item.setPedido(null);
        calcularValorTotal();
    }

    public void calcularValorTotal() {
        this.valorTotal = itens.stream()
                .mapToDouble(ItemPedido::getSubtotal)
                .sum();
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        if (this.dataPedido == null) {
            this.dataPedido = LocalDateTime.now();
        }
        calcularValorTotal();
    }
}
