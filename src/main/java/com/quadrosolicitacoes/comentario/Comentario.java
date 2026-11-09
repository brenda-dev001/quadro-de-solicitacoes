package com.quadrosolicitacoes.comentario;

import java.time.LocalDateTime;
import com.quadrosolicitacoes.solicitacao.Solicitacao;
import com.quadrosolicitacoes.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table (name = "tb_comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String conteudo;

    @Setter(AccessLevel.NONE)
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @ManyToOne
    @JoinColumn(name = "solicitacao_id")
    private Solicitacao solicitacao;

    public Comentario(String conteudo, Usuario autor,
            Solicitacao solicitacao) {
        this.conteudo = conteudo;
        this.autor = autor;
        this.solicitacao = solicitacao;
    }

    @PrePersist 
    public void prePersist(){
        if(dataCriacao == null){
            dataCriacao = LocalDateTime.now();
        }
    }
}
