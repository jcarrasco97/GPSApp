package com.example.gpsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Aquí establezco el layout que contiene el splash screen y el botón para entrar
        setContentView(R.layout.activity_splash);

        // Obtengo la referencia al botón que permite pasar al login
        Button btnEntrar = findViewById(R.id.btnEntrar);

        // Configuro el click listener para que al pulsar el botón
        // se inicie la actividad de Login y cierre esta (Splash) para no poder volver
        btnEntrar.setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish(); // Cierro Splash para que no quede en el back stack
        });
    }
}
