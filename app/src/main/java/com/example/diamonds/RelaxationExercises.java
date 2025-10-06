package com.example.diamonds;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class RelaxationExercises extends AppCompatActivity {

    Button pulseAnimationExerciseBtn, videoExercisesBtn, simpleExercisesBtn, micBtn;
    TextView goBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relaxation_exercises);

        // ids connection
        goBackBtn = findViewById(R.id.goMainFromRelaxationExercises);
        pulseAnimationExerciseBtn = findViewById(R.id.animationExercise);
        videoExercisesBtn = findViewById(R.id.videoExercise);
        simpleExercisesBtn = findViewById(R.id.simpleExercise);
        micBtn = findViewById(R.id.micRelaxationExercises);

        // Navigation with "back" button
        goBackBtn.setOnClickListener(view -> {
            startActivity(new Intent(RelaxationExercises.this, MainActivity.class));
        });

        // Navigate to Interactive Breathing Exercise
        pulseAnimationExerciseBtn.setOnClickListener(view -> {
            startActivity(new Intent(RelaxationExercises.this, BreathAnimationPulse.class));
        });

        // Navigation to Video Exercises
        videoExercisesBtn.setOnClickListener(view -> {
            startActivity(new Intent(RelaxationExercises.this, VideoExercises.class));
        });

        // Navigation to Descriptive Exercises
        simpleExercisesBtn.setOnClickListener(view -> {
            startActivity(new Intent(RelaxationExercises.this, SimpleExercises.class));
        });

        // Microphone: activate voice commands
        micBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Πείτε: 'αρχική', 'αναπνοή', 'βίντεο' ή 'απλές'");

            // We increase the duration of silence
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000);

            try {
                startActivityForResult(intent, 300);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Η φωνητική εισαγωγή δεν υποστηρίζεται", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Editing a result from voice input
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String command = result.get(0).toLowerCase();

                // Voice Command: "αρχική" or "κεντρική"
                if (command.contains("αρχική") || command.contains("κεντρική")) {
                    startActivity(new Intent(this, MainActivity.class));
                }
                // Voice Command: "αναπνοή" or "animation" or "διαδραστική"
                else if (command.contains("αναπνοή") || command.contains("animation") || command.contains("διαδραστική")) {
                    startActivity(new Intent(this, BreathAnimationPulse.class));
                }
                // Voice Command: "βίντεο" or "video"
                else if (command.contains("βίντεο") || command.contains("video")) {
                    startActivity(new Intent(this, VideoExercises.class));
                }
                // Voice Command: "περιγραφικές" or "απλές ασκήσεις" or "simple"
                else if (command.contains("περιγραφικές") || command.contains("απλές") || command.contains("simple")) {
                    startActivity(new Intent(this, SimpleExercises.class));
                }
                // If something doesn't fit
                else {
                    Toast.makeText(this, "Δεν αναγνωρίστηκε η εντολή.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
