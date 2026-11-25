package com.quadrosolicitacoes.comentario;

import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quadrosolicitacoes.comentario.dto.ComentarioCreateRequest;
import com.quadrosolicitacoes.comentario.dto.ComentarioResponse;
import com.quadrosolicitacoes.common.exception.EntidadeNaoEncontradaException;
import com.quadrosolicitacoes.common.exception.RegraNegocioException;
import com.quadrosolicitacoes.solicitacao.Solicitacao;
import com.quadrosolicitacoes.solicitacao.SolicitacaoRepository;
import com.quadrosolicitacoes.solicitacao.StatusSolicitacao;
import com.quadrosolicitacoes.usuario.Usuario;
import com.quadrosolicitacoes.usuario.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class ComentarioService {
    private final ComentarioRepository comentarioRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository,
            SolicitacaoRepository solicitacaoRepository, UsuarioRepository usuarioRepository) {
        this.comentarioRepository = comentarioRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void salvar(Long solicitacaoId, ComentarioCreateRequest form) {
        Solicitacao solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Solicitação não encontrada"));
        if (solicitacao.getStatus() == StatusSolicitacao.CONCLUIDA
                || solicitacao.getStatus() == StatusSolicitacao.CANCELADA) {
            throw new RegraNegocioException("Solicitações encerradas não recebem novos comentários");
        }
        Usuario autor = usuarioRepository.findById(form.autorId())
                .orElseThrow(() -> new RegraNegocioException("Selecione um autor válido"));
        boolean solicitante = solicitacao.getSolicitante() != null
                && Objects.equals(solicitacao.getSolicitante().getId(), autor.getId());
        boolean tecnico = solicitacao.getTecnicoResponsavel() != null
                && Objects.equals(solicitacao.getTecnicoResponsavel().getId(), autor.getId());
        if (!solicitante && !tecnico) {
            throw new RegraNegocioException("Somente o solicitante e o técnico responsável podem comentar");
        }
        comentarioRepository.save(new Comentario(form.conteudo().strip(), autor, solicitacao));
    }

    public Page<ComentarioResponse> pesquisar(Long solicitacaoId, Pageable pageable) {
        return comentarioRepository.findBySolicitacaoId(solicitacaoId, pageable)
                .map(comentario -> new ComentarioResponse(comentario.getId(), comentario.getConteudo(),
                        comentario.getDataCriacao(), comentario.getAutor().getId(), comentario.getAutor().getNome()));
    }
}
