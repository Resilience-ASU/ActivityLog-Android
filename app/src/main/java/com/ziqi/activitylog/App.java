package com.ziqi.activitylog;

import com.parse.Parse;
import com.parse.ParseUser;

import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Remove for production, use to verify FCM is working
        // Look for ParseFCM: FCM registration success messages in Logcat to confirm.
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);


        //Parse.enableLocalDatastore();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("ziqi.actlog")
                .server("https://heatmappers-act-log.herokuapp.com/parse/")
                .enableLocalDataStore()
                .build()
        );

        ParseUser.enableAutomaticUser();
        ParseUser.getCurrentUser().increment("RunCount");
        ParseUser.getCurrentUser().put("platform", "android");
        ParseUser.getCurrentUser().saveInBackground();
    }
}