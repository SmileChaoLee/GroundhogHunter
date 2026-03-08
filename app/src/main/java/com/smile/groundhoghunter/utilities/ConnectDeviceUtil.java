package com.smile.groundhoghunter.utilities;

import android.util.Log;
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread;
import com.smile.groundhoghunter.interfaces.ConnectDevice;
import java.net.Socket;
import java.util.ArrayList;

public class ConnectDeviceUtil {

    private static final String TAG = "ConnectDeviceUtil";

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
                Log.e(TAG, "closeConnectIoSocket.Exception: ", ex);
            }
        }
    }

    public static void stopIoFunctionThreads(ArrayList<IoFunctionThread> btFunctionThreadList) {
        Log.d(TAG, "stopIoFunctionThreads");
        for (IoFunctionThread ioFunctionThread : btFunctionThreadList) {
            stopIoFunctionThread(ioFunctionThread);
        }
    }

    public static void stopIoFunctionThread(IoFunctionThread ioFunctionThread) {
        Log.d(TAG, "stopIoFunctionThread");
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
                    Log.d(TAG, "stopIoFunctionThread.ioFunctionThread.Join()");
                    retry = false;
                    // ioFunctionThread = null;
                } catch (InterruptedException ex) {
                    Log.e(TAG, "stopIoFunctionThread.InterruptedException: ", ex);
                }
            }
        }
    }
}
