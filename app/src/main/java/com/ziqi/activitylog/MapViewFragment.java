package com.ziqi.activitylog;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MapViewFragment extends Fragment {
    MapView mMapView;
    private GoogleMap googleMap;
    private ArrayList latlonList = new ArrayList<LatLng>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map_view, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

                googleMap = mMap;
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);

                }

                ParseQuery<ParseObject> query = ParseQuery.getQuery("Logs");
                //query.whereEqualTo("user", ParseUser.getCurrentUser().getUsername());
                query.orderByDescending("createdAt");
                query.fromLocalDatastore();
                query.whereNotEqualTo("lat", 0);
                query.setLimit(200);

                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> logList, ParseException e) {

                        if (e == null) {

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();

                            for (int i=0; i<logList.size(); i++) {
                                LatLng pos = new LatLng(logList.get(i).getDouble("lat"), logList.get(i).getDouble("lon"));
                                latlonList.add(pos);
                                builder.include(pos);
                                String dateString = logList.get(i).getString("time") + " " + logList.get(i).getString("date");

                                Marker marker = googleMap.addMarker(new MarkerOptions().position(pos).title(logList.get(i).getString("activity")).snippet(dateString));
                                //marker.showInfoWindow();
                            }

                            if (logList.size() > 0) {
                                //Toast.makeText(getActivity(), String.valueOf(logList.size()), Toast.LENGTH_LONG).show();

                                Polyline polyline1 = googleMap.addPolyline(new PolylineOptions().addAll(latlonList));

                                LatLngBounds bounds = builder.build();
                                int width = getResources().getDisplayMetrics().widthPixels;
                                int height = getResources().getDisplayMetrics().heightPixels;
                                int padding = (int) (width * 0.20); // offset from edges of the map 10% of screen

                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                                googleMap.moveCamera(cu);


                            }


                        } else {
                            Toast.makeText(getActivity(), "Network Error", Toast.LENGTH_LONG).show();
                        }
                    }
                });


            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
