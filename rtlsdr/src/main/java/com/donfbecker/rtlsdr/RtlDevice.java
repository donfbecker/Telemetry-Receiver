/*
 * Copyright (C) 2013 by Robert Schoch <r.schoch@t-online.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RtlDevice {
  private static Context ctx;
  private static final String ACTION_USB_PERMISSION = "com.donfbecker.telemetryreceiver.USB_PERMISSION";
  public final static boolean isAndroidUsbSupported;

  public final static int TUNER_UNKNOWN = 0;
  public final static int TUNER_E4000 = 1;
  public final static int TUNER_FC0012 = 2;
  public final static int TUNER_FC0013 = 3;
  public final static int TUNER_FC2580 = 4;
  public final static int TUNER_R820T = 5;

  public static final Map<Integer, String> TUNER_NAMES = new HashMap<Integer, String>();

  static {
    System.loadLibrary("rtlsdr");

    isAndroidUsbSupported = isAndroidUsbSupported();

    TUNER_NAMES.put(TUNER_UNKNOWN, "unknown");
    TUNER_NAMES.put(TUNER_E4000, "Elonics E4000");
    TUNER_NAMES.put(TUNER_FC0012, "Fiticomm FC0012");
    TUNER_NAMES.put(TUNER_FC0013, "Fiticomm FC0013");
    TUNER_NAMES.put(TUNER_FC2580, "SiliconMotion FC2580");
    TUNER_NAMES.put(TUNER_R820T, "Rafael Micro R820T");
  }

  private final int devIndex;
  private long devHandle;

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

  public static UsbStrings getDeviceUsbStrings(int index) {
    UsbDevice device = getDeviceByIndex(index);
    if(device == null) return null;

    return new UsbStrings(
            device.getManufacturerName(),
            device.getProductName(),
            device.getSerialNumber()
    );
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

  public RtlDevice(int devIndex) {
    this.devIndex = devIndex;
    this.devHandle = 0L;
  }

  public void open() throws ExecutionException, InterruptedException {
    UsbDevice usbDevice = getDeviceByIndex(devIndex);
    if(usbDevice == null) {
      Log.d("RtlSdr", "No usb device found.");
      return;
    }

    UsbDeviceConnection deviceConnection = obtainPermissionForDevice(usbDevice).get();
    if (deviceConnection == null) {
      Log.d("RtlSdr", "Could not get a connection");
      return;
    }

    int fd = deviceConnection.getFileDescriptor();
    Log.d("RtlSdr", "Opening fd " + fd);

    openDevice(fd);
  }

  private native void openDevice(int fd);

  public native void close();

  public native void setXtalFreq(XtalFreq freq);
  public native XtalFreq getXtalFreq();
  public native UsbStrings getUsbStrings();

  public native void writeEeprom(byte[] data, int offset, int len);
  public native void readEeprom(byte[] data, int offset, int len);

  public native void setCenterFreq(long freq);
  public native long getCenterFreq();

  public native void setFreqCorrection(int ppm);
  public native int getFreqCorrection();

  public native int getTunerType();
  public native int[] getTunerGains();

  /**
   * Set the gain for the device.
   * Manual gain mode must be enabled for this to work.
   * Valid gain values (in tenths of a dB) for the E4000 tuner:
   * -10, 15, 40, 65, 90, 115, 140, 165, 190,
   * 215, 240, 290, 340, 420, 430, 450, 470, 490.
   * Valid gain values may be queried with rtlsdr_get_tuner_gains function.
   * @param gain in tenths of a dB, 115 means 11.5 dB.
   * @throws RtlException on error
   */
  public native void setTunerGain(int gain);

  /**
   * Get actual gain the device is configured to.
   * @return gain in tenths of a dB, 115 means 11.5 dB.
   * @throws RtlException on error
   */
  public native int getTunerGain();

  /**
   * Set the intermediate frequency gain for the device.
   * @param stage intermediate frequency gain stage number (1 to 6 for E4000)
   * @param gain in tenths of a dB, -30 means -3.0 dB
   * @throws RtlException on error
   */
  public native void setTunerIfGain(int stage, int gain);

  /**
   * Set the gain mode (automatic/manual) for the device.
   * Manual gain mode must be enabled for the gain setter function to work.
   * @param manual gain mode, 1 means manual gain mode shall be enabled.
   * @throws RtlException on error
   */
  public native void setTunerGainMode(int manual);

  public native void setSampleRate(int rate);
  public native int getSampleRate();

  public native void setTestMode(int on);
  public native void setAgcMode(int on);

  public native void setDirectSampling(int on);
  public native int getDirectSampling();

  public native void setOffsetTuning(int on);
  public native int getOffsetTuning();

  public native void resetBuffer();
  public native int readSync(ByteBuffer buf, int len);
  public native void readAsync(RtlCallback cb, int numBuffers, int bufferLen);
  public native void cancelAsync();

  /*
   * End API
   */

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

  @Override

  public String toString() {
    return "RtlDevice[devIndex=" + devIndex +
        ", devHandle=" + devHandle + "]"; 
  }

}
