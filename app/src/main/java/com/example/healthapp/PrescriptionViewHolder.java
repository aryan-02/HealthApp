package com.example.healthapp;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PrescriptionViewHolder extends RecyclerView.ViewHolder {

    TextView medication, dosage, days, time, doctor;

    View view;
    public PrescriptionViewHolder(@NonNull View itemView) {
        super(itemView);

        medication = (TextView) itemView.findViewById(R.id.medicationField);
        dosage = (TextView) itemView.findViewById(R.id.dosageField);
        time = (TextView) itemView.findViewById(R.id.timeField);
        doctor = (TextView) itemView.findViewById(R.id.doctorField);
        days = (TextView) itemView.findViewById(R.id.scheduleField);

        view = itemView;
    }
}
