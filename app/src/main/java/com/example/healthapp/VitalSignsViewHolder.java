package com.example.healthapp;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VitalSignsViewHolder extends RecyclerView.ViewHolder {

    TextView type, reading, time;
    View view;

    public VitalSignsViewHolder(@NonNull View itemView) {
        super(itemView);

        type = (TextView) itemView.findViewById(R.id.typeField);
        reading = (TextView) itemView.findViewById(R.id.readingField);
        time = (TextView) itemView.findViewById(R.id.timeField);

        view = itemView;

    }
}
