package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.domain.model.TipoTokenCuenta;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_cuenta")
public class TokenCuentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_artesano", nullable = false)
    private ArtesanoEntity artesano;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoTokenCuenta tipo;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "usado", nullable = false)
    private boolean usado;

    public TokenCuentaEntity() {
    }

    public TokenCuentaEntity(Long id, ArtesanoEntity artesano, TipoTokenCuenta tipo, String tokenHash,
                              LocalDateTime creadoEn, LocalDateTime expiraEn, boolean usado) {
        this.id = id;
        this.artesano = artesano;
        this.tipo = tipo;
        this.tokenHash = tokenHash;
        this.creadoEn = creadoEn;
        this.expiraEn = expiraEn;
        this.usado = usado;
    }

    public Long getId() {
        return id;
    }

    public ArtesanoEntity getArtesano() {
        return artesano;
    }

    public TipoTokenCuenta getTipo() {
        return tipo;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public LocalDateTime getExpiraEn() {
        return expiraEn;
    }

    public boolean isUsado() {
        return usado;
    }
}