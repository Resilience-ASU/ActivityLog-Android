package com.ziqi.activitylog;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification_id";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {


        Intent intentToRepeat = new Intent(context, MainActivity.class);
        //set flag to restart/relaunch the app
        intentToRepeat.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Pending intent to handle launch of Activity in intent above
        PendingIntent pendingIntent =
                 PendingIntent.getActivity(context, NotificationHelper.ALARM_TYPE_RTC, intentToRepeat, PendingIntent.FLAG_UPDATE_CURRENT);

        //Build notification
        NotificationChannel mChannel = new NotificationChannel("my_channel_01", "logup", NotificationManager.IMPORTANCE_HIGH);
        mChannel.setDescription("No description");
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        NotificationHelper.getNotificationManager(context).createNotificationChannel(mChannel);

        //Build notification
        Notification repeatedNotification = new Notification.Builder(context)
                .setContentTitle("Log Up")
                .setContentText("Time to log your activity!")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setChannelId("my_channel_01")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        //Send local notification
        NotificationHelper.getNotificationManager(context).notify(NotificationHelper.ALARM_TYPE_RTC, repeatedNotification);


    }

}