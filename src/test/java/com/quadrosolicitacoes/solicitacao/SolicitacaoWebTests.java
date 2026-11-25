package com.quadrosolicitacoes.solicitacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import com.quadrosolicitacoes.categoria.Categoria;
import com.quadrosolicitacoes.categoria.CategoriaRepository;
import com.quadrosolicitacoes.comentario.Comentario;
import com.quadrosolicitacoes.comentario.ComentarioRepository;
import com.quadrosolicitacoes.common.enums.Status;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoCreateRequest;
import com.quadrosolicitacoes.usuario.Papel;
import com.quadrosolicitacoes.usuario.Usuario;
import com.quadrosolicitacoes.usuario.UsuarioRepository;

@SpringBootTest
@Transactional
class SolicitacaoWebTests {
    @Autowired private WebApplicationContext context;
    @Autowired private CategoriaRepository categorias;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private SolicitacaoRepository solicitacoes;
    @Autowired private ComentarioRepository comentarios;
    @Autowired private SolicitacaoService service;
    private MockMvc mvc;
    private Categoria categoria;
    private Usuario funcionario;
    private Usuario tecnico;
    private Usuario outroTecnico;
    private Usuario outroFuncionario;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        categoria = new Categoria();
        categoria.setNome("Categoria de teste");
        categoria.setDescricao("Descrição");
        categorias.save(categoria);
        funcionario = usuario("Funcionário teste", "funcionario-teste@example.com", Papel.FUNCIONARIO);
        tecnico = usuario("Técnico teste", "tecnico-teste@example.com", Papel.TECNICO);
        outroTecnico = usuario("Outro técnico", "outro-tecnico-teste@example.com", Papel.TECNICO);
        outroFuncionario = usuario("Outro funcionário", "outro-funcionario-teste@example.com", Papel.FUNCIONARIO);
    }

    private Usuario usuario(String nome, String email, Papel papel) {
        return usuarios.save(new Usuario(null, nome, email, "senha-apenas-para-teste", papel));
    }

    private Solicitacao abrir(String descricao) {
        Long id = service.salvar(new SolicitacaoCreateRequest(descricao, categoria.getId(),
                categoria.getNome(), funcionario.getId(), funcionario.getNome()));
        return solicitacoes.findById(id).orElseThrow();
    }

    @Test
    void renderizaAberturaEPesquisaComLayoutEFragmentos() throws Exception {
        mvc.perform(get("/solicitacoes/nova")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Quadro de Solicitações")))
                .andExpect(content().string(containsString("name=\"categoriaId\"")))
                .andExpect(content().string(containsString("name=\"solicitanteId\"")));
        mvc.perform(get("/solicitacoes/nova").header("HX-Request", "true"))
                .andExpect(status().isOk()).andExpect(content().string(not(containsString("<head>"))));
        mvc.perform(get("/solicitacoes")).andExpect(status().isOk());
    }

    @Test
    void aberturaDefineStatusDataEIgnoraCamposNaoPermitidos() throws Exception {
        mvc.perform(post("/solicitacoes").param("descricao", "Computador não liga")
                .param("categoriaId", categoria.getId().toString())
                .param("solicitanteId", funcionario.getId().toString())
                .param("status", "CONCLUIDA").param("tecnicoId", tecnico.getId().toString()))
                .andExpect(status().is3xxRedirection());
        Solicitacao salva = solicitacoes.findAll().stream()
                .filter(s -> "Computador não liga".equals(s.getDescricao())).findFirst().orElseThrow();
        assertThat(salva.getStatus()).isEqualTo(StatusSolicitacao.ABERTA);
        assertThat(salva.getDataCriacao()).isNotNull();
        assertThat(salva.getTecnicoResponsavel()).isNull();
    }

    @Test
    void rejeitaCamposVaziosEIdsInvalidos() throws Exception {
        long antes = solicitacoes.count();
        mvc.perform(post("/solicitacoes").param("descricao", " "))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("solicitacaoCreateRequest", "descricao", "categoriaId", "solicitanteId"));
        mvc.perform(post("/solicitacoes").param("descricao", "Teste")
                .param("categoriaId", "-1").param("solicitanteId", "0"))
                .andExpect(model().attributeHasFieldErrors("solicitacaoCreateRequest", "categoriaId", "solicitanteId"));
        assertThat(solicitacoes.count()).isEqualTo(antes);
    }

    @Test
    void rejeitaCategoriaInativaETecnicoComoSolicitante() throws Exception {
        categoria.setStatus(Status.INATIVO);
        categorias.saveAndFlush(categoria);
        mvc.perform(post("/solicitacoes").param("descricao", "Teste")
                .param("categoriaId", categoria.getId().toString()).param("solicitanteId", funcionario.getId().toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Selecione uma categoria ativa")));
        categoria.setStatus(Status.ATIVO);
        categorias.saveAndFlush(categoria);
        mvc.perform(post("/solicitacoes").param("descricao", "Teste")
                .param("categoriaId", categoria.getId().toString()).param("solicitanteId", tecnico.getId().toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Selecione um funcionário válido")));
    }

    @Test
    void pesquisaUsuariosPorPapelSemExporSenha() throws Exception {
        mvc.perform(get("/solicitacoes/pesquisarfuncionario").param("termoBusca", "teste"))
                .andExpect(status().isOk()).andExpect(content().string(containsString(funcionario.getNome())))
                .andExpect(content().string(not(containsString(tecnico.getNome()))))
                .andExpect(content().string(not(containsString("senha-apenas-para-teste"))));
        mvc.perform(get("/solicitacoes/pesquisartecnico").param("termoBusca", "teste"))
                .andExpect(status().isOk()).andExpect(content().string(containsString(tecnico.getNome())))
                .andExpect(content().string(not(containsString(funcionario.getNome()))));
        mvc.perform(get("/solicitacoes/pesquisarcategoria").param("termoBusca", "Categoria de teste"))
                .andExpect(status().isOk()).andExpect(content().string(containsString(categoria.getNome())));
    }

    @Test
    void atendeComentaEConcluiMantendoHistorico() throws Exception {
        Solicitacao solicitacao = abrir("Teste atendimento");
        Long id = solicitacao.getId();
        mvc.perform(get("/solicitacoes/{id}", id)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Iniciar atendimento")));
        mvc.perform(post("/solicitacoes/{id}/assumir", id).param("tecnicoId", tecnico.getId().toString()))
                .andExpect(status().is3xxRedirection());
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.EM_ANDAMENTO);
        assertThat(solicitacao.getTecnicoResponsavel()).isEqualTo(tecnico);
        mvc.perform(post("/solicitacoes/{id}/comentarios", id).param("autorId", tecnico.getId().toString())
                .param("conteudo", "Atendimento realizado <script>alert(1)</script>"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/solicitacoes/{id}", id)).andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(containsString("Concluir")));
        mvc.perform(post("/solicitacoes/{id}/concluir", id).param("tecnicoId", tecnico.getId().toString()))
                .andExpect(status().is3xxRedirection());
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.CONCLUIDA);
        mvc.perform(get("/solicitacoes/{id}", id).header("HX-Request", "true"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Atendimento realizado")))
                .andExpect(content().string(not(containsString(">Comentar</button>"))));
    }

    @Test
    void recusaFuncionarioNoAtendimentoEValidacaoMantemDetalhes() throws Exception {
        Long id = abrir("Teste papel").getId();
        mvc.perform(post("/solicitacoes/{id}/assumir", id))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("solicitacaoAtendimentoRequest", "tecnicoId"));
        mvc.perform(post("/solicitacoes/{id}/assumir", id).param("tecnicoId", funcionario.getId().toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Selecione um técnico válido")));
        assertThat(solicitacoes.findById(id).orElseThrow().getStatus()).isEqualTo(StatusSolicitacao.ABERTA);
    }

    @Test
    void naoPermiteOutroTecnicoConcluirNemAssumirNovamente() throws Exception {
        Solicitacao solicitacao = abrir("Teste responsável");
        solicitacao.setStatus(StatusSolicitacao.EM_ANDAMENTO);
        solicitacao.setTecnicoResponsavel(tecnico);
        solicitacoes.saveAndFlush(solicitacao);
        mvc.perform(post("/solicitacoes/{id}/concluir", solicitacao.getId()).param("tecnicoId", outroTecnico.getId().toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Somente o técnico responsável")));
        mvc.perform(post("/solicitacoes/{id}/assumir", solicitacao.getId()).param("tecnicoId", outroTecnico.getId().toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Somente solicitações abertas")));
        assertThat(solicitacao.getTecnicoResponsavel()).isEqualTo(tecnico);
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.EM_ANDAMENTO);
    }

    @Test
    void cancelaApenasPeloSolicitanteEnquantoAberta() throws Exception {
        Solicitacao solicitacao = abrir("Teste cancelamento");
        mvc.perform(post("/solicitacoes/{id}/cancelar", solicitacao.getId())
                .param("solicitanteId", outroFuncionario.getId().toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Somente o funcionário solicitante")));
        mvc.perform(post("/solicitacoes/{id}/cancelar", solicitacao.getId())
                .param("solicitanteId", funcionario.getId().toString())).andExpect(status().is3xxRedirection());
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.CANCELADA);
        mvc.perform(post("/solicitacoes/{id}/assumir", solicitacao.getId()).param("tecnicoId", tecnico.getId().toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Somente solicitações abertas")));
    }

    @Test
    void comentariosExigemConteudoEParticipacao() throws Exception {
        Long id = abrir("Teste comentário").getId();
        mvc.perform(post("/solicitacoes/{id}/comentarios", id).param("conteudo", " "))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("comentarioCreateRequest", "conteudo", "autorId"));
        mvc.perform(post("/solicitacoes/{id}/comentarios", id).param("conteudo", "Teste")
                .param("autorId", outroFuncionario.getId().toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Somente o solicitante e o técnico responsável")));
        mvc.perform(post("/solicitacoes/{id}/comentarios", id).param("conteudo", "Comentário do funcionário")
                .param("autorId", funcionario.getId().toString())).andExpect(status().is3xxRedirection());
        mvc.perform(get("/solicitacoes/{id}/pesquisarautor", id)).andExpect(status().isOk())
                .andExpect(content().string(containsString(funcionario.getNome())))
                .andExpect(content().string(not(containsString(outroFuncionario.getNome()))));
    }

    @Test
    void rejeitaComentariosDepoisDoEncerramento() throws Exception {
        for (StatusSolicitacao status : new StatusSolicitacao[]{StatusSolicitacao.CONCLUIDA, StatusSolicitacao.CANCELADA}) {
            Solicitacao solicitacao = abrir("Teste encerrada " + status);
            solicitacao.setStatus(status);
            solicitacoes.saveAndFlush(solicitacao);
            mvc.perform(post("/solicitacoes/{id}/comentarios", solicitacao.getId()).param("conteudo", "Novo comentário")
                    .param("autorId", funcionario.getId().toString()))
                    .andExpect(status().isOk()).andExpect(content().string(containsString("Solicitações encerradas")));
        }
    }

    @Test
    void pesquisaPaginadaFiltraPorFuncionarioCategoriaSituacaoETecnico() throws Exception {
        for (int i = 0; i < 8; i++) abrir("FiltroSolicitacao " + i);
        Solicitacao outra = abrir("Outra solicitação");
        outra.setSolicitante(outroFuncionario);
        outra.setStatus(StatusSolicitacao.EM_ANDAMENTO);
        outra.setTecnicoResponsavel(tecnico);
        solicitacoes.saveAndFlush(outra);
        mvc.perform(get("/solicitacoes").param("solicitanteId", funcionario.getId().toString())
                .param("categoriaId", categoria.getId().toString()).param("status", "ABERTA").param("sort", "id,asc"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("FiltroSolicitacao 0")))
                .andExpect(content().string(not(containsString("FiltroSolicitacao 7"))))
                .andExpect(content().string(not(containsString("Outra solicitação"))));
        mvc.perform(get("/solicitacoes").param("descricao", "FiltroSolicitacao").param("page", "1").param("sort", "id,asc"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("FiltroSolicitacao 7")));
        mvc.perform(get("/solicitacoes").param("tecnicoId", tecnico.getId().toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Outra solicitação")))
                .andExpect(content().string(not(containsString("FiltroSolicitacao 0"))));
    }

    @Test
    void paginaComentariosNaoMisturaSolicitacoesEPreservaUrlEmErros() throws Exception {
        Solicitacao primeira = abrir("Primeira solicitação");
        Solicitacao segunda = abrir("Segunda solicitação");
        for (int i = 0; i < 8; i++) comentarios.save(new Comentario("ComentarioPagina" + i, funcionario, primeira));
        comentarios.saveAndFlush(new Comentario("Comentário de outra solicitação", funcionario, segunda));
        mvc.perform(get("/solicitacoes/{id}", primeira.getId()).param("page", "1").param("sort", "id,asc"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("ComentarioPagina7")))
                .andExpect(content().string(not(containsString("ComentarioPagina0"))))
                .andExpect(content().string(not(containsString("Comentário de outra solicitação"))));
        mvc.perform(post("/solicitacoes/{id}/comentarios", primeira.getId()).param("conteudo", " "))
                .andExpect(status().isOk()).andExpect(content().string(containsString("/solicitacoes/" + primeira.getId() + "?page=1")))
                .andExpect(content().string(not(containsString("/comentarios?page="))));
    }

    @Test
    void naoConcluiAbertaNemCancelaEmAndamento() throws Exception {
        Solicitacao solicitacao = abrir("Teste transições inválidas");
        mvc.perform(post("/solicitacoes/{id}/concluir", solicitacao.getId())
                .param("tecnicoId", tecnico.getId().toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Somente solicitações em andamento")));
        solicitacao.setStatus(StatusSolicitacao.EM_ANDAMENTO);
        solicitacao.setTecnicoResponsavel(tecnico);
        solicitacoes.saveAndFlush(solicitacao);
        mvc.perform(post("/solicitacoes/{id}/cancelar", solicitacao.getId())
                .param("solicitanteId", funcionario.getId().toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Somente solicitações abertas")));
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.EM_ANDAMENTO);
    }

    @Test
    void cadastroHtmxRetornaLocalizacaoDaSolicitacao() throws Exception {
        mvc.perform(post("/solicitacoes").header("HX-Request", "true")
                .param("descricao", "Teste cadastro htmx")
                .param("categoriaId", categoria.getId().toString())
                .param("solicitanteId", funcionario.getId().toString()))
                .andExpect(status().isOk()).andExpect(header().exists("HX-Location"));
    }

    @Test
    void solicitacaoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/solicitacoes/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
        mvc.perform(post("/solicitacoes/{id}/assumir", Long.MAX_VALUE).param("tecnicoId", tecnico.getId().toString()))
                .andExpect(status().isNotFound());
    }
}
