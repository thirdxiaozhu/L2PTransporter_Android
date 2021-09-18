package com.thirdxiaozhu.Transporter;

import android.os.ParcelFileDescriptor;

public class FdClass {
    private String filename;
    private ParcelFileDescriptor fd;

    public FdClass(String filename, ParcelFileDescriptor fd){
        this.filename = filename;
        this.fd = fd;
    }

    public String getFilename() {
        return filename;
    }

    public ParcelFileDescriptor getFd(){
        return fd;
    }
}
