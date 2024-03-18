package com.example.healthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPrescriptions extends AppCompatActivity {
    PrescriptionsAdapter adapter;
    RecyclerView recyclerView;
    FloatingActionButton addPrescription;
    ImageButton backButton, searchButton;
    EditText dosageEntry;
    Spinner spinnerMedication;
    Spinner spinnerDoctor;

    EditText timeEntry;
    LocalDateTime timeEntered;

    EditText daysEntry;
    boolean[] daysSelected;
    FirebaseUser user;
    FirebaseFirestore db;
    String uid;
    List<Prescription> prescriptionData;
    List<Medication> medicationList;
    List<Contact> doctorList;
    boolean inSearchView;

    boolean loadedDoctors = false;
    boolean loadedMedications = false;

    TextView title;

    FirebaseAuth mAuth;

    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_prescriptions);

        medicationList = new ArrayList<>();
        prescriptionData = new ArrayList<>();
        doctorList = new ArrayList<>();

        //This will in turn call getMedications() and getPrescriptionsFromDB() since each method needs data from previous
        getDoctors();

        inSearchView = false;

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        adapter = new PrescriptionsAdapter(prescriptionData, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewPrescriptions.this));

        addPrescription = findViewById(R.id.addPrescription);
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
            }
        });
        addPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewPrescriptions.this);
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                builder.setTitle("Enter New Prescription");

                builder.setView(R.layout.view_create_prescription);


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
                        if(timeEntry.getText().toString().isEmpty())
                        {
                            Toast.makeText(ViewPrescriptions.this, "Time Required", Toast.LENGTH_SHORT).show();
                        }
                        else if(dosageEntry.getText().toString().isEmpty())
                        {
                            Toast.makeText(ViewPrescriptions.this, "Dosage Required", Toast.LENGTH_SHORT).show();
                        }
                        else if(daysEntry.getText().toString().isEmpty())
                        {
                            Toast.makeText(ViewPrescriptions.this, "Days Required", Toast.LENGTH_SHORT).show();
                        }
                        else if(Prescription.printDaysTakenFromList(daysSelected).isEmpty())
                        {
                            Toast.makeText(ViewPrescriptions.this, "Days Required", Toast.LENGTH_SHORT).show();
                        }
                        else if(!checkForConflicts(medicationList.get(spinnerMedication.getSelectedItemPosition())))
                        {
                            Prescription newPrescription = new Prescription(
                                    medicationList.get(spinnerMedication.getSelectedItemPosition()),
                                    dosageEntry.getText().toString(),
                                    doctorList.get(spinnerDoctor.getSelectedItemPosition()),
                                    timeEntered,
                                    daysSelected
                            );

                            addPrescriptionAlarm(newPrescription);

                            addToDB(newPrescription);


                            prescriptionData.add(0, newPrescription);
                            recyclerView.scrollToPosition(0);
                            adapter.notifyItemInserted(0);

                            dialog.dismiss();
                        }



                    }
                });


                dosageEntry = dialog.findViewById(R.id.EnterDosage);
                spinnerDoctor = dialog.findViewById(R.id.SelectDoctor);
                spinnerMedication = dialog.findViewById(R.id.SelectMedication);
                daysEntry = dialog.findViewById(R.id.EnterDays);
                timeEntry = dialog.findViewById(R.id.EnterTime);


                String[] medicationNames = new String[medicationList.size()];
                for (int i = 0; i < medicationList.size(); i++)
                {
                    medicationNames[i] = medicationList.get(i).getName();
                }
                String[] doctorNames = new String[doctorList.size()];
                for (int i = 0; i < doctorList.size(); i++)
                {
                    doctorNames[i] = doctorList.get(i).getName();
                }

                ArrayAdapter<String> adapterMedicine = new ArrayAdapter<String>(ViewPrescriptions.this, android.R.layout.simple_spinner_item, medicationNames);


                ArrayAdapter<String> adapterDoctors = new ArrayAdapter<String>(ViewPrescriptions.this, android.R.layout.simple_spinner_item, doctorNames);


                spinnerMedication.setAdapter(adapterMedicine);
                spinnerDoctor.setAdapter(adapterDoctors);

                timeEntered = LocalDateTime.now();
                timeEntry.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int initialHour = timeEntered.getHour();
                        int initialMinute = timeEntered.getMinute();

                        TimePickerDialog timePicker = new TimePickerDialog(ViewPrescriptions.this, new TimePickerDialog.OnTimeSetListener()
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

                daysSelected = new boolean[7];
                String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                daysEntry.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        AlertDialog.Builder daysBuilder = new AlertDialog.Builder(ViewPrescriptions.this);
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

        searchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(inSearchView)
                {
                    inSearchView = false;
                    title.setText("Prescriptions");
                    Uri imgUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/search");
                    searchButton.setImageURI(imgUri);

                    getPrescriptionsFromDB();
                }
                else
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewPrescriptions.this);
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    builder.setTitle("Search Prescriptions");

                    builder.setView(R.layout.view_search_prescription);


                    builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Will override this later, this declaration is left in to prevent compatibility issues
                        }
                    });

                    AlertDialog dialog = builder.show();

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                    {
                        EditText enterMedicationName = dialog.findViewById(R.id.EnterMedicationName);
                        EditText enterDoctorName = dialog.findViewById(R.id.EnterDoctorName);

                        @Override
                        public void onClick(View v)
                        {
                            String medName = enterMedicationName.getText().toString();
                            String doctorName = enterDoctorName.getText().toString();

                            searchPrescriptionsFromDB(medName, doctorName);

                            inSearchView = true;
                            title.setText("Search Results:");
                            Uri imgUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/cancel_search");
                            searchButton.setImageURI(imgUri);

                            dialog.dismiss();
                        }
                    });
                }
            }
        });
    }
    public void addPrescriptionAlarm(Prescription alarmFor)
    {
        BroadcastReceiver alarmReceiver;

        LocalTime alarmTime = LocalTime.from(alarmFor.getTime());
        ZoneId timeZone = ZoneId.systemDefault();
        LocalDate alarmDate = alarmFor.getNextDayTaken();

        Instant alarmDateTime = alarmDate.atTime(alarmTime).atZone(timeZone).toInstant();
        Duration timeToAlarm = Duration.between(LocalDateTime.now().atZone(timeZone).toInstant(), alarmDateTime);

        Intent alarmIntent = new Intent("PRESCRIPTION_ALARM");
        String medname = alarmFor.getMedication().getName();
        alarmIntent.putExtra("medication", medname);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_MUTABLE);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        long alarmTimeMillis = System.currentTimeMillis() + timeToAlarm.getSeconds()*1000;
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,  alarmTimeMillis, pendingIntent);

        IntentFilter filter = new IntentFilter("PRESCRIPTION_ALARM");
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        alarmReceiver = new PrescriptionReminderReceiver();
        registerReceiver(alarmReceiver, filter);

        Toast.makeText(ViewPrescriptions.this, "Medication Saved.\nNext Dose: "+ dateTimeFormat.format(LocalDateTime.of(alarmDate, alarmTime)), Toast.LENGTH_SHORT).show();

    }


    private boolean checkForConflicts(Medication toAdd)
    {
        List<Medication> foundConflicts = new ArrayList<>();
        for(Prescription currentPrescription : prescriptionData)
        {
            if(toAdd.checkConflict(currentPrescription.getMedication()))
            {
                foundConflicts.add(currentPrescription.getMedication());
            }
        }

        if(foundConflicts.size() == 0)
            return false;
        else
        {
            AlertDialog.Builder warningBuilder = new AlertDialog.Builder(ViewPrescriptions.this);
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



    private void addToDB(Prescription toAdd)
    {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("prescriptions");

            Map<String, Object> data = new HashMap<>();
            data.put("medication", toAdd.getMedication().getDatabaseID());
            data.put("dosage", toAdd.getDosage());
            data.put("doctor", toAdd.getPrescribedBy().getDatabaseID());
            data.put("time", timeFormat.format(toAdd.getTime()));
            data.put("schedule", toAdd.printDaysTaken());

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


    private void getDoctors()
    {
        List<Contact> data = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
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
                        doctorList = new ArrayList<>();
                        doctorList.addAll(data);

                        adapter.notifyDataSetChanged();
                        getMedications();
                    }
                }
            });
        }
    }
    private void getMedications()
    {
        List<Medication> data = new ArrayList<>();
        List<String> conflictsRawStr = new ArrayList<>();


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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

                medicationList.clear();
                medicationList.addAll(data);

                adapter.notifyDataSetChanged();
                getPrescriptionsFromDB();
            }
        });
    }
    private void searchPrescriptionsFromDB(String medName, String docName)
    {
        List<Prescription> data = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
            CollectionReference prescriptionsCollection = db.collection("users").document(uid).collection("prescriptions");
            prescriptionsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {


                            String doctorId = document.getString("doctor");
                            String medicationId = document.getString("medication");

                            Contact savedDoctor = null;
                            for(Contact c : doctorList)
                                if(c.getDatabaseID().equals(doctorId))
                                    savedDoctor = c;
                            if(savedDoctor == null)
                                savedDoctor = new Contact("Unknown", "1234567890", "abc@def.ghi", "Doctor Contact");

                            if(!savedDoctor.getName().contains(docName))
                                continue;


                            Medication savedMedication = null;
                            for(Medication m : medicationList)
                                if(m.getDatabaseID().equals(medicationId))
                                    savedMedication = m;
                            if(savedMedication == null)
                                savedMedication = new Medication("Unknown");

                            if(!savedMedication.getName().contains(medName))
                                continue;

                            String timeString = document.getString("time");

                            if(timeString == null)
                                timeString = timeFormat.format(LocalDateTime.now());

                            LocalDateTime prescriptionTime;
                            try
                            {
                                prescriptionTime = LocalDateTime.parse(timeString, timeFormat);
                            }
                            catch (DateTimeParseException e)
                            {
                                prescriptionTime = LocalDateTime.now();
                            }

                            String scheduleStr = document.getString("schedule");
                            if(scheduleStr == null)
                                scheduleStr = "Every Day";

                            Prescription toAdd = new Prescription(
                                    savedMedication,
                                    document.getString("dosage"),
                                    savedDoctor,
                                    prescriptionTime,
                                    Prescription.parseDaysTakenFromString(scheduleStr));

                            toAdd.setDatabaseID(document.getId());

                            data.add(toAdd);
                        }
                    }
                    prescriptionData.clear();
                    prescriptionData.addAll(data);

                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void getPrescriptionsFromDB()
    {
        List<Prescription> data = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
            CollectionReference prescriptionsCollection = db.collection("users").document(uid).collection("prescriptions");
            prescriptionsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {


                            String doctorId = document.getString("doctor");
                            String medicationId = document.getString("medication");

                            Contact savedDoctor = null;
                            for(Contact c : doctorList)
                                if(c.getDatabaseID().equals(doctorId))
                                    savedDoctor = c;
                            if(savedDoctor == null)
                                savedDoctor = new Contact("Unknown", "1234567890", "abc@def.ghi", "Doctor Contact");

                            Medication savedMedication = null;
                            for(Medication m : medicationList)
                                if(m.getDatabaseID().equals(medicationId))
                                    savedMedication = m;
                            if(savedMedication == null)
                                savedMedication = new Medication("Unknown");

                            String timeString = document.getString("time");

                            if(timeString == null)
                                timeString = timeFormat.format(LocalDateTime.now());

                            LocalDateTime prescriptionTime;
                            try
                            {
                                prescriptionTime = LocalDateTime.parse(timeString, timeFormat);
                            }
                            catch (DateTimeParseException e)
                            {
                                prescriptionTime = LocalDateTime.now();
                            }

                            String scheduleStr = document.getString("schedule");
                            if(scheduleStr == null)
                                scheduleStr = "Every Day";

                            Prescription toAdd = new Prescription(
                                    savedMedication,
                                    document.getString("dosage"),
                                    savedDoctor,
                                    prescriptionTime,
                                    Prescription.parseDaysTakenFromString(scheduleStr));

                            toAdd.setDatabaseID(document.getId());

                            data.add(toAdd);
                        }
                    }
                    prescriptionData.clear();
                    prescriptionData.addAll(data);

                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

}