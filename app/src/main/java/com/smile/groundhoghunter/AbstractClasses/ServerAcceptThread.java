package com.smile.groundhoghunter.AbstractClasses;

import android.os.Handler;

import com.smile.groundhoghunter.Interfaces.ConnectDevice;

public abstract class ServerAcceptThread extends Thread {

    protected Handler mHandler;
    protected boolean keepRunning;

    public ServerAcceptThread(Handler handler) {
        mHandler = handler;
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    abstract public IoFunctionThread getIoFunctionThread(ConnectDevice mDevice);
    abstract public void closeServerSocket();
}
