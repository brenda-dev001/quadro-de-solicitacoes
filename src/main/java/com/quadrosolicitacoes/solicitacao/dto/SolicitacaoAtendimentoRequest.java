package com.quadrosolicitacoes.solicitacao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SolicitacaoAtendimentoRequest(
    @NotNull(message = "O técnico é obrigatório")
    @Positive(message = "Selecione um técnico válido")
    Long tecnicoId,
    String nomeTecnico) { }
