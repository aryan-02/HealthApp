package com.example.healthapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VitalSignsAdapter extends RecyclerView.Adapter<VitalSignsViewHolder> {

    List<VitalSign> list;
    Context context;


    EditText readingEntry;
    EditText reading2Entry;
    LinearLayout reading2Container;
    Spinner spinnerReadingType;
    EditText dateEntry;
    EditText timeEntry;

    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    LocalDate dateEntered;
    LocalTime timeEntered;

    public VitalSignsAdapter(List<VitalSign> list, Context context)
    {
        this.list = list;
        this.context = context;
    }
    @NonNull
    @Override
    public VitalSignsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View photoView = inflater.inflate(R.layout.list_vitalsign,parent, false);
        VitalSignsViewHolder viewHolder = new VitalSignsViewHolder(photoView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull VitalSignsViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.type.setText(list.get(position).getType());

        if(list.get(position).getType().equals("Blood Pressure"))
        {
            String readingStr = list.get(position).getReading() + " over " + list.get(position).getReading2();
            holder.reading.setText(readingStr);
        }
        else
            holder.reading.setText(String.valueOf(list.get(position).getReading()));

        if(list.get(position).getTime() != null)
            holder.time.setText(dateTimeFormat.format(list.get(position).getTime()));
        else
            holder.time.setText("Unknown: please edit.");

        holder.view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                builder.setTitle("Manage Reading");

                builder.setView(R.layout.view_create_vitalsign);
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
                        builder2.setTitle("Delete Reading");
                        builder2.setMessage("Are you sure?");
                        builder2.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                deleteVitalSign(list.get(holder.getAdapterPosition())); //Delete from DB
                                list.remove(holder.getAdapterPosition()); //Delete from in-memory list

                                notifyItemRemoved(holder.getAdapterPosition());

                                Toast.makeText(context.getApplicationContext(), "Reading Deleted", Toast.LENGTH_SHORT).show();
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
                        String type = spinnerReadingType.getSelectedItem().toString();
                        String reading = readingEntry.getText().toString();
                        String reading2 = reading2Entry.getText().toString();

                        if(!type.equals("Blood Pressure") && reading.isEmpty())
                        {
                            Toast.makeText(v.getContext(), "Reading Value Required", Toast.LENGTH_SHORT).show();
                        }
                        else if(type.equals("Blood Pressure") && (reading.isEmpty() || reading2.isEmpty()))
                        {
                            Toast.makeText(v.getContext(), "Reading Values Required", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            if(type.equals("Blood Pressure"))
                            {
                                list.get(holder.getAdapterPosition()).setReading(Double.parseDouble(reading));
                                list.get(holder.getAdapterPosition()).setReading2(Double.parseDouble(reading2));

                                String readingStr = list.get(position).getReading() + " over " + list.get(position).getReading2();
                                holder.reading.setText(readingStr);
                            }
                            else
                            {
                                list.get(holder.getAdapterPosition()).setReading(Double.parseDouble(reading));
                                holder.reading.setText(reading);
                            }

                            list.get(holder.getAdapterPosition()).setTime(LocalDateTime.of(dateEntered, timeEntered));
                            holder.time.setText(dateTimeFormat.format(LocalDateTime.of(dateEntered, timeEntered)));

                            updateVitalSign(list.get(holder.getAdapterPosition()));

                            Toast.makeText(context.getApplicationContext(), "Reading Saved", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                    }
                });

                readingEntry = dialog.findViewById(R.id.EnterReading);
                reading2Entry = dialog.findViewById(R.id.EnterReading2);
                reading2Container = dialog.findViewById(R.id.Reading2Container);

                spinnerReadingType = dialog.findViewById(R.id.SelectVitalSignType);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(v.getContext(), R.array.vitals_type, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerReadingType.setAdapter(adapter);
                spinnerReadingType.setSelection(adapter.getPosition(list.get(holder.getAdapterPosition()).getType()));

                dateEntry=dialog.findViewById(R.id.EnterDate);
                timeEntry = dialog.findViewById(R.id.EnterTime);

                readingEntry.setText(String.valueOf(list.get(holder.getAdapterPosition()).getReading()));
                reading2Entry.setText(String.valueOf(list.get(holder.getAdapterPosition()).getReading2()));

                timeEntered = list.get(holder.getAdapterPosition()).getTime().toLocalTime();
                dateEntered = list.get(holder.getAdapterPosition()).getTime().toLocalDate();

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
    private void updateVitalSign(VitalSign toUpdate)
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("vitalsigns");

            Map<String, Object> data = new HashMap<>();
            data.put("type", toUpdate.getType());
            data.put("reading", toUpdate.getReading());

            if(toUpdate.getType().equals("Blood Pressure"))
                data.put("reading2", toUpdate.getReading2());

            data.put("time", dateTimeFormat.format(toUpdate.getTime()));

            collection.document(toUpdate.getDatabaseID()).set(data);
        }
    }
    private void deleteVitalSign(VitalSign toDelete)
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            CollectionReference contactsCollection = db.collection("users").document(uid).collection("vitalsigns");

            contactsCollection.document(toDelete.getDatabaseID()).delete();
        }
    }
}
