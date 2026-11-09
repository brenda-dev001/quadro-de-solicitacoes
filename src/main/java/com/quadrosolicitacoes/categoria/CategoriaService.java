package com.quadrosolicitacoes.categoria;

import java.util.List;
import org.springframework.stereotype.Service;

@Service    
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository){
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> findAll(){
        return categoriaRepository.findAll();
    }

    public Categoria findById(Long id){
        return categoriaRepository.findById(id).orElse(null);
    }

    public Categoria save(Categoria categoria){
        return categoriaRepository.save(categoria);
    }

    public Categoria update(Long id, Categoria dados){
        Categoria categoria = findById(id);

        categoria.setNome(dados.getNome());
        categoria.setDescricao(dados.getDescricao());

        return categoriaRepository.save(categoria);
    }

    public void delete(Long id){
        if(!categoriaRepository.existsById(id)){
            throw new RuntimeException("Categoria não encontrada");
        }
        categoriaRepository.deleteById(id);
    }
}
