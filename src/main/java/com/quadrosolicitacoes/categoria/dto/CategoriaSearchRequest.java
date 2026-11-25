package com.quadrosolicitacoes.categoria.dto;

public record CategoriaSearchRequest(Long id, String nome, String descricao) {
    public boolean isEmpty() {
        return id == null && (nome == null || nome.isBlank())
                && (descricao == null || descricao.isBlank());
    }
}
