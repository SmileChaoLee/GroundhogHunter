package com.smile.groundhoghunter.AbstractClasses;

import android.os.Handler;
import android.util.Log;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class IoFunctionThread extends Thread {
    private final String TAG = new String(".AbstractClasses.IoFunctionThread");

    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected Handler mHandler;
    protected String mBuffer;
    protected boolean keepRunning;
    protected boolean startRead;

    private final IoFunctionThread ioFunctionThread;

    public IoFunctionThread(Handler handler) {
        mHandler = handler;
        ioFunctionThread = this;
    }

    protected IoFunctionThread getThisThread() {
        return ioFunctionThread;
    }

    public void write(int headByte, String data) {
        try {
            Log.d(TAG, "Started to write data to the other.");

            int dataLength = data.length();
            byte[] byteWrite = new byte[dataLength + 3];
            byteWrite[0] = (byte)headByte;
            Log.d(TAG, "write()->byteWrite[0] = " + byteWrite[0]);
            byteWrite[1] = (byte)dataLength;
            Log.d(TAG, "write()->byteWrite[1] = " + byteWrite[1]);
            System.arraycopy(data.getBytes(), 0, byteWrite, 2, dataLength);
            byteWrite[byteWrite.length - 1] = '\n';
            Log.d(TAG, "write()->data = " + data);

            outputStream.write(byteWrite);

            Log.d(TAG, "Succeeded to write data to the other.");
        } catch (Exception ex) {
            Log.d(TAG, "Failed to write data.", ex);
        }
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }

    public void setStartRead(boolean startRead) {
        synchronized (ioFunctionThread) {
            ioFunctionThread.startRead = startRead;
            ioFunctionThread.notify();
        }
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    abstract public void closeIoSocket();
}
