package com.petshop.functions.shared.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AgendamentoRequestDTO {

    @NotNull(message = "Data do agendamento é obrigatória")
    private LocalDate dataAgendamento;

    @NotNull(message = "Horário é obrigatório")
    private LocalTime horario;

    @NotBlank(message = "Método de atendimento é obrigatório")
    private String metodoAtendimento;

    private String portePet;

    private String observacoes;

    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;

    @NotNull(message = "Pet é obrigatório")
    private Long petId;

    @NotEmpty(message = "Selecione pelo menos um serviço")
    private List<Long> servicoIds;

    public AgendamentoRequestDTO() {
    }

    public LocalDate getDataAgendamento() {
        return dataAgendamento;
    }

    public void setDataAgendamento(LocalDate dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
    }

    public String getMetodoAtendimento() {
        return metodoAtendimento;
    }

    public void setMetodoAtendimento(String metodoAtendimento) {
        this.metodoAtendimento = metodoAtendimento;
    }

    public String getPortePet() {
        return portePet;
    }

    public void setPortePet(String portePet) {
        this.portePet = portePet;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public List<Long> getServicoIds() {
        return servicoIds;
    }

    public void setServicoIds(List<Long> servicoIds) {
        this.servicoIds = servicoIds;
    }
}
