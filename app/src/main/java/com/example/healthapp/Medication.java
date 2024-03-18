package com.example.healthapp;

import java.util.ArrayList;

public class Medication {

    private String name;
    private ArrayList<Medication> conflictsWith;

    private String databaseID;

    public Medication(String name)
    {
        this.name = name;
        conflictsWith = new ArrayList<Medication>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public boolean checkConflict(Medication other)
    {
        return conflictsWith.contains(other) || other.conflictsWith.contains(this);
    }
    public void addConflict(Medication other)
    {
        conflictsWith.add(other);
    }
    public void removeConflict(Medication other)
    {
        conflictsWith.remove(other);
    }

    public ArrayList<Medication> getConflicts()
    {
        return conflictsWith;
    }

    public String getDatabaseID()
    {
        return databaseID;
    }
    public void setDatabaseID(String databaseID)
    {
        this.databaseID = databaseID;
    }

    public String printConflicts()
    {
        StringBuilder conflictsStr = new StringBuilder("No Conflicts");

        if(conflictsWith.size() >= 1)
            conflictsStr = new StringBuilder("Conflicts: " + conflictsWith.get(0).getName());

        for(int i = 1; i < conflictsWith.size(); i++)
            conflictsStr.append(", ").append(conflictsWith.get(i).getName());

        return conflictsStr.toString();
    }
}
