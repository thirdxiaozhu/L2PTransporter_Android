package com.example.myapplication;

public class HostInfo {
    private String hostIP;
    private String hostName;
    private String osName;
    private String osArch;
    private String osVersion;


    public String getHostIP(){
        return this.hostIP;
    }

    public String getHostName(){
        return this.hostName;
    }

    public String getOSName(){
        return this.osName;
    }
    public String getOSArch(){
        return this.osArch;
    }
    public String getOSVersion(){
        return this.osVersion;
    }
}
