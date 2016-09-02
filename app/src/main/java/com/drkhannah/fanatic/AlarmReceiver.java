package com.drkhannah.fanatic;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

/**
 * Created by dhannah on 9/2/16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final int ALARM_NOTIFICATION_ID = 1;


    @Override
    public void onReceive(Context context, Intent intent) {
        //build notification
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.placeholder)
                .setContentTitle("Alarm Notification")
                .setContentText("Alarm Triggered");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, EventListActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(EventListActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // ARTIST_NOTIFICATION_ID allows you to update the notification later on.
        mNotificationManager.notify(ALARM_NOTIFICATION_ID, notificationBuilder.build());
    }
}
