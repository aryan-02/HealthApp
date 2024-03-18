package com.example.healthapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewMedications extends AppCompatActivity {
    MedicationsAdapter adapter;
    RecyclerView recyclerView;
    FloatingActionButton addMedication;
    ImageButton backButton, searchButton;
    EditText nameEntry;
    EditText conflictsEntry;
    FirebaseFirestore db;
    List<Medication> medicationData;
    List<Medication> conflictsList;

    boolean [] selectedConflicts;
    String [] medicationNames;

    boolean inSearchView;
    TextView title;

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_medications);

        medicationData = new ArrayList<>();
        getMedicationsFromDB();

        inSearchView = false;

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        adapter = new MedicationsAdapter(medicationData, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewMedications.this));

        addMedication = findViewById(R.id.addMedication);
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
        addMedication.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewMedications.this);
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                builder.setTitle("Create New Medication");

                builder.setView(R.layout.view_create_medication);


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
                        String name = nameEntry.getText().toString();

                        if(name.isEmpty())
                        {
                            Toast.makeText(ViewMedications.this, "Name Required", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            //input validated
                            Medication newMedication = new Medication(name);
                            newMedication.getConflicts().clear();
                            newMedication.getConflicts().addAll(conflictsList);

                            addToDB(newMedication);
                            medicationData.add(0, newMedication);
                            recyclerView.scrollToPosition(0);
                            adapter.notifyItemInserted(0);

                            dialog.dismiss();
                        }
                    }
                });

                nameEntry = dialog.findViewById(R.id.EnterName);
                conflictsEntry = dialog.findViewById(R.id.EnterConflicts);

                medicationNames = new String[medicationData.size()];
                selectedConflicts = new boolean[medicationData.size()];
                for (int i = 0; i < medicationData.size(); i++)
                {
                    medicationNames[i] = medicationData.get(i).getName();
                    selectedConflicts[i] = false;
                }
                conflictsList = new ArrayList<>();

                conflictsEntry.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        AlertDialog.Builder conflictsBuilder = new AlertDialog.Builder(ViewMedications.this);
                        conflictsBuilder.setTitle("Setup Medication Conflicts");

                        conflictsBuilder.setMultiChoiceItems(medicationNames, selectedConflicts, new DialogInterface.OnMultiChoiceClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked)
                            {
                                if(isChecked)
                                {
                                    conflictsList.add(medicationData.get(which));
                                }
                                else
                                {
                                    conflictsList.remove(medicationData.get(which));
                                }
                            }
                        });
                        conflictsBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                            }
                        });

                        AlertDialog dialogConflicts = conflictsBuilder.show();

                        dialogConflicts.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                StringBuilder conflictsStr = new StringBuilder("No Conflicts");

                                if(conflictsList.size() >= 1)
                                    conflictsStr = new StringBuilder("Conflicts: " + conflictsList.get(0).getName());

                                for(int i = 1; i < conflictsList.size(); i++)
                                    conflictsStr.append(", ").append(conflictsList.get(i).getName());

                                conflictsEntry.setText(conflictsStr);

                                dialogConflicts.dismiss();
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
                    title.setText("Medications");
                    Uri imgUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/search");
                    searchButton.setImageURI(imgUri);

                    getMedicationsFromDB();
                }
                else
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewMedications.this);
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    builder.setTitle("Search Medications");

                    builder.setView(R.layout.view_search_medication);


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
                            String name = nameEntry.getText().toString();

                            searchMedicationsFromDB(name);

                            inSearchView = true;
                            title.setText("Search Results:");
                            Uri imgUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/cancel_search");
                            searchButton.setImageURI(imgUri);

                            dialog.dismiss();
                        }
                    });

                    nameEntry = dialog.findViewById(R.id.EnterName);
                }
            }
        });


    }



    private void addToDB(Medication toAdd)
    {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        CollectionReference collection = db.collection("medications");

        Map<String, Object> data = new HashMap<>();
        data.put("name", toAdd.getName());

        StringBuilder conflicts = new StringBuilder();
        if(toAdd.getConflicts().size() >= 1)
            conflicts.append(toAdd.getConflicts().get(0).getDatabaseID());
        for(int i = 0; i < toAdd.getConflicts().size(); i++)
            conflicts.append(",").append(toAdd.getConflicts().get(i).getDatabaseID());

        data.put("conflicts", conflicts.toString());

        collection.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
        {
            @Override
            public void onSuccess(DocumentReference documentReference)
            {
                toAdd.setDatabaseID(documentReference.getId());
            }
        });

    }



    private void searchMedicationsFromDB(String nameSearch)
    {
        List<Medication> data = new ArrayList<>();
        List<String> conflictsRawStr = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        CollectionReference collection = db.collection("medications");
        collection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String currentName = document.getString("name");

                        if(currentName == null)
                            continue;

                        boolean matches = true;
                        if(!nameSearch.isEmpty() && !currentName.contains(nameSearch))
                        {
                            matches = false;
                            Log.d("search", nameSearch + " != " + currentName);
                        }
                        if(matches)
                        {
                            Medication toAdd = new Medication(document.getString("name"));

                            toAdd.setDatabaseID(document.getId());

                            data.add(toAdd);
                            String c = document.getString("conflicts");
                            if(c == null)
                                c = "";
                            conflictsRawStr.add(c);
                        }


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
                medicationData.clear();
                medicationData.addAll(data);
                Toast.makeText(ViewMedications.this, medicationData.size() + " results found.", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void getMedicationsFromDB()
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

                medicationData.clear();
                medicationData.addAll(data);
                adapter.notifyDataSetChanged();
            }
        });
    }

}