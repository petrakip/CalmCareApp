package com.example.diamonds;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.speech.RecognizerIntent;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "a24dbf8b22c77f3c324c8b8fdbf131b9";
    private static final int LOCATION_SETTINGS_REQUEST = 2001;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    private TextView cityNameText, temperatureText, humidityText, windText, dateText, timeText, locationView;
    private ImageView weatherIcon;
    Button calendarButton, musicPlayerButton, callSupport, exercisesButton, micButton, exitButton, settingsButton;
    private boolean waitingForCity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        exitButton = findViewById(R.id.exitBtn);
        settingsButton = findViewById(R.id.settingsBtn);
        micButton = findViewById(R.id.mic_operationBtn);
        exercisesButton = findViewById(R.id.operation1);
        calendarButton = findViewById(R.id.operation2);
        callSupport = findViewById(R.id.operation3);
        musicPlayerButton = findViewById(R.id.operation4);

        exitButton.setOnClickListener(v -> {
            finishAffinity(); // End of all Activities
        });

        settingsButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Αλλαγή Πόλης");

            final TextView inputLabel = new TextView(MainActivity.this);
            inputLabel.setText("Πληκτρολόγησε την πόλη:");

            final EditText input = new EditText(MainActivity.this);
            input.setHint("π.χ. Athens");

            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);
            layout.addView(inputLabel);
            layout.addView(input);

            builder.setView(layout);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String newCity = input.getText().toString().trim();
                if (!newCity.isEmpty()) {
                    FetchWeatherData(newCity);
                } else {
                    Toast.makeText(MainActivity.this, "Δεν δόθηκε πόλη.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Άκυρο", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        // Microphone permission check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        // Voice command with Google Prompt
        micButton.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-GR");

            // prompt message
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Πείτε:\"ασκήσεις χαλάρωσης\", \"ημερολόγιο διάθεσης\", \"μουσική\", \"κλήσεις\", \"ρυθμίσεις\", \"τοποθεσία\",\"έξοδος\"");

            // More time to start/stop
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000); // αναμονή μετά το τέλος
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000); // πιθανό τέλος
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000); // min συνολική διάρκεια ομιλίας

            try {
                startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
            } catch (Exception e) {
                Toast.makeText(this, "Η φωνητική λειτουργία δεν υποστηρίζεται.", Toast.LENGTH_SHORT).show();
            }
        });

        exercisesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RelaxationExercises.class);
            startActivity(intent);
        });

        calendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarApp.class);
            startActivity(intent);
        });

        callSupport.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CallSupport.class);
            startActivity(intent);
        });

        musicPlayerButton.setOnClickListener(v -> {
            Intent musicPlayerIntent = new Intent(MainActivity.this, MusicMediaPlayerList.class);
            startActivity(musicPlayerIntent);
        });

        // Views
        locationView = findViewById(R.id.locationView);
        cityNameText = findViewById(R.id.cityNameText);
        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        weatherIcon = findViewById(R.id.weatherIcon);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);

        // Set date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy");
        dateText.setText(dateFormat.format(Calendar.getInstance().getTime()));

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        timeText.setText(timeFormat.format(Calendar.getInstance().getTime()));

        // Weather fetch
        FetchWeatherData("Athens");

        // Location click handler
        locationView.setOnClickListener(view -> checkLocationSettingsAndLaunchMap());
    }

    private void checkLocationSettingsAndLaunchMap() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            // GPS active – we start MapsActivity
            startActivity(new Intent(MainActivity.this, MapsActivity.class));
        });

        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivity.this, LOCATION_SETTINGS_REQUEST);
                } catch (IntentSender.SendIntentException ex) {
                    ex.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Η τοποθεσία δεν είναι διαθέσιμη.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_SETTINGS_REQUEST) {
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            } else {
                Toast.makeText(this, "Η τοποθεσία παραμένει απενεργοποιημένη.", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && results.size() > 0) {
                String input = results.get(0).toLowerCase();

                if (waitingForCity) {
                    waitingForCity = false;
                    FetchWeatherData(input);
                    Toast.makeText(this, "Ανανεώθηκε ο καιρός για: " + input, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (input.contains("έξοδος") || input.contains("exit")) {
                    finishAffinity();
                } else if (input.contains("που βρισκομαι") || input.contains("πού βρίσκομαι") || input.contains("τοποθεσία") || input.contains("location")) {
                    checkLocationSettingsAndLaunchMap();
                } else if (input.contains("ασκήσεις") || input.contains("exercise")) {
                    startActivity(new Intent(MainActivity.this, RelaxationExercises.class));
                } else if (input.contains("ημερολόγιο") || input.contains("calendar")) {
                    startActivity(new Intent(MainActivity.this, CalendarApp.class));
                } else if (input.contains("ψυχολογική υποστήριξη") || input.contains("support") || input.contains("call") || input.contains("κλησεις ψυχολογικης υποστηριξης")) {
                    startActivity(new Intent(MainActivity.this, CallSupport.class));
                } else if (input.contains("μουσική") || input.contains("music")) {
                    startActivity(new Intent(MainActivity.this, MusicMediaPlayerList.class));
                } else if (input.contains("ρυθμίσεις") || input.contains("settings")) {
                    waitingForCity = true;

                    Intent cityIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    cityIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    cityIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-GR");

                    // Increased wait time for pause and completion
                    cityIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000); // Χρόνος μετά την παύση (ms)
                    cityIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000); // Πιθανή παύση (ms)
                    cityIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 6000); // Ελάχιστος χρόνος ομιλίας

                    // Prompt to guide the user
                    cityIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ποια πόλη θέλεις για τον καιρό;");

                    try {
                        startActivityForResult(cityIntent, VOICE_RECOGNITION_REQUEST_CODE);
                    } catch (Exception e) {
                        Toast.makeText(this, "Σφάλμα φωνητικής εισαγωγής.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Δεν αναγνωρίστηκε η εντολή.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void FetchWeatherData(String cityName) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + API_KEY + "&units=metric";
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                runOnUiThread(() -> updateUI(result));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateUI(String result) {
        if (result != null) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject main = jsonObject.getJSONObject("main");
                double temperature = main.getDouble("temp");
                double humidity = main.getDouble("humidity");
                double windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");

                String iconCode = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
                String resourceName = "ic_" + iconCode;
                int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
                weatherIcon.setImageResource(resId);

                cityNameText.setText(jsonObject.getString("name"));
                temperatureText.setText(String.format("%.0f°", temperature));
                humidityText.setText(String.format("%.0f%%", humidity));
                windText.setText(String.format("%.0f km/h", windSpeed));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
