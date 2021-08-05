package com.thirdxiaozhu.Transporter;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author jiaxv
 */
public class MyListAdapter extends BaseAdapter {
    Vector<String> files;
    public Handler handler;
    private List<View> viewList;
    private Map<String, View> posMap;

    Runnable dataChanged = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    public MyListAdapter(Vector<String> files){
        handler = new Handler();
        this.files = files;
        viewList = new Vector<>();
        posMap = new HashMap<>();
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
        String filename = (String)getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_cell,parent,false);
            Log.d("TAG", position + " 被创建");
            //通过文件名在map中设置对应的item view
            posMap.put(filename, convertView);
        }

        TextView textView = (TextView)convertView.findViewById(R.id.id_item_text);

        textView.setText(filename);

        convertView.setTag(R.id.id_receive_list, position);
        viewList.add(convertView);

        return convertView;
    }

    /**
     * 到主线程中更改item的进度条(Message进行线程同步）
     * @param updPro
     * @param s 文件名
     */
    public void finishReceive(Handler updPro,String s){
        //得到item view
        View currview = posMap.get(s);

        if(currview != null){
            Message msg = updPro.obtainMessage();
            msg.obj = currview;
            updPro.sendMessage(msg);
        }

    }

}
