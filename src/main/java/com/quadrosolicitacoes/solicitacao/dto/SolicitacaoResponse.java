package com.quadrosolicitacoes.solicitacao.dto;

import java.time.LocalDateTime;
import com.quadrosolicitacoes.solicitacao.StatusSolicitacao;

public record SolicitacaoResponse(Long id, String descricao, StatusSolicitacao status,
        LocalDateTime dataCriacao, Long categoriaId, String nomeCategoria,
        Long solicitanteId, String nomeSolicitante, Long tecnicoId, String nomeTecnico) { }
