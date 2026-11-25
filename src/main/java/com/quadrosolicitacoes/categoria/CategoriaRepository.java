package com.quadrosolicitacoes.categoria;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.quadrosolicitacoes.common.enums.Status;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    public Page<Categoria> findAllByStatus(Status status, Pageable pageable);
    public Optional<Categoria> findByIdAndStatus(Long id, Status status);
    public List<Categoria> findByNomeContainingIgnoreCaseAndStatusOrderByNomeAsc(String nome, Status status);

}
