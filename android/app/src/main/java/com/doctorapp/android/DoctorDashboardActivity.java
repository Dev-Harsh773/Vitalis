package com.doctorapp.android;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DoctorDashboardActivity extends AppCompatActivity {

    private TextView tvDoctorInfo;
    private TextView tvBookings;
    private long doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        tvDoctorInfo = findViewById(R.id.tvDoctorInfo);
        tvBookings = findViewById(R.id.tvBookings);

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            getSharedPreferences("AuthPrefs", MODE_PRIVATE).edit().clear().apply();
            android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnCreateSlots).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, SlotCreationActivity.class);
            startActivity(intent);
            finish();
        });

        doctorId = getSharedPreferences("AuthPrefs", MODE_PRIVATE).getLong("userId", -1);
        if (doctorId == -1) {
            finish();
            return;
        }

        checkSlots();
    }

    private void checkSlots() {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(MainActivity.SERVER_URL + "/slots/" + doctorId);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                
                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    String input;
                    StringBuilder content = new StringBuilder();
                    while ((input = in.readLine()) != null) content.append(input);
                    in.close();
                    
                    org.json.JSONArray slotsArray = new org.json.JSONArray(content.toString());
                    if (slotsArray.length() == 0) {
                        runOnUiThread(() -> {
                            android.content.Intent intent = new android.content.Intent(this, SlotCreationActivity.class);
                            startActivity(intent);
                            finish(); // prevent backing into dashboard
                        });
                    } else {
                        fetchOwnBookings();
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void fetchOwnBookings() {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(MainActivity.SERVER_URL + "/doctor/bookings/" + doctorId);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                
                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) content.append(inputLine);
                    in.close();
                    
                    org.json.JSONObject obj = new org.json.JSONObject(content.toString());
                    String docName = obj.getString("doctorName");
                    String spec = obj.getString("specialization");
                    org.json.JSONArray slots = obj.getJSONArray("slots");
                    
                    runOnUiThread(() -> {
                        tvDoctorInfo.setText("My Profile: " + docName + " (" + spec + ")");
                        
                        android.widget.LinearLayout laySlotsContainer = findViewById(R.id.laySlotsContainer);
                        laySlotsContainer.removeAllViews();
                        
                        TextView header = new TextView(DoctorDashboardActivity.this);
                        header.setText("Your Prescheduled Slots:");
                        header.setTextSize(18);
                        header.setPadding(0, 0, 0, 16);
                        laySlotsContainer.addView(header);

                        if (slots.length() == 0) {
                            TextView emptyTxt = new TextView(DoctorDashboardActivity.this);
                            emptyTxt.setText("No slots created yet.");
                            laySlotsContainer.addView(emptyTxt);
                            return;
                        }

                        for (int i = 0; i < slots.length(); i++) {
                            try {
                                org.json.JSONObject slot = slots.getJSONObject(i);
                                String time = slot.getString("time");
                                int count = slot.getInt("totalBookings");
                                
                                org.json.JSONArray patients = slot.getJSONArray("patients");
                                String[] patientNames = new String[patients.length()];
                                for (int j = 0; j < patients.length(); j++) {
                                    org.json.JSONObject p = patients.getJSONObject(j);
                                    patientNames[j] = p.getString("name") + " (Patient ID: " + p.getLong("id") + ")";
                                }

                                android.widget.Button btn = new android.widget.Button(DoctorDashboardActivity.this);
                                btn.setText(time + "   |   Bookings: " + count);
                                btn.setAllCaps(false);
                                btn.setOnClickListener(v -> {
                                    if (patientNames.length == 0) {
                                        Toast.makeText(DoctorDashboardActivity.this, "No patients have booked this slot yet.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        new android.app.AlertDialog.Builder(DoctorDashboardActivity.this)
                                            .setTitle("Patients at " + time)
                                            .setItems(patientNames, null)
                                            .setPositiveButton("Close", null)
                                            .show();
                                    }
                                });
                                laySlotsContainer.addView(btn);
                            } catch (Exception e) {}
                        }
                    });

                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to fetch bookings", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                Log.e("DoctorDashboard", "Error", e);
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
