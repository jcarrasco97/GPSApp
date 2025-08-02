// src/main/java/com/example/gpsapp/MapActivity.java
package com.example.gpsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;      // Referencia al mapa
    private String camionId;     // ID del camión recibido desde el intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map); // Asocia con el layout XML (debe contener <fragment> para el mapa)

        // Recibo el ID del camión desde MainActivity
        if (getIntent().hasExtra("camion_id")) {
            camionId = getIntent().getStringExtra("camion_id");
            Log.d("MapActivity", "Mostrando mapa para Camión ID: " + camionId);
            setTitle("Mapa de Camión: " + camionId); // Título dinámico (puedes reemplazar por nombre más adelante)
        } else {
            Log.w("MapActivity", "No se recibió camion_id en el Intent.");
            setTitle("Mapa General");
        }

        // Obtengo el fragmento del mapa y lo cargo de forma asíncrona
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // Espero a que el mapa esté listo
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Ubicación por defecto: Madrid
        LatLng madrid = new LatLng(40.4168, -3.7038);
        mMap.addMarker(new MarkerOptions().position(madrid).title("Ubicación por defecto"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 12f));

        // Mostrar botón de "mi ubicación", si se tiene permiso
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }
}
