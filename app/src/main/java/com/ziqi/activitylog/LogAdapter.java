package com.ziqi.activitylog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends ArrayAdapter<ParseObject> {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<ParseObject> mLogs;

    public LogAdapter(Context context, List<ParseObject> objects) {
        super(context, R.layout.list_item_log, objects);
        mContext = context;
        mLogs = objects;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = mInflater.inflate(R.layout.list_item_log, parent, false);

        TextView titleTextView =
        (TextView) rowView.findViewById(R.id.recipe_list_title);

        //TextView subtitleTextView =
        //(TextView) rowView.findViewById(R.id.recipe_list_subtitle);

        TextView detailTextView =
        (TextView) rowView.findViewById(R.id.recipe_list_detail);

        ParseObject log = mLogs.get(position);

        titleTextView.setText(log.getString("activity"));
        //subtitleTextView.setText(log.time);
        detailTextView.setText(log.getString("time") + " " +log.getString("date"));


        return rowView;
    }

}
