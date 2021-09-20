package com.thirdxiaozhu.Transporter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.security.PublicKey;
import java.util.Vector;

public class SettingActivity extends Fragment {
    private View view;
    public static HostInfo currentPC;
    public BasicActivity basicActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_setting, container, false);
        basicActivity = (BasicActivity) getActivity();
        initPCInfo();

        LinearLayout manualLink = (LinearLayout) view.findViewById(R.id.manualLink);

        //手动连接layout监听器
        manualLink.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ManualLinkDialog mld = new ManualLinkDialog(SettingActivity.this);
                mld.show(getFragmentManager(), "ManualLinkDialog");
            }
        });

        //断开连接监听器
        LinearLayout deleteLink = (LinearLayout) view.findViewById(R.id.deleteLink);
        deleteLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BasicActivity.connectionClient == null){
                    Toast.makeText(BasicActivity.Instance,"当前尚未连接任何PC", Toast.LENGTH_LONG).show();
                    return;
                }
                BasicActivity.connectionClient.closeConnect();
            }
        });
        return view;
    }

    public void initPCInfo(){
        EditText pcInfo = (EditText) view.findViewById(R.id.PCInfomation);
        pcInfo.setText("");
        if(currentPC == null){
            pcInfo.append("              尚未连接到任何PC              \n");
        }else {
            pcInfo.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            pcInfo.append("  主机名:  " + currentPC.getHostName() + "\n");
            pcInfo.append("  主机IP:  " + currentPC.getHostIP() + "\n");
            pcInfo.append("  OS:  " + currentPC.getOSName() + "\n");
            pcInfo.append("  OS架构:  " + currentPC.getOSArch() + "\n");
            pcInfo.append("  OS版本:  " + currentPC.getOSVersion() + "\n");
        }
    }

    public Runnable runable = new Runnable() {
        @Override
        public void run() {
            initPCInfo();
        }
    };

    public void manualLink(String result){
        basicActivity.connectPC(result);
    }
}