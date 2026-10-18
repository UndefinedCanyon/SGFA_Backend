package com.sgfa.backend.infrastructure.config;

import com.sgfa.backend.application.port.in.*;
import com.sgfa.backend.application.port.out.*;
import com.sgfa.backend.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    // ---------- Lugares ----------
    @Bean
    public CrearLugarUseCase crearLugarUseCase(LugarRepositoryPort lugarRepositoryPort) {
        return new CrearLugarService(lugarRepositoryPort);
    }

    @Bean
    public ConsultarLugaresUseCase consultarLugaresUseCase(LugarRepositoryPort lugarRepositoryPort) {
        return new ConsultarLugaresService(lugarRepositoryPort);
    }

    @Bean
    public ActualizarLugarUseCase actualizarLugarUseCase(LugarRepositoryPort lugarRepositoryPort) {
        return new ActualizarLugarService(lugarRepositoryPort);
    }

    // ---------- Artesanos y sesión ----------
    @Bean
    public RegistrarArtesanoUseCase registrarArtesanoUseCase(ArtesanoRepositoryPort artesanoRepositoryPort,
                                                               AdministradorRepositoryPort administradorRepositoryPort,
                                                               CifradoContrasenaPort cifradoContrasenaPort) {
        return new RegistrarArtesanoService(artesanoRepositoryPort, administradorRepositoryPort, cifradoContrasenaPort);
    }

    @Bean
    public IniciarSesionUseCase iniciarSesionUseCase(ArtesanoRepositoryPort artesanoRepositoryPort,
                                                       AdministradorRepositoryPort administradorRepositoryPort,
                                                       CifradoContrasenaPort cifradoContrasenaPort,
                                                       GeneradorTokenPort generadorTokenPort) {
        return new IniciarSesionService(artesanoRepositoryPort, administradorRepositoryPort,
                cifradoContrasenaPort, generadorTokenPort);
    }

    @Bean
    public ConsultarArtesanosUseCase consultarArtesanosUseCase(ArtesanoRepositoryPort artesanoRepositoryPort) {
        return new ConsultarArtesanosService(artesanoRepositoryPort);
    }

    @Bean
    public ActualizarPerfilArtesanoUseCase actualizarPerfilArtesanoUseCase(ArtesanoRepositoryPort artesanoRepositoryPort) {
        return new ActualizarPerfilArtesanoService(artesanoRepositoryPort);
    }

    @Bean
    public CambiarEstadoArtesanoUseCase cambiarEstadoArtesanoUseCase(ArtesanoRepositoryPort artesanoRepositoryPort) {
        return new CambiarEstadoArtesanoService(artesanoRepositoryPort);
    }

    // ---------- Ferias ----------
    @Bean
    public CrearFeriaUseCase crearFeriaUseCase(FeriaRepositoryPort feriaRepositoryPort) {
        return new CrearFeriaService(feriaRepositoryPort);
    }

    @Bean
    public ConsultarFeriasUseCase consultarFeriasUseCase(FeriaRepositoryPort feriaRepositoryPort) {
        return new ConsultarFeriasService(feriaRepositoryPort);
    }

    @Bean
    public ActualizarFeriaUseCase actualizarFeriaUseCase(FeriaRepositoryPort feriaRepositoryPort) {
        return new ActualizarFeriaService(feriaRepositoryPort);
    }

    @Bean
    public CambiarEstadoFeriaUseCase cambiarEstadoFeriaUseCase(FeriaRepositoryPort feriaRepositoryPort) {
        return new CambiarEstadoFeriaService(feriaRepositoryPort);
    }

    // ---------- Ediciones ----------
    @Bean
    public CrearEdicionFeriaUseCase crearEdicionFeriaUseCase(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort) {
        return new CrearEdicionFeriaService(edicionFeriaRepositoryPort);
    }

    @Bean
    public ConsultarEdicionesFeriaUseCase consultarEdicionesFeriaUseCase(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort) {
        return new ConsultarEdicionesFeriaService(edicionFeriaRepositoryPort);
    }

    @Bean
    public CambiarEstadoEdicionFeriaUseCase cambiarEstadoEdicionFeriaUseCase(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort) {
        return new CambiarEstadoEdicionFeriaService(edicionFeriaRepositoryPort);
    }

    @Bean
    public ConfigurarCupoEdicionUseCase configurarCupoEdicionUseCase(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort,
                                                                       InscripcionRepositoryPort inscripcionRepositoryPort) {
        return new ConfigurarCupoEdicionService(edicionFeriaRepositoryPort, inscripcionRepositoryPort);
    }

    @Bean
    public ConsultarOcupacionEdicionUseCase consultarOcupacionEdicionUseCase(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort,
                                                                               InscripcionRepositoryPort inscripcionRepositoryPort) {
        return new ConsultarOcupacionEdicionService(edicionFeriaRepositoryPort, inscripcionRepositoryPort);
    }

    @Bean
    public ConsultarParticipantesEdicionUseCase consultarParticipantesEdicionUseCase(
            InscripcionRepositoryPort inscripcionRepositoryPort,
            ArtesanoRepositoryPort artesanoRepositoryPort,
            ProductoRepositoryPort productoRepositoryPort,
            EdicionFeriaRepositoryPort edicionFeriaRepositoryPort) {
        return new ConsultarParticipantesEdicionService(
                inscripcionRepositoryPort, artesanoRepositoryPort,
                productoRepositoryPort, edicionFeriaRepositoryPort);
    }

    // ---------- Productos ----------
    @Bean
    public RegistrarProductoUseCase registrarProductoUseCase(ProductoRepositoryPort productoRepositoryPort) {
        return new RegistrarProductoService(productoRepositoryPort);
    }

    @Bean
    public ActualizarProductoUseCase actualizarProductoUseCase(ProductoRepositoryPort productoRepositoryPort) {
        return new ActualizarProductoService(productoRepositoryPort);
    }

    @Bean
    public EliminarProductoUseCase eliminarProductoUseCase(ProductoRepositoryPort productoRepositoryPort) {
        return new EliminarProductoService(productoRepositoryPort);
    }

    @Bean
    public ConsultarProductosUseCase consultarProductosUseCase(ProductoRepositoryPort productoRepositoryPort) {
        return new ConsultarProductosService(productoRepositoryPort);
    }

    // ---------- Inscripciones ----------
    @Bean
    public SolicitarParticipacionUseCase solicitarParticipacionUseCase(InscripcionRepositoryPort inscripcionRepositoryPort) {
        return new SolicitarParticipacionService(inscripcionRepositoryPort);
    }

    @Bean
    public AprobarSolicitudUseCase aprobarSolicitudUseCase(InscripcionRepositoryPort inscripcionRepositoryPort,
                                                             EdicionFeriaRepositoryPort edicionFeriaRepositoryPort) {
        return new AprobarSolicitudService(inscripcionRepositoryPort, edicionFeriaRepositoryPort);
    }

    @Bean
    public RechazarSolicitudUseCase rechazarSolicitudUseCase(InscripcionRepositoryPort inscripcionRepositoryPort) {
        return new RechazarSolicitudService(inscripcionRepositoryPort);
    }

    @Bean
    public ConsultarSolicitudesUseCase consultarSolicitudesUseCase(InscripcionRepositoryPort inscripcionRepositoryPort) {
        return new ConsultarSolicitudesService(inscripcionRepositoryPort);
    }
}