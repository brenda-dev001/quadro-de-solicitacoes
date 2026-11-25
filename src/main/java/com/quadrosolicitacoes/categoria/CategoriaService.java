package com.quadrosolicitacoes.categoria;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quadrosolicitacoes.common.exception.EntidadeNaoEncontradaException;
import com.quadrosolicitacoes.categoria.dto.CategoriaCreateRequest;
import com.quadrosolicitacoes.categoria.dto.CategoriaResponse;
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

    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CategoriaUpdateRequest buscarParaEdicao(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria não encontrada"));
        return toUpdateRequest(categoria);
    }

    @Transactional
    public void atualizar(Long id, CategoriaUpdateRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria não encontrada"));
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        categoriaRepository.save(categoria);
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
