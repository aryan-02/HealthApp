package com.example.healthapp;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrescriptionsAdapter extends RecyclerView.Adapter<PrescriptionViewHolder> {

    List<Prescription> list;
    List<Contact> doctors;
    List<Medication> medications;

    EditText dosageEntry;
    Spinner spinnerMedication;
    Spinner spinnerDoctor;

    EditText timeEntry;
    LocalDateTime timeEntered;

    EditText daysEntry;
    boolean[] daysSelected;
    Context context;
    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

    public PrescriptionsAdapter(List<Prescription> list, Context context)
    {
        this.list = list;
        this.context = context;

        medications = new ArrayList<>();
        doctors = new ArrayList<>();

        getDoctors();
        getMedications();
    }
    @NonNull
    @Override
    public PrescriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View photoView = inflater.inflate(R.layout.list_prescription,parent, false);
        PrescriptionViewHolder viewHolder = new PrescriptionViewHolder(photoView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PrescriptionViewHolder holder, int position) {
        holder.medication.setText(list.get(position).getMedication().getName());
        holder.dosage.setText(list.get(position).getDosage());
        holder.time.setText(timeFormat.format(list.get(position).getTime()));
        holder.doctor.setText(list.get(position).getPrescribedBy().getName());
        holder.days.setText(list.get(position).printDaysTaken());


        holder.view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                builder.setTitle("Manage Prescription");

                builder.setView(R.layout.view_create_prescription);


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
                        builder2.setTitle("Delete Prescription");
                        builder2.setMessage("Are you sure?");
                        builder2.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                deletePrescription(list.get(holder.getAdapterPosition())); //Delete from DB
                                list.remove(holder.getAdapterPosition()); //Delete from in-memory list

                                notifyItemRemoved(holder.getAdapterPosition());

                                Toast.makeText(context.getApplicationContext(), "Prescription Deleted", Toast.LENGTH_SHORT).show();
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
                        if(timeEntry.getText().toString().isEmpty())
                        {
                            Toast.makeText(context.getApplicationContext(), "Time Required", Toast.LENGTH_SHORT).show();
                        }
                        else if(dosageEntry.getText().toString().isEmpty())
                        {
                            Toast.makeText(context.getApplicationContext(), "Dosage Required", Toast.LENGTH_SHORT).show();
                        }
                        else if(daysEntry.getText().toString().isEmpty())
                        {
                            Toast.makeText(context.getApplicationContext(), "Days Required", Toast.LENGTH_SHORT).show();
                        }
                        else if(Prescription.printDaysTakenFromList(daysSelected).isEmpty())
                        {
                            Toast.makeText(context.getApplicationContext(), "Days Required", Toast.LENGTH_SHORT).show();
                        }
                        else if(!checkForConflicts(v, medications.get(spinnerMedication.getSelectedItemPosition())))
                        {
                            list.get(holder.getAdapterPosition()).setMedication(medications.get(spinnerMedication.getSelectedItemPosition()));
                            holder.medication.setText(spinnerMedication.getSelectedItem().toString());

                            list.get(holder.getAdapterPosition()).setDosage(dosageEntry.getText().toString());
                            holder.dosage.setText(dosageEntry.getText().toString());

                            list.get(holder.getAdapterPosition()).setPrescribedBy(doctors.get(spinnerDoctor.getSelectedItemPosition()));
                            holder.doctor.setText(spinnerDoctor.getSelectedItem().toString());

                            list.get(holder.getAdapterPosition()).setDaysTaken(daysSelected);
                            holder.days.setText(Prescription.printDaysTakenFromList(daysSelected));

                            list.get(holder.getAdapterPosition()).setTime(timeEntered);
                            holder.time.setText(timeFormat.format(timeEntered));

                            updatePrescription(list.get(holder.getAdapterPosition()));

                            dialog.dismiss();
                        }
                    }
                });

                dosageEntry = dialog.findViewById(R.id.EnterDosage);
                spinnerDoctor = dialog.findViewById(R.id.SelectDoctor);
                spinnerMedication = dialog.findViewById(R.id.SelectMedication);
                daysEntry = dialog.findViewById(R.id.EnterDays);
                timeEntry = dialog.findViewById(R.id.EnterTime);


                String[] medicationNames = new String[medications.size()];
                for (int i = 0; i < medications.size(); i++)
                {
                    medicationNames[i] = medications.get(i).getName();
                }
                String[] doctorNames = new String[doctors.size()];
                for (int i = 0; i < doctors.size(); i++)
                {
                    doctorNames[i] = doctors.get(i).getName();
                }

                ArrayAdapter<String> adapterMedicine = new ArrayAdapter<String>(context.getApplicationContext(), android.R.layout.simple_spinner_item, medicationNames);
                ArrayAdapter<String> adapterDoctors = new ArrayAdapter<String>(context.getApplicationContext(), android.R.layout.simple_spinner_item, doctorNames);

                spinnerMedication.setAdapter(adapterMedicine);
                spinnerDoctor.setAdapter(adapterDoctors);

                timeEntered = list.get(holder.getAdapterPosition()).getTime();

                spinnerMedication.setSelection(adapterMedicine.getPosition(list.get(holder.getAdapterPosition()).getMedication().getName()));
                dosageEntry.setText(list.get(holder.getAdapterPosition()).getDosage());
                spinnerDoctor.setSelection(adapterDoctors.getPosition(list.get(holder.getAdapterPosition()).getPrescribedBy().getName()));


                daysEntry.setText(list.get(holder.getAdapterPosition()).printDaysTaken());
                timeEntry.setText(timeFormat.format(list.get(holder.getAdapterPosition()).getTime()));
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
                                timeEntered = LocalDateTime.of(LocalDate.now(), LocalTime.of(hourOfDay, minute));
                                timeEntry.setText(timeFormat.format(timeEntered));
                            }
                        }, initialHour, initialMinute, false);
                        timePicker.show();
                    }

                });

                daysSelected = list.get(holder.getAdapterPosition()).getDaysTaken();
                String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                daysEntry.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        AlertDialog.Builder daysBuilder = new AlertDialog.Builder(v.getContext());
                        daysBuilder.setTitle("Select Days to take Medication");

                        daysBuilder.setMultiChoiceItems(daysOfWeek, daysSelected, new DialogInterface.OnMultiChoiceClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked)
                            {

                            }
                        });
                        daysBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                            }
                        });

                        AlertDialog dialogSetDays = daysBuilder.show();

                        dialogSetDays.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                daysEntry.setText(Prescription.printDaysTakenFromList(daysSelected));

                                dialogSetDays.dismiss();
                            }
                        });
                    }
                });

            }
        });

    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }
    private void getDoctors()
    {
        List<Contact> data = new ArrayList<>();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("contacts");
            collection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            String currentType = document.getString("type");
                            if(currentType.equals("Doctor Contact"))
                            {
                                Contact toAdd = new Contact(
                                        document.getString("name"),
                                        document.getString("phone"),
                                        document.getString("email"),
                                        document.getString("type"));

                                toAdd.setDatabaseID(document.getId());

                                data.add(toAdd);
                            }

                        }
                        doctors = new ArrayList<>();
                        doctors.addAll(data);

                    }
                }
            });
        }
    }
    private void getMedications()
    {
        List<Medication> data = new ArrayList<>();
        List<String> conflictsRawStr = new ArrayList<>();


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collection = db.collection("medications");
        collection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        Medication toAdd = new Medication(document.getString("name"));

                        toAdd.setDatabaseID(document.getId());

                        data.add(toAdd);

                        String c = document.getString("conflicts");
                        if(c == null)
                            c = "";
                        conflictsRawStr.add(c);
                    }

                    // Set up conflicts from stored list of database ids
                    for(int i = 0; i < data.size(); i++)
                    {
                        List<String> conflictIds = Arrays.asList(conflictsRawStr.get(i).split(","));
                        for(int j = 0; j < data.size(); j++)
                        {
                            if(j == i)
                                continue;

                            if(conflictIds.contains(data.get(j).getDatabaseID()))
                                data.get(i).addConflict(data.get(j));
                        }
                    }


                }

                medications.clear();
                medications.addAll(data);

            }
        });
    }


    private boolean checkForConflicts(View v, Medication toAdd)
    {
        List<Medication> foundConflicts = new ArrayList<>();
        for(Prescription currentPrescription : list)
        {
            if(toAdd.checkConflict(currentPrescription.getMedication()) || currentPrescription.getMedication().checkConflict(toAdd))
            {
                foundConflicts.add(currentPrescription.getMedication());
            }
        }

        if(foundConflicts.size() == 0)
            return false;
        else
        {
            AlertDialog.Builder warningBuilder = new AlertDialog.Builder(v.getContext());
            warningBuilder.setTitle("MEDICATION CONFLICT DETECTED!");
            StringBuilder message = new StringBuilder("Dangerous medication conflict found between " + toAdd.getName() + " and:");
            for(Medication m : foundConflicts)
                message.append("\n\t").append(m.getName());

            message.append("\n\nPlease contact your doctor for more information.");

            warningBuilder.setMessage(message);
            warningBuilder.setPositiveButton("I understand.", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                }
            });
            warningBuilder.show();
            return true;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void updatePrescription(Prescription toUpdate)
    {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("prescriptions");

            Map<String, Object> data = new HashMap<>();
            data.put("medication", toUpdate.getMedication().getDatabaseID());
            data.put("dosage", toUpdate.getDosage());
            data.put("doctor", toUpdate.getPrescribedBy().getDatabaseID());
            data.put("time", timeFormat.format(toUpdate.getTime()));
            data.put("schedule", toUpdate.printDaysTaken());

            collection.document(toUpdate.getDatabaseID()).set(data);
        }
    }
    private void deletePrescription(Prescription toDelete)
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("prescriptions");


            collection.document(toDelete.getDatabaseID()).delete();
        }
    }
}
