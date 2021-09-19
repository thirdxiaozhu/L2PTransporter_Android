package com.thirdxiaozhu.Transporter;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

public class getDeviceInfo {
    private String deviceName;
    private String deviceIP;
    private String deviceMac;

    public getDeviceInfo(BasicActivity basicActivity){
        NetworkUtil util = new NetworkUtil(basicActivity);
        getDeviceName();
        getDeviceIP(util);
        Log.d("Tag", "IP: " + this.deviceIP);
        getDeviceMac(util);
    }

    private void getDeviceName(){
        this.deviceName = BluetoothAdapter.getDefaultAdapter().getName();
    }

    private void getDeviceIP(NetworkUtil util){
        this.deviceIP = util.getIP();
    }

    private void getDeviceMac(NetworkUtil util){
        this.deviceMac = util.getMac();
    }
}
