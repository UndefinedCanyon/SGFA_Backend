package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.Feria;
import com.sgfa.backend.domain.model.Lugar;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;

/**
 * Base de las pruebas de persistencia: levanta un MySQL 8 real en Docker (una sola vez para
 * todas las clases de prueba), crea las tablas con database/schema.sql y deshace cada prueba
 * al terminar. Así se prueban las consultas y las restricciones tal como funcionan en producción.
 */
@DataJpaTest(properties = {
        "spring.test.database.replace=none",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=file:database/schema.sql"
})
@Import({
        AdministradorRepositoryAdapter.class,
        ArtesanoRepositoryAdapter.class,
        EdicionFeriaRepositoryAdapter.class,
        FeriaRepositoryAdapter.class,
        InscripcionRepositoryAdapter.class,
        LugarRepositoryAdapter.class,
        ProductoRepositoryAdapter.class,
        TokenCuentaRepositoryAdapter.class
})
abstract class PersistenciaTestBase {

    private static final GenericContainer<?> MYSQL = new GenericContainer<>("mysql:8.0")
            .withEnv("MYSQL_ROOT_PASSWORD", "test")
            .withEnv("MYSQL_DATABASE", "sgfa_db")
            .withExposedPorts(3306)
            .waitingFor(Wait.forLogMessage(".*ready for connections.*\\n", 2))
            .withStartupTimeout(Duration.ofMinutes(3));

    static {
        MYSQL.start();
        esperarAQueMysqlResponda();
    }

    private static String urlJdbc() {
        return "jdbc:mysql://" + MYSQL.getHost() + ":" + MYSQL.getMappedPort(3306)
                + "/sgfa_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota";
    }

    /**
     * Docker acepta conexiones en el puerto aunque MySQL todavía esté arrancando, así que se
     * reintenta hasta que una conexión real funcione (máximo 2 minutos).
     */
    private static void esperarAQueMysqlResponda() {
        long limite = System.currentTimeMillis() + Duration.ofMinutes(2).toMillis();
        while (true) {
            try (Connection conexion = DriverManager.getConnection(urlJdbc(), "root", "test")) {
                if (conexion.isValid(2)) {
                    return;
                }
            } catch (SQLException e) {
                if (System.currentTimeMillis() > limite) {
                    throw new IllegalStateException("MySQL no respondió a tiempo en el contenedor.", e);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Se interrumpió la espera de MySQL.", e);
            }
        }
    }

    @DynamicPropertySource
    static void propiedadesDeLaBaseDeDatos(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", PersistenciaTestBase::urlJdbc);
        registro.add("spring.datasource.username", () -> "root");
        registro.add("spring.datasource.password", () -> "test");
        registro.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    protected EntityManager em;
    @Autowired
    protected ArtesanoRepositoryAdapter artesanos;
    @Autowired
    protected FeriaRepositoryAdapter ferias;
    @Autowired
    protected LugarRepositoryAdapter lugares;
    @Autowired
    protected EdicionFeriaRepositoryAdapter ediciones;

    /** Las ferias exigen un administrador existente (clave foránea). */
    @BeforeEach
    void insertarAdministrador() {
        em.createNativeQuery("INSERT INTO administrador (id_admin, nombre, correo_electronico, contrasena) "
                + "VALUES (1, 'Admin SGFA', 'admin@sgfa.com', 'hash-admin')").executeUpdate();
    }

    /** Envía los cambios a la base y vacía la caché para que la siguiente lectura sea real. */
    protected void sincronizar() {
        em.flush();
        em.clear();
    }

    protected Artesano nuevoArtesano(String correo, String cc) {
        return artesanos.guardar(new Artesano(null, "Ana Gomez", correo, "hash", cc, "300",
                "Ceramicas Ana", "Vasijas a mano", true, null, true));
    }

    protected Feria nuevaFeria(String nombre, boolean activo) {
        return ferias.guardar(new Feria(null, nombre, 1L, activo));
    }

    protected Lugar nuevoLugar() {
        return lugares.guardar(new Lugar(null, "Plaza Mayor", "Calle 1 # 2-3", 4.3369, -74.3636, true));
    }

    protected EdicionFeria nuevaEdicion(Long idFeria, Long idLugar, boolean activo, Integer cupo) {
        return ediciones.guardar(new EdicionFeria(null, idFeria, idLugar,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), activo, cupo));
    }
}