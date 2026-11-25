package com.quadrosolicitacoes.core.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;

@Controller
public class LayoutController {
    @GetMapping({"/", "/index.html"})
    public String index(HtmxRequest htmxRequest) {
        return htmxRequest.isHtmxRequest() ? "index :: conteudo" : "index";
    }

    @GetMapping("/layout/menu-atualizado")
    public String obterMenuAtualizado() {
        return "layout/fragments/menu :: menu";
    }
}
