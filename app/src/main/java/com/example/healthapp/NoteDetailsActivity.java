package com.example.healthapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

public class NoteDetailsActivity extends AppCompatActivity {
    EditText titleText, contentText;
    ImageButton saveButton;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        titleText = findViewById(R.id.note_title);
        contentText = findViewById(R.id.contents_text);
        saveButton = findViewById(R.id.save_note_button);

        saveButton.setOnClickListener((v)->saveNote());

    }

    public void saveNote(){
        String noteTitle = titleText.getText().toString();
        String noteContent = contentText.getText().toString();
        if(noteTitle == null || noteTitle.isEmpty()){
            titleText.setError("Title cannot be empty!");
            return;
        }

    }
}