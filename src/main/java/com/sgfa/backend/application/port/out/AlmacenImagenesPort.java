package com.sgfa.backend.application.port.out;

import java.util.Optional;

public interface AlmacenImagenesPort {

    /** Guarda el contenido con un nombre nuevo y aleatorio, y devuelve ese nombre. */
    String guardar(byte[] contenido, String extension);

    Optional<byte[]> cargar(String nombre);

    /** Borra el archivo si existe. Nunca lanza error: un archivo huérfano no debe tumbar la operación. */
    void eliminar(String nombre);
}