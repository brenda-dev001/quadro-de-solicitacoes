package com.quadrosolicitacoes.solicitacao.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.quadrosolicitacoes.solicitacao.StatusSolicitacao;

public record SolicitacaoSearchRequest(
    @Positive(message = "Informe um código maior que zero")
    Long id,
    @Size(max = 5000, message = "O filtro excede o tamanho permitido")
    String descricao,
    StatusSolicitacao status,
    @Positive(message = "Informe um código maior que zero")
    Long categoriaId,
    String nomeCategoria,
    @Positive(message = "Informe um código maior que zero")
    Long solicitanteId,
    String nomeSolicitante,
    @Positive(message = "Informe um código maior que zero")
    Long tecnicoId,
    String nomeTecnico) {

    public boolean isEmpty() {
        return id == null && (descricao == null || descricao.isBlank()) && status == null
                && categoriaId == null && solicitanteId == null && tecnicoId == null;
    }
}
