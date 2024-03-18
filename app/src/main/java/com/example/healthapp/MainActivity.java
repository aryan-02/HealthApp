package com.example.healthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity
{

	FirebaseAuth mAuth;
	Button signOutButton, prescriptionsButton, dietButton, vitalSignsButton, notesButton, contactsButton, medicationsButton;
	TextView emailView;
	FirebaseUser user;
	FirebaseFirestore db;
	String uid;

	FloatingActionButton addNoteButton;

	@SuppressLint("MissingInflatedId")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		signOutButton = findViewById(R.id.signOutBtn);
		prescriptionsButton = findViewById(R.id.prescriptionBtn);
		dietButton = findViewById(R.id.dietBtn);
		vitalSignsButton = findViewById(R.id.vitalSignsBtn);
		notesButton = findViewById(R.id.notesBtn);
		contactsButton = findViewById(R.id.contactsBtn);
		medicationsButton = findViewById(R.id.medicationsBtn);
		emailView = findViewById(R.id.emailDisplay);

		//addNoteButton = findViewById(R.id.add_note_button);


		mAuth = FirebaseAuth.getInstance();
		user = mAuth.getCurrentUser();

		if(user == null)
		{
			Intent intent = new Intent(getApplicationContext(), Login.class);
			startActivity(intent);
			finish();
		}
		else
		{
			db = FirebaseFirestore.getInstance();
			uid = user.getUid();
			DocumentReference docRef = db.collection("users").document(uid);
			docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
				@Override
				public void onComplete(@NonNull Task<DocumentSnapshot> task)
				{
					if (task.isSuccessful())
					{
						DocumentSnapshot document = task.getResult();
						if (document.exists())
						{
							emailView.setText(getString(R.string.UserGreeting, document.get("name").toString().split(" ")[0]));
						}
						else
						{
							startActivity(new Intent(getApplicationContext(), CompleteAccountInfo.class));
							finish();
						}
					}
					else
					{
						Toast.makeText(MainActivity.this, "Check internet connection.", Toast.LENGTH_SHORT).show();
					}
				}
			});


		}

		signOutButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				mAuth.signOut();
				Intent intent = new Intent(getApplicationContext(), Login.class);
				startActivity(intent);
				finish();
			}
		});
		prescriptionsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(getApplicationContext(), ViewPrescriptions.class));
			}
		});
		dietButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(getApplicationContext(), ViewDiet.class));
			}
		});
		vitalSignsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(getApplicationContext(), ViewVitalSigns.class));
			}
		});
		notesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(getApplicationContext(), ViewNotes.class));
			}
		});
		contactsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(getApplicationContext(), ViewContacts.class));
			}
		});
		medicationsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(getApplicationContext(), ViewMedications.class));
			}
		});
	}
}