package com.example.gpsapp.model;

import com.google.firebase.firestore.Exclude;

public class Camion {
    private String id; // Será el ID del documento en Firestore
    private String nombre;
    private String matricula;
    private boolean activo; // Para indicar si está siendo rastreado o no

    public Camion() {
        // Constructor vacío requerido por Firestore
    }

    public Camion(String id, String nombre, String matricula, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.matricula = matricula;
        this.activo = activo;
    }

    @Exclude // Para que Firestore no intente mapear este campo al guardar
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}