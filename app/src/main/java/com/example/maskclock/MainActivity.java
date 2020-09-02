package com.example.maskclock;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayAdapter arrayAdapter;
    private ArrayList<String> items;
    private ArrayList<Integer> minutesForItems;
    private int totalMinutes;

    // You need to change the refill of the mask after 150-200 hours of use
    // Here this time is indicated in minutes
    private final int TIME_THRESHOLD = 150 * 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadData();
        Toast.makeText(getApplicationContext(), "Total minutes:" + totalMinutes, Toast.LENGTH_LONG).show();

        displayWarningTime();


        final TimePickerDialog timePickerDialog;
        FloatingActionButton btnAdd = (FloatingActionButton) findViewById(R.id.addTimes);

        // Set the ListView
        final ListView saved = (ListView) findViewById(R.id.times);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        saved.setAdapter(arrayAdapter);


        // Add items
        timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                items.add(hourOfDay + " hours and " + minute + " minutes");
                minutesForItems.add((hourOfDay * 60) + minute);

                totalMinutes += (hourOfDay * 60) + minute;
                arrayAdapter.notifyDataSetChanged();

                saveData();

                displayWarningTime();
            }
        }, 0, 0, true);


        // Show the time picker
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                timePickerDialog.show();
            }
        });


        // Remove items
        saved.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

                alertDialog.setTitle("Delete?");
                alertDialog.setMessage("Are you sure you want to delete this time?");

                final int positionToRemove = position;
                alertDialog.setNegativeButton("Cancel", null);
                alertDialog.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        items.remove(positionToRemove);
                        arrayAdapter.notifyDataSetChanged();

                        // remove the time of the selected item from the total times and then delete it from the list
                        totalMinutes -= minutesForItems.get(positionToRemove);
                        minutesForItems.remove(positionToRemove);

                        saveData();
                    }
                });

                alertDialog.show();

                return true;
            }
        });




        //Toast.makeText(getApplicationContext(), "You wear your mask for " + hour + " hours and " + minute + " minutes", Toast.LENGTH_LONG).show();
    }

    // Display an alert that tells you to change the refill
    // It also allows you to delete all the times --> You changed your refill
    // Or continue --> You didn't change your refill
    private void displayWarningTime() {
        if(totalMinutes >= TIME_THRESHOLD) {
            AlertDialog.Builder needToChangeRefillWarning = new AlertDialog.Builder(MainActivity.this);

            needToChangeRefillWarning.setTitle("Time Expired!");
            needToChangeRefillWarning.setMessage(totalMinutes + " minutes elapsed! You should change the refill of your mask!\nDo you want to delete all the times?");

            needToChangeRefillWarning.setNegativeButton("Cancel", null);
            needToChangeRefillWarning.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    items.clear();
                    minutesForItems.clear();
                    arrayAdapter.notifyDataSetChanged();

                    totalMinutes = 0;

                    saveData();
                }
            });

            needToChangeRefillWarning.show();
        }
    }

    // Save the informations (list and total time)
    private void saveData() {
        // Save the lists
        SharedPreferences sharedPreferencesList = getSharedPreferences("items", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesList.edit();
        Gson gson = new Gson();
        String json = gson.toJson(items);
        editor.putString("times", json);
        editor.apply();

        sharedPreferencesList = getSharedPreferences("minutes for items", MODE_PRIVATE);
        editor = sharedPreferencesList.edit();
        json = gson.toJson(minutesForItems);
        editor.putString("minutes", json);
        editor.apply();

        // Save the time
        SharedPreferences sharedPreferencesTime = getSharedPreferences("total time", MODE_PRIVATE);
        editor = sharedPreferencesTime.edit();
        //json = gson.toJson(totalMinutes);
        editor.putInt("total minutes", totalMinutes);
        editor.apply();
    }

    // Load the informations (list and total time)
    private void loadData() {
        // Load the lists
        SharedPreferences sharedPreferencesList = getSharedPreferences("items", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferencesList.getString("times", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        items = gson.fromJson(json, type);

        sharedPreferencesList = getSharedPreferences("minutes for items", MODE_PRIVATE);
        json = sharedPreferencesList.getString("minutes", null);
        type = new TypeToken<ArrayList<Integer>>() {}.getType();
        minutesForItems = gson.fromJson(json, type);

        if(items == null) {
            items = new ArrayList<>();
            minutesForItems = new ArrayList<>();
        }

        // Load the time
        SharedPreferences sharedPreferencesTime = getSharedPreferences("total time", MODE_PRIVATE);
        totalMinutes = sharedPreferencesTime.getInt("total minutes", 0);
    }

}