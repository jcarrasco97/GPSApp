package com.example.gpsapp.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Ubicacion {

    // El ID del camión al que pertenece esta ubicación
    private String camionId;

    // Coordenadas geográficas de la ubicación
    private double latitud;
    private double longitud;

    // Firestore se encargará de asignar la marca de tiempo automáticamente al guardar
    @ServerTimestamp
    private Date timestamp;

    // Constructor vacío necesario para que Firestore pueda deserializar los datos
    public Ubicacion() {
    }

    // Constructor que uso para crear una ubicación con ID de camión y coordenadas
    public Ubicacion(String camionId, double latitud, double longitud) {
        this.camionId = camionId;
        this.latitud = latitud;
        this.longitud = longitud;
        // No asigno el timestamp manualmente porque Firestore lo pone automáticamente
    }

    // Getters y setters para acceder a los campos desde otras clases

    public String getCamionId() {
        return camionId;
    }

    public void setCamionId(String camionId) {
        this.camionId = camionId;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
