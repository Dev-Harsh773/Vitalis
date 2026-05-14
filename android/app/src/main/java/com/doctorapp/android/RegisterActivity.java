package com.doctorapp.android;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private Double doctorLat = null;
    private Double doctorLng = null;
    private String role;
    
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> mapLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    doctorLat = result.getData().getDoubleExtra("lat", 0.0);
                    doctorLng = result.getData().getDoubleExtra("lng", 0.0);
                    Button btnLocation = findViewById(R.id.btnLocation);
                    btnLocation.setText("Location Confirmed \u2713");
                    btnLocation.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")); // Green
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        role = getIntent().getStringExtra("role");
        if (role == null) role = "patient";

        TextView tvTitle = findViewById(R.id.tvRegisterTitle);
        tvTitle.setText(role.substring(0, 1).toUpperCase() + role.substring(1) + " Register");

        EditText etName = findViewById(R.id.etName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        android.widget.Spinner spinnerSpec = findViewById(R.id.spinnerSpecialization);
        
        String[] domains = {"Neurologist", "ENT", "Ophthalmologist", "Dentist", "Endocrinologist", "Pulmonologist", "Cardiologist", "Gastroenterologist", "Urologist", "Orthopedic", "General Physician"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, domains);
        spinnerSpec.setAdapter(adapter);
        
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnLocation = findViewById(R.id.btnLocation);

        if (role.equals("doctor")) {
            spinnerSpec.setVisibility(View.VISIBLE);
            btnLocation.setVisibility(View.VISIBLE);
        }

        btnLocation.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(RegisterActivity.this, MapSelectionActivity.class);
            mapLauncher.launch(intent);
        });

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String spec = role.equals("doctor") ? spinnerSpec.getSelectedItem().toString() : "";

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || (role.equals("doctor") && spec.isEmpty())) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            registerUser(name, email, password, spec);
        });
    }

    private void registerUser(String name, String email, String password, String spec) {
        new Thread(() -> {
            try {
                String endpoint = role.equals("doctor") ? "/doctor/register" : "/patient/register";
                java.net.URL url = new java.net.URL(MainActivity.SERVER_URL + endpoint);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                String json;
                if (role.equals("doctor")) {
                    json = "{\"name\": \"" + name + "\", \"email\": \"" + email + "\", \"password\": \"" + password + "\", \"specialization\": \"" + spec + "\"";
                    if (doctorLat != null && doctorLng != null) {
                        json += ", \"latitude\": " + doctorLat + ", \"longitude\": " + doctorLng;
                    }
                    json += "}";
                } else {
                    json = "{\"name\": \"" + name + "\", \"email\": \"" + email + "\", \"password\": \"" + password + "\"}";
                }

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("utf-8"));
                }

                int code = conn.getResponseCode();
                if (code == 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Registration Successful! Please login.", Toast.LENGTH_LONG).show();
                        finish();
                    });
                } else if (code == 400) {
                    runOnUiThread(() -> Toast.makeText(this, "Registration Failed: Email already exists", Toast.LENGTH_LONG).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Registration Failed: Server Error", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
