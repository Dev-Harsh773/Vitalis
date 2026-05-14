package com.doctorapp.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private String role; // "doctor" or "patient"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        role = getIntent().getStringExtra("role");
        if (role == null) role = "patient";

        TextView tvTitle = findViewById(R.id.tvLoginTitle);
        tvTitle.setText(role.substring(0, 1).toUpperCase() + role.substring(1) + " Login");

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.putExtra("role", role);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            loginUser(email, password);
        });
    }

    private void loginUser(String email, String password) {
        new Thread(() -> {
            try {
                String endpoint = role.equals("doctor") ? "/doctor/login" : "/patient/login";
                java.net.URL url = new java.net.URL(MainActivity.SERVER_URL + endpoint);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                String json = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}";
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("utf-8"));
                }

                int code = conn.getResponseCode();
                if (code == 200) {
                    java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    String resp = in.readLine();
                    in.close();
                    org.json.JSONObject obj = new org.json.JSONObject(resp);
                    long userId = obj.getLong("id");

                    runOnUiThread(() -> {
                        getSharedPreferences("AuthPrefs", MODE_PRIVATE).edit()
                            .putString("email", email)
                            .putString("password", password)
                            .putString("role", role)
                            .putLong("userId", userId)
                            .apply();

                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        if (role.equals("doctor")) {
                            Intent intent = new Intent(this, DoctorDashboardActivity.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(this, BodySelectionActivity.class);
                            intent.putExtra("patientId", userId);
                            startActivity(intent);
                        }
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Login Failed: Invalid credentials", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
