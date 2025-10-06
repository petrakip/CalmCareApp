package com.example.diamonds;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EventDay {

    private ArrayList<Integer> moods = new ArrayList<>();
    private ArrayList<String> notes = new ArrayList<>();
    private Calendar calendar = Calendar.getInstance();

    public EventDay() {
        // It is necessary to exist default constructor for GSON
    }

    public EventDay(Date date) {
        calendar.setTime(date);
    }

    public int getDay() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public int getMonth() {
        return calendar.get(Calendar.MONTH) + 1;
    }

    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    public Date getCalendarDate() {
        return calendar.getTime();
    }

    public ArrayList<Integer> getMoods() {
        return moods;
    }

    public void addMood(int mood) {
        if (!moods.contains(mood) && moods.size() < 5) {
            moods.add(mood);
        }
    }

    public ArrayList<String> getNotes() {
        return notes;
    }

    public void addNote(String note) {
        if (!note.trim().isEmpty()) {
            notes.add(note.trim());
        }
    }
}
