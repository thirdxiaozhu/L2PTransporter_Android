package com.thirdxiaozhu.Transporter;

import android.util.Log;

import Protocal.BasicProtocol;
import Protocal.ClientRequestTask;
import Protocal.DataProtocol;
import Protocal.PingProtocol;
import Protocal.RequestCallBack;

public class ConnectionClient {

    private boolean isClosed;

    private ConnectionThread connectionThread;

    public ConnectionClient(BasicActivity basicActivity, String iPwithPort) {
        connectionThread = new ConnectionThread(basicActivity, iPwithPort);
        new Thread(connectionThread).start();
    }


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
