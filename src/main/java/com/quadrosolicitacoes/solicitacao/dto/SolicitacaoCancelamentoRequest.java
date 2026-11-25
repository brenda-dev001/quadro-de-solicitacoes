package com.quadrosolicitacoes.solicitacao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SolicitacaoCancelamentoRequest(
    @NotNull(message = "O funcionário é obrigatório")
    @Positive(message = "Selecione um funcionário válido")
    Long solicitanteId) { }
