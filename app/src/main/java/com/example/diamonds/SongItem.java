package com.example.diamonds;

public class SongItem {
    private String title;
    private String duration;

    public SongItem(String title, String duration) {
        this.title = title;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }
}
