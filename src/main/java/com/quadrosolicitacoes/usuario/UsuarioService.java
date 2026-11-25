package com.quadrosolicitacoes.usuario;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quadrosolicitacoes.usuario.dto.UsuarioResponse;

@Service
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponse> buscarPeloNome(String nome, Papel papel) {
        return usuarioRepository.findTop10ByNomeContainingIgnoreCaseAndPapelOrderByNomeAsc(nome, papel)
                .stream().map(usuario -> new UsuarioResponse(usuario.getId(), usuario.getNome())).toList();
    }
}
