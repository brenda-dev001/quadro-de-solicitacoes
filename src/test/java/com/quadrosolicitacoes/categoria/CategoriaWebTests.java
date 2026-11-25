package com.quadrosolicitacoes.categoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import com.quadrosolicitacoes.common.enums.Status;

@SpringBootTest
@Transactional
class CategoriaWebTests {
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private CategoriaRepository repository;
    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void renderizaLayoutEFormularioComOsComponentes() throws Exception {
        mvc.perform(get("/categorias/nova"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Quadro de Solicitações")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"nome\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/js/app.js")));
        mvc.perform(get("/categorias/nova").header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<head>"))));
    }

    @Test
    void rejeitaDadosInvalidosSemSalvar() throws Exception {
        long antes = repository.count();
        mvc.perform(post("/categorias").param("nome", " ").param("descricao", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("categoriaCreateRequest", "nome", "descricao"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("O nome é obrigatório")));
        assertThat(repository.count()).isEqualTo(antes);
        mvc.perform(post("/categorias").param("nome", "x".repeat(256)).param("descricao", "Descrição"))
                .andExpect(model().attributeHasFieldErrors("categoriaCreateRequest", "nome"));
    }

    @Test
    void cadastraEditaEInativaCategoria() throws Exception {
        mvc.perform(post("/categorias").param("nome", "Teste CRUD categoria").param("descricao", "Descrição"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/categorias"));
        Categoria categoria = repository.findAll().stream()
                .filter(c -> "Teste CRUD categoria".equals(c.getNome())).findFirst().orElseThrow();
        Long id = categoria.getId();
        mvc.perform(get("/categorias/{id}/editar", id))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Teste CRUD categoria")));
        mvc.perform(put("/categorias/{id}", id).param("nome", "").param("descricao", "Descrição"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("hx-put=\"/categorias/" + id + "\"")));
        mvc.perform(put("/categorias/{id}", id).param("nome", "Nome alterado").param("descricao", "Descrição alterada"))
                .andExpect(status().is3xxRedirection());
        assertThat(repository.findById(id).orElseThrow().getNome()).isEqualTo("Nome alterado");
        mvc.perform(delete("/categorias/{id}", id)).andExpect(status().is3xxRedirection());
        assertThat(repository.findById(id).orElseThrow().getStatus()).isEqualTo(Status.INATIVO);
        mvc.perform(get("/categorias/{id}/editar", id)).andExpect(status().isNotFound());
    }

    @Test
    void pesquisaPaginadaPreservaFiltroENaoListaInativas() throws Exception {
        for (int i = 0; i < 9; i++) {
            Categoria categoria = new Categoria();
            categoria.setNome("CategoriaPaginada" + i);
            categoria.setDescricao("Descrição " + i);
            if (i == 8) categoria.setStatus(Status.INATIVO);
            repository.save(categoria);
        }
        mvc.perform(get("/categorias").param("nome", "CategoriaPaginada").param("size", "7"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CategoriaPaginada0")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("CategoriaPaginada7"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("page=1")));
        mvc.perform(get("/categorias").header("HX-Request", "true")
                .param("nome", "CategoriaPaginada").param("size", "7").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CategoriaPaginada7")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("CategoriaPaginada8"))));
    }
}
