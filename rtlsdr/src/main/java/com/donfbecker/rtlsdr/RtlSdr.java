package com.donfbecker.rtlsdr;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import java.util.Set;
import java.util.concurrent.Future;

public class RtlSdr {
    private static Context ctx;
    private static final String ACTION_USB_PERMISSION = "com.donfbecker.telemetryreceiver.USB_PERMISSION";

    public final static boolean isAndroidUsbSupported;

    static {
        System.loadLibrary("rtlsdr");
        isAndroidUsbSupported = isAndroidUsbSupported();
    }

    public static boolean initialize(Context c) {
        ctx = c;
        return true;
    }

    public static int getDeviceCount() {
        List<UsbDevice> devices = getDeviceList();
        if(devices == null) return 0;
        return devices.size();
    }

    public static String getDeviceName(int index) {
        UsbDevice device = getDeviceByIndex(index);
        if(device == null) return null;
        return device.getDeviceName();
    }

    public static String[] getDeviceUsbStrings(int index) {
        UsbDevice device = getDeviceByIndex(index);
        if(device == null) return null;

        return new String[] {
          device.getManufacturerName(),
          device.getProductName(),
          device.getSerialNumber()
        };
    }

    public static int getIndexBySerial(String serialNumber) {
        if(serialNumber == null) return -1;

        List<UsbDevice> devices = getDeviceList();
        if(devices.size() == 0) return -2;

        for(int i = 0; i < devices.size(); i++) {
            UsbDevice device = devices.get(i);
            if(device.getSerialNumber() == serialNumber) return i;
        }
        return -3;
    }

    public static boolean open() throws ExecutionException, InterruptedException {
        UsbDevice usbDevice = getFirstUsbDevice();
        if(usbDevice == null) {
            Log.d("RtlSdr", "No usb device found.");
            return false;
        }

        UsbDeviceConnection deviceConnection = obtainPermissionForDevice(usbDevice).get();
        if (deviceConnection == null) {
            Log.d("RtlSdr", "Could not get a connection");
            return false;
        }

        int fd = deviceConnection.getFileDescriptor();
        Log.d("RtlSdr", "Opening fd " + fd);

        return true;
    }

    public int close(int dev) {
        return 0;
    }

    public static UsbDevice getDeviceByIndex(int index) {
        List<UsbDevice> devices = getDeviceList();
        if(devices == null) return null;
        if(index > devices.size() - 1) return null;
        return devices.get(index);
    }
    public static UsbDevice getFirstUsbDevice() {
        return getDeviceByIndex(0);
    }

    private static List<UsbDevice> getDeviceList() {
        if (isAndroidUsbSupported) {
            List<UsbDevice> devices = new ArrayList<>();
            final UsbManager manager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
            final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

            for (final Map.Entry<String, UsbDevice> desc : deviceList.entrySet()) {
                UsbDevice candidate = desc.getValue();
                if(isSupportedDevice(candidate)) devices.add(candidate);
            }
            return devices;
        }
        return null;
    }

    private static boolean isSupportedDevice(UsbDevice device) {
        if(device.getVendorId() == 0x0bda && device.getProductId() == 0x2832) return true; //Generic RTL2832U
        if(device.getVendorId() == 0x0bda && device.getProductId() == 0x2838) return true; //Generic RTL2832U OEM
        if(device.getVendorId() == 0x0413 && device.getProductId() == 0x6680) return true; //DigitalNow Quad DVB-T PCI-E card
        if(device.getVendorId() == 0x0413 && device.getProductId() == 0x6f0f) return true; //Leadtek WinFast DTV Dongle mini D
        if(device.getVendorId() == 0x0458 && device.getProductId() == 0x707f) return true; //Genius TVGo DVB-T03 USB dongle (Ver. B)
        if(device.getVendorId() == 0x0ccd && device.getProductId() == 0x00a9) return true; //Terratec Cinergy T Stick Black (rev 1)
        if(device.getVendorId() == 0x0ccd && device.getProductId() == 0x00b3) return true; //Terratec NOXON DAB/DAB+ USB dongle (rev 1)
        if(device.getVendorId() == 0x0ccd && device.getProductId() == 0x00b4) return true; //Terratec Deutschlandradio DAB Stick
        if(device.getVendorId() == 0x0ccd && device.getProductId() == 0x00b5) return true; //Terratec NOXON DAB Stick - Radio Energy
        if(device.getVendorId() == 0x0ccd && device.getProductId() == 0x00b7) return true; //Terratec Media Broadcast DAB Stick
        if(device.getVendorId() == 0x0ccd && device.getProductId() == 0x00b8) return true; //Terratec BR DAB Stick
        if(device.getVendorId() == 0x0ccd && device.getProductId() == 0x00b9) return true; //Terratec WDR DAB Stick
        if(device.getVendorId() == 0x0ccd && device.getProductId() == 0x00c0) return true; //Terratec MuellerVerlag DAB Stick
        if(device.getVendorId() == 0x0ccd && device.getProductId() == 0x00c6) return true; //Terratec Fraunhofer DAB Stick
        if(device.getVendorId() == 0x0ccd && device.getProductId() == 0x00d3) return true; //Terratec Cinergy T Stick RC (Rev.3)
        if(device.getVendorId() == 0x0ccd && device.getProductId() == 0x00d7) return true; //Terratec T Stick PLUS
        if(device.getVendorId() == 0x0ccd && device.getProductId() == 0x00e0) return true; //Terratec NOXON DAB/DAB+ USB dongle (rev 2)
        if(device.getVendorId() == 0x1554 && device.getProductId() == 0x5020) return true; //PixelView PV-DT235U(RN)
        if(device.getVendorId() == 0x15f4 && device.getProductId() == 0x0131) return true; //Astrometa DVB-T/DVB-T2
        if(device.getVendorId() == 0x15f4 && device.getProductId() == 0x0133) return true; //HanfTek DAB+FM+DVB-T
        if(device.getVendorId() == 0x185b && device.getProductId() == 0x0620) return true; //Compro Videomate U620F
        if(device.getVendorId() == 0x185b && device.getProductId() == 0x0650) return true; //Compro Videomate U650F
        if(device.getVendorId() == 0x185b && device.getProductId() == 0x0680) return true; //Compro Videomate U680F
        if(device.getVendorId() == 0x1b80 && device.getProductId() == 0xd393) return true; //GIGABYTE GT-U7300
        if(device.getVendorId() == 0x1b80 && device.getProductId() == 0xd394) return true; //DIKOM USB-DVBT HD
        if(device.getVendorId() == 0x1b80 && device.getProductId() == 0xd395) return true; //Peak 102569AGPK
        if(device.getVendorId() == 0x1b80 && device.getProductId() == 0xd397) return true; //KWorld KW-UB450-T USB DVB-T Pico TV
        if(device.getVendorId() == 0x1b80 && device.getProductId() == 0xd398) return true; //Zaapa ZT-MINDVBZP
        if(device.getVendorId() == 0x1b80 && device.getProductId() == 0xd39d) return true; //SVEON STV20 DVB-T USB & FM
        if(device.getVendorId() == 0x1b80 && device.getProductId() == 0xd3a4) return true; //Twintech UT-40
        if(device.getVendorId() == 0x1b80 && device.getProductId() == 0xd3a8) return true; //ASUS U3100MINI_PLUS_V2
        if(device.getVendorId() == 0x1b80 && device.getProductId() == 0xd3af) return true; //SVEON STV27 DVB-T USB & FM
        if(device.getVendorId() == 0x1b80 && device.getProductId() == 0xd3b0) return true; //SVEON STV21 DVB-T USB & FM
        if(device.getVendorId() == 0x1d19 && device.getProductId() == 0x1101) return true; //Dexatek DK DVB-T Dongle (Logilink VG0002A)
        if(device.getVendorId() == 0x1d19 && device.getProductId() == 0x1102) return true; //Dexatek DK DVB-T Dongle (MSI DigiVox mini II V3.0)
        if(device.getVendorId() == 0x1d19 && device.getProductId() == 0x1103) return true; //Dexatek Technology Ltd. DK 5217 DVB-T Dongle
        if(device.getVendorId() == 0x1d19 && device.getProductId() == 0x1104) return true; //MSI DigiVox Micro HD
        if(device.getVendorId() == 0x1f4d && device.getProductId() == 0xa803) return true; //Sweex DVB-T USB
        if(device.getVendorId() == 0x1f4d && device.getProductId() == 0xb803) return true; //GTek T803
        if(device.getVendorId() == 0x1f4d && device.getProductId() == 0xc803) return true; //Lifeview LV5TDeluxe
        if(device.getVendorId() == 0x1f4d && device.getProductId() == 0xd286) return true; //MyGica TD312
        if(device.getVendorId() == 0x1f4d && device.getProductId() == 0xd803) return true; //PROlectrix DV107669
        return false;
    }

    private static boolean isAndroidUsbSupported() {
        try {
            Class.forName( "android.hardware.usb.UsbManager" );
            return true;
        } catch( ClassNotFoundException e ) {
            return false;
        }
    }

    public static Future<UsbDeviceConnection> obtainPermissionForDevice(UsbDevice usbDevice) {
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = PendingIntent.FLAG_MUTABLE;
        }
        UsbManager manager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
        if (!manager.hasPermission(usbDevice)) {
            AsyncFuture<UsbDeviceConnection> task = new AsyncFuture<>();
            registerNewBroadcastReceiver(ctx, usbDevice, task);
            manager.requestPermission(usbDevice, PendingIntent.getBroadcast(ctx, 0, new Intent(ACTION_USB_PERMISSION), flags));
            return task;
        } else {
            return new CompletedFuture<>(manager.openDevice(usbDevice));
        }
    }

    private static void registerNewBroadcastReceiver(final Context ctx, final UsbDevice usbDevice, final AsyncFuture<UsbDeviceConnection> task) {
        ctx.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        if (task.isDone()) {
                            Log.d("USB", "Permission already should be processed, ignoring.");
                            return;
                        }
                        UsbManager manager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (device != null && device.equals(usbDevice)) {
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                if (!manager.hasPermission(device)) {
                                    Log.d("USB", "Permissions were granted but can't access the device");
                                    task.setDone(null);
                                } else {
                                    Log.d("USB", "Permissions granted and device is accessible");
                                    task.setDone(manager.openDevice(device));
                                }
                            } else {
                                Log.d("USB", "Extra permission was not granted");
                                task.setDone(null);
                            }
                            context.unregisterReceiver(this);
                        } else {
                            Log.d("USB", "Got a permission for an unexpected device.");
                            task.setDone(null);
                        }
                    }
                } else {
                    Log.d("USB" , "Unexpected action");
                    task.setDone(null);
                }
            }
        }, new IntentFilter(ACTION_USB_PERMISSION));
    }
}
