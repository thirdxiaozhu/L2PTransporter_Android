package com.thirdxiaozhu.Transporter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Vector;


/**
 * @author jiaxv
 */
public class MainActivity extends AppCompatActivity {
    public EditText messages;
    public Handler handler;
    private String scanResult;
    public HostInfo currentPC;
    public Vector<String> files;
    public MyListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChatManager.getCM().setMainActivity(this);
        handler = new Handler();

        TextView manualLink = (TextView)findViewById(R.id.manualLink);

        ListView listView = (ListView)this.findViewById(R.id.id_receive_list);
        files = new Vector<>();
        listAdapter = new MyListAdapter(files);
        listView.setAdapter(listAdapter);

        manualLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManualLinkDialog mld = new ManualLinkDialog(MainActivity.this);
                mld.show(getFragmentManager(), "ManualLinkDialog");
            }
        });
    }

    public void onConnect(View view){

    }


    public void onScan(View view){
        // 创建IntentIntegrator对象
        IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
        // 开始扫描
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.initiateScan();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 获取解析结果
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "取消扫描", Toast.LENGTH_LONG).show();
            } else {
                try {
                    //scanResult = result.getContents();
                    //Gson gson = new Gson();
                    //currentPC = gson.fromJson(scanResult, QRinfo.class);
                    //Log.d("Tag", currentPC.getHostIP());
                    connectPC(result.getContents());
                }catch (Exception e){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("警告");
                    builder.setMessage("无效二维码，请重新扫描");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.out.println("点了确定");
                        }
                    });
                    builder.show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void connectPC(String result){
        ChatManager.getCM().connect(result);
    }


    public void manualLink(String result){
        connectPC(result);
    }

    public void initPCInfo(){
        EditText pcinfo = (EditText)findViewById(R.id.PCInfomation);
        pcinfo.setText("");
        pcinfo.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        pcinfo.append("  主机名:  " + currentPC.getHostName() + "\n");
        pcinfo.append("  主机IP:  " + currentPC.getHostIP() + "\n");
        pcinfo.append("  OS:  " + currentPC.getOSName() + "\n");
        pcinfo.append("  OS架构:  " + currentPC.getOSArch() + "\n");
        pcinfo.append("  OS版本:  " + currentPC.getOSVersion() + "\n");
    }

    Runnable runable = new Runnable() {
        @Override
        public void run() {
            initPCInfo();
        }
    };

    public void receiveFile(String s){
        listAdapter.files.add(s);
        listAdapter.handler.post(listAdapter.runable);
    }

}