package com.quadrosolicitacoes.comentario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ComentarioCreateRequest(
    @NotBlank(message = "O comentário é obrigatório")
    @Size(max = 5000, message = "O comentário deve ter no máximo 5000 caracteres")
    String conteudo,
    @NotNull(message = "O autor é obrigatório")
    @Positive(message = "Selecione um autor válido")
    Long autorId,
    String nomeAutor) { }
