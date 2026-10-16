package com.sgfa.backend.application.port.out;

public interface CifradoContrasenaPort {

    String cifrar(String contrasenaPlana);

    boolean coincide(String contrasenaPlana, String contrasenaCifrada);
}