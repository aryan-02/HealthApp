package com.example.healthapp;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity
{

	TextInputEditText emailField, passwordField;
	Button registerButton;
	ProgressBar progressBar;
	TextView registerLink;
	Button loginButton;
	FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mAuth = FirebaseAuth.getInstance();
		emailField = findViewById(R.id.email);
		passwordField = findViewById(R.id.password);
		loginButton = findViewById(R.id.loginBtn);
		progressBar = findViewById(R.id.progressBar);
		registerLink = findViewById(R.id.registerInstead);

		registerLink.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent registerIntent = new Intent(getApplicationContext(), Register.class);
				startActivity(registerIntent);
            }
		});

		loginButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				progressBar.setVisibility(View.VISIBLE);

				String email = String.valueOf(emailField.getText());
				String password = String.valueOf(passwordField.getText());

				String emailMatchRegex = "[a-zA-Z0-9._-]+@[a-z]+\\\\.+[a-z]+";

				if(TextUtils.isEmpty(email))
				{
					Toast.makeText(Login.this, "Cannot leave email blank.", Toast.LENGTH_SHORT).show();
				}
				else if(! (email.contains(".") && email.contains("@")))
				{
					Toast.makeText(Login.this, "Invalid email.", Toast.LENGTH_SHORT).show();
				}
				else if(TextUtils.isEmpty(password))
				{
					Toast.makeText(Login.this, "Cannot leave password blank.", Toast.LENGTH_SHORT).show();
				}
				else
				{
					mAuth.signInWithEmailAndPassword(email, password)
							.addOnCompleteListener(new OnCompleteListener<AuthResult>()
							{
								@Override
								public void onComplete(@NonNull Task<AuthResult> task)
								{
									if (task.isSuccessful())
									{
										Intent intent = new Intent(getApplicationContext(), MainActivity.class);
										startActivity(intent);
										finish();
									}
									else
									{
										Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
									}
								}
							});
				}

				progressBar.setVisibility(View.GONE);
            }
		});


	}

}