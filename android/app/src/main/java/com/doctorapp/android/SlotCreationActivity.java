package com.doctorapp.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class SlotCreationActivity extends AppCompatActivity {
    private long doctorId;
    private List<String> pendingSlots = new ArrayList<>();
    private LinearLayout layTimesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_creation);

        doctorId = getSharedPreferences("AuthPrefs", MODE_PRIVATE).getLong("userId", -1);
        if (doctorId == -1) {
            finish();
            return;
        }

        EditText etStartTime = findViewById(R.id.etStartTime);
        Spinner spinStartAmPm = findViewById(R.id.spinStartAmPm);
        EditText etEndTime = findViewById(R.id.etEndTime);
        Spinner spinEndAmPm = findViewById(R.id.spinEndAmPm);
        Button btnEnterSlot = findViewById(R.id.btnEnterSlot);
        Button btnSaveAll = findViewById(R.id.btnSaveAll);
        Button btnGo = findViewById(R.id.btnGoToDashboard);
        layTimesContainer = findViewById(R.id.layTimesContainer);

        String[] amPm = {"AM", "PM"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, amPm);
        spinStartAmPm.setAdapter(adapter);
        spinEndAmPm.setAdapter(adapter);

        btnEnterSlot.setOnClickListener(v -> {
            String start = etStartTime.getText().toString().trim();
            String end = etEndTime.getText().toString().trim();
            
            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Please specify start and end times", Toast.LENGTH_SHORT).show();
                return;
            }

            String startMeridian = spinStartAmPm.getSelectedItem().toString();
            String endMeridian = spinEndAmPm.getSelectedItem().toString();
            String slotText = start + " " + startMeridian + " - " + end + " " + endMeridian;

            pendingSlots.add(slotText);
            TextView tv = new TextView(this);
            tv.setText("• " + slotText);
            tv.setTextSize(18);
            tv.setPadding(0, 8, 0, 8);
            layTimesContainer.addView(tv);

            etStartTime.setText("");
            etEndTime.setText("");
        });

        btnSaveAll.setOnClickListener(v -> {
            if (pendingSlots.isEmpty()) {
                Toast.makeText(this, "No slots to save!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            v.setEnabled(false);
            for (String ts : pendingSlots) {
                createSlot(ts);
            }
            
            Toast.makeText(this, "Successfully saved " + pendingSlots.size() + " slots!", Toast.LENGTH_SHORT).show();
            pendingSlots.clear();
            layTimesContainer.removeAllViews();
            v.setEnabled(true);
        });

        btnGo.setOnClickListener(v -> checkSlotsAndProceed());
    }

    private void createSlot(String time) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(MainActivity.SERVER_URL + "/slots/create");
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
        }).start();
    }

    private void checkSlotsAndProceed() {
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
                    if (slotsArray.length() > 0) {
                        runOnUiThread(() -> {
                            Intent intent = new Intent(this, DoctorDashboardActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "You must create slots first!", Toast.LENGTH_LONG).show());
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error checking slots", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
