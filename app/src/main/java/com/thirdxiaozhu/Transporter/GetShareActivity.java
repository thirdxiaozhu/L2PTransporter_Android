package com.thirdxiaozhu.Transporter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class GetShareActivity extends AppCompatActivity {
    private FileInputStream inputStream = null;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_share);
        intent = getIntent();
        Uri data_uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        String[] uri = data_uri.toString().split("/");

        ContentResolver resolver = getContentResolver();
        // 使用ContentResolver的openFileDescriptor方法获取ParcelFileDescriptor对象
        ParcelFileDescriptor fd= null;
        try {
            fd = resolver.openFileDescriptor(data_uri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 使用ParcelFileDescriptor的getFileDescriptor方法获取FileDescriptor对象
        // 利用FileDescriptor对象建立文件输入流(FileInputStream)
        if(fd != null) {
            inputStream = new FileInputStream(fd.getFileDescriptor());
            MainActivity.toSendFile(new FdClass(uri[uri.length-1], fd));
        }


        finish();
    }
}