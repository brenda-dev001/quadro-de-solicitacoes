package com.quadrosolicitacoes.solicitacao;

public enum StatusSolicitacao {
    ABERTA("Aberta"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusSolicitacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
