package com.example.healthapp;

import androidx.annotation.GravityInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.os.Handler;


public class Register extends AppCompatActivity
{

	TextInputEditText emailField, passwordField, confirmPasswordField;
	Button registerButton;

	ProgressBar progressBar;

	FirebaseAuth mAuth;

	TextView signInLink;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		mAuth = FirebaseAuth.getInstance();
		emailField = findViewById(R.id.email);
		passwordField = findViewById(R.id.password);
		confirmPasswordField = findViewById(R.id.confirmPassword);
		registerButton = findViewById(R.id.registerBtn);
		progressBar = findViewById(R.id.progressBar);
		signInLink = findViewById(R.id.signInInstead);

		signInLink.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent loginIntent = new Intent(getApplicationContext(), Login.class);
				startActivity(loginIntent);
				finish();
            }
		});

		registerButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				progressBar.setVisibility(View.VISIBLE);

				String email = String.valueOf(emailField.getText());
				String password = String.valueOf(passwordField.getText());
				String confirmPassword = String.valueOf(confirmPasswordField.getText());



				if(TextUtils.isEmpty(email))
				{
					Toast.makeText(Register.this, "Enter Email", Toast.LENGTH_SHORT).show();
				}

				else if(TextUtils.isEmpty(password))
				{
					Toast.makeText(Register.this, "Enter Password", Toast.LENGTH_SHORT).show();
				}

				else if(!TextUtils.equals(password, confirmPassword))
				{
					Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
				}

				else
				{
					mAuth.createUserWithEmailAndPassword(email, password)
							.addOnCompleteListener(new OnCompleteListener<AuthResult>()
							{
								@Override
								public void onComplete(@NonNull Task<AuthResult> task)
								{
									progressBar.setVisibility(View.GONE);
									if (task.isSuccessful())
				 					{
										Toast.makeText(Register.this, "Account created", Toast.LENGTH_SHORT).show();
										Handler handler = new Handler();
										handler.postDelayed(new Runnable()
										{
											@Override
											public void run()
											{
												Intent intent = new Intent(getApplicationContext(), Login.class);
												startActivity(intent);
												finish();
											}
										}, 500);
									}
									else
									{
										// If sign in fails, display a message to the user.

										Toast.makeText(Register.this, "Account creation failed.", Toast.LENGTH_SHORT).show();

									}
								}
							});
				}

            }
		});
	}
}