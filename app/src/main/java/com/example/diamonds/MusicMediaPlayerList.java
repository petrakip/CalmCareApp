package com.example.diamonds;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

public class MusicMediaPlayerList extends AppCompatActivity {

    ListView songListView;
    Button goBackButton;

    private String getDurationFromRaw(Context context, int rawResId) throws IOException {
        android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
        String duration = "00:00";
        try {
            android.net.Uri uri = android.net.Uri.parse("android.resource://" + context.getPackageName() + "/" + rawResId);
            retriever.setDataSource(context, uri);

            String time = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            int timeInMillisec = Integer.parseInt(time);
            int minutes = (timeInMillisec / 1000) / 60;
            int seconds = (timeInMillisec / 1000) % 60;
            duration = String.format("%02d:%02d", minutes, seconds);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return duration;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_media_player_list);

        // Return to Main Activity
        goBackButton = findViewById(R.id.goBackBtn);
        goBackButton.setOnClickListener(v -> {
            Intent intent = new Intent(MusicMediaPlayerList.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Optionally, closes MusicMediaPlayer if you don't want to go back
        });

        songListView = findViewById(R.id.songListView);
        ArrayList<SongItem> songItems = new ArrayList<>();

        // Add songs
        try {
            songItems.add(new SongItem("Weekends", getDurationFromRaw(this, R.raw.weekends)));
            songItems.add(new SongItem("Breath of Life", getDurationFromRaw(this, R.raw.breathoflife)));
            songItems.add(new SongItem("Chill Abstract Intention", getDurationFromRaw(this, R.raw.chillabstractintention)));
            songItems.add(new SongItem("Water Fountain Healing Music", getDurationFromRaw(this, R.raw.waterfountainhealingmusic)));
            songItems.add(new SongItem("God is Always with Me", getDurationFromRaw(this, R.raw.godisalwayswithme)));
            songItems.add(new SongItem("Good Night Lofi Cozy Chill", getDurationFromRaw(this, R.raw.goodnightloficozychill)));
            songItems.add(new SongItem("Indigo Lofi Hip-hop", getDurationFromRaw(this, R.raw.indigolofihiphop)));
            songItems.add(new SongItem("The Beat of Nature", getDurationFromRaw(this, R.raw.thebeatofnature)));
            songItems.add(new SongItem("Sunset Reverie", getDurationFromRaw(this, R.raw.sunsetreverie)));
            songItems.add(new SongItem("Soft Birds Sound", getDurationFromRaw(this, R.raw.softbirdssound)));
            songItems.add(new SongItem("Spring Me Loby", getDurationFromRaw(this, R.raw.springmelody)));
            songItems.add(new SongItem("Delicate Reverie", getDurationFromRaw(this, R.raw.delicatereveriebackground)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Set custom adapter
        SongAdapter adapter = new SongAdapter(this, songItems);
        songListView.setAdapter(adapter);

        songListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MusicMediaPlayerList.this, MusicMediaPlayer.class);
            intent.putExtra("selectedIndex", position);
            startActivity(intent);
        });
    }
}
