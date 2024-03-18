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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewContacts extends AppCompatActivity {
    ContactsAdapter adapter;
    RecyclerView recyclerView;
    FloatingActionButton addContact;
    ImageButton backButton, searchButton;
    EditText nameEntry;
    EditText emailEntry;
    EditText phoneEntry;
    Spinner spinnerContactType;
    FirebaseUser user;
    FirebaseFirestore db;
    String uid;
    List<Contact> contactData;

    boolean inSearchView;
    TextView title;

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contacts);

        contactData = new ArrayList<>();
        getContactsFromDB();

        inSearchView = false;

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        adapter = new ContactsAdapter(contactData, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewContacts.this));

        addContact = findViewById(R.id.addContact);
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
        addContact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewContacts.this);
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                builder.setTitle("Create New Contact");

                builder.setView(R.layout.view_create_contact);


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
                        String phone = phoneEntry.getText().toString();
                        String email = emailEntry.getText().toString();
                        String type = spinnerContactType.getSelectedItem().toString();

                        if(name.isEmpty())
                        {
                            Toast.makeText(ViewContacts.this, "Name Required", Toast.LENGTH_SHORT).show();
                        }
                        else if(!Contact.ValidatePhone(phone))
                        {
                            Toast.makeText(ViewContacts.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                        }
                        else if(!Contact.ValidateEmail(email))
                        {
                            Toast.makeText(ViewContacts.this, "Invalid email", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            //input validated
                            Contact newContact = new Contact(name, phone, email, type);
                            addToDB(newContact);
                            contactData.add(0, newContact);
                            recyclerView.scrollToPosition(0);
                            adapter.notifyItemInserted(0);

                            dialog.dismiss();
                        }
                    }
                });

                nameEntry = dialog.findViewById(R.id.EnterName);
                emailEntry = dialog.findViewById(R.id.EnterEmail);
                phoneEntry = dialog.findViewById(R.id.EnterPhone);
                spinnerContactType = dialog.findViewById(R.id.SelectContactType);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ViewContacts.this, R.array.contact_types, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerContactType.setAdapter(adapter);

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
                    title.setText("Contacts");
                    Uri imgUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/search");
                    searchButton.setImageURI(imgUri);

                    getContactsFromDB();
                }
                else
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewContacts.this);
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    builder.setTitle("Search Contacts");

                    builder.setView(R.layout.view_search_contact);


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
                            String type = spinnerContactType.getSelectedItem().toString();

                            searchContactsFromDB(name, type);

                            inSearchView = true;
                            title.setText("Search Results:");
                            Uri imgUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/cancel_search");
                            searchButton.setImageURI(imgUri);

                            dialog.dismiss();
                        }
                    });

                    nameEntry = dialog.findViewById(R.id.EnterName);
                    spinnerContactType = dialog.findViewById(R.id.SelectContactType);
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ViewContacts.this, R.array.search_contact_types, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerContactType.setAdapter(adapter);
                }
            }
        });


    }



    private void addToDB(Contact toAdd)
    {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("contacts");

            Map<String, Object> data = new HashMap<>();
            data.put("name", toAdd.getName());
            data.put("phone", toAdd.getPhoneNumber());
            data.put("email", toAdd.getEmail());
            data.put("type", toAdd.getType());

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



    private void searchContactsFromDB(String nameSearch, String typeSearch)
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

                            String currentName = document.getString("name");
                            String currentType = document.getString("type");

                            if(currentName == null || currentType == null)
                                continue;

                            boolean matches = true;
                            if(!nameSearch.isEmpty() && !currentName.contains(nameSearch))
                            {
                                matches = false;
                                Log.d("search", nameSearch + " != " + currentName);
                            }
                            if(!typeSearch.equals("Any") && !currentType.equals(typeSearch))
                            {
                                matches = false;
                                Log.d("search", typeSearch + " != " + currentType);

                            }

                            if(matches)
                            {
                                Contact toAdd = new Contact(
                                        document.getString("name"),
                                        document.getString("phone"),
                                        document.getString("email"),
                                        document.getString("type"));

                                toAdd.setDatabaseID(document.getId());

                                data.add(toAdd);
                                Log.d("search", "FOUND: " + toAdd.getName());
                            }

                        }
                    }
                    Log.d("Search", data.size() + " results found.");
                    contactData.clear();
                    contactData.addAll(data);
                    Toast.makeText(ViewContacts.this, contactData.size() + " results found.", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void getContactsFromDB()
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
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            Contact toAdd = new Contact(
                                    document.getString("name"),
                                    document.getString("phone"),
                                    document.getString("email"),
                                    document.getString("type"));

                            toAdd.setDatabaseID(document.getId());

                            data.add(toAdd);
                        }
                    }

                    contactData.clear();
                    contactData.addAll(data);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

}