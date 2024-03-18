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

public class ContactsAdapter extends RecyclerView.Adapter<contactViewHolder>{

    List<Contact> list;
    EditText nameEntry;
    EditText emailEntry;
    EditText phoneEntry;
    Spinner spinnerContactType;
    Context context;

    public ContactsAdapter(List<Contact> list, Context context)
    {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public contactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View photoView = inflater.inflate(R.layout.list_contact,parent, false);
        contactViewHolder viewHolder = new contactViewHolder(photoView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull contactViewHolder holder, int position) {
        holder.name.setText(list.get(position).getName());
        holder.email.setText(list.get(position).getEmail());
        holder.phone.setText(list.get(position).getFormattedPhoneNumber());
        holder.type.setText(list.get(position).getType());


        holder.view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                builder.setTitle("Manage Contact");

                builder.setView(R.layout.view_create_contact);


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
                        builder2.setTitle("Delete Contact");
                        builder2.setMessage("Are you sure?");
                        builder2.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                deleteContact(list.get(holder.getAdapterPosition())); //Delete from DB
                                list.remove(holder.getAdapterPosition()); //Delete from in-memory list

                                notifyItemRemoved(holder.getAdapterPosition());

                                Toast.makeText(context.getApplicationContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
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
                        if(nameEntry.getText().toString().isEmpty())
                        {
                            Toast.makeText(context.getApplicationContext(), "Name Required", Toast.LENGTH_SHORT).show();
                        }
                        else if(!Contact.ValidatePhone(phoneEntry.getText().toString()))
                        {
                            Toast.makeText(context.getApplicationContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();
                        }
                        else if(!Contact.ValidateEmail(emailEntry.getText().toString()))
                        {
                            Toast.makeText(context.getApplicationContext(), "Invalid email", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            list.get(holder.getAdapterPosition()).setName(nameEntry.getText().toString());
                            holder.name.setText(nameEntry.getText().toString());

                            list.get(holder.getAdapterPosition()).setEmail(emailEntry.getText().toString());
                            holder.email.setText(emailEntry.getText().toString());

                            list.get(holder.getAdapterPosition()).setPhoneNumber(phoneEntry.getText().toString());
                            holder.phone.setText(list.get(holder.getAdapterPosition()).getFormattedPhoneNumber());

                            list.get(holder.getAdapterPosition()).setType(spinnerContactType.getSelectedItem().toString());
                            holder.type.setText(spinnerContactType.getSelectedItem().toString());

                            updateContact(list.get(holder.getAdapterPosition()));

                            Toast.makeText(context.getApplicationContext(), "Contact Saved!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                    }
                });



                nameEntry = dialog.findViewById(R.id.EnterName);
                emailEntry = dialog.findViewById(R.id.EnterEmail);
                phoneEntry = dialog.findViewById(R.id.EnterPhone);
                spinnerContactType = dialog.findViewById(R.id.SelectContactType);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(v.getContext(), R.array.contact_types, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerContactType.setAdapter(adapter);

                nameEntry.setText(list.get(holder.getAdapterPosition()).getName());
                emailEntry.setText(list.get(holder.getAdapterPosition()).getEmail());
                phoneEntry.setText(list.get(holder.getAdapterPosition()).getPhoneNumber());
                spinnerContactType.setSelection(adapter.getPosition(list.get(holder.getAdapterPosition()).getType()));

            }
        });
    }

    @Override
    public int getItemCount() {
        if(!(list == null))
            return list.size();
        return 0;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }


    private void updateContact(Contact toUpdate)
    {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            CollectionReference contactsCollection = db.collection("users").document(uid).collection("contacts");

            Map<String, Object> data = new HashMap<>();
            data.put("name", toUpdate.getName());
            data.put("phone", toUpdate.getPhoneNumber());
            data.put("email", toUpdate.getEmail());
            data.put("type", toUpdate.getType());

            contactsCollection.document(toUpdate.getDatabaseID()).set(data);
        }
    }
    private void deleteContact(Contact toDelete)
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            CollectionReference contactsCollection = db.collection("users").document(uid).collection("contacts");


            contactsCollection.document(toDelete.getDatabaseID()).delete();
        }
    }


}
