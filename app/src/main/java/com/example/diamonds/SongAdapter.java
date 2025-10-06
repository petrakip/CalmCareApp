package com.example.diamonds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class SongAdapter extends ArrayAdapter<SongItem> {

    Context context;
    ArrayList<SongItem> songs;

    public SongAdapter(Context context, ArrayList<SongItem> songs) {
        super(context, R.layout.song_list_item, songs);
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = LayoutInflater.from(context).inflate(R.layout.song_list_item, parent, false);
        }

        ImageView songIcon = row.findViewById(R.id.songListIcon);
        TextView songTitle = row.findViewById(R.id.songListTitle);
        TextView songDuration = row.findViewById(R.id.songListDuration);

        SongItem item = songs.get(position);
        songTitle.setText(item.getTitle());
        songDuration.setText(item.getDuration());
        songIcon.setImageResource(R.drawable.music_note_icon);

        return row;
    }
}