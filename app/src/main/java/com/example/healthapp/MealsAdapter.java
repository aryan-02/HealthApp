package com.example.healthapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealsAdapter extends RecyclerView.Adapter<MealsViewHolder> {

    List<Meal> list;
    Context context;

    EditText caloriesEntry;
    EditText fatsEntry;
    EditText carbsEntry;
    EditText proteinEntry;

    Spinner spinnerMealType;

    EditText dateEntry;
    EditText timeEntry;

    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    LocalDate dateEntered;
    LocalTime timeEntered;
    public MealsAdapter(List<Meal> list, Context context)
    {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MealsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View photoView = inflater.inflate(R.layout.list_meal,parent, false);
        MealsViewHolder viewHolder = new MealsViewHolder(photoView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MealsViewHolder holder, int position) {
        holder.calories.setText(String.valueOf(list.get(position).getCalories() + " calories"));
        holder.carbs.setText(String.valueOf(list.get(position).getCarbs() + "g carbs"));
        holder.fats.setText(String.valueOf(list.get(position).getFats()) + "g fats");
        holder.protein.setText(String.valueOf(list.get(position).getProtein()) + "g protein");
        holder.type.setText(list.get(position).getMealType());
        if(list.get(position).getMealTime() != null)
            holder.time.setText(dateTimeFormat.format(list.get(position).getMealTime()));
        else
            holder.time.setText("Unknown: please edit.");

        holder.view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                builder.setTitle("Manage Contact");

                builder.setView(R.layout.view_create_meal);
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(v.getContext());
                        builder2.setTitle("Delete Meal");
                        builder2.setMessage("Are you sure?");
                        builder2.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                deleteMeal(list.get(holder.getAdapterPosition())); //Delete from DB
                                list.remove(holder.getAdapterPosition()); //Delete from in-memory list

                                notifyItemRemoved(holder.getAdapterPosition());

                                Toast.makeText(context.getApplicationContext(), "Meal Deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder2.setNegativeButton("No", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //Do nothing, just close dialog without saving
                            }
                        });
                        AlertDialog dialog2 = builder2.show();
                    }
                });
                AlertDialog dialog = builder.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        list.get(holder.getAdapterPosition()).setCalories(Integer.parseInt(caloriesEntry.getText().toString()));
                        holder.calories.setText(caloriesEntry.getText().toString().concat(" calories"));

                        list.get(holder.getAdapterPosition()).setProtein(Double.parseDouble(proteinEntry.getText().toString()));
                        holder.protein.setText(proteinEntry.getText().toString().concat("g protein"));

                        list.get(holder.getAdapterPosition()).setFats(Double.parseDouble(fatsEntry.getText().toString()));
                        holder.fats.setText(fatsEntry.getText().toString().concat("g fats"));

                        list.get(holder.getAdapterPosition()).setCarbs(Double.parseDouble(carbsEntry.getText().toString()));
                        holder.carbs.setText(carbsEntry.getText().toString().concat("g carbs"));

                        list.get(holder.getAdapterPosition()).setMealType(spinnerMealType.getSelectedItem().toString());
                        holder.type.setText(spinnerMealType.getSelectedItem().toString());


                        list.get(holder.getAdapterPosition()).setMealTime(LocalDateTime.of(dateEntered, timeEntered));
                        holder.time.setText(dateTimeFormat.format(LocalDateTime.of(dateEntered, timeEntered)));

                        updateMeal(list.get(holder.getAdapterPosition()));

                        Toast.makeText(context.getApplicationContext(), "Meal Saved!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

                caloriesEntry = dialog.findViewById(R.id.EnterCalories);
                carbsEntry = dialog.findViewById(R.id.EnterCarbs);
                fatsEntry = dialog.findViewById(R.id.EnterFats);
                proteinEntry = dialog.findViewById(R.id.EnterProtein);

                spinnerMealType = dialog.findViewById(R.id.SelectMealType);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(v.getContext(), R.array.meal_types, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMealType.setAdapter(adapter);
                spinnerMealType.setSelection(adapter.getPosition(list.get(holder.getAdapterPosition()).getMealType()));

                dateEntry=dialog.findViewById(R.id.EnterDate);
                timeEntry = dialog.findViewById(R.id.EnterTime);

                caloriesEntry.setText(String.valueOf(list.get(holder.getAdapterPosition()).getCalories()));
                carbsEntry.setText(String.valueOf(list.get(holder.getAdapterPosition()).getCarbs()));
                fatsEntry.setText(String.valueOf(list.get(holder.getAdapterPosition()).getFats()));
                proteinEntry.setText(String.valueOf(list.get(holder.getAdapterPosition()).getProtein()));

                timeEntered = list.get(holder.getAdapterPosition()).getMealTime().toLocalTime();
                dateEntered = list.get(holder.getAdapterPosition()).getMealTime().toLocalDate();

                timeEntry.setText(timeFormat.format(timeEntered));
                dateEntry.setText(dateFormat.format(dateEntered));

                dateEntry.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int initialYear = dateEntered.getYear();
                        int initialMonth = dateEntered.getMonthValue() - 1;
                        int initialDay = dateEntered.getDayOfMonth();

                        DatePickerDialog datePicker = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener(){

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

                        TimePickerDialog timePicker = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener()
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


    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }
    private void updateMeal(Meal toUpdate)
    {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            CollectionReference contactsCollection = db.collection("users").document(uid).collection("meals");

            Map<String, Object> data = new HashMap<>();
            data.put("type", toUpdate.getMealType());
            data.put("calories", toUpdate.getCalories());
            data.put("carbs", toUpdate.getCarbs());
            data.put("fats", toUpdate.getFats());
            data.put("protein", toUpdate.getProtein());
            data.put("time", dateTimeFormat.format(toUpdate.getMealTime()));

            contactsCollection.document(toUpdate.getDatabaseID()).set(data);
        }
    }
    private void deleteMeal(Meal toDelete)
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            CollectionReference contactsCollection = db.collection("users").document(uid).collection("meals");


            contactsCollection.document(toDelete.getDatabaseID()).delete();
        }
    }

}
