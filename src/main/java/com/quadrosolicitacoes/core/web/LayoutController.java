package com.quadrosolicitacoes.core.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.quadrosolicitacoes.solicitacao.SolicitacaoRepository;
import com.quadrosolicitacoes.solicitacao.StatusSolicitacao;
import org.springframework.web.bind.annotation.GetMapping;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;

@Controller
public class LayoutController {
    private final SolicitacaoRepository solicitacaoRepository;

    public LayoutController(SolicitacaoRepository solicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @GetMapping({"/", "/index.html"})
    public String index(HtmxRequest htmxRequest, Model model) {
        model.addAttribute("abertas", solicitacaoRepository.countByStatus(StatusSolicitacao.ABERTA));
        model.addAttribute("andamento", solicitacaoRepository.countByStatus(StatusSolicitacao.EM_ANDAMENTO));
        model.addAttribute("concluidas", solicitacaoRepository.countByStatus(StatusSolicitacao.CONCLUIDA));
        return htmxRequest.isHtmxRequest() ? "index :: conteudo" : "index";
    }

    @GetMapping("/layout/menu-atualizado")
    public String obterMenuAtualizado() {
        return "layout/fragments/menu :: menu";
    }
}
