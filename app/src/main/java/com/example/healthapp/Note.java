package com.example.healthapp;


import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Note {

    private String title;
    private String noteType;
    private String content;
    private String databaseID;



    public Note(String title, String noteType, String content)
    {
        this.title = title;
        this.noteType = noteType;
        this.content = content;

    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getNoteType() {
        return noteType;
    }
    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getDatabaseID()
    {
        return databaseID;
    }
    public void setDatabaseID(String databaseID)
    {
        this.databaseID = databaseID;
    }
}
