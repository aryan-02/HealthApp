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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class ViewDiet extends AppCompatActivity {

    MealsAdapter adapter;
    RecyclerView recyclerView;
    FloatingActionButton addMeal;
    ImageButton backButton, searchButton;
    EditText caloriesEntry;
    EditText fatsEntry;
    EditText carbsEntry;
    EditText proteinEntry;


    Spinner spinnerMealType;

    EditText dateEntry;
    EditText timeEntry;
    LocalDate dateEntered;
    LocalTime timeEntered;
    LocalDate searchStartDate = null;
    LocalDate searchEndDate = null;

    FirebaseUser user;
    FirebaseFirestore db;
    String uid;
    List<Meal> mealData;

    boolean inSearchView;
    TextView title;
    FirebaseAuth mAuth;

    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_diet);


        mealData = new ArrayList<>();
        getMealsFromDB();

        inSearchView = false;

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        adapter = new MealsAdapter(mealData, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewDiet.this));

        addMeal = findViewById(R.id.addMeal);
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

        addMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewDiet.this);
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                builder.setTitle("Enter a Meal:");

                builder.setView(R.layout.view_create_meal);

                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
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
                        String calories = caloriesEntry.getText().toString();
                        String carbs = carbsEntry.getText().toString();
                        String fats = fatsEntry.getText().toString();
                        String protein = proteinEntry.getText().toString();
                        String type = spinnerMealType.getSelectedItem().toString();

                        if(calories.isEmpty())
                            calories = "0";

                        if(carbs.isEmpty())
                            carbs = "0";

                        if(fats.isEmpty())
                            fats = "0";

                        if(protein.isEmpty())
                            protein = "0";

                        LocalDateTime enteredDateTime = LocalDateTime.of(dateEntered, timeEntered);

                        Meal newMeal = new Meal(type, Double.parseDouble(carbs),Double.parseDouble(fats), Double.parseDouble(protein), Integer.parseInt(calories), enteredDateTime);

                        addToDB(newMeal);

                        mealData.add(newMeal);
                        Collections.sort(mealData);
                        Collections.reverse(mealData);

                        adapter.notifyDataSetChanged();

                        dialog.dismiss();
                    }
                });




                caloriesEntry = dialog.findViewById(R.id.EnterCalories);
                carbsEntry = dialog.findViewById(R.id.EnterCarbs);
                fatsEntry = dialog.findViewById(R.id.EnterFats);
                proteinEntry = dialog.findViewById(R.id.EnterProtein);
                spinnerMealType = dialog.findViewById(R.id.SelectMealType);


                dateEntry=dialog.findViewById(R.id.EnterDate);
                dateEntry.setText(dateFormat.format(LocalDateTime.now()));

                timeEntry = dialog.findViewById(R.id.EnterTime);
                timeEntry.setText(timeFormat.format(LocalDateTime.now()));


                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ViewDiet.this, R.array.meal_types, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMealType.setAdapter(adapter);

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

                        DatePickerDialog datePicker = new DatePickerDialog(ViewDiet.this, new DatePickerDialog.OnDateSetListener(){

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

                        TimePickerDialog timePicker = new TimePickerDialog(ViewDiet.this, new TimePickerDialog.OnTimeSetListener()
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
                    title.setText("Diet Information");
                    Uri imgUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/search");
                    searchButton.setImageURI(imgUri);

                    getMealsFromDB();
                }
                else
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewDiet.this);
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    builder.setTitle("Search Meals");

                    builder.setView(R.layout.view_search_meal);

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
                            String type = spinnerMealType.getSelectedItem().toString();

                            searchMealsFromDB(searchStartDate, searchEndDate, type);

                            inSearchView = true;
                            title.setText("Search Results:");
                            Uri imgUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/cancel_search");
                            searchButton.setImageURI(imgUri);

                            dialog.dismiss();
                        }
                    });

                    enterStartDate = dialog.findViewById(R.id.EnterStartDate);
                    enterEndDate = dialog.findViewById(R.id.EnterEndDate);

                    spinnerMealType = dialog.findViewById(R.id.SelectMealType);
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ViewDiet.this, R.array.search_meal_types, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerMealType.setAdapter(adapter);
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

                            DatePickerDialog datePicker = new DatePickerDialog(ViewDiet.this, new DatePickerDialog.OnDateSetListener(){

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

                            DatePickerDialog datePicker = new DatePickerDialog(ViewDiet.this, new DatePickerDialog.OnDateSetListener(){

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


    private void addToDB(Meal toAdd)
    {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("meals");

            Map<String, Object> data = new HashMap<>();
            data.put("type", toAdd.getMealType());
            data.put("carbs", toAdd.getCarbs());
            data.put("fats", toAdd.getFats());
            data.put("protein", toAdd.getProtein());
            data.put("calories", toAdd.getCalories());
            data.put("time", dateTimeFormat.format(toAdd.getMealTime()));

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
    private void searchMealsFromDB(LocalDate startDate, LocalDate endDate, String typeSearch)
    {
        List<Meal> data = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("meals");
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
                                Log.d("search", typeSearch + " != " + currentType);
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
                                Double carbs = document.getDouble("carbs");
                                if(carbs == null)
                                    carbs = 0.0;

                                Double fats = document.getDouble("fats");
                                if(fats == null)
                                    fats = 0.0;

                                Double protein = document.getDouble("protein");
                                if(protein == null)
                                    protein = 0.0;

                                Double caloriesDouble = document.getDouble("calories");
                                if(caloriesDouble == null)
                                    caloriesDouble = 0.0;

                                String timeString = document.getString("time");

                                if(timeString == null)
                                    timeString = dateTimeFormat.format(LocalDateTime.now());

                                LocalDateTime mealTime;
                                try
                                {
                                    mealTime = LocalDateTime.parse(timeString, dateTimeFormat);
                                }
                                catch (DateTimeParseException e)
                                {
                                    mealTime = LocalDateTime.now();
                                }

                                Meal toAdd = new Meal(
                                        document.getString("type"),
                                        carbs, fats, protein, caloriesDouble.intValue(), mealTime);

                                toAdd.setDatabaseID(document.getId());

                                data.add(toAdd);
                            }

                        }
                    }
                    mealData.clear();
                    mealData.addAll(data);

                    Collections.sort(mealData);
                    Collections.reverse(mealData);

                    adapter.notifyDataSetChanged();
                    Toast.makeText(ViewDiet.this, mealData.size() + " results found.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void getMealsFromDB()
    {
        List<Meal> data = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("meals");
            collection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            Double carbs = document.getDouble("carbs");
                            if(carbs == null)
                                carbs = 0.0;

                            Double fats = document.getDouble("fats");
                            if(fats == null)
                                fats = 0.0;

                            Double protein = document.getDouble("protein");
                            if(protein == null)
                                protein = 0.0;

                            Double caloriesDouble = document.getDouble("calories");
                            if(caloriesDouble == null)
                                caloriesDouble = 0.0;

                            String timeString = document.getString("time");

                            if(timeString == null)
                                timeString = dateTimeFormat.format(LocalDateTime.now());

                            LocalDateTime mealTime;
                            try
                            {
                                mealTime = LocalDateTime.parse(timeString, dateTimeFormat);
                            }
                            catch (DateTimeParseException e)
                            {
                                mealTime = LocalDateTime.now();
                            }

                            Meal toAdd = new Meal(
                                    document.getString("type"),
                                    carbs, fats, protein, caloriesDouble.intValue(), mealTime);

                            toAdd.setDatabaseID(document.getId());

                            data.add(toAdd);
                        }
                    }

                    mealData.clear();
                    mealData.addAll(data);

                    Collections.sort(mealData);
                    Collections.reverse(mealData);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}