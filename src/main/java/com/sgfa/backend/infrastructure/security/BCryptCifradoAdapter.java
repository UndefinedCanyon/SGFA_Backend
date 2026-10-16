package com.sgfa.backend.infrastructure.security;

import com.sgfa.backend.application.port.out.CifradoContrasenaPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptCifradoAdapter implements CifradoContrasenaPort {

    private final PasswordEncoder passwordEncoder;

    public BCryptCifradoAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String cifrar(String contrasenaPlana) {
        return passwordEncoder.encode(contrasenaPlana);
    }

    @Override
    public boolean coincide(String contrasenaPlana, String contrasenaCifrada) {
        return passwordEncoder.matches(contrasenaPlana, contrasenaCifrada);
    }
}