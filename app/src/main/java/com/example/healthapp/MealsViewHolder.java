package com.example.healthapp;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MealsViewHolder extends RecyclerView.ViewHolder {

    TextView calories, carbs, fats, protein, type, time;
    View view;
    public MealsViewHolder(@NonNull View itemView) {
        super(itemView);

        calories = (TextView) itemView.findViewById(R.id.caloriesField);
        carbs = (TextView) itemView.findViewById(R.id.carbsField);
        fats = (TextView) itemView.findViewById(R.id.fatsField);
        protein = (TextView) itemView.findViewById(R.id.proteinField);
        type = (TextView) itemView.findViewById(R.id.typeField);
        time = (TextView) itemView.findViewById(R.id.timeField);

        view = itemView;
    }
}
