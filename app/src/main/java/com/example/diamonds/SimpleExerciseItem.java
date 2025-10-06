package com.example.diamonds;

public class SimpleExerciseItem {
    private int imageResId;
    private String title;
    private String shortDescription;
    private String fullDescription;

    public SimpleExerciseItem(int imageResId, String title, String shortDescription, String fullDescription) {
        this.imageResId = imageResId;
        this.title = title;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
    }

    public int getImageResId() { return imageResId; }
    public String getTitle() { return title; }
    public String getShortDescription() { return shortDescription; }
    public String getFullDescription() { return fullDescription; }
}
