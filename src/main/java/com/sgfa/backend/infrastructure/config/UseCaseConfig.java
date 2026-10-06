package com.sgfa.backend.infrastructure.config;

import com.sgfa.backend.application.port.in.CambiarEstadoArtesanoUseCase;
import com.sgfa.backend.application.port.in.CambiarEstadoFeriaUseCase;
import com.sgfa.backend.application.port.in.CambiarEstadoEdicionFeriaUseCase;
import com.sgfa.backend.application.usecase.CambiarEstadoArtesanoService;
import com.sgfa.backend.application.usecase.CambiarEstadoFeriaService;
import com.sgfa.backend.application.usecase.CambiarEstadoEdicionFeriaService;
import com.sgfa.backend.application.port.in.SolicitarParticipacionUseCase;
import com.sgfa.backend.application.port.in.AprobarSolicitudUseCase;
import com.sgfa.backend.application.port.in.RechazarSolicitudUseCase;
import com.sgfa.backend.application.port.in.ConsultarSolicitudesUseCase;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.application.usecase.SolicitarParticipacionService;
import com.sgfa.backend.application.usecase.AprobarSolicitudService;
import com.sgfa.backend.application.usecase.RechazarSolicitudService;
import com.sgfa.backend.application.usecase.ConsultarSolicitudesService;
import com.sgfa.backend.application.port.in.ConsultarProductosUseCase;
import com.sgfa.backend.application.usecase.ConsultarProductosService;
import com.sgfa.backend.application.port.in.RegistrarProductoUseCase;
import com.sgfa.backend.application.port.in.ActualizarProductoUseCase;
import com.sgfa.backend.application.port.in.EliminarProductoUseCase;
import com.sgfa.backend.application.port.out.ProductoRepositoryPort;
import com.sgfa.backend.application.usecase.RegistrarProductoService;
import com.sgfa.backend.application.usecase.ActualizarProductoService;
import com.sgfa.backend.application.usecase.EliminarProductoService;
import com.sgfa.backend.infrastructure.security.JwtService;
import com.sgfa.backend.application.port.in.CrearLugarUseCase;
import com.sgfa.backend.application.port.in.ConsultarLugaresUseCase;
import com.sgfa.backend.application.port.in.RegistrarArtesanoUseCase;
import com.sgfa.backend.application.port.in.CrearFeriaUseCase;
import com.sgfa.backend.application.port.in.ConsultarFeriasUseCase;
import com.sgfa.backend.application.port.in.CrearEdicionFeriaUseCase;
import com.sgfa.backend.application.port.in.ConsultarEdicionesFeriaUseCase;
import com.sgfa.backend.application.port.in.IniciarSesionUseCase;
import com.sgfa.backend.application.port.out.LugarRepositoryPort;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.FeriaRepositoryPort;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.application.port.out.AdministradorRepositoryPort;
import com.sgfa.backend.application.usecase.CrearLugarService;
import com.sgfa.backend.application.usecase.ConsultarLugaresService;
import com.sgfa.backend.application.usecase.RegistrarArtesanoService;
import com.sgfa.backend.application.usecase.CrearFeriaService;
import com.sgfa.backend.application.usecase.ConsultarFeriasService;
import com.sgfa.backend.application.usecase.CrearEdicionFeriaService;
import com.sgfa.backend.application.usecase.ConsultarEdicionesFeriaService;
import com.sgfa.backend.application.usecase.IniciarSesionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CrearLugarUseCase crearLugarUseCase(LugarRepositoryPort lugarRepositoryPort) {
        return new CrearLugarService(lugarRepositoryPort);
    }

    @Bean
    public ConsultarLugaresUseCase consultarLugaresUseCase(LugarRepositoryPort lugarRepositoryPort) {
        return new ConsultarLugaresService(lugarRepositoryPort);
    }

      @Bean
    public RegistrarArtesanoUseCase registrarArtesanoUseCase(ArtesanoRepositoryPort artesanoRepositoryPort,AdministradorRepositoryPort administradorRepositoryPort,PasswordEncoder passwordEncoder) {
        return new RegistrarArtesanoService(artesanoRepositoryPort, administradorRepositoryPort, passwordEncoder);
    }

    @Bean
    public CrearFeriaUseCase crearFeriaUseCase(FeriaRepositoryPort feriaRepositoryPort) {
        return new CrearFeriaService(feriaRepositoryPort);
    }
    
    @Bean
    public IniciarSesionUseCase iniciarSesionUseCase(ArtesanoRepositoryPort artesanoRepositoryPort,
                                                    AdministradorRepositoryPort administradorRepositoryPort,
                                                    PasswordEncoder passwordEncoder,
                                                    JwtService jwtService) {
        return new IniciarSesionService(artesanoRepositoryPort, administradorRepositoryPort, passwordEncoder, jwtService);
    }

    @Bean
    public ConsultarProductosUseCase consultarProductosUseCase(ProductoRepositoryPort productoRepositoryPort) {
        return new ConsultarProductosService(productoRepositoryPort);
    }

    @Bean
    public ConsultarFeriasUseCase consultarFeriasUseCase(FeriaRepositoryPort feriaRepositoryPort) {
        return new ConsultarFeriasService(feriaRepositoryPort);
    }

    @Bean
    public CrearEdicionFeriaUseCase crearEdicionFeriaUseCase(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort) {
        return new CrearEdicionFeriaService(edicionFeriaRepositoryPort);
    }

    @Bean
    public ConsultarEdicionesFeriaUseCase consultarEdicionesFeriaUseCase(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort) {
        return new ConsultarEdicionesFeriaService(edicionFeriaRepositoryPort);
    }

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
    public SolicitarParticipacionUseCase solicitarParticipacionUseCase(InscripcionRepositoryPort inscripcionRepositoryPort) {
        return new SolicitarParticipacionService(inscripcionRepositoryPort);
    }

    @Bean
    public AprobarSolicitudUseCase aprobarSolicitudUseCase(InscripcionRepositoryPort inscripcionRepositoryPort) {
        return new AprobarSolicitudService(inscripcionRepositoryPort);
    }

    @Bean
    public RechazarSolicitudUseCase rechazarSolicitudUseCase(InscripcionRepositoryPort inscripcionRepositoryPort) {
        return new RechazarSolicitudService(inscripcionRepositoryPort);
    }

    @Bean
    public ConsultarSolicitudesUseCase consultarSolicitudesUseCase(InscripcionRepositoryPort inscripcionRepositoryPort) {
        return new ConsultarSolicitudesService(inscripcionRepositoryPort);
    }

    @Bean
    public CambiarEstadoArtesanoUseCase cambiarEstadoArtesanoUseCase(ArtesanoRepositoryPort artesanoRepositoryPort) {
        return new CambiarEstadoArtesanoService(artesanoRepositoryPort);
    }

    @Bean
    public CambiarEstadoFeriaUseCase cambiarEstadoFeriaUseCase(FeriaRepositoryPort feriaRepositoryPort) {
        return new CambiarEstadoFeriaService(feriaRepositoryPort);
    }

    @Bean
    public CambiarEstadoEdicionFeriaUseCase cambiarEstadoEdicionFeriaUseCase(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort) {
        return new CambiarEstadoEdicionFeriaService(edicionFeriaRepositoryPort);
    }
}