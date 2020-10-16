package com.mask.maskclock;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

public class AlertDetails extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Get the minutes
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final String minutesPrefAsString = sharedPref.getString(SettingsActivity.KEY_PREF_ALARM_TIME, "0");
        final int minutesPref = Integer.parseInt(minutesPrefAsString) * 60;

        // Set and send the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarmTime")
                .setSmallIcon(R.drawable.ic_baseline_masks_24)
                .setContentTitle(minutesPref < 3600 ? minutesPref/60 + " " + context.getString(R.string.title_minutes) : minutesPref/60/60 + " " + ((minutesPref == 3600) ? context.getString(R.string.title_one_hour) : context.getString(R.string.title_hours)))
                .setContentText(context.getString(R.string.text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}
