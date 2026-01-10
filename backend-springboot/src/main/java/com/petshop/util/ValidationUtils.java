package com.petshop.util;

import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Utilitários de validação para garantir null safety em operações de repositório.
 * Centraliza validações comuns para evitar duplicação de código.
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
     * Retorna a lista fornecida ou uma lista vazia se for null.
     * Garante que o retorno é @NonNull.
     * 
     * @param <T> o tipo dos elementos da lista
     * @param list a lista a ser validada (pode ser null)
     * @return a mesma lista se não for null, ou uma lista vazia imutável
     */
    @SuppressWarnings("null") // Eclipse null checker limitation with generics
    @NonNull
    public static <T> java.util.List<T> requireNonNullList(@Nullable java.util.List<T> list) {
        if (list == null) {
            return java.util.List.of();
        }
        return list;
    }

    /**
     * Valida que a quantidade (Integer) não é null.
     * 
     * @param quantity a quantidade a ser validada
     * @param fieldName nome do campo para a mensagem de erro
     * @return a mesma quantidade se não for null
     * @throws NullPointerException se a quantidade for null
     */
    @NonNull
    public static Integer requireNonNullQuantity(Integer quantity, String fieldName) {
        return Objects.requireNonNull(quantity, fieldName + " não pode ser null");
    }
}
