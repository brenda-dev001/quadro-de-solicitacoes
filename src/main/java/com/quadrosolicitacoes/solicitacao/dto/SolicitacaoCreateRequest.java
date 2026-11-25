package com.quadrosolicitacoes.solicitacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SolicitacaoCreateRequest(
    @NotBlank(message = "A descrição é obrigatória")
    String descricao,
    @NotNull(message = "A categoria é obrigatória")
    @Positive(message = "Selecione uma categoria válida")
    Long categoriaId,
    String nomeCategoria,
    @NotNull(message = "O funcionário é obrigatório")
    @Positive(message = "Selecione um funcionário válido")
    Long solicitanteId,
    String nomeSolicitante) { }
