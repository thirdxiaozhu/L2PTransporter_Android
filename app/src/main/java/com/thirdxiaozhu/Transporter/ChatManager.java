package com.thirdxiaozhu.Transporter;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatManager {

    private ChatManager(){}
    private static final ChatManager instance = new ChatManager();
    public static ChatManager getCM(){
        return instance;
    }

    MainActivity mainActivity;
    Socket socket;
    String IP;
    BufferedReader reader;
    PrintWriter writer;
    String line;

    public void setMainActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public void connect(String IP){
        this.IP = IP;
        new Thread(){
            @Override
            public void run() {
                try {
                    socket = new Socket(IP, 12345);
                    writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    DataInputStream dis = new DataInputStream(socket.getInputStream());

                    Gson gson = new Gson(); //序列化设备信息
                    writer.write(gson.toJson(new getDeviceInfo(mainActivity))+"\n"); //传到服务器（Ubuntu）
                    writer.flush();

                    //接收服务端传来的主机信息
                    mainActivity.currentPC = gson.fromJson(reader.readLine(), HostInfo.class);
                    mainActivity.handler.post(mainActivity.runable);

                    while(true){
                        byte[] fileMessageByte = new byte[149];
                        dis.read(fileMessageByte,0,fileMessageByte.length);
                        String fileMessage = new String(fileMessageByte);

                        Log.d("Tag",ToolUtil.hexStr2Str(fileMessage.split("--")[1].trim()));
                        Log.d("Tag",Long.parseLong( fileMessage.split("--")[2]) + "");

                        String fileName = ToolUtil.hexStr2Str(fileMessage.split("--")[1].trim());
                        Long fileLength = Long.parseLong( fileMessage.split("--")[2]);

                        File file = new File(mainActivity.getExternalFilesDir("received/"+mainActivity.currentPC.getHostName()),
                                System.currentTimeMillis()+"-"+fileName);

                        System.out.println(file.getAbsolutePath());
                        FileOutputStream fos = new FileOutputStream(file);


                        byte[] bytes = new byte[1024];
                        int length = 0;
                        long progress = 0;

                        while(((length = dis.read(bytes, 0, bytes.length)) != -1)) {

                            fos.write(bytes, 0, length);
                            fos.flush();
                            progress += length;

                            fileLength -= length;
                            //System.out.println(fileLength+" "+length);
                            if (fileLength == 0){
                                break;
                            }
                            if (fileLength < bytes.length){
                                //bytes = new byte[(int)fileLength];
                                bytes = new byte[Integer.parseInt(String.valueOf(fileLength))];
                            }
                        }

                        mainActivity.receiveFile(ToolUtil.hexStr2Str(fileMessage.split("--")[1].trim()));
                    }

                    //writer.close();
                    //reader.close();

                    //writer = null;
                    //reader = null;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void send(String out){
        new Thread(){
            @Override
            public void run() {
                if(writer != null){
                    writer.write(out + "\n");
                    System.out.println(strTo16(out));
                    writer.flush();
                }else{
                    //mainActivity.
                    mainActivity.messages.append("连接已中断");
                }
            }
        }.start();
    }

    public static String strTo16(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }
}
