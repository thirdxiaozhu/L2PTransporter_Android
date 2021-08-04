package com.example.myapplication;

import android.app.AppComponentFactory;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRUtil extends AppCompatActivity {
    MainActivity mainActivity;

    public QRUtil(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void scanQRCOde(){
    }

}
