package com.quadrosolicitacoes.comentario.dto;

import java.time.LocalDateTime;

public record ComentarioResponse(Long id, String conteudo, LocalDateTime dataCriacao,
        Long autorId, String nomeAutor) { }
