//package com.example.myapplication;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.TextView;
//
//public class downLoadPractice extends Activity {
//    private Button button_submit=null;
//    private TextView textView=null;
//    private String content=null;
//    private Handler handler=null;
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//        //创建属于主线程的handler
//        handler=new Handler();
//
//        button_submit=(Button)findViewById(R.id.button_submit);
//        textView=(TextView)findViewById(R.id.textView);
//        button_submit.setOnClickListener(new submitOnClieckListener());
//    }
//    //为按钮添加监听器
//    class submitOnClieckListener implements OnClickListener{
//        @Override
//        public void onClick(View v) {
////本地机器部署为服务器，从本地下载a.txt文件内容在textView上显示
//            final DownFiles df=new DownFiles("http://192.168.75.1:8080/downLoadServer/a.txt");
//            textView.setText("正在加载......");
//            new Thread(){
//                public void run(){
//                    content=df.downLoadFiles();
//                    handler.post(runnableUi);
//                }
//            }.start();
//        }
//
//    }
//
//    // 构建Runnable对象，在runnable中更新界面
//    Runnable   runnableUi=new  Runnable(){
//        @Override
//        public void run() {
//            //更新界面
//            textView.setText("the Content is:"+content);
//        }
//
//    };
//
//
//}