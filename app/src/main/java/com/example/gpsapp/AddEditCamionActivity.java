package com.example.gpsapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gpsapp.model.Camion;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddEditCamionActivity extends AppCompatActivity {

    private EditText editTextNombre, editTextMatricula;
    private Button btnGuardar;
    private FirebaseFirestore db;
    private Camion camionToEdit; // Variable para saber si estamos en modo edición

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_camion);

        // Referencio los elementos del layout
        editTextNombre = findViewById(R.id.editTextNombreCamion);
        editTextMatricula = findViewById(R.id.editTextMatriculaCamion);
        btnGuardar = findViewById(R.id.btnGuardarCamion);

        // Inicializo la base de datos de Firestore
        db = FirebaseFirestore.getInstance();

        // Verifico si la actividad fue lanzada para editar un camión existente
        if (getIntent().hasExtra("camion_id")) {
            setTitle("Editar Camión");
            String camionId = getIntent().getStringExtra("camion_id");

            // Obtengo los datos del camión desde Firestore usando su ID
            db.collection("camiones").document(camionId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            camionToEdit = documentSnapshot.toObject(Camion.class);
                            if (camionToEdit != null) {
                                camionToEdit.setId(documentSnapshot.getId()); // Asigno el ID del documento Firestore
                                // Cargo los datos actuales en los campos de texto
                                editTextNombre.setText(camionToEdit.getNombre());
                                editTextMatricula.setText(camionToEdit.getMatricula());
                            }
                        } else {
                            Toast.makeText(this, "Camión no encontrado.", Toast.LENGTH_SHORT).show();
                            finish(); // Si no existe el camión, cierro la actividad
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al cargar camión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish(); // Cierro también en caso de error
                    });
        } else {
            // Si no hay ID, es un nuevo camión
            setTitle("Añadir Nuevo Camión");
        }

        // Configuro el botón para guardar (ya sea añadir o actualizar)
        btnGuardar.setOnClickListener(v -> guardarCamion());
    }

    // Método para guardar o actualizar un camión en Firestore
    private void guardarCamion() {
        String nombre = editTextNombre.getText().toString().trim();
        String matricula = editTextMatricula.getText().toString().trim();

        // Verifico que los campos no estén vacíos
        if (nombre.isEmpty() || matricula.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (camionToEdit == null) {
            // Si no hay camión a editar, se trata de uno nuevo
            Camion nuevoCamion = new Camion(null, nombre, matricula, false); // Lo creo como inactivo por defecto
            db.collection("camiones")
                    .add(nuevoCamion)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Camión añadido correctamente.", Toast.LENGTH_SHORT).show();
                        finish(); // Cierro la actividad al terminar
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al añadir camión: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            // Si ya tengo un camión cargado, actualizo sus datos
            camionToEdit.setNombre(nombre);
            camionToEdit.setMatricula(matricula);
            db.collection("camiones").document(camionToEdit.getId())
                    .set(camionToEdit) // Sobrescribo el documento en Firestore
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Camión actualizado correctamente.", Toast.LENGTH_SHORT).show();
                        finish(); // También cierro cuando termino
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al actualizar camión: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}
