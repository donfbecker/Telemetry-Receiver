/*
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

package com.donfbecker.telemetryreceiver;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Pair;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class UsbPermissionHelper {
	public final static boolean isAndroidUsbSupported;
	
	static {
		isAndroidUsbSupported = isAndroidUsbSupported();
	}
	
	private UsbPermissionHelper() {}

	private static boolean isAndroidUsbSupported() {
		try {
			Class.forName( "android.hardware.usb.UsbManager" );
			return true;
		} catch( ClassNotFoundException e ) {
			return false;
		}
	}

	/** This method is safe to be called from old Android versions */
	public static UsbDevice findFirstUsbDevice(final Context ctx) {
		Set<UsbDevice> usbDevices = new HashSet<>();
		if (isAndroidUsbSupported) {
			final UsbManager manager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
			final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

			for (final Entry<String, UsbDevice> desc : deviceList.entrySet()) {
				UsbDevice candidate = desc.getValue();
				if(candidate.getVendorId() == 0x0bda && candidate.getProductId() == 0x2838) return candidate;
			}
		}
		return null;
	}
}
