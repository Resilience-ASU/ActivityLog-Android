package com.ziqi.activitylog;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
//Added by Laveena Sachdeva for Heat History
public class HeatHistoryFragment extends Fragment implements View.OnClickListener{

    private ListView mListView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        mListView = rootView.findViewById(R.id.log_list_view);

        //final ArrayList<Log> logList = Log.getLogsFromFile("logs.json", getActivity());
        final LogAdapter adapter = new LogAdapter(getActivity(), new ArrayList<ParseObject>());
        mListView.setAdapter(adapter);


        ParseQuery<ParseObject> query = ParseQuery.getQuery("HeatLogs");
        //query.whereEqualTo("user", ParseUser.getCurrentUser().getUsername());
        query.fromLocalDatastore();
        query.orderByDescending("createdAt");
        query.setLimit(200);


        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> logList, ParseException e) {
                if (e == null) {
                    Log.d("Custom HeatHistory","logList size here "+logList.size());
                    adapter.clear();
                    adapter.addAll(logList);
                }
            }
        });


        FloatingActionButton mapBTN =  rootView.findViewById(R.id.map_btn);
        mapBTN.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = null;
        switch (view.getId()) {
            case R.id.map_btn:
                Log.d("d","clicked");
                fragment = new MapViewHeatHistoryFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(this.getId(), fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
        }
    }

}
