package com.quadrosolicitacoes.comentario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    @EntityGraph(attributePaths = "autor")
    Page<Comentario> findBySolicitacaoId(Long solicitacaoId, Pageable pageable);
}
