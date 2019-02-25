package com.ziqi.activitylog;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.Calendar;

public class SplashActivity extends AppCompatActivity {

    private AlarmManager alarmMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();


        Calendar now = Calendar.getInstance();
        int nowHour = now.get(Calendar.HOUR_OF_DAY);

        for(int l=8; l<=20; l=l+2){
            if (nowHour < l) {
                NotificationHelper.scheduleRepeatingRTCNotification(this, l, false, l);
            }
            else{
                NotificationHelper.scheduleRepeatingRTCNotification(this,l,true,l);
            }
        }

    }

}