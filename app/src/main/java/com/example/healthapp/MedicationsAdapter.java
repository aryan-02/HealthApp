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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicationsAdapter extends RecyclerView.Adapter<MedicationsViewHolder>{

    List<Medication> list;
    EditText nameEntry;
    EditText conflictsEntry;
    Context context;

    List<Medication> otherMedicationData;
    List<Medication> selectedConflictsList;
    boolean [] selectedConflicts;
    String [] medicationNames;

    public MedicationsAdapter(List<Medication> list, Context context)
    {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MedicationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View photoView = inflater.inflate(R.layout.list_medication,parent, false);
        MedicationsViewHolder viewHolder = new MedicationsViewHolder(photoView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationsViewHolder holder, int position) {
        holder.name.setText(list.get(holder.getAdapterPosition()).getName());
        holder.conflicts.setText(list.get(holder.getAdapterPosition()).printConflicts());


        holder.view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                builder.setTitle("Manage Medication");

                builder.setView(R.layout.view_create_medication);


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
                        builder2.setTitle("Delete Medication");
                        builder2.setMessage("Are you sure?");
                        builder2.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                deleteMedication(list.get(holder.getAdapterPosition())); //Delete from DB
                                list.remove(holder.getAdapterPosition()); //Delete from in-memory list

                                notifyItemRemoved(holder.getAdapterPosition());

                                Toast.makeText(context.getApplicationContext(), "Medication Deleted", Toast.LENGTH_SHORT).show();
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
                        else
                        {
                            list.get(holder.getAdapterPosition()).setName(nameEntry.getText().toString());
                            holder.name.setText(nameEntry.getText().toString());

                            list.get(holder.getAdapterPosition()).getConflicts().clear();
                            list.get(holder.getAdapterPosition()).getConflicts().addAll(selectedConflictsList);
                            holder.conflicts.setText(list.get(holder.getAdapterPosition()).printConflicts());

                            updateMedication(list.get(holder.getAdapterPosition()));

                            Toast.makeText(context.getApplicationContext(), "Medication Saved!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                    }
                });



                nameEntry = dialog.findViewById(R.id.EnterName);
                conflictsEntry = dialog.findViewById(R.id.EnterConflicts);

                nameEntry.setText(list.get(holder.getAdapterPosition()).getName());
                conflictsEntry.setText(list.get(holder.getAdapterPosition()).printConflicts());


                otherMedicationData = new ArrayList<>(list);
                otherMedicationData.remove(list.get(holder.getAdapterPosition()));

                medicationNames = new String[otherMedicationData.size()];
                selectedConflicts = new boolean[otherMedicationData.size()];
                selectedConflictsList = new ArrayList<>();

                for (int i = 0; i < otherMedicationData.size(); i++)
                {
                    medicationNames[i] = otherMedicationData.get(i).getName();
                    if(list.get(holder.getAdapterPosition()).getConflicts().contains(otherMedicationData.get(i)))
                    {
                        selectedConflicts[i] = true;
                        selectedConflictsList.add(otherMedicationData.get(i));
                    }
                    else
                        selectedConflicts[i] = false;

                }

                conflictsEntry.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        AlertDialog.Builder conflictsBuilder = new AlertDialog.Builder(v.getContext());
                        conflictsBuilder.setTitle("Setup Medication Conflicts");

                        conflictsBuilder.setMultiChoiceItems(medicationNames, selectedConflicts, new DialogInterface.OnMultiChoiceClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked)
                            {
                                if(isChecked)
                                {
                                    selectedConflictsList.add(otherMedicationData.get(which));
                                }
                                else
                                {
                                    selectedConflictsList.remove(otherMedicationData.get(which));
                                }
                            }
                        });
                        conflictsBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                            }
                        });

                        AlertDialog dialogConflicts = conflictsBuilder.show();

                        dialogConflicts.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                StringBuilder conflictsStr = new StringBuilder("No Conflicts");

                                if(selectedConflictsList.size() >= 1)
                                    conflictsStr = new StringBuilder("Conflicts: " + selectedConflictsList.get(0).getName());

                                for(int i = 1; i < selectedConflictsList.size(); i++)
                                    conflictsStr.append(", ").append(selectedConflictsList.get(i).getName());

                                conflictsEntry.setText(conflictsStr);

                                dialogConflicts.dismiss();
                            }
                        });
                    }
                });
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


    private void updateMedication(Medication toUpdate)
    {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            CollectionReference collection = db.collection("medications");

            Map<String, Object> data = new HashMap<>();
            data.put("name", toUpdate.getName());

            StringBuilder conflicts = new StringBuilder();
            if(toUpdate.getConflicts().size() >= 1)
                conflicts.append(toUpdate.getConflicts().get(0).getDatabaseID());
            for(int i = 0; i < toUpdate.getConflicts().size(); i++)
                conflicts.append(",").append(toUpdate.getConflicts().get(i).getDatabaseID());

            data.put("conflicts", conflicts.toString());

            collection.document(toUpdate.getDatabaseID()).set(data);
        }
    }
    private void deleteMedication(Medication toDelete)
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            CollectionReference collection = db.collection("medications");


            collection.document(toDelete.getDatabaseID()).delete();
        }
    }


}
