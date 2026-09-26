package com.sgfa.backend.infrastructure.config;

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
}