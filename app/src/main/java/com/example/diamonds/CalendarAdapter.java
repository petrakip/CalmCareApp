package com.example.diamonds;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.devlomi.circularstatusview.CircularStatusView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarAdapter extends ArrayAdapter<Date> {

    private final ArrayList<EventDay> eventDayArrayList;
    // for view inflation
    private final LayoutInflater inflater;

    // this will be useful for displaying current month & next month & prev month dates
    private final Calendar currentDisplayed;

    private Date selectedDate;

    public CalendarAdapter(Context context, ArrayList<Date> cells, ArrayList<EventDay> events, Calendar currentDisplayed, Date selectedDate) {
        super(context, R.layout.day_view, cells);
        this.eventDayArrayList = events;
        this.currentDisplayed = currentDisplayed;
        this.selectedDate = selectedDate;
        inflater = LayoutInflater.from(context);
    }

    @Override
    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        // day in question
        Date date = (Date) getItem(position);
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);


            // today
            Date today = new Date();
            Calendar calendarToday = Calendar.getInstance();
            calendarToday.setTime(today);

            // inflate item if it does not exist yet
            if (view == null)
                view = inflater.inflate(R.layout.day_view, parent, false);
            MaterialTextView materialTextView = view.findViewById(R.id.day);
            CircularStatusView circularStatusView = view.findViewById(R.id.circular_status_view);

            materialTextView.setTextColor(Color.BLACK);
            circularStatusView.setVisibility(View.GONE);

            if (selectedDate != null && isSameDay(date, selectedDate)) {
                materialTextView.setBackgroundResource(R.drawable.selected_day);
            } else {
                materialTextView.setBackgroundColor(Color.TRANSPARENT); // καθαρίζεις αλλιώς
            }


            if (month != currentDisplayed.get(Calendar.MONTH) + 1 || year != currentDisplayed.get(Calendar.YEAR)) {
                // if this day is outside current month, grey it out
                materialTextView.setTextColor(Color.parseColor("#E0E0E0"));
                circularStatusView.setVisibility(View.GONE);
            } else if (year == calendarToday.get(Calendar.YEAR) && month == calendarToday.get(Calendar.MONTH) + 1 && day == calendarToday.get(Calendar.DAY_OF_MONTH)) {
                // if it is today, set it to blue text in grey circle
                materialTextView.setTextColor(Color.BLUE);
                circularStatusView.setVisibility(View.VISIBLE);
                circularStatusView.setPortionsCount(1);
                circularStatusView.setPortionWidth(10);
                circularStatusView.setPortionSpacing(25);

            }


            if (eventDayArrayList != null && eventDayArrayList.size() > 0) {
                for (EventDay eventDay : eventDayArrayList) {
                    //checking whether the currentDay is in eventDay or not
                    //if the current day contains event then circle will be visible
                    if (isEventDay(year, month, day, eventDay)) {
                        circularStatusView.setVisibility(View.VISIBLE);
                        setCircularProgress(circularStatusView, getMoodsFilter(eventDay));
                    }
                }

            }

            // set text
            materialTextView.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        }
        return view;
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

    //for my requirement there will be only 5 moods so the maximum size of mood is 5
    private void setCircularProgress(CircularStatusView circularStatusView, ArrayList<Integer> mood) {
        int number = mood.size();
        switch (number) {
            case 1:
                circularStatusView.setPortionsCount(number);
                setCircularMoodColor(0, mood.get(0), circularStatusView);
                break;
            case 2:
                circularStatusView.setPortionsCount(number);
                setCircularMoodColor(0, mood.get(0), circularStatusView);
                setCircularMoodColor(1, mood.get(1), circularStatusView);
                break;
            case 3:
                circularStatusView.setPortionsCount(number);
                setCircularMoodColor(0, mood.get(0), circularStatusView);
                setCircularMoodColor(1, mood.get(1), circularStatusView);
                setCircularMoodColor(2, mood.get(2), circularStatusView);
                break;
            case 4:
                circularStatusView.setPortionsCount(number);
                setCircularMoodColor(0, mood.get(0), circularStatusView);
                setCircularMoodColor(1, mood.get(1), circularStatusView);
                setCircularMoodColor(2, mood.get(2), circularStatusView);
                setCircularMoodColor(3, mood.get(3), circularStatusView);
                break;
            case 5:
                circularStatusView.setPortionsCount(5);
                setCircularMoodColor(0, mood.get(0), circularStatusView);
                setCircularMoodColor(1, mood.get(1), circularStatusView);
                setCircularMoodColor(2, mood.get(2), circularStatusView);
                setCircularMoodColor(3, mood.get(3), circularStatusView);
                setCircularMoodColor(4, mood.get(4), circularStatusView);
                break;

        }
    }

    private void setCircularMoodColor(int index, int mood, CircularStatusView circularStatusView) {
        switch (mood) {
            case 1:
                circularStatusView.setPortionColorForIndex(index, getContext().getColor(R.color.yellow)); // Happy
                break;
            case 2:
                circularStatusView.setPortionColorForIndex(index, getContext().getColor(R.color.light_green)); // Good
                break;
            case 3:
                circularStatusView.setPortionColorForIndex(index, getContext().getColor(R.color.blue)); // Neutral
                break;
            case 4:
                circularStatusView.setPortionColorForIndex(index, getContext().getColor(R.color.brown)); // Bad
                break;
            case 5:
                circularStatusView.setPortionColorForIndex(index, getContext().getColor(R.color.red)); // Worst
                break;
        }
    }



    private boolean isEventDay(int year, int month, int day, EventDay eventDay) {
        return eventDay.getYear() == year && eventDay.getMonth() == month && eventDay.getDay() == day;
    }


    private ArrayList<Integer> getMoodsFilter(EventDay eventDay) {
        return eventDay.getMoods();
    }

}