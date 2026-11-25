package com.quadrosolicitacoes.usuario;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByIdAndPapel(Long id, Papel papel);
    List<Usuario> findByNomeContainingIgnoreCaseAndPapelOrderByNomeAsc(String nome, Papel papel);
}
