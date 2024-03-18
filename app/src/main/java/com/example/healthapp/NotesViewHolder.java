package com.example.healthapp;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotesViewHolder extends RecyclerView.ViewHolder {

    TextView type, title, description, content;
    View view;
    public NotesViewHolder(@NonNull View itemView) {
        super(itemView);

        type = (TextView) itemView.findViewById(R.id.typeField);
        title = (TextView) itemView.findViewById(R.id.titleField);
        content = (TextView) itemView.findViewById(R.id.contentField);

        view = itemView;
    }
}
