package com.example.gpsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // EditTexts para que el usuario escriba su correo y contraseña
    private EditText emailEditText, passwordEditText;

    // FirebaseAuth: clase que se encarga de manejar el sistema de autenticación
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carga el layout XML que contiene los elementos visuales del login
        setContentView(R.layout.activity_login);

        // Asigno los elementos visuales (campo de email y password) a variables
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        Button btnLogin = findViewById(R.id.btnLogin); // Botón de login

        // Inicializo FirebaseAuth para poder usarlo más adelante
        mAuth = FirebaseAuth.getInstance();

        // Configuro el botón para que al hacer clic se intente iniciar sesión
        btnLogin.setOnClickListener(v -> loginUser());
    }

    // Método que se llama cuando el usuario presiona el botón de login
    private void loginUser() {
        // Recupero el texto ingresado en los campos, eliminando espacios innecesarios
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Verifico si alguno de los campos está vacío
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return; // Si falta información, salgo del método
        }

        // Si los datos están completos, intento iniciar sesión en Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Inicio de sesión exitoso
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                    // Cambio de pantalla: voy a MainActivity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                    // Termino esta actividad para que no se pueda volver a ella con el botón "atrás"
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Si hubo un error (email o contraseña incorrectos, red, etc.)
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
