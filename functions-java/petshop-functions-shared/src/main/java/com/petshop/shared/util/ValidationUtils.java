package com.petshop.shared.util;

import java.util.Objects;

import org.springframework.lang.NonNull;

/**
 * Utilitários de validação para garantir null safety em operações de repositório.
 * Centraliza validações comuns para evitar duplicação de código.
 * Compartilhado entre todos os microserviços Java Functions.
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // Classe utilitária - não deve ser instanciada
    }

    /**
     * Valida que o ID não é null.
     * 
     * @param id o ID a ser validado
     * @return o mesmo ID se não for null
     * @throws NullPointerException se o ID for null
     */
    @NonNull
    public static Long requireNonNullId(Long id) {
        return Objects.requireNonNull(id, "ID não pode ser null");
    }

    /**
     * Valida que o ID não é null, com mensagem personalizada incluindo o nome da entidade.
     * 
     * @param id o ID a ser validado
     * @param entityName nome da entidade para a mensagem de erro
     * @return o mesmo ID se não for null
     * @throws NullPointerException se o ID for null
     */
    @NonNull
    public static Long requireNonNullId(Long id, String entityName) {
        return Objects.requireNonNull(id, "ID de " + entityName + " não pode ser null");
    }

    /**
     * Valida que uma entidade não é null.
     * 
     * @param <T> o tipo da entidade
     * @param entity a entidade a ser validada
     * @param entityName nome da entidade para a mensagem de erro
     * @return a mesma entidade se não for null
     * @throws NullPointerException se a entidade for null
     */
    @NonNull
    public static <T> T requireNonNullEntity(T entity, String entityName) {
        return Objects.requireNonNull(entity, entityName + " não pode ser null");
    }

    /**
     * Valida entidade para operação de save/update e garante que o resultado também não é null.
     * Use quando precisar garantir null safety tanto no argumento quanto no retorno de save().
     * 
     * @param <T> o tipo da entidade
     * @param entity a entidade a ser salva (validada antes)
     * @param savedEntity o resultado do save() (validado depois)
     * @param entityName nome da entidade para mensagens de erro
     * @return a entidade salva se não for null
     * @throws NullPointerException se entity ou savedEntity for null
     */
    @NonNull
    public static <T> T validateSaveResult(T entity, T savedEntity, String entityName) {
        Objects.requireNonNull(entity, entityName + " a salvar não pode ser null");
        return Objects.requireNonNull(savedEntity, entityName + " salvo não pode ser null");
    }
}
