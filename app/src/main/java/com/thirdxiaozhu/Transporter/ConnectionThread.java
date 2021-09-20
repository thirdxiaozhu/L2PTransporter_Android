package com.thirdxiaozhu.Transporter;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.SocketFactory;

import Protocal.BasicProtocol;
import Protocal.DataAckProtocol;
import Protocal.DataProtocol;
import Protocal.PingProtocol;
import Protocal.SocketUtil;

public class ConnectionThread implements Runnable{

    private static final int SUCCESS = 100;
    private static final int FAILED = -1;

    private BasicActivity basicActivity;
    private MainActivity mainActivity;
    private SettingActivity settingActivity;
    private BufferedReader reader;
    private PrintWriter writer;

    private boolean isLongConnection = true;
    private SendTask sendTask;
    private ReciveTask receiveTask;
    private HeartBeatTask heartBeatTask;
    private Socket socket;
    private String IP;
    private String port;

    private boolean isSocketAvailable;
    private boolean closeSendTask;
    public ManageFile manageFile;

    protected volatile ConcurrentLinkedQueue<BasicProtocol> dataQueue = new ConcurrentLinkedQueue<>();

    public ConnectionThread(BasicActivity basicActivity, String iPwithPort){
        this.basicActivity = basicActivity;
        this.mainActivity = basicActivity.mainActivity;
        this.settingActivity = basicActivity.settingActivity;

        manageFile = new ManageFile(basicActivity, ConnectionThread.this);
    }

    @Override
    public void run(){
        try {
             try {
                 socket = SocketFactory.getDefault().createSocket(IP, Integer.parseInt(port));
                 isSocketAvailable = true;
             }catch (Exception e){
                 e.printStackTrace();
                 return;
             }

             writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
             reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataInputStream dis = new DataInputStream(socket.getInputStream());

             //序列化设备信息
             Gson gson = new Gson();
             writer.write(gson.toJson(new getDeviceInfo(basicActivity))+"\n");
             writer.flush();

             //接收服务端传来的主机信息
             SettingActivity.currentPC = gson.fromJson(reader.readLine(), HostInfo.class);
             mainActivity.handler.post(settingActivity.runable);

             //开启接收线程
             receiveTask = new ReciveTask();
             receiveTask.inputStream = socket.getInputStream();
             receiveTask.start();


             //开启发送线程
             sendTask = new SendTask();
             sendTask.outputStream = socket.getOutputStream();
             sendTask.start();

             //开启心跳线程
             if (isLongConnection) {
                 heartBeatTask = new HeartBeatTask();
                 heartBeatTask.outputStream = socket.getOutputStream();
                 heartBeatTask.start();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
    }

    public void addMessage(BasicProtocol data) {
        if(!isConnected()){
            return;
        }
        dataQueue.add(data);
        //有新增待发送数据，则唤醒发送线程
        toNotifyAll(dataQueue);
    }

    public synchronized void stop() {

        //关闭接收线程
        closeReciveTask();

        //关闭发送线程
        closeSendTask = true;
        toNotifyAll(dataQueue);

        //关闭心跳线程
        closeHeartBeatTask();

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
    private void closeHeartBeatTask() {
        if (heartBeatTask != null) {
            heartBeatTask.isCancle = true;
            if (heartBeatTask.outputStream != null) {
                SocketUtil.closeOutputStream(heartBeatTask.outputStream);
                heartBeatTask.outputStream = null;
            }
            heartBeatTask = null;
        }
    }

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
        //Log.d("Tag", "成功接收Ack报文");
    }

    private boolean isConnected() {
        if (socket.isClosed() || !socket.isConnected()) {
            ConnectionThread.this.stop();
            return false;
        }
        return true;
    }


    public class ReciveTask extends Thread {

        private boolean isCancle = false;
        private InputStream inputStream;
        private BufferedInputStream bis;

        @Override
        public void run() {
            bis = new BufferedInputStream(inputStream);
            while (!isCancle) {
                if (!isConnected()) {
                    break;
                }
                if (inputStream != null) {
                    BasicProtocol receiveData = SocketUtil.readFromStream(bis);
                    if (receiveData != null) {
                        if (receiveData.getProtocolType() == 1 || receiveData.getProtocolType() == 3) {
                            successMessage(receiveData);
                        }else if (receiveData.getProtocolType() == 0){
                            //Log.d("Tag", "dtype: " + ((DataProtocol) receiveData).getDtype() + ", pattion: " + ((DataProtocol) receiveData).getPattion() + ", msgId: " + ((DataProtocol) receiveData).getMsgId() + ", data: " + ((DataProtocol) receiveData).getData());
                            manageFile.addMessage((DataProtocol) receiveData);

                            DataAckProtocol dataAck = new DataAckProtocol();
                            dataAck.setUnused("收到消息：");
                            dataQueue.offer(dataAck);
                            toNotifyAll(dataQueue); //唤醒发送线程
                        }
                    } else {

                        //断开连接
                        Log.d("Tag", "isBreak");
                        SettingActivity.currentPC = null;
                        mainActivity.handler.post(settingActivity.runable);
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
