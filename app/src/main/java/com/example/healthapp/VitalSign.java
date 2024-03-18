package com.example.healthapp;

import com.google.type.DateTime;

import java.time.LocalDateTime;

public class VitalSign implements Comparable<VitalSign>{

    private String type;
    private double reading;
    private double reading2;
    private LocalDateTime time;
    private String databaseID;

    public VitalSign(String type, double reading, LocalDateTime time)
    {
        this.type = type;
        this.reading = reading;
        this.time = time;
    }

    public VitalSign(String type, double reading, double reading2, LocalDateTime time)
    {
        this.type = type;
        this.reading = reading;
        this.reading2 = reading2;
        this.time = time;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public double getReading() {
        return reading;
    }
    public void setReading(double reading) {
        this.reading = reading;
    }
    public double getReading2() {
        return reading2;
    }
    public void setReading2(double reading) {
        this.reading2 = reading;
    }


    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getDatabaseID()
    {
        return databaseID;
    }
    public void setDatabaseID(String databaseID)
    {
        this.databaseID = databaseID;
    }

    @Override
    public int compareTo(VitalSign o)
    {
        return this.time.compareTo(o.time);
    }
}
