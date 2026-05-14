package com.doctorapp.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String SERVER_URL = "http://localhost:8080"; // Using localhost because adb reverse tcp:8080 tcp:8080 is active

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        android.content.SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("email", null);
        String savedPassword = prefs.getString("password", null);
        String savedRole = prefs.getString("role", null);

        if (savedEmail != null && savedPassword != null && savedRole != null) {
            Toast.makeText(this, "Auto-logging in...", Toast.LENGTH_SHORT).show();
            autoLogin(savedEmail, savedPassword, savedRole);
            return;
        }

        Button btnDoctor = findViewById(R.id.btnDoctor);
        Button btnPatient = findViewById(R.id.btnPatient);

        btnDoctor.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("role", "doctor");
            startActivity(intent);
        });

        btnPatient.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("role", "patient");
            startActivity(intent);
        });
    }

    private void autoLogin(String email, String password, String role) {
        new Thread(() -> {
            try {
                String endpoint = role.equals("doctor") ? "/doctor/login" : "/patient/login";
                java.net.URL url = new java.net.URL(SERVER_URL + endpoint);
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
                            .putLong("userId", userId)
                            .apply();

                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
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
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                        getSharedPreferences("AuthPrefs", MODE_PRIVATE).edit().clear().apply();
                        recreate(); 
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Network Error. Continuing to login screen.", Toast.LENGTH_LONG).show();
                    getSharedPreferences("AuthPrefs", MODE_PRIVATE).edit().clear().apply();
                    recreate();
                });
            }
        }).start();
    }

    private void registerDemoDoctor() {
        Toast.makeText(this, "Sending Temp Doctor Data...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(SERVER_URL + "/doctor/register");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                String jsonInput = "{\"name\": \"Dr. Default\", \"specialization\": \"Neurologist\", \"latitude\": 28.7, \"longitude\": 77.1}";
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInput.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    String resp = in.readLine();
                    in.close();
                    org.json.JSONObject docObj = new org.json.JSONObject(resp);
                    long createdDoctorId = docObj.getLong("id");

                    createSlot(createdDoctorId, "10:00 AM");
                    createSlot(createdDoctorId, "10:30 AM");
                    createSlot(createdDoctorId, "11:00 AM");

                    runOnUiThread(() -> Toast.makeText(this, "Doctor + Slots Registered!", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void createSlot(long doctorId, String time) {
        try {
            java.net.URL url = new java.net.URL(SERVER_URL + "/slots/create");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);
            String jsonInput = "{\"doctorId\": " + doctorId + ", \"time\": \"" + time + "\", \"bookedCount\": 0}";
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes("utf-8"));
            }
            conn.getResponseCode();
        } catch (Exception ignored) {}
    }
}
