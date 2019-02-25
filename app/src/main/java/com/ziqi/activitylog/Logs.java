package com.ziqi.activitylog;


import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Logs")
public class Logs extends ParseObject{
    public Logs(){

    }

    public String getActivity(){
        return getString("activity");
    }
    public String getMyDate(){
        return getString("time") + " " + getString("date");
    }

}