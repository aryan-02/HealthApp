package com.example.healthapp;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

public class contactViewHolder extends RecyclerView.ViewHolder {

    TextView name, email, phone, type;
    View view;

    public contactViewHolder(@NonNull View itemView) {
        super(itemView);

        name = (TextView) itemView.findViewById(R.id.nameField);
        email = (TextView) itemView.findViewById(R.id.emailField);
        phone = (TextView) itemView.findViewById(R.id.phoneField);
        type = (TextView) itemView.findViewById(R.id.typeField);

        view = itemView;
    }
}
