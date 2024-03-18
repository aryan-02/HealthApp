package com.example.healthapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class Prescription implements Serializable
{

    private Medication medication;
    private String dosage;

    private LocalDateTime time;
    private Contact prescribedBy;
    private boolean[] daysTaken;

    private String databaseID;

    public Prescription(Medication medication, String dosage, Contact prescribedBy, LocalDateTime time, boolean[] daysTaken)
    {
        this.medication = medication;
        this.dosage = dosage;
        this.prescribedBy = prescribedBy;
        this.time = time;
        this.daysTaken = daysTaken;
    }

    public Medication getMedication() {
        return medication;
    }
    public void setMedication(Medication medication) {
        this.medication = medication;
    }

    public String getDosage() {
        return dosage;
    }
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }


    public LocalDateTime getTime() {
        return time;
    }
    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Contact getPrescribedBy() {
        return prescribedBy;
    }
    public void setPrescribedBy(Contact prescribedBy) {
        this.prescribedBy = prescribedBy;
    }

    public boolean[] getDaysTaken() {
        return daysTaken;
    }
    public void setDaysTaken(boolean[] daysTaken) {
        this.daysTaken = daysTaken;
    }

    public String printDaysTaken()
    {
        if(daysTaken[0] && daysTaken[1] && daysTaken[2] && daysTaken[3] && daysTaken[4] && daysTaken[5] && daysTaken[6])
            return "Every Day";

        if(daysTaken[1] && daysTaken[2] && daysTaken[3] && daysTaken[4] && daysTaken[5])
            return "Weekdays";

        if(daysTaken[0] && daysTaken[6])
            return "Weekends";

        String schedule = "";
        if(daysTaken[0])
            schedule += "S ";
        if(daysTaken[1])
            schedule += "M ";
        if(daysTaken[2])
            schedule += "T ";
        if(daysTaken[3])
            schedule += "W ";
        if(daysTaken[4])
            schedule += "Th ";
        if(daysTaken[5])
            schedule += "F ";
        if(daysTaken[6])
            schedule += "Sa ";

        return schedule;
    }

    public static String printDaysTakenFromList(boolean[] input)
    {
        if(input[0] && input[1] && input[2] && input[3] && input[4] && input[5] && input[6])
            return "Every Day";

        if(input[1] && input[2] && input[3] && input[4] && input[5])
            return "Weekdays";

        if(input[0] && input[6])
            return "Weekends";

        String schedule = "";
        if(input[0])
            schedule += "S ";
        if(input[1])
            schedule += "M ";
        if(input[2])
            schedule += "T ";
        if(input[3])
            schedule += "W ";
        if(input[4])
            schedule += "Th ";
        if(input[5])
            schedule += "F ";
        if(input[6])
            schedule += "Sa ";

        return schedule;
    }

    public static boolean[] parseDaysTakenFromString(String input)
    {
        boolean[] selected = new boolean[7];

        if(input == null)
        {
            selected[0] = false;
            selected[1] = false;
            selected[2] = false;
            selected[3] = false;
            selected[4] = false;
            selected[5] = false;
            selected[6] = false;
            return selected;
        }

        if(input.equals("Every Day"))
        {
            selected[0] = true;
            selected[1] = true;
            selected[2] = true;
            selected[3] = true;
            selected[4] = true;
            selected[5] = true;
            selected[6] = true;
            return selected;
        }
        if(input.equals("Weekdays"))
        {
            selected[0] = false;
            selected[1] = true;
            selected[2] = true;
            selected[3] = true;
            selected[4] = true;
            selected[5] = true;
            selected[6] = false;
            return selected;
        }
        if(input.equals("Weekends"))
        {
            selected[0] = true;
            selected[1] = false;
            selected[2] = false;
            selected[3] = false;
            selected[4] = false;
            selected[5] = false;
            selected[6] = true;
            return selected;
        }
        else
        {
            selected[0] = input.contains("S ");
            selected[1] = input.contains("M ");
            selected[2] = input.contains("T ");
            selected[3] = input.contains("W ");
            selected[4] = input.contains("Th ");
            selected[5] = input.contains("F ");
            selected[6] = input.contains("Sa ");

            return selected;
        }
    }


    public String getDatabaseID()
    {
        return databaseID;
    }
    public void setDatabaseID(String databaseID)
    {
        this.databaseID = databaseID;
    }


    public LocalDate getNextDayTaken()
    {
        int dayStartIndex = LocalDate.now().getDayOfWeek().getValue(); //Returns 1=Monday -> 7=Sunday

        if(dayStartIndex == 7)
            dayStartIndex = 0;

        int skipToday = 0;
        if(LocalTime.now().isAfter(time.toLocalTime()))
        {
            dayStartIndex++;
            skipToday = 1;
        }

        for(int i = 0; i < 7; i++)
        {
            int d = dayStartIndex + i;
            if(daysTaken[d%7])
            {
                return LocalDate.now().plusDays(i+skipToday);
            }
        }
        return null;
    }

}
