package com.cubbysulotions.proo.Calendar;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cubbysulotions.proo.ModelsClasses.CalendarAdapter;
import com.cubbysulotions.proo.ModelsClasses.CalendarEvents;
import com.cubbysulotions.proo.ModelsClasses.CalendarUtils;
import com.cubbysulotions.proo.ModelsClasses.EventAdapter;
import com.cubbysulotions.proo.ModelsClasses.EventRVAdapter;
import com.cubbysulotions.proo.ModelsClasses.HourEvent;
import com.cubbysulotions.proo.ModelsClasses.HourRVAdapter;
import com.cubbysulotions.proo.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.cubbysulotions.proo.ModelsClasses.CalendarUtils.daysInMonthArray;
import static com.cubbysulotions.proo.ModelsClasses.CalendarUtils.daysInWeekArray;
import static com.cubbysulotions.proo.ModelsClasses.CalendarUtils.monthYearFormatter;
import static com.cubbysulotions.proo.ModelsClasses.CalendarUtils.selectedDate;

public class WeeklyCalendarFragment extends Fragment implements CalendarAdapter.OnItemListener {

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return view = inflater.inflate(R.layout.fragment_weekly_calendar, container, false);
    }

    private Button btnPrevious, btnNext, btnDaily;
    private TextView txtMonth;
    private RecyclerView calendarRecyclerView;
    private RecyclerView eventList;
    private FloatingActionButton btnNewEvent;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference reference;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnPrevious = view.findViewById(R.id.btnPreviousWeekly);
        btnNext = view.findViewById(R.id.btnNextWeekly);
        txtMonth = view.findViewById(R.id.txtMonthWeekly);
        //btnDaily = view.findViewById(R.id.btnDaily);
        calendarRecyclerView = view.findViewById(R.id.calendarDatesRecyclerViewWeekly);
        btnNewEvent = view.findViewById(R.id.addEvent);
        eventList = view.findViewById(R.id.eventList);
        NavController navController = Navigation.findNavController(view);

        //Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("events").child(currentUser.getUid());

        String date = getArguments().getString("date");
        selectedDate = LocalDate.parse(date);
        setWeekVIew();

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = selectedDate.plusWeeks(1);
                setWeekVIew();
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = selectedDate.minusWeeks(1);
                setWeekVIew();
            }
        });

        btnNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String flag = "FROM_WEEKLY";

                Bundle bundle = new Bundle();
                bundle.putString("flag", flag);
                bundle.putString("date", String.valueOf(selectedDate));
                navController.navigate(R.id.action_weeklyCalendarFragment_to_eventEditFragment, bundle);
            }
        });

        /*
        btnDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_weeklyCalendarFragment_to_dailyFragment);
            }
        }); */
    }


    String monthDate;
    ArrayList<LocalDate> days;
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setWeekVIew() {
        monthDate = CalendarUtils.monthDayFormatter(selectedDate);
        txtMonth.setText(monthYearFormatter(selectedDate));
        days = daysInWeekArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(days, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
        //setEventAdapter();
        setHourAdapter();
    }

    private void setEventAdapter() {
        try {
            List<CalendarEvents> events = new ArrayList<>();

            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 1, GridLayoutManager.VERTICAL, false);
            EventRVAdapter adapter = new EventRVAdapter(getActivity(), events);
            eventList.setLayoutManager(layoutManager);
            eventList.setAdapter(adapter);

            reference.orderByChild("timeString").addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot data : snapshot.getChildren()){
                        CalendarEvents ev = data.getValue(CalendarEvents.class);

                        if (days.get(6).isAfter(LocalDate.parse(ev.getDateString())) && days.get(0).isBefore(LocalDate.parse(ev.getDateString()))){
                            events.add(ev);
                        }

                    }
                    adapter.updateDataSet(events);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e){
            toast("Something went wrong, please try again");
            Log.e("Logout error", "exception", e);
        }
    }

    private void setHourAdapter() {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 1, GridLayoutManager.VERTICAL, false);
        HourRVAdapter adapter = new HourRVAdapter(getActivity(), hourEventList(), monthDate);
        eventList.setLayoutManager(layoutManager);
        eventList.setAdapter(adapter);
    }

    private ArrayList<HourEvent> hourEventList() {
        ArrayList<HourEvent> list = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++){
            LocalTime time = LocalTime.of(hour, 0);
            ArrayList<CalendarEvents> events = CalendarEvents.eventsForDateandTime(selectedDate, time);
            HourEvent hourEvent = new HourEvent(time, events);
            list.add(hourEvent);
        }
        return list;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onItemClick(int position, LocalDate date) {
        selectedDate = date;
        setWeekVIew();
    }



    private void toast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        //setEventAdapter();
    }
}