package com.example.diamonds;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ActivityNotFoundException;
import android.speech.RecognizerIntent;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;


public class SimpleExerciseDetails extends AppCompatActivity {

    ImageView imageView;
    TextView titleView, descriptionView, goToSimpleListBtn, micBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_exercise_details);

        goToSimpleListBtn = findViewById(R.id.goSimpleExerciseList);
        micBtn = findViewById(R.id.simpleExerciseMic);
        imageView = findViewById(R.id.detailsImage);
        titleView = findViewById(R.id.detailsTitle);
        descriptionView = findViewById(R.id.detailsDescription);

        descriptionView.setBackgroundColor(Color.TRANSPARENT);
        descriptionView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        int imageResId = getIntent().getIntExtra("imageResId", -1);
        String title = getIntent().getStringExtra("title");
        String htmlFile = getIntent().getStringExtra("description");

        titleView.setText(title);
        imageView.setImageResource(imageResId);

        try {
            InputStream input = getAssets().open(htmlFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder html = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line);
            }
            reader.close();

            Spanned spanned = HtmlCompat.fromHtml(html.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY);
            descriptionView.setText(spanned);

        } catch (IOException e) {
            descriptionView.setText("Αποτυχία φόρτωσης περιγραφής.");
        }

        goToSimpleListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SimpleExerciseDetails.this, SimpleExercises.class);
            startActivity(intent);
            finish(); // optional
        });

        micBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Πείτε 'αρχική' ή 'λίστα'");

            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000);

            try {
                startActivityForResult(intent, 100);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Η συσκευή δεν υποστηρίζει φωνητική εισαγωγή", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String command = result.get(0).toLowerCase();

                if (command.contains("αρχική")) {
                    startActivity(new Intent(this, MainActivity.class));
                } else if (command.contains("λίστα")) {
                    startActivity(new Intent(this, SimpleExercises.class));
                } else {
                    Toast.makeText(this, "Δεν αναγνωρίστηκε εντολή", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
