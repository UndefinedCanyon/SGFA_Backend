package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.application.port.out.TokenCuentaRepositoryPort;
import com.sgfa.backend.domain.model.TipoTokenCuenta;
import com.sgfa.backend.domain.model.TokenCuenta;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class TokenCuentaRepositoryAdapter implements TokenCuentaRepositoryPort {

    private final TokenCuentaJpaRepository tokenCuentaJpaRepository;
    private final ArtesanoJpaRepository artesanoJpaRepository;

    public TokenCuentaRepositoryAdapter(TokenCuentaJpaRepository tokenCuentaJpaRepository,
                                         ArtesanoJpaRepository artesanoJpaRepository) {
        this.tokenCuentaJpaRepository = tokenCuentaJpaRepository;
        this.artesanoJpaRepository = artesanoJpaRepository;
    }

    @Override
    public TokenCuenta guardar(TokenCuenta token) {
        ArtesanoEntity artesano = artesanoJpaRepository.findById(token.getIdArtesano())
                .orElseThrow(() -> new IllegalArgumentException("El artesano indicado no existe."));

        TokenCuentaEntity entity = new TokenCuentaEntity(
                token.getId(),
                artesano,
                token.getTipo(),
                token.getTokenHash(),
                token.getCreadoEn(),
                token.getExpiraEn(),
                token.isUsado()
        );

        return aDominio(tokenCuentaJpaRepository.save(entity));
    }

    @Override
    public Optional<TokenCuenta> buscarPorHash(String tokenHash) {
        return tokenCuentaJpaRepository.findByTokenHash(tokenHash).map(this::aDominio);
    }

    @Override
    public Optional<LocalDateTime> buscarUltimaCreacion(Long idArtesano, TipoTokenCuenta tipo) {
        return tokenCuentaJpaRepository
                .findFirstByArtesanoIdAndTipoOrderByCreadoEnDesc(idArtesano, tipo)
                .map(TokenCuentaEntity::getCreadoEn);
    }

    @Override
    public void invalidarPendientes(Long idArtesano, TipoTokenCuenta tipo) {
        tokenCuentaJpaRepository.invalidarPendientes(idArtesano, tipo);
    }

    @Override
    public boolean consumir(Long idToken) {
        return tokenCuentaJpaRepository.consumir(idToken) == 1;
    }

    private TokenCuenta aDominio(TokenCuentaEntity entity) {
        return new TokenCuenta(
                entity.getId(),
                entity.getArtesano().getId(),
                entity.getTipo(),
                entity.getTokenHash(),
                entity.getCreadoEn(),
                entity.getExpiraEn(),
                entity.isUsado()
        );
    }
}