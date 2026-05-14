package com.doctorapp.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BodySelectionActivity extends AppCompatActivity {

    private Map<String, String[]> bodyToDiseases;
    private Map<String, String> diseaseToSpecialization;
    private long currentPatientId = 1;
    
    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;
    private Double patientLat = null;
    private Double patientLng = null;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // UI Components for Premium Dashboard
    private LinearLayout layDoctorsContainer;
    private TextView tvCtaText;
    private TextView tvEmptyState;
    private String selectedSpecialization = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_selection);

        setupData();
        initUi();

        // Standard Body Map Clicks (IDs preserved)
        setupClick("HEAD", R.id.btnHead);
        setupClick("EYE", R.id.btnEye);
        setupClick("NOSE", R.id.btnNose);
        setupClick("MOUTH", R.id.btnMouth);
        setupClick("NECK", R.id.btnNeck);
        setupClick("CHEST", R.id.btnChest);
        setupClick("ABDOMEN", R.id.btnAbdomen);
        setupClick("ARMS", R.id.btnLeftArm);
        setupClick("ARMS", R.id.btnRightArm);
        setupClick("LEGS", R.id.btnLeftLeg);
        setupClick("LEGS", R.id.btnRightLeg);
        setupClick("PRIVATE PART", R.id.btnPrivatePart);

        // Quick Selection Card Clicks
        setupClick("EYE", R.id.btnQuickEye);
        setupClick("NOSE", R.id.btnQuickNose);
        setupClick("MOUTH", R.id.btnQuickMouth);
        setupClick("NECK", R.id.btnQuickNeck);

        currentPatientId = getIntent().getLongExtra("patientId", 1);

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            getSharedPreferences("AuthPrefs", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this);
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchPatientLocation();
        }

        // CTA Button Logic
        findViewById(R.id.btnFindDoctors).setOnClickListener(v -> {
            if (!selectedSpecialization.isEmpty()) {
                fetchDoctors(selectedSpecialization);
            } else {
                Toast.makeText(this, "Select an area on the map first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initUi() {
        layDoctorsContainer = findViewById(R.id.layDoctorsContainer);
        tvCtaText = findViewById(R.id.tvCtaText);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void fetchPatientLocation() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) return;
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                patientLat = location.getLatitude();
                patientLng = location.getLongitude();
                Log.d("LocationCheck", "Patient GPS: " + patientLat + ", " + patientLng);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fetchPatientLocation();
        }
    }

    private void setupData() {
        bodyToDiseases = new HashMap<>();
        bodyToDiseases.put("HEAD", new String[]{"Migraine", "Tension Headache", "Concussion"});
        bodyToDiseases.put("EYE", new String[]{"Eye Strain", "Conjunctivitis", "Dry Eyes", "Vision Blur"});
        bodyToDiseases.put("NOSE", new String[]{"Sinusitis", "Nasal Infection", "Allergy", "Nose Bleeding"});
        bodyToDiseases.put("MOUTH", new String[]{"Mouth Ulcer", "Gum Infection", "Tooth Decay", "Oral Infection"});
        bodyToDiseases.put("NECK", new String[]{"Thyroid Issue", "Muscle Strain", "Swelling", "Cervical Pain"});
        bodyToDiseases.put("CHEST", new String[]{"Asthma", "Bronchitis", "Pneumonia", "Heart Disease", "Chest Pain"});
        bodyToDiseases.put("ABDOMEN", new String[]{"Acidity", "Ulcer", "Food Poisoning", "Liver Problem", "Kidney Stone"});
        bodyToDiseases.put("ARMS", new String[]{"Muscle Pain", "Fracture", "Joint Pain", "Nerve Damage"});
        bodyToDiseases.put("LEGS", new String[]{"Knee Pain", "Fracture", "Muscle Tear", "Arthritis", "Varicose Veins"});
        bodyToDiseases.put("PRIVATE PART", new String[]{"Infection", "Rash", "Urinary Infection"});

        diseaseToSpecialization = new HashMap<>();
        diseaseToSpecialization.put("Migraine", "Neurologist");
        diseaseToSpecialization.put("Eye Strain", "Ophthalmologist");
        diseaseToSpecialization.put("Sinusitis", "ENT");
        diseaseToSpecialization.put("Mouth Ulcer", "Dentist");
        diseaseToSpecialization.put("Thyroid Issue", "Endocrinologist");
        diseaseToSpecialization.put("Asthma", "Pulmonologist");
        diseaseToSpecialization.put("Heart Disease", "Cardiologist");
        diseaseToSpecialization.put("Acidity", "Gastroenterologist");
        diseaseToSpecialization.put("Kidney Stone", "Urologist");
        diseaseToSpecialization.put("Fracture", "Orthopedic");
        diseaseToSpecialization.put("Arthritis", "Orthopedic");
        diseaseToSpecialization.put("Infection", "General Physician");
        diseaseToSpecialization.put("Urinary Infection", "Urologist");
    }

    private void setupClick(String bodyPart, int viewId) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> showDiseasesDialog(bodyPart));
        }
    }

    private void showDiseasesDialog(String bodyPart) {
        String[] diseases = bodyToDiseases.get(bodyPart);
        if (diseases == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Select Condition in " + bodyPart)
                .setItems(diseases, (dialog, which) -> {
                    String selectedDisease = diseases[which];
                    selectedSpecialization = diseaseToSpecialization.containsKey(selectedDisease) ?
                            diseaseToSpecialization.get(selectedDisease) : "General Physician";
                    
                    tvCtaText.setText("Find Doctors for " + selectedDisease + " →");
                    Toast.makeText(this, "Targeting specialist: " + selectedSpecialization, Toast.LENGTH_SHORT).show();
                    
                    // Auto-fetch to populate the list immediately
                    fetchDoctors(selectedSpecialization);
                })
                .show();
    }

    private void fetchDoctors(String specialization) {
        new Thread(() -> {
            try {
                String specEncoded = java.net.URLEncoder.encode(specialization, "UTF-8");
                java.net.URL url = new java.net.URL(MainActivity.SERVER_URL + "/doctors?specialization=" + specEncoded);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                
                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) content.append(inputLine);
                    in.close();
                    
                    org.json.JSONArray doctorsArray = new org.json.JSONArray(content.toString());
                    final java.util.List<JSONObject> doctorList = new java.util.ArrayList<>();
                    for (int i = 0; i < doctorsArray.length(); i++) {
                        doctorList.add(doctorsArray.getJSONObject(i));
                    }
                    
                    double userLat = patientLat != null ? patientLat : 28.5;
                    double userLon = patientLng != null ? patientLng : 77.0;

                    java.util.Collections.sort(doctorList, (d1, d2) -> {
                        float[] dist1 = new float[1];
                        android.location.Location.distanceBetween(userLat, userLon, d1.optDouble("latitude", 0), d1.optDouble("longitude", 0), dist1);
                        float[] dist2 = new float[1];
                        android.location.Location.distanceBetween(userLat, userLon, d2.optDouble("latitude", 0), d2.optDouble("longitude", 0), dist2);
                        return Float.compare(dist1[0], dist2[0]);
                    });
                    
                    runOnUiThread(() -> {
                        layDoctorsContainer.removeAllViews();
                        
                        if (doctorList.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            tvEmptyState.setText("No specialists found for " + specialization);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                            for (JSONObject doc : doctorList) {
                                addDoctorCard(doc);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("DoctorFetch", "Error", e);
            }
        }).start();
    }

    private void addDoctorCard(JSONObject doc) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_doctor_card, layDoctorsContainer, false);
        
        TextView name = card.findViewById(R.id.tvDoctorName);
        TextView spec = card.findViewById(R.id.tvSpecialization);
        TextView rating = card.findViewById(R.id.tvRating);
        View btnBook = card.findViewById(R.id.btnBookSlot);

        name.setText(doc.optString("name", "Dr. Specialist"));
        spec.setText(doc.optString("specialization", "Specialist").toUpperCase());
        
        double userLat = patientLat != null ? patientLat : 28.5;
        double userLon = patientLng != null ? patientLng : 77.0;
        float[] dist = new float[1];
        android.location.Location.distanceBetween(userLat, userLon, doc.optDouble("latitude", 28.5), doc.optDouble("longitude", 77.0), dist);
        rating.setText(String.format("%.1f km away", dist[0] / 1000f));

        btnBook.setOnClickListener(v -> fetchSlots(doc.optLong("id"), doc.optString("name")));
        
        layDoctorsContainer.addView(card);
    }

    private void fetchSlots(long doctorId, String doctorName) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(MainActivity.SERVER_URL + "/slots/" + doctorId);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                
                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) content.append(inputLine);
                    in.close();
                    
                    org.json.JSONArray slotsArray = new org.json.JSONArray(content.toString());
                    String[] slotsDisplay = new String[slotsArray.length()];
                    long[] slotIds = new long[slotsArray.length()];
                    for (int i = 0; i < slotsArray.length(); i++) {
                        org.json.JSONObject slot = slotsArray.getJSONObject(i);
                        slotsDisplay[i] = slot.optString("time") + " (Booked: " + slot.optInt("bookedCount", 0) + ")";
                        slotIds[i] = slot.optLong("id");
                    }
                    
                    runOnUiThread(() -> {
                        if (slotsDisplay.length == 0) {
                            Toast.makeText(this, "No slots available for " + doctorName, Toast.LENGTH_LONG).show();
                        } else {
                            new AlertDialog.Builder(this)
                                .setTitle("Book with " + doctorName)
                                .setItems(slotsDisplay, (dialog, which) -> {
                                    bookSlot(doctorId, slotIds[which], slotsDisplay[which]);
                                })
                                .show();
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void bookSlot(long doctorId, long slotId, String slotTime) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(MainActivity.SERVER_URL + "/book");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                String jsonInput = "{\"doctorId\": " + doctorId + ", \"slotId\": " + slotId + ", \"patientId\": " + currentPatientId + "}";
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(jsonInput.getBytes("utf-8"));
                }

                if (conn.getResponseCode() == 200) {
                    runOnUiThread(() -> Toast.makeText(this, "✅ Booking Confirmed for " + slotTime, Toast.LENGTH_LONG).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Booking failed", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Connection Error", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}

