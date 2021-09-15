package com.thirdxiaozhu.Transporter;

import android.os.Message;
import android.util.Log;
import android.view.textclassifier.ConversationActions;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.SocketFactory;

import Protocal.BasicProtocol;
import Protocal.ClientRequestTask;
import Protocal.DataAckProtocol;
import Protocal.DataProtocol;
import Protocal.PingProtocol;
import Protocal.SocketUtil;

public class ConnectionThread implements Runnable{

    private static final int SUCCESS = 100;
    private static final int FAILED = -1;

    MainActivity mainActivity;
    BufferedReader reader;
    PrintWriter writer;
    String line;

    private boolean isLongConnection = true;
    //private Handler mHandler;
    private SendTask sendTask;
    private ReciveTask receiveTask;
    //private HeartBeatTask heartBeatTask;
    private Socket socket;
    private String IP;

    private boolean isSocketAvailable;
    private boolean closeSendTask;
    public ManageFile manageFile;

    protected volatile ConcurrentLinkedQueue<BasicProtocol> dataQueue = new ConcurrentLinkedQueue<>();

    public ConnectionThread(MainActivity mainActivity, String IP){
        this.mainActivity = mainActivity;
        this.IP = IP;
        manageFile = new ManageFile(mainActivity);
    }

    @Override
    public void run(){
         try {
             try {
                 socket = SocketFactory.getDefault().createSocket(IP, Integer.parseInt("1234"));
                 isSocketAvailable = true;
             }catch (Exception e){
                 return;
             }

             writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
             reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataInputStream dis = new DataInputStream(socket.getInputStream());

             Gson gson = new Gson(); //序列化设备信息
             writer.write(gson.toJson(new getDeviceInfo(mainActivity))+"\n"); //传到服务器（Ubuntu）
             writer.flush();

             //接收服务端传来的主机信息
             mainActivity.currentPC = gson.fromJson(reader.readLine(), HostInfo.class);
             mainActivity.handler.post(mainActivity.runable);

             //开启接收线程
             receiveTask = new ReciveTask();
             receiveTask.inputStream = socket.getInputStream();
             receiveTask.start();


             ////开启发送线程
             //sendTask = new SendTask();
             //sendTask.outputStream = socket.getOutputStream();
             //sendTask.start();

             //开启心跳线程
             //if (isLongConnection) {
             //    heartBeatTask = new HeartBeatTask();
             //    heartBeatTask.outputStream = socket.getOutputStream();
             //    heartBeatTask.start();
             //}


             //while(true){
             //    byte[] fileMessageByte = new byte[1024];
             //    dis.read(fileMessageByte,0,fileMessageByte.length);
             //    String fileMessage = new String(fileMessageByte);

             //    Log.d("Tag",fileMessage);
             //    Log.d("Tag",ToolUtil.hexStr2Str(fileMessage.split("--")[1].trim()));
             //    Log.d("Tag",Long.parseLong( fileMessage.split("--")[2]) + "");

             //    String fileName = ToolUtil.hexStr2Str(fileMessage.split("--")[1].trim());
             //    Long fileLength = Long.parseLong( fileMessage.split("--")[2]);
             //    mainActivity.receiveFile(ToolUtil.hexStr2Str(fileMessage.split("--")[1].trim()));

             //    File file = new File(mainActivity.getExternalFilesDir("received/"+mainActivity.currentPC.getHostName()), fileName);

             //    System.out.println(file.getAbsolutePath());
             //    FileOutputStream fos = new FileOutputStream(file);


             //    byte[] bytes = new byte[8192];
             //    int length = 0;
             //    long progress = 0;

             //    while(((length = dis.read(bytes, 0, bytes.length)) != -1)) {

             //        fos.write(bytes, 0, length);
             //        fos.flush();
             //        progress += length;

             //        fileLength -= length;
             //        //System.out.println(fileLength+" "+length);
             //        if (fileLength == 0){
             //            break;
             //        }
             //        if (fileLength < bytes.length){
             //            //bytes = new byte[(int)fileLength];
             //            bytes = new byte[Integer.parseInt(String.valueOf(fileLength))];
             //        }
             //    }
             //    //读取完成则隐藏进度条
             //    mainActivity.listAdapter.finishReceive(mainActivity.updatebarHandler, fileName);
             //}

         } catch (Exception e) {
             e.printStackTrace();
         }
    }

    public void addRequest(DataProtocol data) {
        dataQueue.add(data);
        Log.d("Tag", "唤醒！！");
        toNotifyAll(dataQueue);//有新增待发送数据，则唤醒发送线程
    }

    public synchronized void stop() {

        //关闭接收线程
        closeReciveTask();

        //关闭发送线程
        closeSendTask = true;
        toNotifyAll(dataQueue);

        //关闭心跳线程
        //closeHeartBeatTask();

        //关闭socket
        closeSocket();

        //清除数据
        clearData();

        failedMessage(-1, "断开连接");
    }

    /**
     * 关闭接收线程
     */
    private void closeReciveTask() {
        if (receiveTask != null) {
            receiveTask.interrupt();
            receiveTask.isCancle = true;
            if (receiveTask.inputStream != null) {
                try {
                    if (isSocketAvailable && !socket.isClosed() && socket.isConnected()) {
                        socket.shutdownInput();//解决java.net.SocketException问题，需要先shutdownInput
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SocketUtil.closeInputStream(receiveTask.inputStream);
                receiveTask.inputStream = null;
            }
            receiveTask = null;
        }
    }

    /**
     * 关闭发送线程
     */
    private void closeSendTask() {
        if (sendTask != null) {
            sendTask.isCancle = true;
            sendTask.interrupt();
            if (sendTask.outputStream != null) {
                synchronized (sendTask.outputStream) {//防止写数据时停止，写完再停
                    SocketUtil.closeOutputStream(sendTask.outputStream);
                    sendTask.outputStream = null;
                }
            }
            sendTask = null;
        }
    }

    /**
     * 关闭心跳线程
     */
    //private void closeHeartBeatTask() {
    //    if (heartBeatTask != null) {
    //        heartBeatTask.isCancle = true;
    //        if (heartBeatTask.outputStream != null) {
    //            SocketUtil.closeOutputStream(heartBeatTask.outputStream);
    //            heartBeatTask.outputStream = null;
    //        }
    //        heartBeatTask = null;
    //    }
    //}

    private void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
                isSocketAvailable = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 清除数据
     */
    private void clearData() {
        dataQueue.clear();
        isLongConnection = false;
    }

    private void toWait(Object o) {
        synchronized (o) {
            try {
                o.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * notify()调用后，并不是马上就释放对象锁的，而是在相应的synchronized(){}语句块执行结束，自动释放锁后
     *
     * @param o
     */
    protected void toNotifyAll(Object o) {
        synchronized (o) {
            o.notifyAll();
        }
    }

    private void failedMessage(int code, String msg) {
        //Message message = mHandler.obtainMessage(FAILED);
        //message.what = FAILED;
        //message.arg1 = code;
        //message.obj = msg;
        //mHandler.sendMessage(message);
    }

    private void successMessage(BasicProtocol protocol) {
        //Message message = mHandler.obtainMessage(SUCCESS);
        //message.what = SUCCESS;
        //message.obj = protocol;
        //mHandler.sendMessage(message);
    }

    private boolean isConnected() {
        if (socket.isClosed() || !socket.isConnected()) {
            ConnectionThread.this.stop();
            return false;
        }
        return true;
    }

    /**
     * 服务器返回处理，主线程运行
     */
    //public class MyHandler extends Handler {

    //    private RequestCallBack mRequestCallBack;

    //    public MyHandler(RequestCallBack callBack) {
    //        super(Looper.getMainLooper());
    //        this.mRequestCallBack = callBack;
    //    }

    //    @Override
    //    public void handleMessage(Message msg) {
    //        super.handleMessage(msg);
    //        switch (msg.what) {
    //            case SUCCESS:
    //                mRequestCallBack.onSuccess((BasicProtocol) msg.obj);
    //                break;
    //            case FAILED:
    //                mRequestCallBack.onFailed(msg.arg1, (String) msg.obj);
    //                break;
    //            default:
    //                break;
    //        }
    //    }
    //}

    public class ReciveTask extends Thread {

        private boolean isCancle = false;
        private InputStream inputStream;

        @Override
        public void run() {
            int i = 0;
            while (!isCancle) {
                if (!isConnected()) {
                    break;
                }
                if (inputStream != null) {
                    Log.d("Tag", String.valueOf(i));
                    BasicProtocol receiveData = SocketUtil.readFromStream(inputStream, i, ConnectionThread.this.socket);
                    i++;
                    if (receiveData != null) {
                        if (receiveData.getProtocolType() == 1 || receiveData.getProtocolType() == 3) {
                            successMessage(receiveData);
                        }else if (receiveData.getProtocolType() == 0){
                            Log.d("Tag", "dtype: " + ((DataProtocol) receiveData).getDtype() + ", pattion: " + ((DataProtocol) receiveData).getPattion() + ", msgId: " + ((DataProtocol) receiveData).getMsgId() + ", data: " + ((DataProtocol) receiveData).getData());
                            manageFile.addMessage((DataProtocol) receiveData);

                            //DataAckProtocol dataAck = new DataAckProtocol();
                            //dataAck.setUnused("收到消息：");
                            //dataQueue.offer(dataAck);
                            //toNotifyAll(dataQueue); //唤醒发送线程
                        }
                    } else {
                        Log.d("Tag", "isBreak");
                        break;
                    }
                }
            }

            SocketUtil.closeInputStream(inputStream);//循环结束则退出输入流
        }
    }

    /**
     * 数据发送线程
     * 当没有发送数据时让线程等待
     */
    public class SendTask extends Thread {

        private boolean isCancle = false;
        private OutputStream outputStream;

        @Override
        public void run() {
            while (!isCancle) {
                if (!isConnected()) {
                    break;
                }

                BasicProtocol dataContent = dataQueue.poll();
                if (dataContent == null) {
                    toWait(dataQueue);//没有发送数据则等待
                    if (closeSendTask) {
                        closeSendTask();//notify()调用后，并不是马上就释放对象锁的，所以在此处中断发送线程
                    }
                } else if (outputStream != null) {
                    synchronized (outputStream) {
                        SocketUtil.write2Stream(dataContent, outputStream);
                    }
                }
            }

            SocketUtil.closeOutputStream(outputStream);//循环结束则退出输出流
        }
    }

    /**
     * 心跳实现，频率5秒
     * Created by meishan on 16/12/1.
     */
    public class HeartBeatTask extends Thread {

        private static final int REPEATTIME = 5000;
        private boolean isCancle = false;
        private OutputStream outputStream;
        private int pingId;

        @Override
        public void run() {
            pingId = 1;
            while (!isCancle) {
                if (!isConnected()) {
                    break;
                }

                try {
                    socket.sendUrgentData(0xFF);
                } catch (IOException e) {
                    isSocketAvailable = false;
                    ConnectionThread.this.stop();
                    break;
                }

                if (outputStream != null) {
                    PingProtocol pingProtocol = new PingProtocol();
                    pingProtocol.setPingId(pingId);
                    pingProtocol.setUnused("ping...");
                    SocketUtil.write2Stream(pingProtocol, outputStream);
                    pingId = pingId + 2;
                }

                try {
                    Thread.sleep(REPEATTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            SocketUtil.closeOutputStream(outputStream);
        }
    }
}
