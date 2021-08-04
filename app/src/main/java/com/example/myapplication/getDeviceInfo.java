package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;

public class getDeviceInfo {
    private String deviceName;
    private String deviceIP;
    private String deviceMac;

    public getDeviceInfo(MainActivity mainActivity){
        NetworkUtil util = new NetworkUtil(mainActivity);
        getDeviceName();
        getDeviceIP(util);
        getDeviceMac(util);
    }

    private void getDeviceName(){
        this.deviceName = BluetoothAdapter.getDefaultAdapter().getName();
    }

    private void getDeviceIP(NetworkUtil util){
        this.deviceIP = util.getIP();
    }

    private void getDeviceMac(NetworkUtil util){
        this.deviceMac = util.getWifiMac();
    }
}
