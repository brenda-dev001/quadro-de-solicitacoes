package com.quadrosolicitacoes.solicitacao;

import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quadrosolicitacoes.categoria.CategoriaRepository;
import com.quadrosolicitacoes.common.enums.Status;
import com.quadrosolicitacoes.common.exception.EntidadeNaoEncontradaException;
import com.quadrosolicitacoes.common.exception.RegraNegocioException;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoAtendimentoRequest;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoCancelamentoRequest;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoCreateRequest;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoResponse;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoSearchRequest;
import com.quadrosolicitacoes.usuario.Papel;
import com.quadrosolicitacoes.usuario.Usuario;
import com.quadrosolicitacoes.usuario.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class SolicitacaoService {
    private final SolicitacaoRepository solicitacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
            CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Long salvar(SolicitacaoCreateRequest form) {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setDescricao(form.descricao());
        solicitacao.setCategoria(categoriaRepository.findByIdAndStatus(form.categoriaId(), Status.ATIVO)
                .orElseThrow(() -> new RegraNegocioException("Selecione uma categoria ativa")));
        solicitacao.setSolicitante(usuarioRepository.findByIdAndPapel(form.solicitanteId(), Papel.FUNCIONARIO)
                .orElseThrow(() -> new RegraNegocioException("Selecione um funcionário válido")));
        solicitacao.setStatus(StatusSolicitacao.ABERTA);
        return solicitacaoRepository.save(solicitacao).getId();
    }

    public Page<SolicitacaoResponse> pesquisar(SolicitacaoSearchRequest filtro, Pageable pageable) {
        return solicitacaoRepository.findAll(SolicitacaoSpecs.comFiltros(filtro), pageable).map(this::toResponse);
    }

    public SolicitacaoResponse buscarPorId(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public void assumir(Long id, SolicitacaoAtendimentoRequest request) {
        Solicitacao solicitacao = buscar(id);
        if (solicitacao.getStatus() != StatusSolicitacao.ABERTA) {
            throw new RegraNegocioException("Somente solicitações abertas podem ser assumidas");
        }
        Usuario tecnico = usuarioRepository.findByIdAndPapel(request.tecnicoId(), Papel.TECNICO)
                .orElseThrow(() -> new RegraNegocioException("Selecione um técnico válido"));
        solicitacao.setTecnicoResponsavel(tecnico);
        solicitacao.setStatus(StatusSolicitacao.EM_ANDAMENTO);
        solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public void concluir(Long id, SolicitacaoAtendimentoRequest request) {
        Solicitacao solicitacao = buscar(id);
        if (solicitacao.getStatus() != StatusSolicitacao.EM_ANDAMENTO) {
            throw new RegraNegocioException("Somente solicitações em andamento podem ser concluídas");
        }
        if (solicitacao.getTecnicoResponsavel() == null
                || !Objects.equals(solicitacao.getTecnicoResponsavel().getId(), request.tecnicoId())) {
            throw new RegraNegocioException("Somente o técnico responsável pode concluir a solicitação");
        }
        solicitacao.setStatus(StatusSolicitacao.CONCLUIDA);
        solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public void cancelar(Long id, SolicitacaoCancelamentoRequest request) {
        Solicitacao solicitacao = buscar(id);
        if (solicitacao.getStatus() != StatusSolicitacao.ABERTA) {
            throw new RegraNegocioException("Somente solicitações abertas podem ser canceladas");
        }
        if (solicitacao.getSolicitante() == null
                || !Objects.equals(solicitacao.getSolicitante().getId(), request.solicitanteId())) {
            throw new RegraNegocioException("Somente o funcionário solicitante pode cancelar a solicitação");
        }
        solicitacao.setStatus(StatusSolicitacao.CANCELADA);
        solicitacaoRepository.save(solicitacao);
    }

    private Solicitacao buscar(Long id) {
        return solicitacaoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Solicitação não encontrada"));
    }

    private SolicitacaoResponse toResponse(Solicitacao solicitacao) {
        return new SolicitacaoResponse(solicitacao.getId(), solicitacao.getDescricao(), solicitacao.getStatus(),
                solicitacao.getDataCriacao(),
                solicitacao.getCategoria() != null ? solicitacao.getCategoria().getId() : null,
                solicitacao.getCategoria() != null ? solicitacao.getCategoria().getNome() : null,
                solicitacao.getSolicitante() != null ? solicitacao.getSolicitante().getId() : null,
                solicitacao.getSolicitante() != null ? solicitacao.getSolicitante().getNome() : null,
                solicitacao.getTecnicoResponsavel() != null ? solicitacao.getTecnicoResponsavel().getId() : null,
                solicitacao.getTecnicoResponsavel() != null ? solicitacao.getTecnicoResponsavel().getNome() : null);
    }
}
