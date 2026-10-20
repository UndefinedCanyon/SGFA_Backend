package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.domain.model.TipoTokenCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface TokenCuentaJpaRepository extends JpaRepository<TokenCuentaEntity, Long> {

    Optional<TokenCuentaEntity> findByTokenHash(String tokenHash);

    Optional<TokenCuentaEntity> findFirstByArtesanoIdAndTipoOrderByCreadoEnDesc(Long idArtesano, TipoTokenCuenta tipo);

    @Modifying
    @Transactional
    @Query("update TokenCuentaEntity t set t.usado = true "
            + "where t.artesano.id = :idArtesano and t.tipo = :tipo and t.usado = false")
    int invalidarPendientes(@Param("idArtesano") Long idArtesano, @Param("tipo") TipoTokenCuenta tipo);

    @Modifying
    @Transactional
    @Query("update TokenCuentaEntity t set t.usado = true where t.id = :id and t.usado = false")
    int consumir(@Param("id") Long id);
}