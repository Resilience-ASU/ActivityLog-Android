package com.ziqi.activitylog;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;

public class HomeFragment extends Fragment {
    private Button submitBTN;
    private RadioGroup radioGroup1;
    private RadioGroup radioGroup2;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private Integer checked = 0;
    private String notes = " ";

    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        final EditText editText = (EditText) rootView.findViewById(R.id.notes);
        //editText.setCursorVisible(false);
        editText.clearFocus();
        radioGroup1 = (RadioGroup) rootView.findViewById(R.id.radioGroup1);
        radioGroup2 = (RadioGroup) rootView.findViewById(R.id.radioGroup2);
        submitBTN = (Button) rootView.findViewById(R.id.button);
        submitBTN.setBackgroundColor(Color.LTGRAY);
        submitBTN.setTextColor(Color.WHITE);
        submitBTN.setEnabled(false);


        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checked = checked + 1;
                if (checked == 2) {
                    submitBTN.setBackgroundColor(Color.RED);
                    submitBTN.setEnabled(true);
                }
            };

        });
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checked = checked + 1;
                if (checked == 2) {
                    submitBTN.setBackgroundColor(Color.RED);
                    submitBTN.setEnabled(true);
                }
            };

        });






        final ParseObject log = new ParseObject("Logs");
        log.put("lat", 0);
        log.put("lon", 0);
        Log.d("Custom HomeFrag","I know it reaches here for sure");
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("Custom HomeFrag","But does it reach here?");
                        if (location != null) {
                            Log.d("Custom HomeFrag","Or here?");
                            log.put("lat", location.getLatitude());
                            log.put("lon", location.getLongitude());
                        }

                    }
                });
        /*
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Activity Logged");
        alertDialog.setMessage("Please come back later.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        */

        submitBTN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // get selected radio button from radioGroup
                int selectedId1 = radioGroup1.getCheckedRadioButtonId();
                int selectedId2 = radioGroup2.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                radioButton1 = (RadioButton) rootView.findViewById(selectedId1);
                radioButton2 = (RadioButton) rootView.findViewById(selectedId2);


                String activity = radioButton1.getText().toString() + " - " + radioButton2.getText().toString();
                Log.d("rg1",activity);

                Date currentTime = Calendar.getInstance().getTime();
                String time = android.text.format.DateFormat.format("HH:mm", currentTime).toString();
                String date = android.text.format.DateFormat.format("MM/dd", currentTime).toString();


                log.put("activity", activity);
                if (ParseUser.getCurrentUser() != null) {
                    log.put("user", ParseUser.getCurrentUser().getUsername().substring(0, 10));
                }

                if (editText.getText().toString() != null) {
                    log.put("notes", editText.getText().toString());
                }
                log.put("time", time);
                log.put("date", date);
                //log.put("timeLong",currentTime);



                log.saveEventually();
                log.pinInBackground();
                //alertDialog.show();
                Toast mytoast = Toast.makeText(getActivity(), "Activity Logged", Toast.LENGTH_LONG);
                mytoast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);  // for center horizontal
                //mytoast.setGravity(Gravity.CENTER_VERTICAL);       // for center vertical
                //mytoast.setGravity(Gravity.TOP);

                mytoast.show();
                radioGroup1.clearCheck();
                radioGroup2.clearCheck();
                editText.setText("");
                checked = 0;
                submitBTN.setEnabled(false);
                submitBTN.setBackgroundColor(Color.LTGRAY);
            }

        });


        return rootView;

    }



}
