package com.sgfa.backend.application.port.in;

public interface RestablecerContrasenaUseCase {

    void restablecer(String token, String nuevaContrasena);
}