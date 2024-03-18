package com.example.healthapp;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MedicationsViewHolder extends RecyclerView.ViewHolder
{
    TextView name, conflicts;
    View view;

    public MedicationsViewHolder(@NonNull View itemView)
    {
        super(itemView);

        name = (TextView) itemView.findViewById(R.id.nameField);
        conflicts = (TextView) itemView.findViewById(R.id.conflictsField);

        view = itemView;
    }

}
