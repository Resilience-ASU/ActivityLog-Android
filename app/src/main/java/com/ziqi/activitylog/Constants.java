package com.ziqi.activitylog;


import android.os.ParcelUuid;

import java.util.HashMap;

public class Constants {

    private static HashMap<String, String> attributes = new HashMap();
    public static String DROP_SERVICE = "12630000-cc25-497d-9854-9b6c02c77054";
    public static String DROP_TEMPERATURE = "12630001-cc25-497d-9854-9b6c02c77054";
    public static String DROP_HUMIDITY = "12630002-cc25-497d-9854-9b6c02c77054";
    public static String DROP_RELATIVE_TEMPERATURE = "12630003-cc25-497d-9854-9b6c02c77054";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("12630000-cc25-497d-9854-9b6c02c77054", "Temperature Service");
        attributes.put("12630001-cc25-497d-9854-9b6c02c77054", "Temperature");
        attributes.put("12630002-cc25-497d-9854-9b6c02c77054", "Humidity");
        attributes.put("12630003-cc25-497d-9854-9b6c02c77054", "Relative Temperature");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

}