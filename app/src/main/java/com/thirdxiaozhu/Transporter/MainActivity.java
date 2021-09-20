package com.thirdxiaozhu.Transporter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.FileInputStream;
import java.util.Vector;


/**
 * @author jiaxv
 */
public class MainActivity extends Fragment {
    private View view;
    public Handler handler;
    public BasicActivity basicActivity;
    public ListView receiveList;
    public ListView sendList;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_main, container, false);
        handler = new Handler();

        basicActivity = (BasicActivity) getActivity();
        receiveList = (ListView)this.view.findViewById(R.id.id_receive_list);
        receiveList.setAdapter(basicActivity.receiveListAdapter);
        sendList = (ListView)this.view.findViewById(R.id.id_send_text);
        sendList.setAdapter(basicActivity.sendListAdapter);

        return view;
    }

}