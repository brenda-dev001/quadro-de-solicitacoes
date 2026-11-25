package com.quadrosolicitacoes.solicitacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long>,
        JpaSpecificationExecutor<Solicitacao> { }
