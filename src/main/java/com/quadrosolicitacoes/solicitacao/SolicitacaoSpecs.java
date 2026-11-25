package com.quadrosolicitacoes.solicitacao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import com.quadrosolicitacoes.categoria.Categoria_;
import com.quadrosolicitacoes.usuario.Usuario_;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoSearchRequest;

public final class SolicitacaoSpecs {
    private SolicitacaoSpecs() { }

    public static Specification<Solicitacao> comFiltros(SolicitacaoSearchRequest filtro) {
        return (root, query, cb) -> {
            boolean isCountQuery = Long.class.equals(query.getResultType())
                    || long.class.equals(query.getResultType());
            if (!isCountQuery) {
                root.fetch(Solicitacao_.categoria, JoinType.LEFT);
                root.fetch(Solicitacao_.solicitante, JoinType.LEFT);
                root.fetch(Solicitacao_.tecnicoResponsavel, JoinType.LEFT);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (filtro.id() != null) predicates.add(cb.equal(root.get(Solicitacao_.id), filtro.id()));
            if (filtro.descricao() != null && !filtro.descricao().isBlank()) {
                String termo = filtro.descricao().toLowerCase(java.util.Locale.ROOT)
                        .replace("!", "!!").replace("%", "!%").replace("_", "!_");
                predicates.add(cb.like(cb.lower(root.get(Solicitacao_.descricao)), "%" + termo + "%", '!'));
            }
            if (filtro.status() != null) predicates.add(cb.equal(root.get(Solicitacao_.status), filtro.status()));
            if (filtro.categoriaId() != null) predicates.add(cb.equal(
                    root.get(Solicitacao_.categoria).get(Categoria_.id), filtro.categoriaId()));
            if (filtro.solicitanteId() != null) predicates.add(cb.equal(
                    root.get(Solicitacao_.solicitante).get(Usuario_.id), filtro.solicitanteId()));
            if (filtro.tecnicoId() != null) predicates.add(cb.equal(
                    root.get(Solicitacao_.tecnicoResponsavel).get(Usuario_.id), filtro.tecnicoId()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
