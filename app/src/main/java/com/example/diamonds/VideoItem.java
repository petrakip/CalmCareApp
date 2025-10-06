package com.example.diamonds;

public class VideoItem {
    private String videoId;
    private String title;
    private String shortDescription;
    private String fullDescription;

    public VideoItem(String videoId, String title, String shortDescription, String fullDescription) {
        this.videoId = videoId;
        this.title = title;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
    }

    public String getVideoId() { return videoId; }
    public String getTitle() { return title; }
    public String getShortDescription() { return shortDescription; }
    public String getFullDescription() { return fullDescription; }
}


