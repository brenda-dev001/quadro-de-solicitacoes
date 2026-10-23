package com.quadrosolicitacoes.solicitacao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.ManyToAny;
import com.quadrosolicitacoes.categoria.Categoria;
import com.quadrosolicitacoes.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class Solicitacao {

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated (EnumType.STRING)
    private StatusSolicitacao status;

    private LocalDateTime dataCriacao;

    @ManyToOne 
    @JoinColumn(name = "solicitante_id")
    private Usuario solicitante;

    @ManyToOne 
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnicoResponsavel;

    @ManyToOne 
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

}
