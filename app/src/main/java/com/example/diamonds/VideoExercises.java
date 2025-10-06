package com.example.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VideoExercises extends AppCompatActivity {

    RecyclerView recyclerView;
    VideoAdapter videoAdapter;
    List<VideoItem> videoItems;
    TextView goBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_exercises);

        goBackBtn = findViewById(R.id.goRelaxationExercisesFromVideo);
        recyclerView = findViewById(R.id.videoRecyclerView);

        goBackBtn.setOnClickListener(view -> {
            Intent intent = new Intent(VideoExercises.this, RelaxationExercises.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        videoItems = new ArrayList<>();
        videoItems.add(new VideoItem("ihO02wUzgkc", "Προοδευτική Μυϊκή Χαλάρωση", "Μέθοδος χαλάρωσης που περιλαμβάνει σταδιακή σύσπαση και χαλάρωση των μυών για μείωση του άγχους και βελτίωση της ευεξίας.", "progressive_relaxation.html"));
        videoItems.add(new VideoItem("NVPrxcR_RZI", "Ισχυρή Οπτικοποίηση", "Τεχνική νοερής απεικόνισης που χρησιμοποιεί θετικές εικόνες για ενίσχυση της ψυχικής συγκέντρωσης και μείωση του στρες.", "powerful_visualization.html"));
        videoItems.add(new VideoItem("BlWo7sqWLNk", "Διαλογισμός Σάρωσης σώματος", "Τεχνική ενσυνειδητότητας που καθοδηγεί την προσοχή σταδιακά σε κάθε μέρος του σώματος για βαθιά χαλάρωση και αυτοπαρατήρηση.", "body_scan_meditation.html"));
        videoItems.add(new VideoItem("2LFWUogm2f4", "Ενσυνείδητη Παρατήρηση", "Άσκηση εστίασης της προσοχής σε ένα αντικείμενο ή ερέθισμα, με πλήρη επίγνωση της παρούσας στιγμής.", "mindful_observation.html"));
        videoItems.add(new VideoItem("30VMIEmA114", "Άσκηση Γείωσης", "Τεχνική επαναφοράς της προσοχής στο παρόν, ιδανική για διαχείριση άγχους και συναισθηματικής υπερφόρτωσης.", "grounding_exercise.html"));

        videoAdapter = new VideoAdapter(videoItems, this);
        recyclerView.setAdapter(videoAdapter);
    }
}

