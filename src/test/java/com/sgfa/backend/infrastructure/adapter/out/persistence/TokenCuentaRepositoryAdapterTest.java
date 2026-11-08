package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.TipoTokenCuenta;
import com.sgfa.backend.domain.model.TokenCuenta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenCuentaRepositoryAdapterTest extends PersistenciaTestBase {

    private static final LocalDateTime CREADO = LocalDateTime.of(2026, 11, 8, 10, 0, 0);
    private static final String HASH_A = "a".repeat(64);
    private static final String HASH_B = "b".repeat(64);

    @Autowired
    private TokenCuentaRepositoryAdapter tokens;

    private Artesano ana;

    @BeforeEach
    void preparar() {
        ana = nuevoArtesano("ana@correo.com", "1010");
    }

    private TokenCuenta guardar(String hash, TipoTokenCuenta tipo, LocalDateTime creadoEn) {
        return tokens.guardar(new TokenCuenta(null, ana.getId(), tipo, hash, creadoEn, creadoEn.plusHours(1), false));
    }

    @Test
    @DisplayName("guarda el token y lo encuentra por su hash")
    void guardar_y_buscar_por_hash() {
        TokenCuenta guardado = guardar(HASH_A, TipoTokenCuenta.VERIFICACION, CREADO);
        sincronizar();

        assertNotNull(guardado.getId());
        TokenCuenta leido = tokens.buscarPorHash(HASH_A).orElseThrow();
        assertEquals(ana.getId(), leido.getIdArtesano());
        assertEquals(TipoTokenCuenta.VERIFICACION, leido.getTipo());
        assertEquals(CREADO, leido.getCreadoEn());
        assertEquals(CREADO.plusHours(1), leido.getExpiraEn());
        assertFalse(leido.isUsado());
        assertTrue(tokens.buscarPorHash(HASH_B).isEmpty());
    }

    @Test
    @DisplayName("no guarda un token de un artesano que no existe")
    void artesano_inexistente() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> tokens.guardar(new TokenCuenta(null, 9999L, TipoTokenCuenta.VERIFICACION,
                        HASH_A, CREADO, CREADO.plusHours(1), false)));

        assertEquals("El artesano indicado no existe.", error.getMessage());
    }

    @Test
    @DisplayName("devuelve la fecha del token más reciente del tipo pedido")
    void ultima_creacion() {
        guardar(HASH_A, TipoTokenCuenta.RECUPERACION, CREADO);
        guardar(HASH_B, TipoTokenCuenta.RECUPERACION, CREADO.plusMinutes(30));
        sincronizar();

        assertEquals(CREADO.plusMinutes(30),
                tokens.buscarUltimaCreacion(ana.getId(), TipoTokenCuenta.RECUPERACION).orElseThrow());
        assertTrue(tokens.buscarUltimaCreacion(ana.getId(), TipoTokenCuenta.VERIFICACION).isEmpty());
    }

    @Test
    @DisplayName("invalidar pendientes marca como usados solo los del tipo indicado")
    void invalidar_pendientes() {
        guardar(HASH_A, TipoTokenCuenta.RECUPERACION, CREADO);
        guardar(HASH_B, TipoTokenCuenta.VERIFICACION, CREADO);

        tokens.invalidarPendientes(ana.getId(), TipoTokenCuenta.RECUPERACION);
        sincronizar();

        assertTrue(tokens.buscarPorHash(HASH_A).orElseThrow().isUsado());
        assertFalse(tokens.buscarPorHash(HASH_B).orElseThrow().isUsado());
    }

    @Test
    @DisplayName("consumir funciona una sola vez: la segunda devuelve falso")
    void consumir_una_sola_vez() {
        TokenCuenta guardado = guardar(HASH_A, TipoTokenCuenta.VERIFICACION, CREADO);
        sincronizar();

        assertTrue(tokens.consumir(guardado.getId()));
        assertFalse(tokens.consumir(guardado.getId()));
        assertFalse(tokens.consumir(9999L));
        sincronizar();
        assertTrue(tokens.buscarPorHash(HASH_A).orElseThrow().isUsado());
    }
}