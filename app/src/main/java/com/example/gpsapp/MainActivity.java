package com.example.gpsapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpsapp.adapter.CamionAdapter;
import com.example.gpsapp.model.Camion;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CamionAdapter.OnCamionInteractionListener {

    // Constante para identificar la solicitud de permisos
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    // Elementos de la interfaz y componentes de datos
    private RecyclerView recyclerViewCamiones;
    private CamionAdapter camionAdapter;
    private FloatingActionButton fabAddCamion;
    private FirebaseFirestore db;
    private ListenerRegistration camionesListenerRegistration; // Para escuchar los cambios en Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Carga el diseño XML de la actividad

        // Configura la Toolbar superior como barra de navegación
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Referencias a los elementos de la interfaz
        recyclerViewCamiones = findViewById(R.id.recyclerViewCamiones);
        fabAddCamion = findViewById(R.id.fabAddCamion);
        db = FirebaseFirestore.getInstance(); // Inicializa Firestore

        // Configuración del RecyclerView para mostrar los camiones
        recyclerViewCamiones.setLayoutManager(new LinearLayoutManager(this));
        camionAdapter = new CamionAdapter(this); // Usa esta actividad como listener
        recyclerViewCamiones.setAdapter(camionAdapter);

        // Acción al presionar el botón flotante: abrir pantalla para agregar un camión
        fabAddCamion.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditCamionActivity.class);
            startActivity(intent);
        });

        // Inicia la escucha de cambios en la colección "camiones"
        listenForCamiones();

        // Solicita permisos de ubicación al abrir la app
        checkLocationPermissions();
    }

    private void listenForCamiones() {
        // Escucha cambios en Firestore, en la colección "camiones"
        camionesListenerRegistration = db.collection("camiones")
                .orderBy("nombre", Query.Direction.ASCENDING) // Ordenar por nombre
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("MainActivity", "Fallo al escuchar cambios", e);
                        return;
                    }

                    if (snapshots != null) {
                        List<Camion> camiones = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Camion camion = doc.toObject(Camion.class);
                            if (camion != null) {
                                camion.setId(doc.getId()); // Asigna el ID del documento
                                camiones.add(camion);
                            }
                        }
                        camionAdapter.setCamiones(camiones); // Actualiza la lista en el adaptador
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cuando se destruye la actividad, dejamos de escuchar cambios
        if (camionesListenerRegistration != null) {
            camionesListenerRegistration.remove();
        }
    }

    // --- Métodos de la interfaz OnCamionInteractionListener ---

    // Editar un camión
    @Override
    public void onEditCamion(Camion camion) {
        Intent intent = new Intent(MainActivity.this, AddEditCamionActivity.class);
        intent.putExtra("camion_id", camion.getId()); // Pasa el ID al formulario
        startActivity(intent);
    }

    // Eliminar un camión con confirmación
    @Override
    public void onDeleteCamion(Camion camion) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Camión")
                .setMessage("¿Estás seguro de que quieres eliminar el camión " + camion.getNombre() + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    db.collection("camiones").document(camion.getId())
                            .delete()
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(MainActivity.this, "Camión eliminado.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(MainActivity.this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Mostrar ubicación del camión en el mapa
    @Override
    public void onShowGeolocation(Camion camion) {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        intent.putExtra("camion_id", camion.getId());
        startActivity(intent);
    }

    // Activar o desactivar seguimiento de un camión
    @Override
    public void onToggleCamionActivo(Camion camion, boolean isChecked) {
        db.collection("camiones").document(camion.getId())
                .update("activo", isChecked)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this,
                            "Camión " + camion.getNombre() + (isChecked ? " activado." : " desactivado."),
                            Toast.LENGTH_SHORT).show();

                    if (isChecked) {
                        startLocationService(camion.getId()); // Inicia servicio si se activa
                    }
                    // Si se desactiva, podrías detener el servicio, pero eso depende de tu lógica adicional.
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this,
                            "Error al cambiar estado del camión: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    // Revertir cambio en la UI si falla la actualización
                    camion.setActivo(!isChecked);
                    camionAdapter.notifyDataSetChanged();
                });
    }

    // Inicia el servicio de ubicación con el ID del camión
    private void startLocationService(String camionId) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            serviceIntent.putExtra("camion_id", camionId); // Envía el ID del camión
            ContextCompat.startForegroundService(this, serviceIntent);
            Toast.makeText(this, "Servicio de ubicación iniciado para " + camionId, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permiso de ubicación no concedido.", Toast.LENGTH_LONG).show();
        }
    }

    // --- Verificación y solicitud de permisos de ubicación ---
    private void checkLocationPermissions() {
        // Para versiones modernas, se piden dos permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }
        }
    }

    // Resultado del diálogo de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de ubicación concedido.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado. Algunas funciones no estarán disponibles.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
