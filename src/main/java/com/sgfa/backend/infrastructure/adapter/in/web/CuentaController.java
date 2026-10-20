package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.port.in.ReenviarVerificacionUseCase;
import com.sgfa.backend.application.port.in.RestablecerContrasenaUseCase;
import com.sgfa.backend.application.port.in.SolicitarRecuperacionUseCase;
import com.sgfa.backend.application.port.in.VerificarCorreoUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class CuentaController {

    private static final String RESPUESTA_GENERICA =
            "Si el correo está registrado, te enviamos las instrucciones. Revisa tu bandeja de entrada.";

    private final VerificarCorreoUseCase verificarCorreoUseCase;
    private final ReenviarVerificacionUseCase reenviarVerificacionUseCase;
    private final SolicitarRecuperacionUseCase solicitarRecuperacionUseCase;
    private final RestablecerContrasenaUseCase restablecerContrasenaUseCase;

    public CuentaController(VerificarCorreoUseCase verificarCorreoUseCase,
                             ReenviarVerificacionUseCase reenviarVerificacionUseCase,
                             SolicitarRecuperacionUseCase solicitarRecuperacionUseCase,
                             RestablecerContrasenaUseCase restablecerContrasenaUseCase) {
        this.verificarCorreoUseCase = verificarCorreoUseCase;
        this.reenviarVerificacionUseCase = reenviarVerificacionUseCase;
        this.solicitarRecuperacionUseCase = solicitarRecuperacionUseCase;
        this.restablecerContrasenaUseCase = restablecerContrasenaUseCase;
    }

    @PostMapping("/verificar-correo")
    public Map<String, String> verificarCorreo(@RequestBody Map<String, String> body) {
        verificarCorreoUseCase.verificar(body.get("token"));
        return Map.of("mensaje", "Tu correo fue confirmado. Ya puedes iniciar sesión.");
    }

    @PostMapping("/reenviar-verificacion")
    public Map<String, String> reenviarVerificacion(@RequestBody Map<String, String> body) {
        reenviarVerificacionUseCase.reenviar(body.get("correoElectronico"));
        return Map.of("mensaje", RESPUESTA_GENERICA);
    }

    @PostMapping("/olvide-contrasena")
    public Map<String, String> olvideContrasena(@RequestBody Map<String, String> body) {
        solicitarRecuperacionUseCase.solicitar(body.get("correoElectronico"));
        return Map.of("mensaje", RESPUESTA_GENERICA);
    }

    @PostMapping("/restablecer-contrasena")
    public Map<String, String> restablecerContrasena(@RequestBody Map<String, String> body) {
        restablecerContrasenaUseCase.restablecer(body.get("token"), body.get("contrasena"));
        return Map.of("mensaje", "Tu contraseña fue actualizada. Ya puedes iniciar sesión.");
    }
}