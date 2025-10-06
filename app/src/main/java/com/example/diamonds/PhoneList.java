package com.example.diamonds;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.text.Normalizer;
import java.util.*;

public class PhoneList extends AppCompatActivity {
    // request codes
    private static final int VOICE_REQUEST_NAME = 1001;
    private static final int VOICE_REQUEST_PHONE = 1002;
    private static final int VOICE_COMMAND_CODE = 103;
    private static final int VOICE_COMMAND_NAME_REQUEST = 104;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri = null;
    private int imageChangePosition = -1;
    private String pendingVoiceCommand = null; // π.χ. "κλήση" ή "διαγραφή"
    RecyclerView recyclerView;
    PhoneListAdapter adapter;
    Map<String, List<Contact>> dataMap;
    ImageView addContactBtn, imageSelect, voiceAddContactBtn, phoneListMic;
    TextView goBack, callTitle, emptyMessage;
    List<Contact> contactList;
    EditText nameInput, phoneInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_list);

        // ids connection
        phoneListMic = findViewById(R.id.phoneListMic);
        goBack = findViewById(R.id.goMainFromPhoneList);
        callTitle = findViewById(R.id.callTitle);
        emptyMessage = findViewById(R.id.emptyMessage);
        recyclerView = findViewById(R.id.recyclerViewContacts);
        addContactBtn = findViewById(R.id.addContactBtn);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // mic operation
        phoneListMic.setOnClickListener(v -> startVoiceCommand());

        // go to previous screen (CallSupport)
        goBack.setOnClickListener(v -> {
            startActivity(new Intent(PhoneList.this, CallSupport.class));
            finish();
        });

        // add new contact
        addContactBtn.setOnClickListener(v -> showAddContactDialog());

        String category = getIntent().getStringExtra("category");
        updateTitle(category);

        // dummy data for sos category
        dataMap = new HashMap<>();
        dataMap.put("support", Arrays.asList(
                new Contact("Γραμμή Ψυχοκοινωνικής Υποστήριξης", "10306", null),
                new Contact("Γραμμή Βοήθειας <<ΥποΣΤΗΡΙΖΩ>>", "8001180015", "upostirizo_logo"),
                new Contact("Be Positive", "210 6923920", "be_positive_logo")));
        dataMap.put("sos", Arrays.asList(
                new Contact("Άμεση Βοήθεια", "100", "policeman_icon"),
                new Contact("ΕΚΑΒ", "166", "ekav_logo"),
                new Contact("Πυροσβεστική Υπηρεσία", "199", "fire_department"),
                new Contact("Εφημερεύοντα Δημόσια Νοσοκομεία", "1434", "hospital_icon"),
                new Contact("Γραμμή ζωής SOS", "1065", "sos_icon")
        ));

        // load contacts for a certain category
        contactList = loadContacts(category);
        if (contactList == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                contactList = new ArrayList<>(dataMap.getOrDefault(category, new ArrayList<>()));
            }
        }

        // adapter connection to show contact with custom view
        adapter = new PhoneListAdapter(this, contactList, contact -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contact.getPhone()));
            startActivity(intent);
        }, position -> selectImageForContact(position));

        recyclerView.setAdapter(adapter);
        adapter.setOnListChangedListener(isEmpty -> toggleEmptyMessage());

        toggleEmptyMessage();
    }

    // if there are no contacts show an empty message otherwise the list of contacts
    private void toggleEmptyMessage() {
        boolean isEmpty = contactList == null || contactList.isEmpty();
        emptyMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // set title of category
    private void updateTitle(String category) {
        switch (category) {
            case "familiar":
                callTitle.setText("Φίλοι / Οικογένεια");
                break;
            case "doctor":
                callTitle.setText("Γιατροί");
                break;
            case "support":
                callTitle.setText("Ειδικές Γραμμές Υποστήριξης");
                break;
            case "sos":
                callTitle.setText("SOS");
                break;
            default:
                callTitle.setText("Κατηγορία Επαφών");
        }
    }

    // window for adding contact with writing way or voice command
    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Νέα Επαφή");

        View dialogView = getLayoutInflater().inflate(R.layout.add_contact_layout, null);
        nameInput = dialogView.findViewById(R.id.editContactName);
        phoneInput = dialogView.findViewById(R.id.editContactPhone);
        imageSelect = dialogView.findViewById(R.id.contactImageSelect);
        voiceAddContactBtn = dialogView.findViewById(R.id.voiceAddContactBtn);

        // Reset default image at the beginning
        selectedImageUri = null;
        imageSelect.setImageResource(R.drawable.contact_person_icon);

        // Image Selection
        imageSelect.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 100);
            } else {
                requestImagePermissionIfNeeded();
            }
        });

        // Adding with voice
        voiceAddContactBtn.setOnClickListener(v -> {
            promptSpeechInput("Πείτε το όνομα της επαφής", VOICE_REQUEST_NAME);
        });

        builder.setView(dialogView);

        builder.setPositiveButton("Προσθήκη", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (!name.isEmpty() && !phone.isEmpty()) {
                String imageUriString = selectedImageUri != null ? selectedImageUri.toString() : null;

                Contact newContact = new Contact(name, phone, imageUriString);
                contactList.add(newContact);
                adapter.notifyItemInserted(contactList.size() - 1);

                saveContacts(getIntent().getStringExtra("category"));
                toggleEmptyMessage();

                selectedImageUri = null;
            } else {
                Toast.makeText(this, "Συμπληρώστε όνομα και τηλέφωνο", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Άκυρο", (dialog, which) -> {
            selectedImageUri = null;
            dialog.dismiss();
        });

        builder.create().show();
    }


    // Handling image display and speech recognition commands
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            // Access permission for URI
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }

            // change default image
            if (imageChangePosition >= 0) {
                Contact contact = contactList.get(imageChangePosition);
                contact.setImageUri(imageUri.toString());
                adapter.notifyItemChanged(imageChangePosition);
                saveContacts(getIntent().getStringExtra("category"));
                imageChangePosition = -1;
            } else {
                selectedImageUri = imageUri;
                if (imageSelect != null) {
                    imageSelect.setImageDrawable(null);
                    imageSelect.setImageURI(imageUri);
                }
            }
            return;
        }

        // voice recognition dialogs
        if (resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = removeAccents(results.get(0).toLowerCase().trim());

                switch (requestCode) {
                    case VOICE_REQUEST_NAME:
                        String capitalized = spokenText.substring(0, 1).toUpperCase() + spokenText.substring(1);
                        nameInput.setText(capitalized);
                        promptSpeechInput("Πείτε τον αριθμό τηλεφώνου", VOICE_REQUEST_PHONE);
                        break;

                    case VOICE_REQUEST_PHONE:
                        phoneInput.setText(spokenText.replaceAll("\\D", ""));
                        Toast.makeText(this, "Τα πεδία συμπληρώθηκαν", Toast.LENGTH_SHORT).show();
                        selectedImageUri = null;
                        break;

                    case VOICE_COMMAND_CODE:
                        if (spokenText.contains("προσθηκη") || spokenText.contains("εισαγωγη")) {
                            showAddContactDialog();
                        } else if (spokenText.contains("πισω")) {
                            startActivity(new Intent(this, CallSupport.class));
                            finish();
                        } else if (spokenText.contains("αρχική")) {
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else if (spokenText.contains("κληση") || spokenText.contains("κλιση")) {
                            pendingVoiceCommand = "call";
                            promptSpeechInput("Ποιον θέλεις να καλέσεις;", VOICE_COMMAND_NAME_REQUEST);
                        } else if (spokenText.contains("διαγραφη") || spokenText.contains("αφαιρεση")) {
                            pendingVoiceCommand = "delete";
                            promptSpeechInput("Ποια επαφή θέλετε να διαγράψετε;", VOICE_COMMAND_NAME_REQUEST);
                        } else {
                            Toast.makeText(this, "Δεν αναγνωρίστηκε εντολή", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case VOICE_COMMAND_NAME_REQUEST:
                        for (Contact c : contactList) {
                            String normalizedContact = removeAccents(c.getName().toLowerCase());
                            if (spokenText.contains(normalizedContact)) {
                                if ("call".equals(pendingVoiceCommand)) {
                                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + c.getPhone())));
                                } else if ("delete".equals(pendingVoiceCommand)) {
                                    contactList.remove(c);
                                    adapter.notifyDataSetChanged();
                                    saveContacts(getIntent().getStringExtra("category"));
                                    toggleEmptyMessage();
                                    Toast.makeText(this, "Η επαφή διαγράφηκε", Toast.LENGTH_SHORT).show();
                                }
                                pendingVoiceCommand = null;
                                return;
                            }
                        }
                        Toast.makeText(this, "Δεν βρέθηκε επαφή με αυτό το όνομα", Toast.LENGTH_SHORT).show();
                        pendingVoiceCommand = null;
                        break;
                }
            }
        }
    }

    // save contacts in local JSON file
    private void saveContacts(String category) {
        SharedPreferences prefs = getSharedPreferences("contacts", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(category, new Gson().toJson(contactList));
        editor.apply();
    }

    // load contacts from local JSON file
    private List<Contact> loadContacts(String category) {
        SharedPreferences prefs = getSharedPreferences("contacts", MODE_PRIVATE);
        String json = prefs.getString(category, null);
        if (json == null) return null;
        return new ArrayList<>(Arrays.asList(new Gson().fromJson(json, Contact[].class)));
    }

    // if user clicks on image to change it
    private void selectImageForContact(int position) {
        imageChangePosition = position;
        openImagePicker();
    }

    // user can select a image from gallery of his phone
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    private void promptSpeechInput(String prompt, int requestCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-GR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Η φωνητική λειτουργία δεν υποστηρίζεται", Toast.LENGTH_SHORT).show();
        }
    }

    private void startVoiceCommand() {
        promptSpeechInput("Πείτε εντολή: προσθήκη, πίσω, αρχική, όνομα για κλήση ή διαγραφή", VOICE_COMMAND_CODE);
    }

    // remove accents for voice input
    public static String removeAccents(String input) {
        if (input == null) return null;
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private void requestImagePermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 200);
            } else {
                openImagePicker();
            }
        } else if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 201);
        } else {
            openImagePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == 200 || requestCode == 201)
                && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            Toast.makeText(this, "Απαραίτητη η άδεια για επιλογή εικόνας", Toast.LENGTH_SHORT).show();
        }
    }

}
