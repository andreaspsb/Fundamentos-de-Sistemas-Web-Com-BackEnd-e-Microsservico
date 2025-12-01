package com.petshop.functions.shared.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class PedidoRequestDTO {

    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;

    private String formaPagamento;

    private String observacoes;

    private String enderecoEntrega;

    @NotEmpty(message = "O pedido deve conter pelo menos um item")
    private List<ItemPedidoRequestDTO> itens;

    public PedidoRequestDTO() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
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

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(String enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    public List<ItemPedidoRequestDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoRequestDTO> itens) {
        this.itens = itens;
    }
}
