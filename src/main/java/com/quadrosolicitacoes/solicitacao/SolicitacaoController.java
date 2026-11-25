package com.quadrosolicitacoes.solicitacao;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.quadrosolicitacoes.categoria.CategoriaService;
import com.quadrosolicitacoes.comentario.ComentarioService;
import com.quadrosolicitacoes.comentario.dto.ComentarioCreateRequest;
import com.quadrosolicitacoes.common.exception.RegraNegocioException;
import com.quadrosolicitacoes.core.web.notification.NotificacaoSweetAlert2;
import com.quadrosolicitacoes.core.web.notification.TipoNotificacaoSweetAlert2;
import com.quadrosolicitacoes.core.web.pagination.PageWrapper;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoAtendimentoRequest;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoCancelamentoRequest;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoCreateRequest;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoResponse;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoSearchRequest;
import com.quadrosolicitacoes.usuario.Papel;
import com.quadrosolicitacoes.usuario.UsuarioService;

@Controller
@RequestMapping("/solicitacoes")
public class SolicitacaoController {
    private final SolicitacaoService solicitacaoService;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;
    private final ComentarioService comentarioService;

    public SolicitacaoController(SolicitacaoService solicitacaoService, CategoriaService categoriaService,
            UsuarioService usuarioService, ComentarioService comentarioService) {
        this.solicitacaoService = solicitacaoService;
        this.categoriaService = categoriaService;
        this.usuarioService = usuarioService;
        this.comentarioService = comentarioService;
    }

    @GetMapping("/nova")
    public String abrirFormulario(SolicitacaoCreateRequest form, HtmxRequest htmxRequest) {
        return htmxRequest.isHtmxRequest() ? "solicitacoes/create :: formulario" : "solicitacoes/create";
    }

    @PostMapping
    public String cadastrar(@Valid SolicitacaoCreateRequest form, BindingResult resultado,
            RedirectAttributes redirect, HtmxRequest htmxRequest) {
        if (!resultado.hasErrors()) {
            try {
                Long id = solicitacaoService.salvar(form);
                return redirecionar(id, "Solicitação aberta com sucesso!", redirect, htmxRequest);
            } catch (RegraNegocioException ex) {
                resultado.reject("regraNegocio", ex.getMessage());
            }
        }
        return htmxRequest.isHtmxRequest() ? "solicitacoes/create :: formulario" : "solicitacoes/create";
    }

    @GetMapping
    public String pesquisar(SolicitacaoSearchRequest form, Model model,
            @PageableDefault(size = 7) @SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest request, HtmxRequest htmxRequest) {
        model.addAttribute("situacoes", StatusSolicitacao.values());
        var pagina = solicitacaoService.pesquisar(form, pageable);
        model.addAttribute("pagina", new PageWrapper<>(pagina, request));
        if (pagina.isEmpty()) model.addAttribute("notificacaoSA2", new NotificacaoSweetAlert2(
                "A busca não encontrou solicitações", TipoNotificacaoSweetAlert2.INFO, 4000));
        return htmxRequest.isHtmxRequest() ? "solicitacoes/lista :: conteudo" : "solicitacoes/lista";
    }

    @GetMapping("/pesquisarcategoria")
    public String pesquisarCategoria(@RequestParam(defaultValue = "") String termoBusca, Model model) {
        model.addAttribute("categorias", categoriaService.buscarPeloNome(termoBusca));
        return "categorias/list_li :: lista";
    }

    @GetMapping("/pesquisarfuncionario")
    public String pesquisarFuncionario(@RequestParam(defaultValue = "") String termoBusca, Model model) {
        model.addAttribute("usuarios", usuarioService.buscarPeloNome(termoBusca, Papel.FUNCIONARIO));
        return "usuarios/list_li :: lista";
    }

    @GetMapping("/pesquisartecnico")
    public String pesquisarTecnico(@RequestParam(defaultValue = "") String termoBusca, Model model) {
        model.addAttribute("usuarios", usuarioService.buscarPeloNome(termoBusca, Papel.TECNICO));
        return "usuarios/list_li :: lista";
    }

    @GetMapping("/{id}/pesquisarautor")
    public String pesquisarAutor(@PathVariable Long id,
            @RequestParam(defaultValue = "") String termoBusca, Model model) {
        model.addAttribute("usuarios", solicitacaoService.buscarParticipantes(id, termoBusca));
        return "usuarios/list_li :: lista";
    }

    @GetMapping("/{id}")
    public String detalhar(@PathVariable Long id, Model model,
            @PageableDefault(size = 7) @SortDefault(sort = {"dataCriacao", "id"}) Pageable pageable,
            HttpServletRequest request, HtmxRequest htmxRequest) {
        prepararDetalhes(id, model, pageable, request);
        return detalhes(htmxRequest);
    }

    @PostMapping("/{id}/assumir")
    public String assumir(@PathVariable Long id, @Valid SolicitacaoAtendimentoRequest form,
            BindingResult resultado, Model model, RedirectAttributes redirect,
            HttpServletRequest request, HtmxRequest htmxRequest) {
        if (!resultado.hasErrors()) {
            try {
                solicitacaoService.assumir(id, form);
                return redirecionar(id, "Atendimento iniciado com sucesso!", redirect, htmxRequest);
            } catch (RegraNegocioException ex) {
                resultado.reject("regraNegocio", ex.getMessage());
            }
        }
        prepararDetalhes(id, model, paginaComentarios(), request);
        return detalhes(htmxRequest);
    }

    @PostMapping("/{id}/concluir")
    public String concluir(@PathVariable Long id, @Valid SolicitacaoAtendimentoRequest form,
            BindingResult resultado, Model model, RedirectAttributes redirect,
            HttpServletRequest request, HtmxRequest htmxRequest) {
        if (!resultado.hasErrors()) {
            try {
                solicitacaoService.concluir(id, form);
                return redirecionar(id, "Solicitação concluída com sucesso!", redirect, htmxRequest);
            } catch (RegraNegocioException ex) {
                resultado.reject("regraNegocio", ex.getMessage());
            }
        }
        prepararDetalhes(id, model, paginaComentarios(), request);
        return detalhes(htmxRequest);
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, @Valid SolicitacaoCancelamentoRequest form,
            BindingResult resultado, Model model, RedirectAttributes redirect,
            HttpServletRequest request, HtmxRequest htmxRequest) {
        if (!resultado.hasErrors()) {
            try {
                solicitacaoService.cancelar(id, form);
                return redirecionar(id, "Solicitação cancelada com sucesso!", redirect, htmxRequest);
            } catch (RegraNegocioException ex) {
                resultado.reject("regraNegocio", ex.getMessage());
            }
        }
        prepararDetalhes(id, model, paginaComentarios(), request);
        return detalhes(htmxRequest);
    }

    @PostMapping("/{id}/comentarios")
    public String comentar(@PathVariable Long id, @Valid ComentarioCreateRequest form,
            BindingResult resultado, Model model, RedirectAttributes redirect,
            HttpServletRequest request, HtmxRequest htmxRequest) {
        if (!resultado.hasErrors()) {
            try {
                comentarioService.salvar(id, form);
                return redirecionar(id, "Comentário adicionado com sucesso!", redirect, htmxRequest);
            } catch (RegraNegocioException ex) {
                resultado.reject("regraNegocio", ex.getMessage());
            }
        }
        prepararDetalhes(id, model, paginaComentarios(), request);
        return detalhes(htmxRequest);
    }

    private void prepararDetalhes(Long id, Model model, Pageable pageable, HttpServletRequest request) {
        SolicitacaoResponse solicitacao = solicitacaoService.buscarPorId(id);
        model.addAttribute("solicitacao", solicitacao);
        model.addAttribute("pagina", new PageWrapper<>(comentarioService.pesquisar(id, pageable), request, "/solicitacoes/" + id));
        if (!model.containsAttribute("solicitacaoAtendimentoRequest")) model.addAttribute(
                "solicitacaoAtendimentoRequest", new SolicitacaoAtendimentoRequest(null, null));
        if (!model.containsAttribute("solicitacaoCancelamentoRequest")) model.addAttribute(
                "solicitacaoCancelamentoRequest", new SolicitacaoCancelamentoRequest(solicitacao.solicitanteId()));
        if (!model.containsAttribute("comentarioCreateRequest")) model.addAttribute(
                "comentarioCreateRequest", new ComentarioCreateRequest(null, null, null));
    }

    private Pageable paginaComentarios() {
        return PageRequest.of(0, 7, Sort.by("dataCriacao", "id"));
    }

    private String detalhes(HtmxRequest htmxRequest) {
        return htmxRequest.isHtmxRequest() ? "solicitacoes/detalhes :: conteudo" : "solicitacoes/detalhes";
    }

    private String redirecionar(Long id, String mensagem, RedirectAttributes redirect, HtmxRequest htmxRequest) {
        redirect.addFlashAttribute("notificacaoSA2",
                new NotificacaoSweetAlert2(mensagem, TipoNotificacaoSweetAlert2.SUCCESS, 4000));
        return (htmxRequest.isHtmxRequest() ? "redirect:htmx:location:" : "redirect:") + "/solicitacoes/" + id;
    }
}
