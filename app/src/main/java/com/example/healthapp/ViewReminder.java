package com.example.healthapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class ViewReminder extends AppCompatActivity
{

    TextView reminderMessage, countdownText;
    int seconds, minutes;
    Button doneButton;

    private static final String formatTimer = "%02d:%02d:%02d";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reminder);

        reminderMessage = findViewById(R.id.ReminderText);
        countdownText = findViewById(R.id.ReminderCountdown);
        doneButton = findViewById(R.id.BackButton);

        String medication = getIntent().getStringExtra("medication");
        String reminderText = "It is time to take your prescription for " + medication;
        reminderMessage.setText(reminderText);

        long timerLength =  60 * 60 * 1000;

        CountDownTimer timer = new CountDownTimer(timerLength, 1000)
        {

            @Override
            public void onTick(long millisUntilFinished)
            {
                @SuppressLint("DefaultLocale")
                String timerStr = String.format(formatTimer,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

                countdownText.setText(timerStr);
            }

            @Override
            public void onFinish()
            {
                //TODO: Add doctor contacting functionality
                Intent reminderIntent = new Intent(ViewReminder.this, MainActivity.class);
                startActivity(reminderIntent);
            }
        }.start();

        doneButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                timer.cancel();

                Intent reminderIntent = new Intent(ViewReminder.this, MainActivity.class);
                startActivity(reminderIntent);
            }
        });
    }
}