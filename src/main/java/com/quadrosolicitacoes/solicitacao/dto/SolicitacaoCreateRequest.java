package com.quadrosolicitacoes.solicitacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SolicitacaoCreateRequest(
    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 5000, message = "A descrição deve ter no máximo 5000 caracteres")
    String descricao,
    @NotNull(message = "A categoria é obrigatória")
    @Positive(message = "Selecione uma categoria válida")
    Long categoriaId,
    String nomeCategoria,
    @NotNull(message = "O funcionário é obrigatório")
    @Positive(message = "Selecione um funcionário válido")
    Long solicitanteId,
    String nomeSolicitante) { }
