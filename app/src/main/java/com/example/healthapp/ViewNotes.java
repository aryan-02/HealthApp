package com.example.healthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewNotes extends AppCompatActivity {
    NoteAdapter adapter;
    RecyclerView recyclerView;
    FloatingActionButton addNote;
    ImageButton backButton, searchButton;
    EditText titleEntry;
    EditText contentEntry;
    Spinner spinnerNoteType;
    FirebaseUser user;
    FirebaseFirestore db;
    String uid;
    List<Note> notesData;
    boolean inSearchView;
    TextView title;

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes);

        notesData = new ArrayList<>();
        getNotesFromDB();

        inSearchView = false;

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        adapter = new NoteAdapter(notesData, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewNotes.this));

        addNote = findViewById(R.id.addNote);
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
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewNotes.this);
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                builder.setTitle("Write a New Note");

                builder.setView(R.layout.view_create_note);


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
                        String title = titleEntry.getText().toString();
                        String content = contentEntry.getText().toString();
                        String type = spinnerNoteType.getSelectedItem().toString();

                        if(title.isEmpty())
                        {
                            Toast.makeText(ViewNotes.this, "Please enter a title.", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            //input validated
                            Note newNote = new Note(title, type, content);
                            addToDB(newNote);
                            notesData.add(0, newNote);
                            recyclerView.scrollToPosition(0);
                            adapter.notifyItemInserted(0);

                            dialog.dismiss();
                        }
                    }
                });

                titleEntry = dialog.findViewById(R.id.EnterTitle);
                contentEntry = dialog.findViewById(R.id.EnterContent);
                spinnerNoteType = dialog.findViewById(R.id.SelectNoteType);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ViewNotes.this, R.array.note_types, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerNoteType.setAdapter(adapter);

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
                    title.setText("Notes");
                    Uri imgUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/search");
                    searchButton.setImageURI(imgUri);

                    getNotesFromDB();
                }
                else
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewNotes.this);
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    builder.setTitle("Search Notes");

                    builder.setView(R.layout.view_search_note);


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
                            String titleEntered = titleEntry.getText().toString();
                            String type = spinnerNoteType.getSelectedItem().toString();

                            searchNotesFromDB(titleEntered, type);

                            inSearchView = true;
                            title.setText("Search Results:");
                            Uri imgUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/cancel_search");
                            searchButton.setImageURI(imgUri);

                            dialog.dismiss();
                        }
                    });

                    titleEntry = dialog.findViewById(R.id.EnterTitle);
                    spinnerNoteType = dialog.findViewById(R.id.SelectNoteType);
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ViewNotes.this, R.array.search_note_types, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerNoteType.setAdapter(adapter);
                }
            }
        });
    }



    private void addToDB(Note toAdd)
    {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("notes");

            Map<String, Object> data = new HashMap<>();
            data.put("title", toAdd.getTitle());
            data.put("content", toAdd.getContent());
            data.put("type", toAdd.getNoteType());

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
    private void searchNotesFromDB(String titleSearch, String typeSearch)
    {
        List<Note> data = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
            CollectionReference collection = db.collection("users").document(uid).collection("notes");
            collection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            String currentTitle = document.getString("title");
                            String currentType = document.getString("type");

                            if(currentTitle == null || currentType == null)
                                continue;

                            boolean matches = true;
                            if(!titleSearch.isEmpty() && !currentTitle.contains(titleSearch))
                            {
                                matches = false;
                            }
                            if(!typeSearch.equals("Any") && !currentType.equals(typeSearch))
                            {
                                matches = false;
                            }

                            if(matches)
                            {
                                Note toAdd = new Note(
                                        document.getString("title"),
                                        document.getString("type"),
                                        document.getString("content"));

                                toAdd.setDatabaseID(document.getId());

                                data.add(toAdd);
                            }

                        }
                    }
                    notesData.clear();
                    notesData.addAll(data);

                    Toast.makeText(ViewNotes.this, notesData.size() + " results found.", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }


    private void getNotesFromDB()
    {
        List<Note> data = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null)
        {
            db = FirebaseFirestore.getInstance();
            uid = user.getUid();
            CollectionReference notesCollection = db.collection("users").document(uid).collection("notes");
            notesCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            Note toAdd = new Note(
                                    document.getString("title"),
                                    document.getString("type"),
                                    document.getString("content"));

                            toAdd.setDatabaseID(document.getId());

                            data.add(toAdd);
                        }
                    } else {
                        Toast.makeText(ViewNotes.this, "No contacts found", Toast.LENGTH_SHORT).show();
                    }

                    notesData.clear();
                    notesData.addAll(data);

                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

}