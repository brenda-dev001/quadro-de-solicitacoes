package com.quadrosolicitacoes.categoria;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

@Controller 
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping 
    public String listarCategorias(Model model){
        List<Categoria> categorias = categoriaService.findAll();

        model.addAttribute("categorias", categorias);

        return "categorias/lista";

    }

    @GetMapping("/nova")
    public String novaCategoria(Model model){
        model.addAttribute("categoria", new Categoria());

        return "categorias/formulario";
    }

    @PostMapping
    public String salvarCategoria(@ModelAttribute Categoria categoria) {
        categoriaService.save(categoria);

        return "redirect:/categorias";
    }

    @GetMapping("/editar/{id}")
    public String editarCategoria(@PathVariable Long id, Model model) {
        Categoria categoria = categoriaService.findById(id);

        model.addAttribute("categoria", categoria);

        return "categorias/formulario";
    }

    @PostMapping("/editar/{id}")
    public String atualizarCategoria(
            @PathVariable Long id,
            @ModelAttribute Categoria categoria) {

        categoriaService.update(id, categoria);

        return "redirect:/categorias";
    }
}
