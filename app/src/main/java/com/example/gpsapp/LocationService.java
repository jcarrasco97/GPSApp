package com.example.gpsapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.gpsapp.model.Ubicacion;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocationService extends Service {

    // Constantes para el canal de notificaciones y logs
    private static final String CHANNEL_ID = "location_channel";
    private static final String TAG = "LocationService";

    // Cliente de ubicación y callback
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // Firestore para guardar la ubicación
    private FirebaseFirestore db;

    // ID del camión que se está rastreando
    private String currentCamionId;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "LocationService onCreate");

        // Creo el canal de notificaciones para Android 8+ (requerido para servicios en primer plano)
        createNotificationChannel();

        // Inicializo el cliente de ubicación y la base de datos
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        // Defino qué hacer cada vez que se reciba una nueva ubicación
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d(TAG, "LocationResult is null");
                    return;
                }

                // Obtengo la última ubicación registrada
                Location lastLocation = locationResult.getLastLocation();
                if (lastLocation != null && currentCamionId != null) {
                    Log.d(TAG, "Ubicación recibida: " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude() + " para Camión ID: " + currentCamionId);

                    // Creo un objeto Ubicacion y lo guardo en Firestore
                    Ubicacion nuevaUbicacion = new Ubicacion(
                            currentCamionId,
                            lastLocation.getLatitude(),
                            lastLocation.getLongitude()
                    );

                    db.collection("ubicaciones")
                            .add(nuevaUbicacion)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Ubicación guardada con ID: " + documentReference.getId());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al guardar ubicación: " + e.getMessage());
                            });
                } else if (currentCamionId == null) {
                    Log.w(TAG, "Ubicación recibida, pero currentCamionId es null. No se guarda.");
                }
            }
        };

        // Inicio el servicio en primer plano con una notificación inicial
        startForeground(1, createNotification("Iniciando servicio de ubicación..."));
        // No llamo aún a startLocationUpdates() hasta recibir el ID del camión en onStartCommand
    }

    // Método encargado de iniciar la escucha de actualizaciones de ubicación
    private void startLocationUpdates() {
        Log.d(TAG, "Iniciando actualizaciones de ubicación...");

        // Creo una solicitud de ubicación con alta precisión
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 10000) // Actualización cada 10 segundos
                .setMinUpdateIntervalMillis(5000)       // Pero al menos 5 segundos entre una y otra
                .setWaitForAccurateLocation(false)      // No espero la mejor ubicación posible
                .build();

        // Verifico que se hayan concedido los permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Inicio las actualizaciones si tengo permisos
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } else {
            // Si no tengo permisos, lo informo y detengo el servicio
            Log.e(TAG, "Permiso ACCESS_FINE_LOCATION no concedido. No se pueden iniciar actualizaciones.");
            stopSelf();
        }
    }

    // Creo una notificación para el servicio en primer plano
    private Notification createNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Servicio de Ubicación GPSApp")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_location)
                .setPriority(NotificationCompat.PRIORITY_LOW) // No molesta al usuario
                .setOngoing(true) // El usuario no puede cerrarla deslizando
                .build();
    }

    // Creo el canal de notificación (obligatorio en Android 8 o superior)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal de Ubicación en Segundo Plano",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Notificaciones para el seguimiento de ubicación de GPSApp");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    // Método que se llama cuando otro componente inicia el servicio
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LocationService onStartCommand");

        if (intent != null && intent.hasExtra("camion_id")) {
            // Recupero el ID del camión a rastrear desde el intent
            currentCamionId = intent.getStringExtra("camion_id");
            Log.d(TAG, "Camión ID recibido en onStartCommand: " + currentCamionId);

            // Actualizo la notificación para indicar qué camión se está rastreando
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.notify(1, createNotification("Rastreo activo para: " + currentCamionId));
            }

            // Ahora que tengo el ID, inicio las actualizaciones
            startLocationUpdates();
        } else {
            // Si no recibo el ID, detengo el servicio para evitar errores
            Log.w(TAG, "No se recibió camion_id en el Intent de LocationService. Deteniendo servicio.");
            stopSelf();
        }

        // START_STICKY hace que Android intente reiniciar el servicio si se cierra inesperadamente
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LocationService onDestroy");

        // Al detenerse, cancelo las actualizaciones de ubicación para evitar fugas de memoria
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d(TAG, "Actualizaciones de ubicación detenidas.");
        }

        // Elimino la notificación del sistema
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.cancel(1);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // No permito que otros componentes se enlacen con este servicio
        return null;
    }
}
