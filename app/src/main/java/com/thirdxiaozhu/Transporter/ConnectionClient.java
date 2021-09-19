package com.thirdxiaozhu.Transporter;

import Protocal.ClientRequestTask;
import Protocal.DataProtocol;
import Protocal.RequestCallBack;

public class ConnectionClient {

    private boolean isClosed;

    private ConnectionThread connectionThread;

    public ConnectionClient(BasicActivity basicActivity, String IP) {
        connectionThread = new ConnectionThread(basicActivity, IP);
        new Thread(connectionThread).start();
    }

    //public void addNewRequest(DataProtocol data) {
    //    if (connectionThread != null && !isClosed) {
    //        connectionThread.addMessage(data);
    //    }
    //}

    public void addNewFd(FdClass fdClass) {
        if (connectionThread != null && !isClosed) {
            connectionThread.manageFile.addFD(fdClass);
        }
    }

    public void closeConnect() {
        isClosed = true;
        connectionThread.stop();
    }
}
