package com.sgfa.backend.infrastructure.security;

import com.sgfa.backend.application.port.out.GeneradorTokenPort;
import org.springframework.stereotype.Component;

@Component
public class JwtGeneradorTokenAdapter implements GeneradorTokenPort {

    private final JwtService jwtService;

    public JwtGeneradorTokenAdapter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String generar(String correoElectronico, String rol, Long id) {
        return jwtService.generarToken(correoElectronico, rol, id);
    }
}