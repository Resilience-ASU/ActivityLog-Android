package com.ziqi.activitylog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.location.Location;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.ParseObject;

/**
 * Scans for Bluetooth Low Energy Advertisements matching a filter and displays them to the user.
 */
public class HeatFragment extends Fragment {
    private Button BTStatusBTN;
    private TextView tempTextView;
    private TextView humidityTextView;
    private TextView reTempTextView;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private String mdropID;
    private Handler mHandler;
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        //Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        //getActivity().bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        mdropID = preferences.getString("dropID", null);
        mDeviceAddress = preferences.getString("deviceAddress", null);
        mDeviceName = preferences.getString("deviceName", null);
        Log.w("asd","asdsd" + mdropID + mDeviceAddress);
        mHandler = new Handler();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "not supported", Toast.LENGTH_SHORT).show();
        }

        Intent gattServiceIntent = new Intent(getActivity(), BluetoothLeService.class);
        getActivity().bindService(gattServiceIntent, mServiceConnection, getActivity().BIND_AUTO_CREATE);
        if (mdropID != null){
            scanLeDevice(true);
        }
        */

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.fragment_heat, container, false);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        tempTextView = (TextView) rootView.findViewById(R.id.temp_textview);
        humidityTextView = (TextView) rootView.findViewById(R.id.humidity_textview);
        reTempTextView = (TextView) rootView.findViewById(R.id.relativetemp_textview);
        tempTextView.setText("Temperature\n\n--");
        humidityTextView.setText("Relative Humidity\n\n--\n");
        reTempTextView.setText("Heat Stress Index\n\n--\n");

        BTStatusBTN = (Button) rootView.findViewById(R.id.BT_status_button);
        BTStatusBTN.setBackgroundColor(Color.LTGRAY);
        BTStatusBTN.setTextColor(Color.WHITE);
        BTStatusBTN.setEnabled(false);

//        heatLog.put("lat", 0);
//        heatLog.put("lon", 0);
//        Log.d("Custom HomeFrag","I know it reaches here for sure");
//        mFusedLocationClient.getLastLocation()
//                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        Log.d("Custom HeatFrag","But does it reach here?");
//                        if (location != null) {
//                            Log.d("Custom HeatFrag","Or here?");
//                            heatLog.put("lat", location.getLatitude());
//                            heatLog.put("lon", location.getLongitude());
//                        }
//
//                    }
//                });
        String state = getArguments().getString("connectionState");
        updateStatus(state);
//        heatLog.saveEventually();
//        heatLog.pinInBackground();
        return rootView;
    }
    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menubarheat, menu);
    }

    public void updateStatus(String state){
        if (state.startsWith("Connected")){
            BTStatusBTN.setBackgroundColor(getResources().getColor(R.color.green));
        }
        else{
            BTStatusBTN.setBackgroundColor(Color.LTGRAY);
        }
        BTStatusBTN.setText(state);
    }
    public void updateTemp(String state){
//        heatLog.put("temp",state);
        tempTextView.setText(state);
//        heatLog.saveEventually();
//        heatLog.pinInBackground();

    }
    public void updateHumid(String state){
//        heatLog.put("humidity",state);
        humidityTextView.setText(state);
//        heatLog.saveEventually();
//        heatLog.pinInBackground();

    }
    public void updateHSI(String state){
//        heatLog.put("hsi",state);
        reTempTextView.setText(state);
//        heatLog.saveEventually();
//        heatLog.pinInBackground();

    }


//
//    // Code to manage Service lifecycle.
//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//            if (!mBluetoothLeService.initialize()) {
//                Log.e("asd", "Unable to initialize Bluetooth");
//            }
//            // Automatically connects to the device upon successful start-up initialization.
//            mBluetoothLeService.connect(mDeviceAddress);
//            Log.w("asd", "auto connect to"+ mDeviceAddress);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mBluetoothLeService = null;
//        }
//    };
//
//
//    private void scanLeDevice(final boolean enable) {
//        if (enable) {
//            // Stops scanning after a pre-defined scan period.
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mScanning = false;
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                }
//            }, SCAN_PERIOD);
//
//            mScanning = true;
//            mBluetoothAdapter.startLeScan(mLeScanCallback);
//        } else {
//            mScanning = false;
//            mBluetoothAdapter.stopLeScan(mLeScanCallback);
//        }
//    }
//
//    private BluetoothAdapter.LeScanCallback mLeScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
//
//                @Override
//                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            //BTStatusBTN.setText("Scanning");
//                            if (device != null && device.getName()!=null && device.getName().endsWith(mdropID)) {
//                                Log.e("asd", "Device" + device.getName());
//                                mDeviceName = device.getName();
//                                mDeviceAddress = device.getAddress();
//
//                                SharedPreferences preferences = getActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
//                                SharedPreferences.Editor editor = preferences.edit();
//
//                                editor.putString("deviceAddress", mDeviceAddress);
//                                editor.putString("deviceName", mDeviceName);
//                                editor.apply();
//                                Log.e("asd", "Device" + mDeviceAddress);
//                                mBluetoothLeService.connect(mDeviceAddress);
//                                scanLeDevice(false);
//
//                            }
//                        }
//                    });
//                }
//            };
//
//
//    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
//                mConnected = true;
//                BTStatusBTN.setText("Connected to " + mDeviceName);
//                BTStatusBTN.setBackgroundColor(Color.GREEN);
//            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
//                mConnected = false;
//                BTStatusBTN.setText("Disconnected");
//                BTStatusBTN.setBackgroundColor(Color.LTGRAY);
//                tempTextView.setText("Temperature\n--");
//            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
//
//                final List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();
//                for (BluetoothGattService gattService : gattServices) {
//                    if (gattService.getUuid().toString().equals(Constants.DROP_SERVICE)){
//                        List<BluetoothGattCharacteristic> gattCharacteristics =
//                                gattService.getCharacteristics();
//                        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                            if (Constants.DROP_TEMPERATURE.equals(gattCharacteristic.getUuid().toString())) {
//                                Log.d("asd", "Temp-set");
//                                //mBluetoothLeService.readCharacteristic(gattCharacteristic);
//                                mBluetoothLeService.setCharacteristicNotification(
//                                        gattCharacteristic, true);
//                            }
//                            if (Constants.DROP_HUMIDITY.equals(gattCharacteristic.getUuid().toString())) {
//                                Log.w("asd", "write-set");
//                                //mBluetoothLeService.readCharacteristic(gattCharacteristic);
//                                mBluetoothLeService.setCharacteristicNotification(
//                                        gattCharacteristic, true);
//                            }
//                            if (Constants.DROP_RELATIVE_TEMPERATURE.equals(gattCharacteristic.getUuid().toString())) {
//                                Log.w("asd", "write-set");
//                                //mBluetoothLeService.readCharacteristic(gattCharacteristic);
//                                mBluetoothLeService.setCharacteristicNotification(
//                                        gattCharacteristic, true);
//                            }
//
//                        }
//                    }
//                }
//
//            }
//
//            else if (BluetoothLeService.ACTION_TEMP_DATA_AVAILABLE.equals(action)) {
//                tempTextView.setText(intent.getStringExtra(BluetoothLeService.TEMP_DATA));
//            }
//            else if (BluetoothLeService.ACTION_HUMID_DATA_AVAILABLE.equals(action)){
//                humidityTextView.setText(intent.getStringExtra(BluetoothLeService.HUMID_DATA));
//            }
//            else if (BluetoothLeService.ACTION_RELATEMP_DATA_AVAILABLE.equals(action)){
//                reTempTextView.setText(intent.getStringExtra(BluetoothLeService.RELATEMP_DATA));
//            }
//        }
//    };
//
//    private static IntentFilter makeGattUpdateIntentFilter() {
//        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
//        intentFilter.addAction(BluetoothLeService.ACTION_TEMP_DATA_AVAILABLE);
//        intentFilter.addAction(BluetoothLeService.ACTION_HUMID_DATA_AVAILABLE);
//        intentFilter.addAction(BluetoothLeService.ACTION_RELATEMP_DATA_AVAILABLE);
//        return intentFilter;
//    };
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
//        if (mBluetoothLeService != null && mDeviceAddress!=null) {
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            Log.d("asd", "Connect request result=" + result);
//        }
//    }
//    @Override
//    public void onPause() {
//        super.onPause();
//        getActivity().unregisterReceiver(mGattUpdateReceiver);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        getActivity().unbindService(mServiceConnection);
//        mBluetoothLeService = null;
//    }





}