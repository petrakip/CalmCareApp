package com.example.diamonds;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

public class VideoPlayerActivity extends AppCompatActivity {

    YouTubePlayerView youTubePlayerView;
    TextView titleView, descView, goListBtn, micBtn;
    private YouTubePlayer youTubePlayerInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Views
        goListBtn = findViewById(R.id.goVideoExerciseList);
        micBtn = findViewById(R.id.videoExerciseMic);
        youTubePlayerView = findViewById(R.id.youtubePlayerView);
        titleView = findViewById(R.id.videoItemTitle);
        descView = findViewById(R.id.descView);

        // Data from intent
        String videoId = getIntent().getStringExtra("videoId");
        String title = getIntent().getStringExtra("title");
        String htmlFile = getIntent().getStringExtra("htmlFile");

        titleView.setText(title);

        // Load description from HTML file
        try {
            InputStream input = getAssets().open(htmlFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder htmlContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                htmlContent.append(line);
            }
            reader.close();
            descView.setText(HtmlCompat.fromHtml(htmlContent.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        } catch (Exception e) {
            descView.setText("Δεν ήταν δυνατή η φόρτωση της περιγραφής.");
            e.printStackTrace();
        }

        // YouTube Player
        getLifecycle().addObserver(youTubePlayerView);
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayerInstance = youTubePlayer;
                youTubePlayer.loadVideo(videoId, 0);
            }
        });

        // Video List Button
        goListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(VideoPlayerActivity.this, VideoExercises.class);
            startActivity(intent);
            finish();
        });

        // Voice Command Button
        micBtn.setOnClickListener(v -> {
            if (youTubePlayerInstance != null) {
                youTubePlayerInstance.pause();
            }

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Πείτε 'αρχική' ή 'λίστα'");

            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000);

            try {
                startActivityForResult(intent, 200);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Δεν υποστηρίζεται η φωνητική εισαγωγή", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (youTubePlayerInstance != null) {
            youTubePlayerInstance.pause();
        }

        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String command = result.get(0).toLowerCase();

                if (command.contains("αρχική")) {
                    startActivity(new Intent(this, MainActivity.class));
                } else if (command.contains("λίστα")) {
                    startActivity(new Intent(this, VideoExercises.class));
                } else {
                    Toast.makeText(this, "Δεν αναγνωρίστηκε εντολή", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
