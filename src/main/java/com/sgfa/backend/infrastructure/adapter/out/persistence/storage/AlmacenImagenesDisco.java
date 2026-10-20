package com.sgfa.backend.infrastructure.adapter.out.storage;

import com.sgfa.backend.application.port.out.AlmacenImagenesPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class AlmacenImagenesDisco implements AlmacenImagenesPort {

    private static final Logger log = LoggerFactory.getLogger(AlmacenImagenesDisco.class);

    // 32 caracteres hexadecimales aleatorios y una de las tres extensiones permitidas.
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[a-f0-9]{32}\\.(jpg|png|webp)$");

    private final Path directorio;

    public AlmacenImagenesDisco(@Value("${IMAGENES_DIR:./uploads}") String ruta) {
        this.directorio = Paths.get(ruta).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directorio);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta de imágenes: " + directorio, e);
        }
    }

    @Override
    public String guardar(byte[] contenido, String extension) {
        String nombre = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        try {
            Files.write(directorio.resolve(nombre), contenido, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen.", e);
        }
        return nombre;
    }

    @Override
    public Optional<byte[]> cargar(String nombre) {
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) {
            return Optional.empty();
        }

        Path archivo = directorio.resolve(nombre);
        try {
            return Files.isRegularFile(archivo)
                    ? Optional.of(Files.readAllBytes(archivo))
                    : Optional.empty();
        } catch (IOException e) {
            log.warn("No se pudo leer la imagen {}", nombre, e);
            return Optional.empty();
        }
    }

    @Override
    public void eliminar(String nombre) {
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) {
            return;
        }

        try {
            Files.deleteIfExists(directorio.resolve(nombre));
        } catch (IOException e) {
            log.warn("No se pudo borrar la imagen {}", nombre, e);
        }
    }
}