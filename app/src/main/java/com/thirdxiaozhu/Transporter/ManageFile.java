package com.thirdxiaozhu.Transporter;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import Protocal.DataProtocol;

public class ManageFile {
    private static final int HEAD=0;
    private static final int BODY=1;

    //在迭代的时候保证一致性（添加、移除），需要使用CopyOnWriteArrayList
    //private CopyOnWriteArrayList<DataProtocol> haveProcessed;
    //private CopyOnWriteArrayList<DataProtocol> waitProcessed;
    protected volatile ConcurrentLinkedQueue<DataProtocol> dataQueue;
    private MainActivity mainActivity;
    private GenerateFile generateFile;

    public ManageFile(MainActivity mainActivity){
        dataQueue = new ConcurrentLinkedQueue<>();
        this.mainActivity = mainActivity;
        generateFile = new GenerateFile();
        generateFile.start();
    }

    public void addMessage(DataProtocol dataProtocol){
        dataQueue.offer(dataProtocol);
        toNotifyAll(dataQueue);
    }

    private void toWaitAll(Object o){
        synchronized (o){
            try{
                o.wait();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private void toNotifyAll(Object o){
        synchronized (o){
            o.notifyAll();
        }
    }

    public class GenerateFile extends Thread{
        FileOutputStream fos = null;
        Long fileLength = null;
        String fileName = null;
        @Override
        public void run() {
            try {
                while (true) {
                    DataProtocol dataProtocol = dataQueue.poll();
                    if (dataProtocol == null) {
                        Log.d("Tag","aaa");
                        toWaitAll(dataQueue);
                    } else {
                        if (dataProtocol.getMsgId() == HEAD) {

                            byte[] fileMessageByte = dataProtocol.getData();
                            String fileMessage = new String(fileMessageByte);

                            Log.d("Tag", fileMessage);
                            Log.d("Tag", ToolUtil.hexStr2Str(fileMessage.split("--")[1].trim()));
                            Log.d("Tag", Long.parseLong(fileMessage.split("--")[2]) + "");

                            fileName = ToolUtil.hexStr2Str(fileMessage.split("--")[1].trim());
                            fileLength = Long.parseLong(fileMessage.split("--")[2]);
                            mainActivity.receiveFile(ToolUtil.hexStr2Str(fileMessage.split("--")[1].trim()));

                            Thread.sleep(1000000);

                            File file = new File(mainActivity.getExternalFilesDir("received/" + mainActivity.currentPC.getHostName()), fileName);
                            //System.out.println(file.getAbsolutePath());

                            fos = new FileOutputStream(file);
                        } else if(dataProtocol.getMsgId() == BODY){
                            int length = dataProtocol.getData().length;

                            fos.write(dataProtocol.getData(), 0, length);
                            fos.flush();

                            mainActivity.listAdapter.finishReceive(mainActivity.updatebarHandler, fileName);
                        }
                    }
                }
            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}
