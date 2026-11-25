package com.quadrosolicitacoes.categoria.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CategoriaSearchRequest(
    @Positive(message = "Informe um código maior que zero")
    Long id,
    @Size(max = 255, message = "O filtro excede o tamanho permitido")
    String nome,
    @Size(max = 255, message = "O filtro excede o tamanho permitido")
    String descricao) {

    public boolean isEmpty() {
        return id == null && (nome == null || nome.isBlank())
                && (descricao == null || descricao.isBlank());
    }
}
