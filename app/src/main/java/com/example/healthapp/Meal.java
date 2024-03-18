package com.example.healthapp;

import com.google.type.DateTime;

import java.sql.Time;
import java.time.LocalDateTime;

public class Meal implements Comparable<Meal>{

    private String mealType;
    private double carbs;
    private double fats;
    private double protein;
    private int calories;

    private String time;

    private LocalDateTime mealTime;
    private String databaseID;


    public Meal(String type, double carbs, double fats, double protein, int calories, LocalDateTime mealTime)
    {
        this.mealType = type;
        this.carbs = carbs;
        this.fats = fats;
        this.protein = protein;
        this.calories = calories;
        this.mealTime = mealTime;
    }

    public String getMealType() {
        return mealType;
    }
    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public double getCarbs() {
        return carbs;
    }
    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getFats() {
        return fats;
    }
    public void setFats(double fats) {
        this.fats = fats;
    }

    public double getProtein() {
        return protein;
    }
    public void setProtein(double protein) {
        this.protein = protein;
    }

    public int getCalories() {
        return calories;
    }
    public void setCalories(int calories) {
        this.calories = calories;
    }

    public void setMealTime(LocalDateTime mealTime)
    {
        this.mealTime = mealTime;
    }

    public LocalDateTime getMealTime()
    {
        return mealTime;
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
    public int compareTo(Meal o)
    {
        return this.mealTime.compareTo(o.mealTime);
    }
}
