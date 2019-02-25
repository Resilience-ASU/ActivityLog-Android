package com.ziqi.activitylog;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseUser;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private final static int REQUEST_ENABLE_BT = 1;
    private String mdropID;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;
    private String mConnectionState;

    private HomeFragment mHomeFragment;
    private ListFragment mListFragment;
    private HeatFragment mHeatFragment;
    private WebFragment mWebFragment;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 15000;


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 2: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService(new Intent(this, MyService.class));
                } else {
                    //Permission denied
                }
                return;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHomeFragment = new HomeFragment();
        mListFragment = new ListFragment();
        mHeatFragment = new HeatFragment();
        mWebFragment = new WebFragment();


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, mHomeFragment);
        transaction.commit();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            int REQUEST_LOCATION = 2;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        else{
            startService(new Intent(this, MyService.class));
        }


        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        mdropID = preferences.getString("dropID", null);
        mDeviceName = preferences.getString("mDeviceName", null);
        mConnectionState = "Disconnected";

        Log.w("asd","asdsd" + mdropID + mDeviceName + mDeviceAddress);
        mHandler = new Handler();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        /*
        if (mdropID != null){
            scanLeDevice(true);
        }
        */



        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);

        disableShiftMode(bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;

                        switch (item.getItemId()) {
                            case R.id.navigation_home:
                                Log.d("myTag", "Home");
                                selectedFragment = mHomeFragment;
                                break;
                            case R.id.navigation_history:
                                Log.d("myTag", "List");
                                selectedFragment = mListFragment;
                                break;
                            case R.id.navigation_heat:
                                Log.d("myTag", "Heat");
                                selectedFragment = mHeatFragment;
                                Bundle bundle = new Bundle();
                                bundle.putString("connectionState", mConnectionState);
                                mHeatFragment.setArguments(bundle);
                                break;
                            case R.id.navigation_web:
                                Log.d("myTag", "Survey");
                                selectedFragment = mWebFragment;
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menubar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.chech_id:
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("My ID number is");
                if (ParseUser.getCurrentUser() != null) {
                    alertDialog.setMessage(ParseUser.getCurrentUser().getUsername().substring(0, 10));
                }
                else{
                    alertDialog.setMessage("Your ID will be shown here soon.");
                }
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface alertDialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();
                break;
            case R.id.input_drop_id:
                SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = preferences.edit();


                LayoutInflater li = LayoutInflater.from(this);
                View dialogView = li.inflate(R.layout.text_inpu_password, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Plase enter your Kestrel Drop ID");
                //final EditText input = new EditText(this);
                //input.setPadding(50,0,0,50);
                //input.offsetLeftAndRight(100);


                final EditText input = (EditText) dialogView.findViewById(R.id.input);

                input.setText(mdropID);
                input.setInputType(InputType.TYPE_CLASS_PHONE);

                builder.setView(dialogView);

                builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Log.w("asd","msdg" +  mConnected +mBluetoothLeService);

                        scanLeDevice(false);
                        mdropID = input.getText().toString();
                        editor.putString("dropID", mdropID);
                        editor.apply();
                        if (mBluetoothLeService!=null && mConnected) {
                            Log.i("asd","closing");
                            mBluetoothLeService.close();
                            mDeviceName = null;
                            mDeviceAddress = null;
                            editor.putString("mDeviceName", null);
                            editor.apply();

                        }

                        scanLeDevice(true);


                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            default:
                break;
        }

        return true;
    }

    private void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }





    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("asd", "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    if (mDeviceName == null){
                        if (mHeatFragment.isVisible()) {
                            mHeatFragment.updateTemp("Temperature\n\n--");
                            mHeatFragment.updateHumid("Relative Humidity\n\n--\n");
                            mHeatFragment.updateHSI("Heat Stress Index\n\n--\n");
                            Log.w("asd","m" + mConnected);
                            mConnectionState = "Not Found";
                            mHeatFragment.updateStatus(mConnectionState);
                        }

                    }

                }
            }, SCAN_PERIOD);

            mConnectionState = "Scanning";
            if (mHeatFragment.isVisible()) {
                mHeatFragment.updateTemp("Temperature\n\n--");
                mHeatFragment.updateHumid("Relative Humidity\n\n--\n");
                mHeatFragment.updateHSI("Heat Stress Index\n\n--\n");
                mHeatFragment.updateStatus(mConnectionState);
            }

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (device != null && device.getName()!=null && device.getName().contains(mdropID)) {
                                Log.w("asd","asdasd"+mdropID);

                                Log.e("asd", "Device" + device.getName());
                                mDeviceName = device.getName();
                                mDeviceAddress = device.getAddress();

                                SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                                final SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("mDeviceName", mDeviceName);
                                editor.apply();


                                mConnectionState = "Connecting to " + mDeviceName;
                                if (mHeatFragment.isVisible()) {
                                    mHeatFragment.updateStatus(mConnectionState);
                                }
                                Log.e("asd", "Device" + mDeviceAddress);
                                mBluetoothLeService.connect(mDeviceAddress);
                                scanLeDevice(false);
                            }
                        }
                    });
                }
            };


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mConnectionState = "Disconnected";
                if (mHeatFragment.isVisible()) {
                    mHeatFragment.updateTemp("Temperature\n\n--");
                    mHeatFragment.updateHumid("Relative Humidity\n\n--\n");
                    mHeatFragment.updateHSI("Heat Stress Index\n\n--\n");
                    mHeatFragment.updateStatus(mConnectionState);
                }

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                final List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();
                for (BluetoothGattService gattService : gattServices) {
                    if (gattService.getUuid().toString().equals(Constants.DROP_SERVICE)){
                        List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                            if (Constants.DROP_TEMPERATURE.equals(gattCharacteristic.getUuid().toString())) {
                                mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                            }
                            if (Constants.DROP_HUMIDITY.equals(gattCharacteristic.getUuid().toString())) {
                                mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                            }
                            if (Constants.DROP_RELATIVE_TEMPERATURE.equals(gattCharacteristic.getUuid().toString())) {
                                mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                            }

                        }
                    }
                }

            }

            else if (BluetoothLeService.ACTION_TEMP_DATA_AVAILABLE.equals(action)) {
                if (mHeatFragment.isVisible()) {
                    mHeatFragment.updateTemp(intent.getStringExtra(BluetoothLeService.TEMP_DATA));
                    mConnectionState = "Connected to " + mDeviceName;
                    mHeatFragment.updateStatus(mConnectionState);
                }
            }
            else if (BluetoothLeService.ACTION_HUMID_DATA_AVAILABLE.equals(action)){
                if (mHeatFragment.isVisible()) {
                    mHeatFragment.updateHumid(intent.getStringExtra(BluetoothLeService.HUMID_DATA));
                    mConnectionState = "Connected to " + mDeviceName;
                    mHeatFragment.updateStatus(mConnectionState);
                }
            }
            else if (BluetoothLeService.ACTION_RELATEMP_DATA_AVAILABLE.equals(action)){
                if (mHeatFragment.isVisible()) {
                    mHeatFragment.updateHSI(intent.getStringExtra(BluetoothLeService.RELATEMP_DATA));
                    mConnectionState = "Connected to " + mDeviceName;
                    mHeatFragment.updateStatus(mConnectionState);
                }
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_TEMP_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_HUMID_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_RELATEMP_DATA_AVAILABLE);
        return intentFilter;
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null && mDeviceAddress!=null) {
            //final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d("asd", "Connect request result=" + "ha");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


}
