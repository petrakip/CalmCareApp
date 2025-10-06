package com.example.diamonds;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;

public class CalendarApp extends AppCompatActivity {
    private static final int VOICE_CHOICE_REQUEST_CODE = 101;
    private static final int VOICE_MOOD_REQUEST_CODE = 102;
    private static final int VOICE_NOTE_REQUEST_CODE = 103;
    private static final String FILE_NAME = "calendar_data.json";
    private MaterialButton calendarSettingsBtn, micCalendarBtn, homeCalendarButton;
    private Date selectedDate = null;
    private ArrayList<EventDay> allEvents = new ArrayList<>();
    private CalendarView calendarView;
    private NotesAdapter notesAdapter;
    TextView emptyNotesMessage;
    RecyclerView notesRecyclerView;
    private static final Map<Integer, List<String>> MOOD_KEYWORDS = new HashMap<Integer, List<String>>() {
        {
            put(1, Arrays.asList("χαρούμενος", "χαρούμενη", "χαρά", "ευτυχισμένος", "ευτυχισμένη", "ευτυχία"));
            put(2, Arrays.asList("καλά", "ήρεμος", "ήρεμη", "εντάξει", "okay"));
            put(3, Arrays.asList("αδιάφορα", "ουδέτερος", "ουδέτερη", "ουδέτερα","κανονικά"));
            put(4, Arrays.asList("κακά", "θυμωμένος", "θυμωμένη", "στρες", "άγχος", "νεύρα", "στεναχωρημένος", "στεναχωρημένη"));
            put(5, Arrays.asList("χάλια", "απαράδεκτα", "θλίψη", "κατάθλιψη", "πολύ άσχημα", "θλιμμένος", "θλιμμένη", "καταθλιπτικά"));

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_app);

        // Hide status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Return to Main Activity
        homeCalendarButton = findViewById(R.id.homeCalendarButton);
        homeCalendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarApp.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Optionally, closes CalendarApp if you don't want to go back
        });

        micCalendarBtn = findViewById(R.id.micCalendarButton);
        micCalendarBtn.setOnClickListener(v -> {
            if (selectedDate == null) {
                Toast.makeText(this, "Πρώτα επίλεξτε ημερομηνία", Toast.LENGTH_SHORT).show();
            } else {
                // Start voice recognition to choose between "Mood" or "Note"
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-GR");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Πείτε 'διάθεση' ή 'σημείωση'");
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 4000);
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
                try {
                    startActivityForResult(intent, VOICE_CHOICE_REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "Δεν υποστηρίζεται η αναγνώριση φωνής", Toast.LENGTH_SHORT).show();
                }
            }
        });

        calendarSettingsBtn = findViewById(R.id.calendarSettingsBtn);
        calendarSettingsBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(CalendarApp.this)
                    .setTitle("Επιλογές")
                    .setItems(new CharSequence[]{"Επαναφορά όλων"}, (dialog, which) -> {
                        if (which == 0) confirmReset();
                    })
                    .setNegativeButton("Άκυρο", null)
                    .show();
        });

        allEvents = loadEventsFromFile(); // Φόρτωση δεδομένων
        if (allEvents.isEmpty()) allEvents.add(new EventDay(new Date()));

        emptyNotesMessage = findViewById(R.id.emptyNotesMessage);
        calendarView = findViewById(R.id.calendar_view);
        calendarView.setSelectedDate(selectedDate);
        calendarView.updateCalendar(allEvents); // Ενημέρωση του ημερολογίου με όλα τα δεδομένα
        updateMoodLegend(allEvents);

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesAdapter = new NotesAdapter();
        notesRecyclerView.setAdapter(notesAdapter);

        calendarView.setEventHandler(new CalendarView.EventHandler() {
            private long lastClickTime = 0;
            @Override
            public void onDayClickListener(Date calendar) {
                long currentTime = System.currentTimeMillis();

                if (selectedDate != null && isSameDay(selectedDate, calendar) && (currentTime - lastClickTime) < 400) {
                    // Add note with double click
                    showNoteDialog(calendar);
                } else {
                    // Choose day with single click
                    selectedDate = calendar;

                    calendarView.setSelectedDate(selectedDate);
                    calendarView.updateCalendar(allEvents);

                    EventDay selectedEvent = getEventForDate(calendar);
                    if (selectedEvent != null && !selectedEvent.getNotes().isEmpty()) {
                        notesAdapter.submitList(selectedEvent.getNotes());
                        notesRecyclerView.setVisibility(View.VISIBLE);
                        notesRecyclerView.setBackground(ContextCompat.getDrawable(CalendarApp.this, R.drawable.note_table_border));
                        emptyNotesMessage.setVisibility(View.GONE);
                    } else {
                        notesAdapter.submitList(new ArrayList<>());
                        notesRecyclerView.setVisibility(View.INVISIBLE);
                        emptyNotesMessage.setVisibility(View.VISIBLE);
                    }
                }

                lastClickTime = currentTime;
            }


            @Override
            public void onPrevMonthClickListener(Calendar calendar) {
                calendarView.setSelectedDate(selectedDate);
                calendarView.updateCalendar(allEvents);
                updateMoodLegend(allEvents);

            }

            @Override
            public void onNextMonthClickListener(Calendar calendar) {
                calendarView.setSelectedDate(selectedDate);
                calendarView.updateCalendar(allEvents);
                updateMoodLegend(allEvents);
            }

            @Override
            public void onFilterClickListener() {
                String[] moodLabels = {"Χαρούμεν-ος/η", "Καλά", "Αδιάφορα", "Στεναχωρημέν-ος/η", "Χάλια"};
                int[] moodValues = {1, 2, 3, 4, 5};

                new AlertDialog.Builder(CalendarApp.this)
                        .setTitle("Φιλτράρισμα ημερών")
                        .setItems(moodLabels, (dialog, which) -> {
                            int selectedMood = moodValues[which];
                            ArrayList<EventDay> filtered = new ArrayList<>();

                            for (EventDay day : allEvents) {
                                if (day.getMoods().contains(selectedMood)) {
                                    filtered.add(day);
                                }
                            }

                            calendarView.setSelectedDate(selectedDate);
                            calendarView.updateCalendar(filtered);
                            updateMoodLegend(filtered);

                            Toast.makeText(CalendarApp.this, "Εμφάνιση μόνο: " + moodLabels[which], Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Ακύρωση", (dialog, which) -> {
                            calendarView.setSelectedDate(selectedDate);
                            calendarView.updateCalendar(allEvents);
                            updateMoodLegend(allEvents);
                        })
                        .show();
            }

        });

        setupMoodButtons();
    }

    private void setupMoodButtons() {
        findViewById(R.id.happy_icon).setOnClickListener(v -> addMoodToSelectedDay(1));
        findViewById(R.id.good_icon).setOnClickListener(v -> addMoodToSelectedDay(2));
        findViewById(R.id.neutral_icon).setOnClickListener(v -> addMoodToSelectedDay(3));
        findViewById(R.id.bad_icon).setOnClickListener(v -> addMoodToSelectedDay(4));
        findViewById(R.id.worst_icon).setOnClickListener(v -> addMoodToSelectedDay(5));
    }

    private void addMoodToSelectedDay(int mood) {
        if (selectedDate == null) {
            Toast.makeText(this, "Πρώτα επίλεξτε ημερομηνία", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.after(new Date())) {
            Toast.makeText(this, "Δεν μπορείτε να προσθέσετε συναίσθημα σε μελλοντική ημερομηνία", Toast.LENGTH_SHORT).show();
            return;
        }

        EventDay event = getEventForDate(selectedDate);
        if (event != null) {
            if (event.getMoods().contains(mood)) {
                Toast.makeText(this, "Αυτό το συναίσθημα έχει ήδη οριστεί για τη μέρα", Toast.LENGTH_SHORT).show();
                return;
            }
            event.addMood(mood);
        } else {
            event = new EventDay(selectedDate);
            event.addMood(mood);
            allEvents.add(event);
        }

        calendarView.updateCalendar(allEvents);
        updateMoodLegend(allEvents);
        saveEventsToFile();
    }

    private void showNoteDialog(Date date) {
        EventDay selectedEvent = getEventForDate(date);
        if (selectedEvent == null) {
            selectedEvent = new EventDay(date);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Σημειώσεις για " + date.toString());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        EditText input = new EditText(this);
        input.setHint("Νέα σημείωση");

        TextView notesText = new TextView(this);
        StringBuilder sb = new StringBuilder();
        for (String note : selectedEvent.getNotes()) {
            sb.append("• ").append(note).append("\n");
        }
        notesText.setText(sb.toString());

        layout.addView(notesText);
        layout.addView(input);

        builder.setView(layout);

        EventDay finalSelectedEvent = selectedEvent;
        builder.setPositiveButton("Προσθήκη", (dialog, which) -> {
            String noteText = input.getText().toString().trim();
            if (!noteText.isEmpty()) {
                if (!allEvents.contains(finalSelectedEvent)) {
                    allEvents.add(finalSelectedEvent);
                }
                finalSelectedEvent.addNote(noteText);
                saveEventsToFile();
                calendarView.updateCalendar(allEvents);
                Toast.makeText(this, "Η σημείωση προστέθηκε!", Toast.LENGTH_SHORT).show();
                notesAdapter.submitList(finalSelectedEvent.getNotes());
                notesRecyclerView.setVisibility(View.VISIBLE);
                notesRecyclerView.setBackground(ContextCompat.getDrawable(CalendarApp.this, R.drawable.note_table_border));
                emptyNotesMessage.setVisibility(View.GONE);
            }

        });

        builder.setNegativeButton("Κλείσιμο", null);
        builder.show();
    }

    private EventDay getEventForDate(Date date) {
        for (EventDay event : allEvents) {
            if (isSameDay(date, event.getCalendarDate())) {
                return event;
            }
        }
        return null;
    }

    private boolean isSameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("Επαναφορά")
                .setMessage("Θέλετε σίγουρα να διαγραφούν όλα τα δεδομένα;")
                .setPositiveButton("Ναι", (dialog, which) -> {
                    allEvents.clear();
                    allEvents.add(new EventDay(new Date()));
                    calendarView.updateCalendar(allEvents);
                    updateMoodLegend(allEvents);
                    saveEventsToFile();
                    notesAdapter.submitList(new ArrayList<>());
                    Toast.makeText(this, "Τα δεδομένα διαγράφηκαν", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Όχι", null)
                .show();
    }

    private void updateMoodLegend(ArrayList<EventDay> events) {
        int happy = 0, good = 0, neutral = 0, bad = 0, worst = 0;

        for (EventDay day : events) {
            ArrayList<Integer> moods = day.getMoods();

            if (moods.contains(1)) happy++;
            if (moods.contains(2)) good++;
            if (moods.contains(3)) neutral++;
            if (moods.contains(4)) bad++;
            if (moods.contains(5)) worst++;
        }

        ((TextView) findViewById(R.id.count_happy)).setText(String.valueOf(happy));
        ((TextView) findViewById(R.id.count_good)).setText(String.valueOf(good));
        ((TextView) findViewById(R.id.count_neutral)).setText(String.valueOf(neutral));
        ((TextView) findViewById(R.id.count_bad)).setText(String.valueOf(bad));
        ((TextView) findViewById(R.id.count_worst)).setText(String.valueOf(worst));
    }

    private void saveEventsToFile() {
        try {
            String json = new Gson().toJson(allEvents);
            File file = new File(getFilesDir(), FILE_NAME);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(json.getBytes());
                fos.flush();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Αποτυχία αποθήκευσης", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<EventDay> loadEventsFromFile() {
        File file = new File(getFilesDir(), FILE_NAME);
        if (!file.exists()) return new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return new Gson().fromJson(sb.toString(), new TypeToken<ArrayList<EventDay>>(){}.getType());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results == null || results.isEmpty()) return;
            String spokenText = results.get(0).toLowerCase(new Locale("el"));

            if (requestCode == VOICE_CHOICE_REQUEST_CODE) {
                if (spokenText.contains("διάθεση")) {
                    // Start new recognition for mood input
                    startVoiceRecognition(VOICE_MOOD_REQUEST_CODE, "Πείτε το συναίσθημά σας");
                } else if (spokenText.contains("σημείωση")) {
                    // Start new recognition for note input
                    startVoiceRecognition(VOICE_NOTE_REQUEST_CODE, "Πείτε τη σημείωσή σας");
                } else {
                    Toast.makeText(this, "Πρέπει να πείτε 'διάθεση' ή 'σημείωση'", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == VOICE_MOOD_REQUEST_CODE) {
                handleVoiceMood(spokenText);
            } else if (requestCode == VOICE_NOTE_REQUEST_CODE) {
                EventDay event = getEventForDate(selectedDate);
                if (event == null) {
                    event = new EventDay(selectedDate);
                    allEvents.add(event);
                }
                event.addNote(spokenText);
                saveEventsToFile();
                calendarView.updateCalendar(allEvents);
                notesAdapter.submitList(event.getNotes());
                notesRecyclerView.setVisibility(View.VISIBLE);
                notesRecyclerView.setBackground(ContextCompat.getDrawable(this, R.drawable.note_table_border));
                emptyNotesMessage.setVisibility(View.GONE);
                Toast.makeText(this, "Η σημείωση προστέθηκε!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void startVoiceRecognition(int requestCode, String prompt) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-GR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 4000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);

        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Δεν υποστηρίζεται η αναγνώριση φωνής", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleVoiceMood(String text) {
        int detectedMood = -1;
        for (Map.Entry<Integer, List<String>> entry : MOOD_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (text.contains(keyword)) {
                    detectedMood = entry.getKey();
                    break;
                }
            }
            if (detectedMood != -1) break;
        }

        if (detectedMood != -1) {
            addMoodToSelectedDay(detectedMood);
        } else {
            Toast.makeText(this, "Δεν αναγνωρίστηκε κάποιο συναίσθημα", Toast.LENGTH_SHORT).show();
        }
    }

}
