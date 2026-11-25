package com.quadrosolicitacoes.comentario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ComentarioCreateRequest(
    @NotBlank(message = "O comentário é obrigatório")
    String conteudo,
    @NotNull(message = "O autor é obrigatório")
    @Positive(message = "Selecione um autor válido")
    Long autorId,
    String nomeAutor) { }
