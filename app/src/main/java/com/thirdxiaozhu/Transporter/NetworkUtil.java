package com.thirdxiaozhu.Transporter;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class NetworkUtil {

    Context context;
    MainActivity mainActivity;
    private WifiInfo wifiInfo;

    public NetworkUtil(MainActivity mainActivity){
        this.context = mainActivity;
        this.mainActivity = mainActivity;
        wifiInfo = getWIFIInfo();
    }

    /**
     * 获取当前设备IP
     * @return
     */
    public String getIP() {
        String ip = null;

        if ( wifiInfo != null) {
            int ipAddress = wifiInfo.getIpAddress();
            ip = intToIp(ipAddress);
        }else{
            //使用移动数据或者没联网的时候
        }

        return ip;
    }

    /**
     * 解析WIFI IP地址
     * @param i 解析之前的WIFI IP地址
     * @return
     */
    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 获取mac地址
     * @return
     */
    public String getWifiMac(){
        if(wifiInfo != null) {
            try {
                List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface nif : all) {
                    if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            } catch (Exception ignored) {
            }
            return "02:00:00:00:00:00";
        } else{
          return null;
        }
    }

    private WifiInfo getWIFIInfo(){
        ConnectivityManager conMann = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifiNetworkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo;
        }else{
            return null;
        }
    }

}