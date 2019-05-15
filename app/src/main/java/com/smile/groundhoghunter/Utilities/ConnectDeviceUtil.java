package com.smile.groundhoghunter.Utilities;

import android.util.Log;
import com.smile.groundhoghunter.AbstractClasses.IoFunctionThread;
import com.smile.groundhoghunter.Interfaces.ConnectDevice;
import java.net.Socket;
import java.util.ArrayList;

public class ConnectDeviceUtil {

    private static final String TAG = new String(".Utilities.ConnectDeviceUtil");

    public static String getConnectDeviceName(ConnectDevice mDevice) {
        String deviceName = mDevice.getName();
        String deviceHardwareAddress = mDevice.getAddress(); // MAC address
        if (deviceName == null) {
            deviceName = "";
        }
        if (deviceName.isEmpty()) {
            if (deviceHardwareAddress != null) {
                if (!deviceHardwareAddress.isEmpty()) {
                    deviceName = deviceHardwareAddress;
                }
            }
        }

        return deviceName;
    }

    public static void closeConnectIoSocket(Socket mSocket ) {

        if (mSocket != null) {
            // close connection
            try {
                mSocket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void stopIoFunctionThreads(ArrayList<IoFunctionThread> btFunctionThreadList) {
        for (IoFunctionThread ioFunctionThread : btFunctionThreadList) {
            stopIoFunctionThread(ioFunctionThread);
        }
    }

    public static void stopIoFunctionThread(IoFunctionThread ioFunctionThread) {
        if (ioFunctionThread != null) {
            synchronized (ioFunctionThread) {
                ioFunctionThread.setKeepRunning(false);
                ioFunctionThread.closeIoSocket();
                ioFunctionThread.setStartRead(true);
                ioFunctionThread.notify();
            }
            boolean retry = true;
            while (retry) {
                try {
                    ioFunctionThread.join();
                    Log.d(TAG, "ioFunctionThread.Join()........\n");
                    retry = false;
                    ioFunctionThread = null;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
        }
    }
}
