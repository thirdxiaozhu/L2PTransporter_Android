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
        manualLink.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ManualLinkDialog mld = new ManualLinkDialog(SettingActivity.this);
                mld.show(getFragmentManager(), "ManualLinkDialog");
            }
        });
        return view;
    }

    public void initPCInfo(){
        if(currentPC == null){
            return;
        }else {
            EditText pcinfo = (EditText) view.findViewById(R.id.PCInfomation);
            pcinfo.setText("");
            pcinfo.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            pcinfo.append("  主机名:  " + currentPC.getHostName() + "\n");
            pcinfo.append("  主机IP:  " + currentPC.getHostIP() + "\n");
            pcinfo.append("  OS:  " + currentPC.getOSName() + "\n");
            pcinfo.append("  OS架构:  " + currentPC.getOSArch() + "\n");
            pcinfo.append("  OS版本:  " + currentPC.getOSVersion() + "\n");
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