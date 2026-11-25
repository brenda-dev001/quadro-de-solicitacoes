package com.quadrosolicitacoes.categoria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import com.quadrosolicitacoes.core.web.notification.NotificacaoSweetAlert2;
import com.quadrosolicitacoes.core.web.notification.TipoNotificacaoSweetAlert2;
import com.quadrosolicitacoes.core.web.pagination.PageWrapper;
import com.quadrosolicitacoes.categoria.dto.CategoriaCreateRequest;
import com.quadrosolicitacoes.categoria.dto.CategoriaResponse;
import com.quadrosolicitacoes.categoria.dto.CategoriaSearchRequest;
import com.quadrosolicitacoes.categoria.dto.CategoriaUpdateRequest;

@Slf4j
@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping("/nova")
    public String abrirFormulario(CategoriaCreateRequest request,
            HtmxRequest htmxRequest) {
        return htmxRequest.isHtmxRequest() ? "categorias/create :: formulario" : "categorias/create";
    }

    @PostMapping
    public String cadastrar(@Valid CategoriaCreateRequest form,
            BindingResult resultado,
            RedirectAttributes redirect,
            HtmxRequest htmxRequest) {
        if (resultado.hasErrors()) {
            log.info("A categoria recebida para cadastrar não é válida.");
            log.info("Erros encontrados:");
            for (FieldError erro : resultado.getFieldErrors()) {
                log.info("{}", erro);
            }
            for (ObjectError erro : resultado.getGlobalErrors()) {
                log.info("{}", erro);
            }
            return htmxRequest.isHtmxRequest() ? "categorias/create :: formulario" : "categorias/create";
        } else {
            categoriaService.salvar(form);

            redirect.addFlashAttribute("notificacaoSA2",
                    new NotificacaoSweetAlert2("Categoria salva com sucesso!",
                            TipoNotificacaoSweetAlert2.SUCCESS, 4000));

            return htmxRequest.isHtmxRequest() ? "redirect:htmx:location:/categorias" : "redirect:/categorias";
        }
    }

    @GetMapping
    public String pesquisar(CategoriaSearchRequest form, Model model,
            @PageableDefault(size = 7) @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request,
            HtmxRequest htmxRequest) {
        Page<CategoriaResponse> pagina = categoriaService.pesquisar(form, pageable);
        log.info("Categorias pesquisadas: {}", pagina.getContent());
        if (pagina.getContent().isEmpty()) {
            model.addAttribute("notificacaoSA2",
                    new NotificacaoSweetAlert2("A busca não encontrou categorias",
                            TipoNotificacaoSweetAlert2.INFO, 4000));
        }
        PageWrapper<CategoriaResponse> paginaWrapper = new PageWrapper<>(pagina, request);
        model.addAttribute("pagina", paginaWrapper);
        return htmxRequest.isHtmxRequest() ? "categorias/lista :: conteudo" : "categorias/lista";
    }

    @GetMapping("/{id}/editar")
    public String abrirEditar(@PathVariable("id") Long id, Model model,
            HtmxRequest htmxRequest) {
        CategoriaUpdateRequest request = categoriaService.buscarParaEdicao(id);
        model.addAttribute("categoriaUpdateRequest", request);
        model.addAttribute("id", id);
        return htmxRequest.isHtmxRequest() ? "categorias/edit :: formulario" : "categorias/edit";
    }

    @PutMapping("/{id}")
    public String editar(@PathVariable("id") Long id,
            @Valid CategoriaUpdateRequest request,
            BindingResult resultado,
            RedirectAttributes redirect,
            Model model,
            HtmxRequest htmxRequest) {
        model.addAttribute("id", id);
        if (resultado.hasErrors()) {
            log.info("A categoria recebida para editar não é válida.");
            log.info("Erros encontrados:");
            for (FieldError erro : resultado.getFieldErrors()) {
                log.info("{}", erro);
            }
            for (ObjectError erro : resultado.getGlobalErrors()) {
                log.info("{}", erro);
            }
            return htmxRequest.isHtmxRequest() ? "categorias/edit :: formulario" : "categorias/edit";
        } else {
            categoriaService.atualizar(id, request);
            redirect.addFlashAttribute("notificacaoSA2",
                    new NotificacaoSweetAlert2("Categoria alterada com sucesso!",
                            TipoNotificacaoSweetAlert2.SUCCESS, 4000));
            return htmxRequest.isHtmxRequest() ? "redirect:htmx:location:/categorias" : "redirect:/categorias";
        }
    }

    @DeleteMapping("/{id}")
    public String remover(@PathVariable("id") Long id,
            RedirectAttributes redirect,
            HtmxRequest htmxRequest) {
        categoriaService.remover(id);
        redirect.addFlashAttribute("notificacaoSA2",
                new NotificacaoSweetAlert2("Categoria removida com sucesso!",
                        TipoNotificacaoSweetAlert2.SUCCESS, 4000));
        return htmxRequest.isHtmxRequest() ? "redirect:htmx:location:/categorias" : "redirect:/categorias";
    }

}
