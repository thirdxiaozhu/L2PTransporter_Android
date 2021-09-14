package com.thirdxiaozhu.Transporter;

import Protocal.ClientRequestTask;
import Protocal.DataProtocol;
import Protocal.RequestCallBack;

public class ConnectionClient {

    private boolean isClosed;

    private ConnectionThread connectionThread;

    public ConnectionClient(MainActivity mainActivity, String IP) {
        connectionThread = new ConnectionThread(mainActivity, IP);
        new Thread(connectionThread).start();
    }

    public void addNewRequest(DataProtocol data) {
        if (connectionThread != null && !isClosed) {
            connectionThread.addRequest(data);
        }
    }

    public void closeConnect() {
        isClosed = true;
        connectionThread.stop();
    }
}
