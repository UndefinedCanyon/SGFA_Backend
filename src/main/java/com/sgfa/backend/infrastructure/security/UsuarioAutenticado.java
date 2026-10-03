package com.sgfa.backend.infrastructure.security;

public class UsuarioAutenticado {

    private final Long id;
    private final String correoElectronico;
    private final String rol;

    public UsuarioAutenticado(Long id, String correoElectronico, String rol) {
        this.id = id;
        this.correoElectronico = correoElectronico;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public String getRol() {
        return rol;
    }
}