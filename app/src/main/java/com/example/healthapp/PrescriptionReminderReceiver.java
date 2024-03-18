package com.example.healthapp;

import static android.app.PendingIntent.getActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;


public class PrescriptionReminderReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {

        String medication = intent.getStringExtra("medication");
        Log.d("debug", "alarm called for " + medication);

        Intent reminderIntent = new Intent(context.getApplicationContext(), ViewReminder.class);
        reminderIntent.putExtra("medication", medication);
        context.startActivity(reminderIntent);


    }
}
