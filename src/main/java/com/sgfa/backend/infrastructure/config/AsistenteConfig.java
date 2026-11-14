package com.sgfa.backend.infrastructure.config;

import com.sgfa.backend.application.port.in.AnalizarSolicitudesUseCase;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.AsistenteIAPort;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.application.port.out.FeriaRepositoryPort;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.application.port.out.LugarRepositoryPort;
import com.sgfa.backend.application.port.out.ProductoRepositoryPort;
import com.sgfa.backend.application.usecase.AnalizarSolicitudesService;
import com.sgfa.backend.infrastructure.adapter.out.ia.AsistenteIAFalsoAdapter;
import com.sgfa.backend.infrastructure.adapter.out.ia.AsistenteIALimitado;
import com.sgfa.backend.infrastructure.adapter.out.ia.GeminiAsistenteAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.Locale;

/**
 * Elige qué asistente de IA se usa:
 * <ul>
 *   <li>{@code asistente.proveedor=auto} (por defecto): Gemini si hay {@code GEMINI_API_KEY}; si no, la demostración.</li>
 *   <li>{@code gemini}: exige la clave y falla al arrancar si no está.</li>
 *   <li>{@code falso}: siempre la demostración (pruebas end to end, desarrollo sin internet).</li>
 * </ul>
 */
@Configuration
public class AsistenteConfig {

    static final String URL_GEMINI = "https://generativelanguage.googleapis.com/v1beta";

    private static final Logger log = LoggerFactory.getLogger(AsistenteConfig.class);

    @Bean
    public AsistenteIAPort asistenteIAPort(@Value("${asistente.proveedor:auto}") String proveedor,
                                           @Value("${GEMINI_API_KEY:}") String claveGemini,
                                           @Value("${asistente.gemini.modelo:gemini-2.5-flash}") String modelo,
                                           @Value("${asistente.limite-por-minuto:6}") int limitePorMinuto) {
        return crearAsistente(proveedor, claveGemini, modelo, limitePorMinuto, URL_GEMINI);
    }

    @Bean
    public AnalizarSolicitudesUseCase analizarSolicitudesUseCase(InscripcionRepositoryPort inscripcionRepositoryPort,
                                                                 EdicionFeriaRepositoryPort edicionFeriaRepositoryPort,
                                                                 FeriaRepositoryPort feriaRepositoryPort,
                                                                 LugarRepositoryPort lugarRepositoryPort,
                                                                 ArtesanoRepositoryPort artesanoRepositoryPort,
                                                                 ProductoRepositoryPort productoRepositoryPort,
                                                                 AsistenteIAPort asistenteIAPort) {
        return new AnalizarSolicitudesService(inscripcionRepositoryPort, edicionFeriaRepositoryPort,
                feriaRepositoryPort, lugarRepositoryPort, artesanoRepositoryPort, productoRepositoryPort,
                asistenteIAPort);
    }

    static AsistenteIAPort crearAsistente(String proveedor, String claveGemini, String modelo,
                                          int limitePorMinuto, String urlBase) {
        String elegido = proveedor == null ? "auto" : proveedor.trim().toLowerCase(Locale.ROOT);
        boolean hayClave = claveGemini != null && !claveGemini.isBlank();

        switch (elegido) {
            case "falso":
                log.info("Asistente de IA: modo demostración (sin IA).");
                return new AsistenteIAFalsoAdapter();
            case "gemini":
                if (!hayClave) {
                    throw new IllegalStateException(
                            "asistente.proveedor=gemini requiere definir GEMINI_API_KEY.");
                }
                return crearGemini(claveGemini, modelo, limitePorMinuto, urlBase);
            case "auto":
                if (hayClave) {
                    return crearGemini(claveGemini, modelo, limitePorMinuto, urlBase);
                }
                log.warn("GEMINI_API_KEY no está definida: el asistente funciona en modo demostración (sin IA).");
                return new AsistenteIAFalsoAdapter();
            default:
                throw new IllegalStateException(
                        "asistente.proveedor debe ser auto, gemini o falso (se recibió: " + proveedor + ").");
        }
    }

    private static AsistenteIAPort crearGemini(String clave, String modelo, int limitePorMinuto, String urlBase) {
        HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(cliente);
        fabrica.setReadTimeout(Duration.ofSeconds(30));

        RestClient restClient = RestClient.builder()
                .baseUrl(urlBase)
                .defaultHeader("x-goog-api-key", clave.trim())
                .requestFactory(fabrica)
                .build();

        log.info("Asistente de IA: Gemini (modelo {}), máximo {} análisis por minuto.", modelo, limitePorMinuto);
        return new AsistenteIALimitado(new GeminiAsistenteAdapter(restClient, modelo), limitePorMinuto, Clock.systemUTC());
    }
}
