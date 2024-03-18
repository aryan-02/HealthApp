package com.example.healthapp;

import static android.text.TextUtils.isEmpty;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CompleteAccountInfo extends AppCompatActivity
{
	String name;
	String phoneNumber;
	String dateOfBirth;

	TextInputEditText nameBox;
	TextInputEditText phoneNumberBox;
	TextInputEditText dateBox;
	Button submitButton;
	DatePickerDialog datePicker;

	FirebaseAuth mAuth;
	FirebaseUser user;
	FirebaseFirestore db;

	String uid;

	void toast(String message)
	{
		Toast.makeText(CompleteAccountInfo.this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_complete_account_info);
		mAuth = FirebaseAuth.getInstance();
		db = FirebaseFirestore.getInstance();
		nameBox = findViewById(R.id.name);
		phoneNumberBox = findViewById(R.id.phone);
		dateBox = findViewById(R.id.dateOfBirth);
		submitButton = findViewById(R.id.submitBtn);

		user = mAuth.getCurrentUser();

		if(user == null)
		{
			Intent intent = new Intent(getApplicationContext(), Login.class);
			startActivity(intent);
			finish();
		}

		uid = user.getUid();


		dateBox.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View view)
			{
				final Calendar calendar = Calendar.getInstance();
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				int month = calendar.get(Calendar.MONTH);
				int year = calendar.get(Calendar.YEAR);

				datePicker = new DatePickerDialog(CompleteAccountInfo.this, new DatePickerDialog.OnDateSetListener()
				{
					@Override
					public void onDateSet(DatePicker datePicker, int yearSet, int monthSet, int daySet)
					{
						dateBox.setText(getString(R.string.dateFormatString, daySet, monthSet + 1, yearSet));
					}
				}, year, month, day);
				datePicker.show();
			}
		});

		submitButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				name = String.valueOf(nameBox.getText());
				phoneNumber = String.valueOf(phoneNumberBox.getText());
				dateOfBirth = String.valueOf(dateBox.getText());

				if(name.isEmpty())
				{
					toast("Name cannot be empty.");
				}
				else if(phoneNumber.isEmpty())
				{
					toast("Phone number cannot be empty.");
				}
				else if(dateOfBirth.isEmpty())
				{
					toast("Date of birth cannot be empty.");
				}
				else
				{
					// Input is valid
					CollectionReference users = db.collection("users");
					Map<String, Object> data = new HashMap<>();
					data.put("name", name);
					data.put("phoneNumber", phoneNumber);
					data.put("dateOfBirth", dateOfBirth);
					users.document(uid).set(data).addOnSuccessListener(new OnSuccessListener<Void>()
					{
						@Override
						public void onSuccess(Void literallyNothing)
						{
							toast("Successful!");
							Handler handler = new Handler();
							handler.postDelayed(new Runnable()
							{
								@Override
								public void run()
								{
									Intent intent = new Intent(getApplicationContext(), MainActivity.class);
									startActivity(intent);
									finish();
								}
							}, 500);
						}
					});
				}
			}
		});


	}
}