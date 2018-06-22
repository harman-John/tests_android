/*
 * USB.java
 * internal
 *
 * Created by Brian Doyle on 1/17/16
 * Copyright (c) 2016 Balance Software, Inc.
 *
 */
package com.avnera.smartdigitalheadset;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;

import java.util.HashSet;
import java.util.Iterator;

public class USB {
	private final Activity						mActivity;
	private final ConcreteBroadcastReceiver		mBroadcastReceiver;
	private UsbDeviceConnection					mConnection;
	private final Delegate						mDelegate;
	private UsbInterface						mInterface;
	private final UsbManager					mUsbManager;

	private static final int					kInterfaceClassHID = 3;

	// TODO: currently only one device is supported, this could be expanded
	// to support multiple vendor and product ids
	public static final HashSet<Integer>		sUSBVendorIds = new HashSet();
	public static final HashSet<Integer>		sUSBProductIds = new HashSet();

	/**
	 * Instantiate a USB object for communication between an Android device and a USB accessory.
	 * After instantiation, set add the USB accessory's vendor and product ids to sUSBVendorIds
	 * and sUSBProductIds respectively, then call start().
	 *
	 * @param delegate The receiver of USB.Delegate messages.  This parameter cannot be null.
	 * @param activity The Android Activity to use for event registration.  This parameter cannot be null.
	 */
	public USB(Activity activity, Delegate delegate) {
		if (activity == null) throw new IllegalArgumentException("activity cannot be null");
		if (delegate == null) throw new IllegalArgumentException("delegate cannot be null");

		mActivity = activity;
		mDelegate = delegate;
		mBroadcastReceiver = new ConcreteBroadcastReceiver();
		mUsbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
	}

	/**
	 * The USB.Delegate interface defines methods that will be called from a USB instance
	 * instance to inform an interested object (typically the entity that creates the
	 * USB class) of USB events that relate to the USB device whose vendor and product ids
	 * match those registered with the USB instance.
	 */
	public interface Delegate {
		/**
		 * usbDeviceConnected() is called when a USB device whose vendor and product ids
		 * match those registered with the USB instance is connected.  The USBSocket
		 * object is ready for bidirectional communication and should be passed to a
		 * LightX instance.
		 *
		 * @param usb The instance of USB class that is delivering the usbDeviceConnected message
		 * @param device The UsbDevice that is connected
		 * @param socket A bidirectional socket for communication with the USB device.
		 */
		void usbDeviceConnected( USB usb, UsbDevice device, USBSocket socket );

		/**
		 * usbDeviceDisconnected() is called when a USB device whose vendor and product ids
		 * match those registered with the USB instance is disconnected.  All objects associated
		 * with this device (such as an open USBSocket and/or LightX instance) should be closed.
		 *
		 * @param usb The instance of USB class that is delivering the usbDeviceConnected message
		 * @param device The UsbDevice that is connected
		 */
		void usbDeviceDisconnected( USB usb, UsbDevice device );
	}

	/**
	 * Closes the USB instance. This releases the connected UsbDevice if any.
	 */
	public synchronized void close() {

		if (mConnection != null) {
			mConnection.releaseInterface( mInterface );
			mConnection.close();

			mConnection = null;
			mInterface = null;
		}
	}


	/**
	 * This unregisters the broadcast listener that detects
	 * connect and disconnect events
	 */
	public synchronized void stop() {
		mActivity.unregisterReceiver( mBroadcastReceiver );
	}

//	public void reportDeviceInfo( UsbDevice device ) {
//		String s = "";
//
//		s += "USB device attached:\n";
//		s += "deviceClass: " + device.getDeviceClass() + "\n";
//		s += "deviceSubclass: " + device.getDeviceSubclass() + "\n";
//		s += "deviceId: " + device.getDeviceId() + "\n";
//		s += "deviceName: " + device.getDeviceName() + "\n";
//		s += "deviceProtocol: " + device.getDeviceProtocol() + "\n";
//		if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP) { // these methods are only available on API 21+
//			s += "deviceManufacturer: " + device.getManufacturerName() + "\n";
//			s += "deviceProductName: " + device.getProductName() + "\n";
//			s += "deviceSerialNumber: " + device.getSerialNumber() + "\n";
//		}
//		s += "deviceProductId: " + device.getProductId() + "\n";
//		s += "deviceVendorId: " + device.getVendorId() + "\n";
//		s += "deviceInterfaceCount: " + device.getInterfaceCount();
//
//		Log.d( s );
//	}

	/*
	 * deviceAttached() is to be called when a new device is discovered.
	 *
	 * TODO: @Cal - Currently this is called both by the broadcast receiver
	 * in this class and an intent receiver in MainActivity's onResume().
	 * I am not sure if both are required.  Ideally, both this method and
	 * deviceDetached() would be private methods that are called by the
	 * ConcreteBroadcastReceiver class below.
	 */
	public void deviceAttached(UsbDevice device) {
//		reportDeviceInfo( device );

		int productId = device.getProductId();
		int vendorId = device.getVendorId();

		if ( sUSBVendorIds.contains( vendorId ) && sUSBProductIds.contains( productId ) ) {
			close();

			Log.d( "Found USB LightX device " + vendorId + ":" + productId );

//			enumerateInterfaces( device );

			// if we get this far we have permission, see device_list.xml
			UsbInterface	hidInterface = findHIDInterface( device );

			if ( hidInterface != null ) {
//				Log.d( "Found HID interface:\n" + describeInterface( hidInterface ));

				UsbDeviceConnection		connection;

				if ((connection = mUsbManager.openDevice(device)) != null) {
					if (connection.claimInterface( hidInterface, true )) {
						if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP)
							Log.d( "Successfully claimed HID interface \"" + hidInterface.getName() + "\"" );

						try {
							USBSocket socket = new USBSocket( connection, hidInterface);

							synchronized( this ) {
								mConnection = connection;
								mInterface = hidInterface;
							}
							mConnection.releaseInterface(hidInterface);
							mDelegate.usbDeviceConnected( this, device, socket );
						} catch ( Exception e ) {
							Log.e( "failed to start usb socket: " + e.getLocalizedMessage() );
							close();
						}
					} else {
						if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP)
							Log.e( "Failed to claim HID interface \"" + hidInterface.getName() + "\"" );
					}
				}
			}
		}
	}

	/*
		Called when a device is detached.
	 */
	protected void deviceDetached(UsbDevice device) {
		mDelegate.usbDeviceDisconnected( this, device );
	}

	protected UsbInterface findHIDInterface(UsbDevice device) {
		int						i, n;

		for ( i = 0, n = device.getInterfaceCount(); i < n; ++i ) {
			UsbInterface		usbInterface = device.getInterface( i );

			if ( usbInterface.getInterfaceClass() == kInterfaceClassHID ) {
				return usbInterface;
			}
		}

		return null;
	}

//	protected void enumerateInterfaces(UsbDevice device) {
//		int						i, n;
//		String					s = "";
//
//		for ( i = 0, n = device.getInterfaceCount(); i < n; ++i ) {
//			UsbInterface		usbInterface = device.getInterface( i );
//
//			s += describeInterface( usbInterface );
//		}
//
//		Log.d(s);
//	}

//	protected String describeInterface(UsbInterface usbInterface) {
//		int						i, n;
//		String					s = "";
//
//		s += "interface " + usbInterface.getId() + ":" + usbInterface.getAlternateSetting() + " (" + usbInterface.getName() + ")\n";
//		s += "  class: " + usbInterface.getInterfaceClass() + "\n";
//		s += "  subclass: " + usbInterface.getInterfaceSubclass() + "\n";
//		s += "  protocol: " + usbInterface.getInterfaceProtocol() + "\n";
//		s += "  " + usbInterface.getEndpointCount() + " endpoint(s):" + "\n";
//
//		for ( i = 0, n = usbInterface.getEndpointCount(); i < n; ++i ) {
//			UsbEndpoint		usbEndpoint = usbInterface.getEndpoint( i );
//
//			s += describeEndpoint( usbEndpoint );
//		}
//
//		return s;
//	}
//
//	protected String describeEndpoint(UsbEndpoint usbEndpoint) {
//		String					s = "";
//
//		s += "  " + "endpoint " + usbEndpoint.getEndpointNumber() + ":\n";
//		s += "    " + "address: " + usbEndpoint.getAddress() + "\n";
//		s += "    " + "attributes: " + usbEndpoint.getAttributes() + "\n";
//		s += "    " + "direction: " + usbEndpoint.getDirection() + "\n";
//		s += "    " + "interval: " + usbEndpoint.getInterval() + "\n";
//		s += "    " + "max packet size: " + usbEndpoint.getMaxPacketSize() + "\n";
//		s += "    " + "type: " + usbEndpoint.getType() + "\n";
//
//		return s;
//	}

	// bulk interface
//	protected void useDevice(UsbDevice device) {
//		UsbDeviceConnection		connection;
//		UsbEndpoint				endpointIn = null, endpointOut = null;
//		int						i, j, n;
//		UsbInterface			usbInterface;
//
//		for (i = 0, n = device.getInterfaceCount(); i < n; ++i) {
//			usbInterface = device.getInterface( i );
//
//			if ( usbInterface.getEndpointCount() != 2 ) continue;
//
//			for ( j = 0; j < 2; ++j ) {
//				UsbEndpoint endpoint = usbInterface.getEndpoint( j );
//
//				if ( endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK ) {
//					switch ( endpoint.getDirection() ) {
//						case UsbConstants.USB_DIR_IN:
//							endpointIn = endpoint;
//							break;
//						case UsbConstants.USB_DIR_OUT:
//							endpointOut = endpoint;
//							break;
//					}
//				}
//			}
//
//			if ( endpointIn != null && endpointOut != null ) {
//				if ((connection = mUsbManager.openDevice(device)) != null) {
//					if (connection.claimInterface( usbInterface, true )) {
//						USBSocket socket = new USBSocket(connection, endpointIn, endpointOut);
//
//						mDelegate.usbDeviceConnected( this, device, socket );
//
//						break;
//					} else {
//						Log.e("Failed to claim interface \"" + usbInterface.getName() + "\"");
//					}
//				}
//			}
//
//			endpointIn = null;
//			endpointOut = null;
//		}
//
//		if (mConnection == null) {
//			Log.e("Failed to setup USB device connection");
//		}
//	}

	protected void scanDevices() {
		Iterator<UsbDevice> iterator = mUsbManager.getDeviceList().values().iterator();

		while (iterator.hasNext()) {
			deviceAttached(iterator.next());
		}
	}

	/**
	 * Call start after setting the vendor and product ids and any time after close() is called.
	 */
	public void start() {
		IntentFilter			filter;

		filter = new IntentFilter();

		filter.addAction( UsbManager.ACTION_USB_DEVICE_ATTACHED );
		filter.addAction( UsbManager.ACTION_USB_DEVICE_DETACHED );

		mActivity.registerReceiver( mBroadcastReceiver, filter );

		scanDevices();
	}

	private class ConcreteBroadcastReceiver extends BroadcastReceiver {
		public void onReceive( Context context, Intent intent ) {
			switch ( intent.getAction() ) {
				case UsbManager.ACTION_USB_DEVICE_ATTACHED: {
					Log.d("UsbManager.ACTION_USB_DEVICE_ATTACHED received");

					UsbDevice		device = intent.getParcelableExtra( UsbManager.EXTRA_DEVICE );

					deviceAttached( device );
				} break;

				case UsbManager.ACTION_USB_DEVICE_DETACHED: {
					Log.d("UsbManager.ACTION_USB_DEVICE_DETACHED received");

					UsbDevice		device = intent.getParcelableExtra( UsbManager.EXTRA_DEVICE );

					deviceDetached( device );
				} break;
			}
		}
	}
}
