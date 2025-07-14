package com.example.pmiexa2p;

//Grupo #1
//Francisco Roberto Martinez Leiva , 201910010453
//Gustavo Adolfo Siu Marquez , 200710510076
//Dennis Mauricio Amaya Reyes , 202110030035
//Jonathan Rodrigo Paz ,  202310010374


import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.*;

import java.io.ByteArrayOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private ImageView imageView;
    private EditText editNmae, editPhone, editLat, editLon;
    private Bitmap photoBitmap;

    private FusedLocationProviderClient fusedLocationClient;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private Button btnUbicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //Objetos
        imageView = findViewById(R.id.imageFoto);
        editNmae = findViewById(R.id.editTextNombre);
        editPhone = findViewById(R.id.editTextTelefono);
        editLat = findViewById(R.id.editTextLatitud);
        editLon = findViewById(R.id.editTextLongitud);
        Button btnTakePhoto = findViewById(R.id.btnTomarFoto);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnViewContacts = findViewById(R.id.btnContactos);
        Button btnUbicacion = findViewById(R.id.btnPedirUbi);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null){
                        Bundle extras = result.getData().getExtras();
                        photoBitmap = (Bitmap) extras.get("data");
                        imageView.setImageBitmap(photoBitmap);
                    }
                }
        );

        btnTakePhoto.setOnClickListener(v -> openCamera());

        //requestLocationPermission();
        btnUbicacion = findViewById(R.id.btnPedirUbi);
        btnUbicacion.setOnClickListener(v -> requestLocationPermission());

        btnSave.setOnClickListener(v -> saveContact());

        btnViewContacts.setOnClickListener( v -> {
            Intent intent = new Intent(MainActivity.this, ContactosGuardados.class);
            startActivity(intent);
        });

    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    101);

        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        }
    }

    private void requestLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    private void getLocation(){
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if(location != null){
                        editLat.setText(String.valueOf(location.getLatitude()));
                        editLon.setText(String.valueOf(location.getLongitude()));
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ubicacion", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                          @NonNull String[] permissions,
                                          @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLocation();
            } else {
                Toast.makeText(this, "Permiso de ubicacion denegado", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveContact(){
        String name = editNmae.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String latitude = editLat.getText().toString().trim();
        String longitude = editLon.getText().toString().trim();

        if (photoBitmap == null){
            Toast.makeText(this,"Debe tomar una fotografía", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty() || phone.isEmpty() || latitude.isEmpty() || longitude.isEmpty()){
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir imagen a Base64
        String imageBase64 = "data:image/jpeg;base64," + imageToBase64(photoBitmap);

        // Crear objeto Contact
        Contactos contactos = new Contactos(0, name, phone, latitude, longitude, imageBase64);

        // Llamada a la API
        ApiService apiService = ApiClient.getApiService();
        Call<Void> call = apiService.saveContact(contactos);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Contacto guardado correctamente", Toast.LENGTH_SHORT).show();
                    clearFields();
                } else {
                    Toast.makeText(MainActivity.this, "Error al guardar el contacto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String imageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    private void clearFields() {
        editNmae.setText("");
        editPhone.setText("");
        editLat.setText("");
        editLon.setText("");
        imageView.setImageResource(R.drawable.ic_launcher_background);
        photoBitmap = null;
    }
}