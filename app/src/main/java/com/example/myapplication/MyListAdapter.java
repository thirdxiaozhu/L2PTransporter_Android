package com.example.myapplication;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.Vector;

/**
 * @author jiaxv
 */
public class MyListAdapter extends BaseAdapter {
    Vector<String> files;
    public Handler handler;
    Runnable runable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    public MyListAdapter(Vector<String> files){
        handler = new Handler();
        this.files = files;
    }
    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_cell,parent,false);
        }

        TextView textView = (TextView)convertView.findViewById(R.id.id_item_text);

        String path = (String)getItem(position);
        textView.setText(path);

        return convertView;
    }

}
