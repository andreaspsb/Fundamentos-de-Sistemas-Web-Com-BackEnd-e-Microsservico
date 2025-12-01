package com.petshop.functions.shared.dto;

public class TokenValidationResponseDTO {

    private boolean valid;
    private String username;
    private String role;
    private Long clienteId;

    public TokenValidationResponseDTO() {
    }

    public TokenValidationResponseDTO(boolean valid, String username, String role, Long clienteId) {
        this.valid = valid;
        this.username = username;
        this.role = role;
        this.clienteId = clienteId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}
