package com.quadrosolicitacoes.solicitacao;

import java.time.LocalDateTime;
import com.quadrosolicitacoes.categoria.Categoria;
import com.quadrosolicitacoes.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@Entity 
@Table (name = "tb_solicitacao")
public class Solicitacao {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated (EnumType.STRING)
    private StatusSolicitacao status;

    @Setter(AccessLevel.NONE)
    @Column(nullable = false, updatable = false)
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

    @PrePersist 
    public void prePersist(){
        if(dataCriacao == null){
            dataCriacao = LocalDateTime.now();
        }
        if(status == null){
            status = StatusSolicitacao.ABERTA;
        }
    }

}
