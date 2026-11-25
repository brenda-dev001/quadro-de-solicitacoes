package com.quadrosolicitacoes.solicitacao.dto;

import com.quadrosolicitacoes.solicitacao.StatusSolicitacao;

public record SolicitacaoSearchRequest(Long id, String descricao, StatusSolicitacao status,
        Long categoriaId, String nomeCategoria, Long solicitanteId, String nomeSolicitante,
        Long tecnicoId, String nomeTecnico) { }
