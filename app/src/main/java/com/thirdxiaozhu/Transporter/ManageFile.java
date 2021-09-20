package com.thirdxiaozhu.Transporter;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import Protocal.BasicProtocol;
import Protocal.DataProtocol;

public class ManageFile {
    private static final int HEAD = 0;
    private static final int BODY = 1;
    private static final int BUFFERLENGTH = 8192;

    //在迭代的时候保证一致性（添加、移除），需要使用CopyOnWriteArrayList
    protected volatile ConcurrentLinkedQueue<DataProtocol> dataQueue;
    protected volatile ConcurrentLinkedQueue<FdClass> fdQueue;
    private ConnectionThread connectionThread;
    private BasicActivity basicActivity;
    private GenerateFile generateFile;
    private GenerateMessage generateMessage;

    public ManageFile(BasicActivity basicActivity, ConnectionThread connectionThread) {
        dataQueue = new ConcurrentLinkedQueue<>();
        fdQueue = new ConcurrentLinkedQueue<>();
        this.basicActivity = basicActivity;
        this.connectionThread = connectionThread;
        generateFile = new GenerateFile();
        generateFile.start();
        generateMessage = new GenerateMessage();
        generateMessage.start();
    }

    public void addMessage(DataProtocol dataProtocol) {
        dataQueue.offer(dataProtocol);
        toNotifyAll(dataQueue);
    }

    public void addFD(FdClass fdClass) {
        fdQueue.offer(fdClass);
        toNotifyAll(fdQueue);
    }

    private void toWaitAll(Object o) {
        synchronized (o) {
            try {
                o.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void toNotifyAll(Object o) {
        synchronized (o) {
            o.notifyAll();
        }
    }

    public class GenerateFile extends Thread {
        FileOutputStream fos = null;
        String fileName = null;

        @Override
        public void run() {
            int length = 0;
            Long fileLength = null;
            try {
                while (true) {
                    DataProtocol dataProtocol = dataQueue.poll();
                    if (dataProtocol == null) {
                        toWaitAll(dataQueue);
                    } else {
                        if (dataProtocol.getPattion() == HEAD) {

                            byte[] fileMessageByte = dataProtocol.getData();
                            String fileMessage = new String(fileMessageByte);

                            Log.d("Tag", fileMessage);
                            Log.d("Tag", ToolUtil.hexStr2Str(fileMessage.split("--")[1].trim()));
                            fileLength = Long.parseLong( fileMessage.split("--")[2]);

                            fileName = ToolUtil.hexStr2Str(fileMessage.split("--")[1].trim());
                            basicActivity.receiveFile(ToolUtil.hexStr2Str(fileMessage.split("--")[1].trim()));

                            File file = new File(basicActivity.getExternalFilesDir("received/" + SettingActivity.currentPC.getHostName()), fileName);

                            fos = new FileOutputStream(file);
                            length = 0;
                        } else if (dataProtocol.getPattion() == BODY) {
                            length += dataProtocol.getData().length;

                            fos.write(dataProtocol.getData(), 0, dataProtocol.getData().length);
                            fos.flush();

                            if(fileLength != null && length >= fileLength){
                                basicActivity.receiveListAdapter.finishTask(basicActivity.updatebarHandler, fileName);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class GenerateMessage extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    FdClass fd = fdQueue.poll();
                    if (fd == null) {
                        toWaitAll(fdQueue);
                    } else {
                        FileInputStream fis = new FileInputStream(fd.getFd().getFileDescriptor());
                        String fileName = ToolUtil.getURLDecoderString(fd.getFilename());
                        //由于是从URI中获取到的文件名，所以需要进行转码
                        String fileMessage = String.format(Locale.CHINA,"Start--%-1003s--%012d", ToolUtil.str2HexStr(fileName), fis.getChannel().size());
                        connectionThread.addMessage(generateProtocol(0, HEAD, fileMessage.getBytes()));

                        byte[] data = new byte[BUFFERLENGTH];
                        int length = 0;
                        long progress = 0;

                        int msgId = 0x00;
                        while ((length = fis.read(data, 0, data.length)) != -1) {
                            connectionThread.addMessage(generateProtocol(msgId, BODY, data));
                            msgId++;
                            progress += length;
                            //TODO 为什么data需要重新new
                            data = null;
                            data = new byte[BUFFERLENGTH];
                        }
                        basicActivity.sendListAdapter.finishTask(basicActivity.updatebarHandler, fileName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public BasicProtocol generateProtocol(int msgId, int Pattern, byte[] data) {
        DataProtocol dataProtocol = new DataProtocol();
        dataProtocol.setReserved(0);
        dataProtocol.setVersion(1);
        dataProtocol.setPattion(Pattern);
        dataProtocol.setDtype(0);
        dataProtocol.setMsgId(msgId);
        dataProtocol.setData(data);
        return dataProtocol;
    }
}
