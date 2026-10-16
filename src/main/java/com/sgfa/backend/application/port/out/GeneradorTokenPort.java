package com.sgfa.backend.application.port.out;

public interface GeneradorTokenPort {

    String generar(String correoElectronico, String rol, Long id);
}