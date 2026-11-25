package com.quadrosolicitacoes.core.web.validation;

import org.springframework.validation.BindingResult;
import jakarta.servlet.http.HttpServletRequest;

public final class FormularioUtil {
    private FormularioUtil() { }

    public static void preservarValores(BindingResult resultado, HttpServletRequest request, String... campos) {
        if (resultado.getTarget() != null) return;
        for (String campo : campos) {
            if (!resultado.hasFieldErrors(campo)) {
                resultado.recordFieldValue(campo, String.class, request.getParameter(campo));
            }
        }
    }
}
