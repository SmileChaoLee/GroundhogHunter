package com.smile.groundhoghunter.abstract_threads;

import android.os.Handler;

public abstract class ClientConnectToThread extends Thread {

    protected Handler mHandler;

    public ClientConnectToThread(Handler handler) {
        mHandler = handler;
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    abstract public IoFunctionThread getIoFunctionThread();
    abstract public void closeClientSocket();
}
