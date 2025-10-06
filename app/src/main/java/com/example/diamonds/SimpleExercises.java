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

public class SimpleExercises extends AppCompatActivity {

    RecyclerView recyclerView;
    List<SimpleExerciseItem> exerciseList;
    TextView goBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_exercises);

        goBackBtn = findViewById(R.id.goRelaxationExercisesFromSimpleExercise);
        recyclerView = findViewById(R.id.simpleExerciseRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        goBackBtn.setOnClickListener(view -> {
            Intent intent = new Intent(SimpleExercises.this, RelaxationExercises.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

        });

        exerciseList = new ArrayList<>();

        exerciseList.add(new SimpleExerciseItem(
                R.drawable.relaxing_movement,
                "Χαλαρωτική Κίνηση",
                "Απαλές και ρυθμικές κινήσεις του σώματος που διώχνουν την ένταση και βοηθούν στην επανασύνδεση με τη φυσική σου ροή.",
                "relaxing_movement.html"
        ));

        exerciseList.add(new SimpleExerciseItem(
                R.drawable.music_observation,
                "Μουσική Παρατήρηση",
                "Εστίασε στην ακρόαση μουσικής με πλήρη επίγνωση, παρατηρώντας τους ήχους και τις αντιδράσεις του σώματός σου χωρίς σκέψεις.",
                "music_observation.html"
        ));

        exerciseList.add(new SimpleExerciseItem(
                R.drawable.journaling,
                "Χαλαρωτικό Γράψιμο",
                "Κατέγραψε σκέψεις και συναισθήματα χωρίς φιλτράρισμα, για να αποφορτιστείς και να γνωρίσεις καλύτερα τον εσωτερικό σου κόσμο.",
                "journaling.html"
        ));

        exerciseList.add(new SimpleExerciseItem(
                R.drawable.mantra_focus,
                "Άσκηση με Λέξεις",
                "Επανάλαβε μια λέξη ή φράση για να ηρεμήσεις το μυαλό και να επαναφέρεις τη συγκέντρωσή σου στο παρόν.",
                "mantra_focus.html"
        ));

        exerciseList.add(new SimpleExerciseItem(
                R.drawable.nature_observation,
                "Παρατήρηση της Φύσης",
                "Αντί να σκέφτεσαι, παρατήρησε με προσοχή ένα στοιχείο της φύσης και νιώσε τη σύνδεση με την παρούσα στιγμή.",
                "nature_observation.html"
        ));


        SimpleExerciseAdapter adapter = new SimpleExerciseAdapter(exerciseList, this);
        recyclerView.setAdapter(adapter);
    }
}

