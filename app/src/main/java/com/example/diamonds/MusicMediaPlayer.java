package com.example.diamonds;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class MusicMediaPlayer extends AppCompatActivity {

    ImageView play, prev, next, minVolume, maxVolume, mic;
    TextView songTitle, progressTimeDuration, reverseTimeDuration;
    SeekBar mSeekBarTime, mSeekBarVol;
    MediaPlayer mMediaPlayer;
    Button musicBackBtn;
    private AudioManager mAudioManager;
    int currentIndex = 0;
    boolean isActivityRunning = true;
    Handler handler;
    ArrayList<Integer> songs;

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_media_player);

        musicBackBtn = findViewById(R.id.musicBackBtn);
        musicBackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MusicMediaPlayer.this, MusicMediaPlayerList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        Intent intent = getIntent();
        currentIndex = intent.getIntExtra("selectedIndex", 0);

        // Initialize views
        play = findViewById(R.id.play);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        songTitle = findViewById(R.id.songTitle);
        mSeekBarTime = findViewById(R.id.seekBarTime);
        mSeekBarVol = findViewById(R.id.seekBarVol);
        progressTimeDuration = findViewById(R.id.progressTimeDuration);
        reverseTimeDuration = findViewById(R.id.reverseTimeDuration);
        minVolume = findViewById(R.id.minSoundBox);
        maxVolume = findViewById(R.id.maxSoundBox);
        mic = findViewById(R.id.musicPlayerMic);

        mic.setOnClickListener(v -> startVoiceRecognition());

        // adding songs to arraylist
        songs = new ArrayList<>();
        songs.add(R.raw.weekends);
        songs.add(R.raw.breathoflife);
        songs.add(R.raw.chillabstractintention);
        songs.add(R.raw.waterfountainhealingmusic);
        songs.add(R.raw.godisalwayswithme);
        songs.add(R.raw.goodnightloficozychill);
        songs.add(R.raw.indigolofihiphop);
        songs.add(R.raw.thebeatofnature);
        songs.add(R.raw.sunsetreverie);
        songs.add(R.raw.softbirdssound);
        songs.add(R.raw.springmelody);
        songs.add(R.raw.delicatereveriebackground);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxV = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curV = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mSeekBarVol.setMax(maxV);
        mSeekBarVol.setProgress(curV);

        mSeekBarVol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        minVolume.setOnClickListener(new View.OnClickListener() {
            boolean isMuted = false;
            @Override
            public void onClick(View view) {
                if (!isMuted) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                    mSeekBarVol.setProgress(0);
                    minVolume.setImageResource(R.drawable.no_volume_icon);
                    isMuted = true;
                } else {
                    int defaultVolume = mSeekBarVol.getMax() / 2;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVolume, 0);
                    mSeekBarVol.setProgress(defaultVolume);
                    minVolume.setImageResource(R.drawable.min_volume_icon);
                    isMuted = false;
                }
            }
        });

        maxVolume.setOnClickListener(view -> {
            int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0);
            mSeekBarVol.setProgress(max);
        });

        playSong(currentIndex);

        handler = new Handler(msg -> {
            int currentPosition = msg.what;
            if (mMediaPlayer != null) {
                int totalDuration = mMediaPlayer.getDuration();
                mSeekBarTime.setProgress(currentPosition);
                progressTimeDuration.setText(formatTime(currentPosition));
                reverseTimeDuration.setText("-" + formatTime(totalDuration - currentPosition));
            }
            return true;
        });

        // Thread for playing
        new Thread(() -> {
            while (isActivityRunning) {
                try {
                    if (mMediaPlayer != null) {
                        synchronized (mMediaPlayer) {
                            try {
                                if (mMediaPlayer.isPlaying()) {
                                    handler.sendMessage(Message.obtain(handler, mMediaPlayer.getCurrentPosition()));
                                }
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void playSong(int index) {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        mMediaPlayer = MediaPlayer.create(getApplicationContext(), songs.get(index));

        mMediaPlayer.setOnPreparedListener(mp -> {
            mSeekBarTime.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.start();
            play.setImageResource(R.drawable.pause_icon);
            songNames();
        });

        mMediaPlayer.setOnCompletionListener(mp -> {
            currentIndex = (currentIndex + 1) % songs.size();
            playSong(currentIndex);
        });

        mSeekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mMediaPlayer.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        play.setOnClickListener(view -> {
            if (mMediaPlayer != null) {
                try {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        play.setImageResource(R.drawable.play_icon);
                    } else {
                        mMediaPlayer.start();
                        play.setImageResource(R.drawable.pause_icon);
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
            songNames();
        });

        next.setOnClickListener(view -> {
            currentIndex = (currentIndex + 1) % songs.size();
            playSong(currentIndex);
        });

        prev.setOnClickListener(view -> {
            currentIndex = (currentIndex - 1 + songs.size()) % songs.size();
            playSong(currentIndex);
        });
    }

    private void songNames() {
        String[] titles = {
                "Weekends", "Breath of Life", "Chill Abstract Intention", "Water Fountain Healing Music",
                "God is Always with Me", "Good Night Lofi Cozy Chill", "Indigo Lofi Hip-hop",
                "The Beat of Nature", "Sunset Reverie", "Soft Birds Sound", "Spring Me Loby", "Delicate Reverie"
        };
        if (currentIndex >= 0 && currentIndex < titles.length) {
            songTitle.setText(titles[currentIndex]);
        }
    }

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    // Voice Recognition Operation
    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-GR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Πείτε: \"αρχική\", \"παίξε\", \"σταμάτα\", \"επόμενο\", \"προηγούμενο\", \"δυνάμωσε\", \"χαμήλωσε\", \"λίστα κομματιών\", \"σίγαση\"");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    // Result from voice input
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String command = results.get(0).toLowerCase();
                handleVoiceCommand(command);
            }
        }
    }

    // word matching from phonetic function to function execution
    private void handleVoiceCommand(String command) {
        boolean wasStopCommand = false;

        if (command.contains("αρχική")) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        } else if (command.contains("παίξε") || command.contains("έναρξη") || command.contains("play") || command.contains("ξεκίνα")) {
            if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
                play.setImageResource(R.drawable.pause_icon);
            }
            return;
        } else if (command.contains("σταμάτα") || command.contains("stop")) {
            wasStopCommand = true;
            if (mMediaPlayer != null) {
                try {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                play.setImageResource(R.drawable.play_icon);
            }
        } else if (command.contains("επόμενο")) {
            currentIndex = (currentIndex + 1) % songs.size();
            playSong(currentIndex);
        } else if (command.contains("προηγούμενο")) {
            currentIndex = (currentIndex - 1 + songs.size()) % songs.size();
            playSong(currentIndex);
        } else if (command.contains("σίγαση") || command.contains("σιγαση") || command.contains("mute")) {
            if (mAudioManager != null) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                mSeekBarVol.setProgress(0);
                minVolume.setImageResource(R.drawable.no_volume_icon);
            }
        } else if (command.contains("δυνάμωσε")) {
            if (mAudioManager != null) {
                int cur = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                if (cur == 0) {
                    int defaultVolume = max / 2;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVolume, 0);
                    mSeekBarVol.setProgress(defaultVolume);
                    minVolume.setImageResource(R.drawable.min_volume_icon);
                } else if (cur < max) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, cur + 1, 0);
                    mSeekBarVol.setProgress(cur + 1);
                }
            }
        } else if (command.contains("χαμήλωσε")) {
            if (mAudioManager != null) {
                int cur = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (cur > 0) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, cur - 1, 0);
                    mSeekBarVol.setProgress(cur - 1);
                }
            }
        } else if (command.contains("πίσω") || command.contains("λίστα κομματιών") || command.contains("λίστα") || command.contains("κομματιών")) {
            Intent intent = new Intent(MusicMediaPlayer.this, MusicMediaPlayerList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return;
        }

        // Automatic music continuation unless we said "stop"
        if (!wasStopCommand && mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            play.setImageResource(R.drawable.pause_icon);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityRunning = false;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onBackPressed();
    }
}
