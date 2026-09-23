package com.sgfa.backend.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "artesano")
public class ArtesanoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_artesano")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "correo_electronico", nullable = false, unique = true)
    private String correoElectronico;

    @Column(name = "contrasena", nullable = false)
    private String contrasena;

    @Column(name = "cc", nullable = false, unique = true)
    private String cc;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "nombre_emprendimiento", nullable = false)
    private String nombreEmprendimiento;

    @Column(name = "descripcion_corta")
    private String descripcionCorta;

    public ArtesanoEntity() {
    }

    public ArtesanoEntity(Long id, String nombre, String correoElectronico, String contrasena,
                           String cc, String telefono, String nombreEmprendimiento, String descripcionCorta) {
        this.id = id;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
        this.contrasena = contrasena;
        this.cc = cc;
        this.telefono = telefono;
        this.nombreEmprendimiento = nombreEmprendimiento;
        this.descripcionCorta = descripcionCorta;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public String getContrasena() {
        return contrasena;
    }

    public String getCc() {
        return cc;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getNombreEmprendimiento() {
        return nombreEmprendimiento;
    }

    public String getDescripcionCorta() {
        return descripcionCorta;
    }
}