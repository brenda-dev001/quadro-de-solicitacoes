package com.quadrosolicitacoes.categoria;

import java.util.List;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quadrosolicitacoes.common.exception.EntidadeNaoEncontradaException;
import com.quadrosolicitacoes.categoria.dto.CategoriaCreateRequest;
import com.quadrosolicitacoes.categoria.dto.CategoriaResponse;
import com.quadrosolicitacoes.categoria.dto.CategoriaSearchRequest;
import com.quadrosolicitacoes.categoria.dto.CategoriaUpdateRequest;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public void salvar(CategoriaCreateRequest form) {
        categoriaRepository.save(toEntity(form));
    }

    public Page<CategoriaResponse> pesquisar(CategoriaSearchRequest request,
            Pageable pageable) {
        if (request.isEmpty()) {
            return categoriaRepository.findAll(pageable)
                    .map(this::toResponse);
        }

        Categoria categoriaFiltro = toEntity(request);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        return categoriaRepository.findAll(Example.of(categoriaFiltro, matcher), pageable)
                .map(this::toResponse);
    }

    public CategoriaUpdateRequest buscarParaEdicao(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria não encontrada"));
        return toUpdateRequest(categoria);
    }

    public List<CategoriaResponse> buscarPeloNome(String nome) {
        return categoriaRepository.findTop10ByNomeContainingIgnoreCaseOrderByNomeAsc(nome).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public void atualizar(Long id, CategoriaUpdateRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria não encontrada"));
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        categoriaRepository.save(categoria);
    }

    private Categoria toEntity(CategoriaSearchRequest request) {
        Categoria categoria = new Categoria();
        categoria.setId(request.id());
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        return categoria;
    }

    private Categoria toEntity(CategoriaCreateRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        return categoria;
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getDescricao());
    }

    private CategoriaUpdateRequest toUpdateRequest(Categoria categoria) {
        return new CategoriaUpdateRequest(categoria.getNome(), categoria.getDescricao());
    }
}
