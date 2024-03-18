package com.example.healthapp;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteAdapter extends RecyclerView.Adapter<NotesViewHolder> {

    List<Note> list;
    Context context;
    EditText titleEntry;
    EditText contentEntry;
    Spinner spinnerNoteType;

    public NoteAdapter(List<Note> list, Context context)
    {
        this.list = list;
        this.context = context;
    }
    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View photoView = inflater.inflate(R.layout.list_note,parent, false);
        NotesViewHolder viewHolder = new NotesViewHolder(photoView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        holder.type.setText(list.get(position).getNoteType());
        holder.title.setText(list.get(position).getTitle());
        holder.content.setText(list.get(position).getContent());

        holder.view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                builder.setTitle("Manage Note");

                builder.setView(R.layout.view_create_note);


                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(v.getContext());
                        builder2.setTitle("Delete Note");
                        builder2.setMessage("Are you sure?");
                        builder2.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                deleteNote(list.get(holder.getAdapterPosition())); //Delete from DB
                                list.remove(holder.getAdapterPosition()); //Delete from in-memory list

                                notifyItemRemoved(holder.getAdapterPosition());

                                Toast.makeText(context.getApplicationContext(), "Note Deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder2.setNegativeButton("No", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //Do nothing, just close dialog without saving
                            }
                        });
                        AlertDialog dialog2 = builder2.show();
                    }
                });

                AlertDialog dialog = builder.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if(titleEntry.getText().toString().isEmpty())
                        {
                            Toast.makeText(v.getContext(), "Please enter a title.", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            list.get(holder.getAdapterPosition()).setTitle(titleEntry.getText().toString());
                            holder.title.setText(titleEntry.getText().toString());

                            list.get(holder.getAdapterPosition()).setContent(contentEntry.getText().toString());
                            holder.content.setText(contentEntry.getText().toString());

                            list.get(holder.getAdapterPosition()).setNoteType(spinnerNoteType.getSelectedItem().toString());
                            holder.type.setText(spinnerNoteType.getSelectedItem().toString());

                            updateNote(list.get(holder.getAdapterPosition()));

                            Toast.makeText(context.getApplicationContext(), "Note Saved!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                    }
                });



                titleEntry = dialog.findViewById(R.id.EnterTitle);
                contentEntry = dialog.findViewById(R.id.EnterContent);

                spinnerNoteType = dialog.findViewById(R.id.SelectNoteType);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(v.getContext(), R.array.note_types, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerNoteType.setAdapter(adapter);

                titleEntry.setText(list.get(holder.getAdapterPosition()).getTitle());
                contentEntry.setText(list.get(holder.getAdapterPosition()).getContent());
                spinnerNoteType.setSelection(adapter.getPosition(list.get(holder.getAdapterPosition()).getNoteType()));

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void updateNote(Note toUpdate)
    {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            CollectionReference contactsCollection = db.collection("users").document(uid).collection("notes");

            Map<String, Object> data = new HashMap<>();
            data.put("title", toUpdate.getTitle());
            data.put("content", toUpdate.getContent());
            data.put("type", toUpdate.getNoteType());

            contactsCollection.document(toUpdate.getDatabaseID()).set(data);
        }
    }
    private void deleteNote(Note toDelete)
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            CollectionReference contactsCollection = db.collection("users").document(uid).collection("notes");


            contactsCollection.document(toDelete.getDatabaseID()).delete();
        }
    }

}
