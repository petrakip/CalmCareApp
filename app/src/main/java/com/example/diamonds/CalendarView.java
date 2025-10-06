package com.example.diamonds;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.GridView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarView extends LinearLayout {
    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 42;

    LinearLayout header;
    MaterialButton next, prev, filter;
    MaterialTextView monthAndYear;
    GridView gridView;
    //event handling
    EventHandler eventHandler;
    ArrayList<EventDay> events;
    private Date selectedDate;

    // default date format
    private static final String DATE_FORMAT = "MMM yyyy";

    // current displayed month
    private final Calendar currentDate = Calendar.getInstance();

    public CalendarView(Context context) {
        super(context);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initControl(context, attrs);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    private void assignUiElements() {
        // layout is inflated, assign local variables to components
        header = findViewById(R.id.calendar_header);
        prev = findViewById(R.id.calendar_prev_button);
        next = findViewById(R.id.calendar_next_button);
        monthAndYear = findViewById(R.id.monthAndYear);
        filter = findViewById(R.id.dailyMoods);
        gridView = findViewById(R.id.calendar_grid);
    }

    private void initControl(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.calendar_layout, this);
        assignUiElements();
        assignClickHandlers();
    }

    public void updateCalendar(ArrayList<EventDay> events) {
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar) currentDate.clone();

        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells
        while (cells.size() < DAYS_COUNT) {
            cells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        if (events != null)
            this.events = events;

        // update grid
        gridView.setAdapter(new CalendarAdapter(getContext(), cells, this.events, currentDate, selectedDate));

        // update title
        // date format
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        monthAndYear.setText(sdf.format(currentDate.getTime()));
    }

    private void assignClickHandlers() {
        // next month
        next.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, 1);
            if (eventHandler == null) {
                updateCalendar(this.events);
                return;
            }
            eventHandler.onNextMonthClickListener(currentDate);
        });

        // previous month
        prev.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, -1);
            if (eventHandler == null) {
                updateCalendar(this.events);
                return;
            }
            eventHandler.onPrevMonthClickListener(currentDate);
        });

        // click on day
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (eventHandler == null)
                return;
            eventHandler.onDayClickListener((Date) parent.getItemAtPosition(position));
        });

        // filter
        filter.setOnClickListener(v -> {
            if (eventHandler == null)
                return;
            eventHandler.onFilterClickListener();
        });
    }

    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public interface EventHandler {
        void onDayClickListener(Date calendar);
        void onPrevMonthClickListener(Calendar calendar);
        void onNextMonthClickListener(Calendar calendar);
        void onFilterClickListener();
    }
}