package com.example.healthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ViewVitalSigns extends AppCompatActivity
{
    VitalSignsAdapter adapter;
    RecyclerView recyclerView;
    FloatingActionButton addVitalSign;
    ImageButton backButton, searchButton;
    EditText readingEntry;
    EditText reading2Entry;
    LinearLayout reading2Container;
    Spinner spinnerReadingType;
    EditText dateEntry;
    EditText timeEntry;
    LocalDate dateEntered;
    LocalTime timeEntered;
    LocalDate searchStartDate = null;
    LocalDate searchEndDate = null;
    FirebaseUser user;
    FirebaseFirestore db;
    String uid;
    List<VitalSign> vitalSignsData;

    boolean inSearchView;
    TextView title;

    FirebaseAuth mAuth;
    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_vital_signs);

        vitalSignsData = new ArrayList<>();
        getVitalSignsFromDB();

        inSearchView = false;

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        adapter = new VitalSignsAdapter(vitalSignsData, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewVitalSigns.this));

        addVitalSign = findViewById(R.id.addVitalSign);
        backButton = findViewById(R.id.BackButton);
        searchButton = findViewById(R.id.SearchButton);
        title = findViewById(R.id.Title);

        backButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        addVitalSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewVitalSigns.this);
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                builder.setTitle("Log Vital Sign");

                builder.setView(R.layout.view_create_vitalsign);


                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog dialog = builder.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String type = spinnerReadingType.getSelectedItem().toString();
                        String reading = readingEntry.getText().toString();
                        String reading2 = reading2Entry.getText().toString();

                        if(!type.equals("Blood Pressure") && reading.isEmpty())
                        {
                            Toast.makeText(ViewVitalSigns.this, "Reading Value Required", Toast.LENGTH_SHORT).show();
                        }
                        else if(type.equals("Blood Pressure") && (reading.isEmpty() || reading2.isEmpty()))
                        {
                            Toast.makeText(ViewVitalSigns.this, "Reading Values Required", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            //input validated
                            LocalDateTime enteredDateTime = LocalDateTime.of(dateEntered, timeEntered);

                            VitalSign newVitalSign;
                            if(!type.equals("Blood Pressure"))
                                newVitalSign = new VitalSign(type, Double.parseDouble(reading), enteredDateTime);
                            else
                                newVitalSign = new VitalSign(type, Double.parseDouble(reading), Double.parseDouble(reading2), enteredDateTime);

                            addToDB(newVitalSign);

                            vitalSignsData.add(newVitalSign);

                            Collections.sort(vitalSignsData);
                            Collections.reverse(vitalSignsData);

                            adapter.notifyDataSetChanged();

                            dialog.dismiss();

                        }


                    }
                });

                readingEntry = dialog.findViewById(R.id.EnterReading);
                reading2Entry = dialog.findViewById(R.id.EnterReading2);
                reading2Container = dialog.findViewById(R.id.Reading2Container);
                spinnerReadingType = dialog.findViewById(R.id.SelectVitalSignType);

                dateEntry=dialog.findViewById(R.id.EnterDate);
                dateEntry.setText(dateFormat.format(LocalDateTime.now()));

                timeEntry = dialog.findViewById(R.id.EnterTime);
                timeEntry.setText(timeFormat.format(LocalDateTime.now()));

                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ViewVitalSigns.this, R.array.vitals_type, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerReadingType.setAdapter(adapter);

                dateEntered = LocalDate.now();
                timeEntered = LocalTime.now();

                dateEntry.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int initialYear = dateEntered.getYear();
                        int initialMonth = dateEntered.getMonthValue() - 1;
                        int initialDay = dateEntered.getDayOfMonth();

                        DatePickerDialog datePicker = new DatePickerDialog(ViewVitalSigns.this, new DatePickerDialog.OnDateSetListener(){

                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                            {
                                dateEntered = LocalDate.of(year, month+1, dayOfMonth);
                                dateEntry.setText(dateFormat.format(dateEntered));
                            }
                        }, initialYear, initialMonth, initialDay);

                        datePicker.show();
                    }
                });
                timeEntry.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int initialHour = timeEntered.getHour();
                        int initialMinute = timeEntered.getMinute();

                        TimePickerDialog timePicker = new TimePickerDialog(ViewVitalSigns.this, new TimePickerDialog.OnTimeSetListener()
                        {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                            {
                                timeEntered = LocalTime.of(hourOfDay, minute);
                                timeEntry.setText(timeFormat.format(timeEntered));
                            }
                        }, initialHour, initialMinute, false);
                        timePicker.show();
                    }

                });
                spinnerReadingType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                    {
                        if(spinnerReadingType.getSelectedItem().toString().equals("Blood Pressure"))
                        {
                            reading2Container.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            reading2Container.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent)
                    {

                    }
                });
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(inSearchView)
                {
                    inSearchView = false;
                    title.setText("Vital Sign Readings");
                    Uri imgUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/search");
                    searchButton.setImageURI(imgUri);

                    getVitalSignsFromDB();
                }
                else
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewVitalSigns.this);
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    builder.setTitle("Search Vital Sign Readings");

                    builder.setView(R.layout.view_search_vitalsign);

                    EditText enterStartDate;
                    EditText enterEndDate;


                    builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Will override this later, this declaration is left in to prevent compatibility issues
                        }
                    });

                    AlertDialog dialog = builder.show();

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            String type = spinnerReadingType.getSelectedItem().toString();

                            searchVitalSignsFromDB(searchStartDate, searchEndDate, type);

                            inSearchView = true;
                            title.setText("Search Results:");
                            Uri imgUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/cancel_search");
                            searchButton.setImageURI(imgUri);

                            dialog.dismiss();
                        }
                    });

                    enterStartDate = dialog.findViewById(R.id.EnterStartDate);
                    enterEndDate = dialog.findViewById(R.id.EnterEndDate);

                    spinnerReadingType = dialog.findViewById(R.id.SelectReadingType);
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ViewVitalSigns.this, R.array.search_vitals_type, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerReadingType.setAdapter(adapter);
                    enterStartDate.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if(searchStartDate == null)
                                searchStartDate = LocalDate.now();

                            int initialYear = searchStartDate.getYear();
                            int initialMonth = searchStartDate.getMonthValue() - 1;
                            int initialDay = searchStartDate.getDayOfMonth();

                            DatePickerDialog datePicker = new DatePickerDialog(ViewVitalSigns.this, new DatePickerDialog.OnDateSetListener(){

                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                                {
                                    searchStartDate = LocalDate.of(year, month+1, dayOfMonth);
                                    enterStartDate.setText(dateFormat.format(searchStartDate));
                                }
                            }, initialYear, initialMonth, initialDay);

                            datePicker.show();
                        }
                    });
                    enterEndDate.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if(searchEndDate == null)
                                searchEndDate = LocalDate.now();

                            int initialYear = searchEndDate.getYear();
                            int initialMonth = searchEndDate.getMonthValue() - 1;
                            int initialDay = searchEndDate.getDayOfMonth();

                            DatePickerDialog datePicker = new DatePickerDialog(ViewVitalSigns.this, new DatePickerDialog.OnDateSetListener(){

                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                                {
                                    searchEndDate = LocalDate.of(year, month+1, dayOfMonth);
                                    enterEndDate.setText(dateFormat.format(searchEndDate));
                                }
                            }, initialYear, initialMonth, initialDay);

                            datePicker.show();
                        }
                    });
                }
            }
        });


    }



    private void addToDB(VitalSign toAdd)
    {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("vitalsigns");

            Map<String, Object> data = new HashMap<>();
            data.put("type", toAdd.getType());
            data.put("reading", toAdd.getReading());

            if(toAdd.getType().equals("Blood Pressure"))
                data.put("reading2", toAdd.getReading2());

            data.put("time", dateTimeFormat.format(toAdd.getTime()));

            collection.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
            {
                @Override
                public void onSuccess(DocumentReference documentReference)
                {
                    toAdd.setDatabaseID(documentReference.getId());
                }
            });
        }
    }
    private void searchVitalSignsFromDB(LocalDate startDate, LocalDate endDate, String typeSearch)
    {
        List<VitalSign> data = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("vitalsigns");
            collection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            String currentType = document.getString("type");
                            String currentDateStr = document.getString("time");

                            LocalDateTime currentDateTime = LocalDateTime.parse(currentDateStr, dateTimeFormat);

                            boolean matches = true;
                            if(!typeSearch.equals("Any") && !currentType.equals(typeSearch))
                            {
                                matches = false;
                            }
                            if(startDate != null && startDate.isAfter(ChronoLocalDate.from(currentDateTime)))
                            {
                                matches = false;
                            }
                            if(endDate != null && endDate.isBefore(ChronoLocalDate.from(currentDateTime)))
                            {
                                matches = false;
                            }

                            if(matches)
                            {
                                Double reading = document.getDouble("reading");
                                if(reading == null)
                                    reading = 0.0;

                                Double reading2 = 0.0;
                                if(Objects.equals(document.getString("type"), "Blood Pressure"))
                                {
                                    reading2 = document.getDouble("reading2");
                                    if(reading2 == null)
                                        reading2 = 0.0;
                                }

                                String timeString = null;
                                try
                                {
                                    timeString = document.getString("time");

                                }
                                catch(Exception e)
                                {
                                    timeString = null;
                                }

                                if(timeString == null)
                                    timeString = dateTimeFormat.format(LocalDateTime.now());

                                LocalDateTime vitalTime;
                                try
                                {
                                    vitalTime = LocalDateTime.parse(timeString, dateTimeFormat);
                                }
                                catch (DateTimeParseException e)
                                {
                                    vitalTime = LocalDateTime.now();
                                }

                                VitalSign toAdd;

                                if(Objects.equals(document.getString("type"), "Blood Pressure"))
                                    toAdd = new VitalSign(document.getString("type"), reading, reading2, vitalTime);
                                else
                                    toAdd = new VitalSign(document.getString("type"), reading, vitalTime);

                                toAdd.setDatabaseID(document.getId());
                                data.add(toAdd);
                            }

                        }
                    }
                    vitalSignsData.clear();
                    vitalSignsData.addAll(data);

                    Collections.sort(vitalSignsData);
                    Collections.reverse(vitalSignsData);

                    adapter.notifyDataSetChanged();
                    Toast.makeText(ViewVitalSigns.this, vitalSignsData.size() + " results found.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void getVitalSignsFromDB()
    {
        List<VitalSign> data = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("vitalsigns");
            collection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            Double reading = document.getDouble("reading");
                            if(reading == null)
                                reading = 0.0;

                            Double reading2 = 0.0;
                            if(Objects.equals(document.getString("type"), "Blood Pressure"))
                            {
                                reading2 = document.getDouble("reading2");
                                if(reading2 == null)
                                    reading2 = 0.0;
                            }

                            String timeString = null;
                            try
                            {
                                timeString = document.getString("time");

                            }
                            catch(Exception e)
                            {
                                timeString = null;
                            }

                            if(timeString == null)
                                timeString = dateTimeFormat.format(LocalDateTime.now());

                            LocalDateTime vitalTime;
                            try
                            {
                                vitalTime = LocalDateTime.parse(timeString, dateTimeFormat);
                            }
                            catch (DateTimeParseException e)
                            {
                                vitalTime = LocalDateTime.now();
                            }

                            VitalSign toAdd;

                            if(Objects.equals(document.getString("type"), "Blood Pressure"))
                                toAdd = new VitalSign(document.getString("type"), reading, reading2, vitalTime);
                            else
                                toAdd = new VitalSign(document.getString("type"), reading, vitalTime);

                            toAdd.setDatabaseID(document.getId());
                            data.add(toAdd);
                        }
                    } else {
                        Toast.makeText(ViewVitalSigns.this, "No vital readings found", Toast.LENGTH_SHORT).show();
                    }

                    vitalSignsData.clear();
                    vitalSignsData.addAll(data);

                    Collections.sort(vitalSignsData);
                    Collections.reverse(vitalSignsData);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

}