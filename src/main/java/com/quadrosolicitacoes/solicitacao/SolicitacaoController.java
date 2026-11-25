package com.quadrosolicitacoes.solicitacao;

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
import com.quadrosolicitacoes.common.exception.RegraNegocioException;
import com.quadrosolicitacoes.core.web.notification.NotificacaoSweetAlert2;
import com.quadrosolicitacoes.core.web.notification.TipoNotificacaoSweetAlert2;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoCreateRequest;
import com.quadrosolicitacoes.solicitacao.dto.SolicitacaoResponse;
import com.quadrosolicitacoes.usuario.Papel;
import com.quadrosolicitacoes.usuario.UsuarioService;

@Controller
@RequestMapping("/solicitacoes")
public class SolicitacaoController {
    private final SolicitacaoService solicitacaoService;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;

    public SolicitacaoController(SolicitacaoService solicitacaoService, CategoriaService categoriaService,
            UsuarioService usuarioService) {
        this.solicitacaoService = solicitacaoService;
        this.categoriaService = categoriaService;
        this.usuarioService = usuarioService;
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

    @GetMapping("/{id}")
    public String detalhar(@PathVariable Long id, Model model,
            HttpServletRequest request, HtmxRequest htmxRequest) {
        prepararDetalhes(id, model, request);
        return detalhes(htmxRequest);
    }

    private void prepararDetalhes(Long id, Model model, HttpServletRequest request) {
        SolicitacaoResponse solicitacao = solicitacaoService.buscarPorId(id);
        model.addAttribute("solicitacao", solicitacao);
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
