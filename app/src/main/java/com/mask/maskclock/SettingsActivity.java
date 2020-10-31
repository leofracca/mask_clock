package com.mask.maskclock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_ALARM_SWITCH = "switch_alarm";
    public static final String KEY_PREF_ALARM_TIME = "time_alarm";
    public static final String KEY_PREF_THEME_CHOICE = "theme_selection";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        // Back arrow
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // Handle notification switch changes
        if (key.equals(KEY_PREF_ALARM_SWITCH)) {
            SharedPreferences sharedPreferencesSwitch = getSharedPreferences("current switch state", MODE_PRIVATE);
            boolean maskOnOff = sharedPreferencesSwitch.getBoolean("switch state", false); // the switch in MainActivity
            boolean switchPref = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_ALARM_SWITCH, false); // the switch in SettingsActivity

            // Setting up or delete the alarm manager
            if (maskOnOff) {
                if (!switchPref) {
                    if (MainActivity.alarmManager != null)
                        MainActivity.alarmManager.cancel(MainActivity.pendingIntent);
                }
                else {
                    MainActivity.alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    MainActivity.alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + MainActivity.minutesPref * 1000, MainActivity.minutesPref * 1000, MainActivity.pendingIntent);
                }
            }
        }

        // Handle notification time changes
        if (key.equals(KEY_PREF_ALARM_TIME)) {
            SharedPreferences sharedPreferencesSwitch = getSharedPreferences("current switch state", MODE_PRIVATE);
            boolean maskOnOff = sharedPreferencesSwitch.getBoolean("switch state", false); // the switch in MainActivity

            MainActivity.minutesPrefAsString = sharedPreferences.getString(SettingsActivity.KEY_PREF_ALARM_TIME, "0");
            MainActivity.minutesPref = Integer.parseInt(MainActivity.minutesPrefAsString) * 60;

            if (maskOnOff) {
                if (MainActivity.alarmManager != null)
                    MainActivity.alarmManager.cancel(MainActivity.pendingIntent);

                MainActivity.alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + MainActivity.minutesPref * 1000, MainActivity.minutesPref * 1000, MainActivity.pendingIntent);
            }
        }

        // Handle theme changes
        if (key.equals(KEY_PREF_THEME_CHOICE)) {
            final String[] themeValues = getResources().getStringArray(R.array.themes_values);

            String selected = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_PREF_THEME_CHOICE, getString(R.string.default_theme_value));

            if (selected.equals(themeValues[0]))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            if (selected.equals(themeValues[1]))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            if (selected.equals(themeValues[2]))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

}