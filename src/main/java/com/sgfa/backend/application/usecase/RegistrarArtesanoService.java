package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoDuplicadoException;
import com.sgfa.backend.application.port.in.RegistrarArtesanoUseCase;
import com.sgfa.backend.application.port.out.AdministradorRepositoryPort;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.CifradoContrasenaPort;
import com.sgfa.backend.domain.model.Artesano;

public class RegistrarArtesanoService implements RegistrarArtesanoUseCase {

    private final ArtesanoRepositoryPort artesanoRepositoryPort;
    private final AdministradorRepositoryPort administradorRepositoryPort;
    private final CifradoContrasenaPort cifradoContrasenaPort;
    private final NotificadorCuenta notificadorCuenta;

    public RegistrarArtesanoService(ArtesanoRepositoryPort artesanoRepositoryPort,
                                     AdministradorRepositoryPort administradorRepositoryPort,
                                     CifradoContrasenaPort cifradoContrasenaPort,
                                     NotificadorCuenta notificadorCuenta) {
        this.artesanoRepositoryPort = artesanoRepositoryPort;
        this.administradorRepositoryPort = administradorRepositoryPort;
        this.cifradoContrasenaPort = cifradoContrasenaPort;
        this.notificadorCuenta = notificadorCuenta;
    }

    @Override
    public Artesano registrar(String nombre, String correoElectronico, String contrasena,
                               String cc, String telefono, String nombreEmprendimiento,
                               String descripcionCorta, boolean correoVerificado) {

        String correo = DatosCuenta.normalizarCorreo(correoElectronico);
        DatosCuenta.validarCorreo(correo);
        DatosCuenta.validarContrasena(contrasena);
        DatosCuenta.validarTexto("El nombre", nombre, 100, true);
        DatosCuenta.validarTexto("La cédula", cc, 20, true);
        DatosCuenta.validarTexto("El teléfono", telefono, 20, false);
        DatosCuenta.validarTexto("El nombre del emprendimiento", nombreEmprendimiento, 150, true);
        DatosCuenta.validarTexto("La descripción", descripcionCorta, 300, false);

        String cedula = cc.trim();

        if (artesanoRepositoryPort.existePorCorreo(correo)) {
            throw new DatoDuplicadoException("Ya existe un artesano registrado con ese correo electrónico.");
        }

        if (artesanoRepositoryPort.existePorCc(cedula)) {
            throw new DatoDuplicadoException("Ya existe un artesano registrado con esa cédula.");
        }

        if (administradorRepositoryPort.buscarPorCorreo(correo).isPresent()) {
            throw new DatoDuplicadoException("Ese correo electrónico no está disponible.");
        }

        Artesano nuevoArtesano = new Artesano(
                null,
                nombre.trim(),
                correo,
                cifradoContrasenaPort.cifrar(contrasena),
                cedula,
                telefono,
                nombreEmprendimiento.trim(),
                descripcionCorta,
                true,
                null,
                correoVerificado
        );

        Artesano guardado = artesanoRepositoryPort.guardar(nuevoArtesano);

        if (!correoVerificado) {
            notificadorCuenta.enviarVerificacion(guardado);
        }

        return guardado;
    }
}