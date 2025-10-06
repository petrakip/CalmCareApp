package com.example.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.speech.RecognizerIntent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class CallSupport extends AppCompatActivity {
    Button goBack, familiarCall, doctorCall, callSupport, sosCall, callSupportMic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call_support);

        // ids connection
        callSupportMic = findViewById(R.id.callSupportMic);
        goBack = findViewById(R.id.goBackFromCallSupportToMain);
        familiarCall = findViewById(R.id.familiarCallOperation);
        doctorCall = findViewById(R.id.doctorCallOperation);
        callSupport = findViewById(R.id.callSupportOperation);
        sosCall = findViewById(R.id.sosCallOperation);

        // when mic clicked, voice operation starts
        callSupportMic.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
            } else {
                startVoiceRecognition();
            }
        });

        // go to main activity
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CallSupport.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Depending on the category clicked, it will bring up the corresponding contacts.
        familiarCall.setOnClickListener(v -> openPhoneList("familiar"));
        doctorCall.setOnClickListener(v -> openPhoneList("doctor"));
        callSupport.setOnClickListener(v -> openPhoneList("support"));
        sosCall.setOnClickListener(v -> openPhoneList("sos"));
    }

    private void openPhoneList(String category) {
        Intent intent = new Intent(this, PhoneList.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-GR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Πείτε: Οικείο Πρόσωπο, Γιατρός, Γραμμή Υποστήριξης, Βοήθεια, Αρχική");

        try {
            startActivityForResult(intent, 10);
        } catch (Exception e) {
            Toast.makeText(this, "Η συσκευή δεν υποστηρίζει φωνητικές εντολές", Toast.LENGTH_SHORT).show();
        }
    }

    // Handling voice commands of user
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spoken = results.get(0).toLowerCase();

                if (spoken.contains("οικείο") || spoken.contains("οικείο πρόσωπο")
                        || spoken.contains("φίλος") || spoken.contains("φίλη")
                        || spoken.contains("οικογένεια" )) {
                    findViewById(R.id.familiarCallOperation).performClick();
                } else if (spoken.contains("γιατρός")) {
                    findViewById(R.id.doctorCallOperation).performClick();
                } else if (spoken.contains("υποστήριξη") || spoken.contains("γραμμή")) {
                    findViewById(R.id.callSupportOperation).performClick();
                } else if (spoken.contains("sos") || spoken.contains("επείγον") || spoken.contains("βοήθεια")) {
                    findViewById(R.id.sosCallOperation).performClick();
                } else if (spoken.contains("αρχική") || spoken.contains("πίσω")) {
                    Intent intent = new Intent(CallSupport.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Δεν αναγνωρίστηκε η εντολή.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}