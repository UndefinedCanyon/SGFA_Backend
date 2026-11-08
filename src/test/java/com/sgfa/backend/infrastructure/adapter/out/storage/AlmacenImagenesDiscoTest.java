package com.sgfa.backend.infrastructure.adapter.out.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlmacenImagenesDiscoTest {

    @TempDir
    Path carpeta;

    @Test
    @DisplayName("crea la carpeta de imágenes si no existe")
    void crea_la_carpeta() {
        Path destino = carpeta.resolve("a").resolve("b");

        new AlmacenImagenesDisco(destino.toString());

        assertTrue(Files.isDirectory(destino));
    }

    @Test
    @DisplayName("falla con un error claro si la ruta es un archivo")
    void falla_si_la_ruta_es_un_archivo() throws IOException {
        Path archivo = Files.createFile(carpeta.resolve("ocupado"));

        assertThrows(UncheckedIOException.class, () -> new AlmacenImagenesDisco(archivo.toString()));
    }

    @Test
    @DisplayName("guarda con un nombre aleatorio válido y se puede volver a cargar")
    void guardar_y_cargar() {
        AlmacenImagenesDisco almacen = new AlmacenImagenesDisco(carpeta.toString());
        byte[] contenido = {1, 2, 3, 4};

        String nombre = almacen.guardar(contenido, "png");

        assertTrue(nombre.matches("^[a-f0-9]{32}\\.png$"));
        assertArrayEquals(contenido, almacen.cargar(nombre).orElseThrow());
    }

    @Test
    @DisplayName("dos imágenes iguales reciben nombres distintos")
    void nombres_distintos() {
        AlmacenImagenesDisco almacen = new AlmacenImagenesDisco(carpeta.toString());

        assertNotEquals(almacen.guardar(new byte[]{1}, "jpg"), almacen.guardar(new byte[]{1}, "jpg"));
    }

    @Test
    @DisplayName("no carga nombres nulos, con rutas relativas o con extensión no permitida")
    void no_carga_nombres_invalidos() throws IOException {
        AlmacenImagenesDisco almacen = new AlmacenImagenesDisco(carpeta.toString());
        Files.write(carpeta.resolve("secreto.txt"), new byte[]{9});

        assertTrue(almacen.cargar(null).isEmpty());
        assertTrue(almacen.cargar("../secreto.txt").isEmpty());
        assertTrue(almacen.cargar("secreto.txt").isEmpty());
        assertTrue(almacen.cargar("abc.png").isEmpty());
    }

    @Test
    @DisplayName("un nombre válido que no existe devuelve vacío")
    void nombre_valido_inexistente() {
        AlmacenImagenesDisco almacen = new AlmacenImagenesDisco(carpeta.toString());

        assertTrue(almacen.cargar("0".repeat(32) + ".webp").isEmpty());
    }

    @Test
    @DisplayName("si el nombre válido corresponde a una carpeta, no la trata como imagen")
    void una_carpeta_no_es_imagen() throws IOException {
        AlmacenImagenesDisco almacen = new AlmacenImagenesDisco(carpeta.toString());
        Files.createDirectory(carpeta.resolve("1".repeat(32) + ".jpg"));

        assertTrue(almacen.cargar("1".repeat(32) + ".jpg").isEmpty());
    }

    @Test
    @DisplayName("eliminar borra la imagen")
    void eliminar_borra_la_imagen() {
        AlmacenImagenesDisco almacen = new AlmacenImagenesDisco(carpeta.toString());
        String nombre = almacen.guardar(new byte[]{1, 2}, "webp");

        almacen.eliminar(nombre);

        assertFalse(Files.exists(carpeta.resolve(nombre)));
        assertTrue(almacen.cargar(nombre).isEmpty());
    }

    @Test
    @DisplayName("eliminar ignora nombres nulos, inválidos o inexistentes")
    void eliminar_ignora_lo_invalido() throws IOException {
        AlmacenImagenesDisco almacen = new AlmacenImagenesDisco(carpeta.toString());
        Path otro = Files.write(carpeta.resolve("otro.txt"), new byte[]{1});

        assertDoesNotThrow(() -> almacen.eliminar(null));
        assertDoesNotThrow(() -> almacen.eliminar("otro.txt"));
        assertDoesNotThrow(() -> almacen.eliminar("../otro.txt"));
        assertDoesNotThrow(() -> almacen.eliminar("2".repeat(32) + ".png"));
        assertTrue(Files.exists(otro));
    }
}