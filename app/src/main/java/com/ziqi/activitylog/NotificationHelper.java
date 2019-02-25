package com.ziqi.activitylog;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.widget.Toast;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by ptyagi on 4/17/17.
 */

public class NotificationHelper {
    public static int ALARM_TYPE_RTC = 100;
    private static AlarmManager alarmManagerRTC;
    private static PendingIntent alarmIntentRTC;

    public static int ALARM_TYPE_ELAPSED = 101;
    private static AlarmManager alarmManagerElapsed;
    private static PendingIntent alarmIntentElapsed;

    /**
     * This is the real time /wall clock time
     * @param context
     */
    public static void scheduleRepeatingRTCNotification(Context context, int hour, boolean nextDay, int id) {
        //get calendar instance to be able to select what time notification should be scheduled
        Calendar calendar_next = Calendar.getInstance();
        calendar_next.setTimeInMillis(System.currentTimeMillis());
        calendar_next.set(Calendar.HOUR_OF_DAY, hour);
        calendar_next.set(Calendar.MINUTE, 0);

        //Setting intent to class where Alarm broadcast message will be handled
        Intent intent = new Intent(context, AlarmReceiver.class);
        //Setting alarm pending intent
        alarmIntentRTC = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //getting instance of AlarmManager service
        alarmManagerRTC = (AlarmManager)context.getSystemService(ALARM_SERVICE);

        //Setting alarm to wake up device every day for clock time.
        //AlarmManager.RTC_WAKEUP is responsible to wake up device for sure, which may not be good practice all the time.
        // Use this when you know what you're doing.
        //Use RTC when you don't need to wake up device, but want to deliver the notification whenever device is woke-up
        //We'll be using RTC.WAKEUP for demo purpose only
        if (nextDay) {
            alarmManagerRTC.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    calendar_next.getTimeInMillis() + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, alarmIntentRTC);
        }
        else {
            alarmManagerRTC.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    calendar_next.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntentRTC);
        }
    }


    public static void cancelAlarmRTC() {
        if (alarmManagerRTC!= null) {
            alarmManagerRTC.cancel(alarmIntentRTC);
        }
    }


    public static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

}