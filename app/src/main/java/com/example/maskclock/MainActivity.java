package com.example.maskclock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private ArrayAdapter arrayAdapter;
    private ArrayList<String> items;
    private ArrayList<Integer> minutesForItems;
    private int totalMinutes;

    private int hourOn, hourOff;
    private int minuteOn, minuteOff;
    private int dayOn, dayOff;
    private Switch maskOnOff;

    // You need to change the refill of the mask after 150-200 hours of use
    // Here this time is indicated in minutes
    private final int TIME_THRESHOLD = 150 * 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Switch
        maskOnOff = findViewById(R.id.switch2);

        loadData();
        loadTimeOn();

        Toast.makeText(getApplicationContext(), getString(R.string.total_minutes) + " " + totalMinutes, Toast.LENGTH_LONG).show();

        displayWarningTime();

        // Set the ListView
        final ListView saved = findViewById(R.id.times);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        saved.setAdapter(arrayAdapter);


        final TimePickerDialog timePickerDialog;
        FloatingActionButton btnAdd = findViewById(R.id.addTimes);

        // Switch
        // Change the background
        if(maskOnOff.isChecked())
            maskOnOff.setBackgroundColor(getResources().getColor(R.color.colorSwitchOnBackground));
        else
            maskOnOff.setBackgroundColor(getResources().getColor(R.color.colorSwitchOffBackground));

        // Calculate the time on switch state change
        maskOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int elapsedMinutes;

                // If it is checked, save current day, hours and minutes
                if(maskOnOff.isChecked()) {
                    maskOnOff.setBackgroundColor(getResources().getColor(R.color.colorSwitchOnBackground));

                    hourOn = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    minuteOn = Calendar.getInstance().get(Calendar.MINUTE);
                    dayOn = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

                    saveTimeOn();
                }
                // When it is unchecked calculate the difference between the current time and the previous
                else {
                    maskOnOff.setBackgroundColor(getResources().getColor(R.color.colorSwitchOffBackground));

                    hourOff = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    minuteOff = Calendar.getInstance().get(Calendar.MINUTE);
                    dayOff = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

                    // If dayOn is the last day of the week, and dayOff is the first day of the week
                    if(dayOff != dayOn)
                        dayOff = dayOn + 1;


                    elapsedMinutes = ((hourOff * 60) + (dayOff * 24 * 60) + minuteOff) - ((hourOn * 60) + (dayOn * 24 * 60) + minuteOn);

                    int hour = elapsedMinutes / 60;
                    int minute = elapsedMinutes % 60;

                    items.add(hour + " " + ((hour == 1) ? getString(R.string.hour) : getString(R.string.hours)) + " " + getString(R.string.and) + " " + minute + " " + ((minute == 1) ? getString(R.string.minute) : getString(R.string.minutes)));
                    minutesForItems.add((hour * 60) + minute);

                    totalMinutes += (hour * 60) + minute;
                    arrayAdapter.notifyDataSetChanged();

                    saveData();

                    displayWarningTime();
                }
            }
        });


        // Add items
        timePickerDialog = new TimePickerDialog(MainActivity.this, R.style.TimePickerDialog, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                items.add(hourOfDay + " " + ((hourOfDay == 1) ? getString(R.string.hour) : getString(R.string.hours)) + " " + getString(R.string.and) + " " + minute + " " + ((minute == 1) ? getString(R.string.minute) : getString(R.string.minutes)));
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
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);

                alertDialog.setTitle(getString(R.string.delete));
                alertDialog.setMessage(getString(R.string.text_delete));

                final int positionToRemove = position;
                alertDialog.setNegativeButton(getString(R.string.cancel_button), null);
                alertDialog.setPositiveButton(getString(R.string.ok_button), new AlertDialog.OnClickListener() {
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


    // Create the theme selector button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.light_dark_theme_button, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Handle the theme selector button activities
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.theme_selector_button) {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    // Display an alert that tells you to change the refill
    // It also allows you to delete all the times --> You changed your refill
    // Or continue --> You didn't change your refill
    private void displayWarningTime() {
        if(totalMinutes >= TIME_THRESHOLD) {
            AlertDialog.Builder needToChangeRefillWarning = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);

            needToChangeRefillWarning.setTitle(getString(R.string.time_expired));
            needToChangeRefillWarning.setMessage((totalMinutes / 60) + " " + getString(R.string.hours) + " " + getString(R.string.and) + " " + (totalMinutes % 60) + " " + ((totalMinutes % 60 == 1) ? getString(R.string.minute) : getString(R.string.minutes)) + " " + getString(R.string.delete_all_times));

            needToChangeRefillWarning.setNegativeButton(getString(R.string.cancel_button), null);
            needToChangeRefillWarning.setPositiveButton(getString(R.string.ok_button), new AlertDialog.OnClickListener() {
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

        // Save the total time
        SharedPreferences sharedPreferencesTime = getSharedPreferences("total time", MODE_PRIVATE);
        editor = sharedPreferencesTime.edit();
        editor.putInt("total minutes", totalMinutes);
        editor.apply();

        // Save the switch state
        editor = getSharedPreferences("current switch state", MODE_PRIVATE).edit();
        editor.putBoolean("switch state", maskOnOff.isChecked());
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

        // Load the switch state
        SharedPreferences sharedPreferencesSwitch = getSharedPreferences("current switch state", MODE_PRIVATE);
        maskOnOff.setChecked(sharedPreferencesSwitch.getBoolean("switch state", false));
    }


    // Save informations about when the switch is turned to ON
    private void saveTimeOn() {
        // Save the hour
        SharedPreferences sharedPreferences = getSharedPreferences("current hour", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("hour", hourOn);
        editor.apply();

        // Save the minute
        sharedPreferences = getSharedPreferences("current minute", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt("minute", minuteOn);
        editor.apply();

        // Save the day
        sharedPreferences = getSharedPreferences("current day", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt("day", dayOn);
        editor.apply();

        // Save the state of the switch
        sharedPreferences = getSharedPreferences("current switch state", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean("switch state", maskOnOff.isChecked());
        editor.apply();
    }


    // Load informations about when the switch is turned to ON
    private void loadTimeOn() {
        // Load the hour
        SharedPreferences sharedPreferencesHour = getSharedPreferences("current hour", MODE_PRIVATE);
        hourOn = sharedPreferencesHour.getInt("hour", 0);

        // Load the minute
        SharedPreferences sharedPreferencesMinute = getSharedPreferences("current minute", MODE_PRIVATE);
        minuteOn = sharedPreferencesMinute.getInt("minute", 0);

        // Load the day
        SharedPreferences sharedPreferencesDay = getSharedPreferences("current day", MODE_PRIVATE);
        dayOn = sharedPreferencesDay.getInt("day", 0);
    }

}