package com.donfbecker.telemetryreceiver;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.util.Log;

import java.util.concurrent.ExecutionException;

import java.util.Set;

public class RtlSdr {
    private Context ctx;

    static {
        System.loadLibrary("rtlsdr");
    }

    public RtlSdr(Context ctx) {
        this.ctx = ctx;
    }
    public boolean open() throws ExecutionException, InterruptedException {
        UsbDevice usbDevice = UsbPermissionHelper.findFirstUsbDevice(ctx);
        if(usbDevice == null) {
            Log.d("RtlSdr", "No usb device found.");
            return false;
        }

        UsbDeviceConnection deviceConnection = UsbPermissionObtainer.obtainFdFor(ctx, usbDevice).get();
        if (deviceConnection == null) {
            Log.d("RtlSdr", "Could not get a connection");
            return false;
        }

        int fd = deviceConnection.getFileDescriptor();
        Log.d("RtlSdr", "Opening fd " + fd);

        return true;
    }
    public native int getDeviceCount();
}
