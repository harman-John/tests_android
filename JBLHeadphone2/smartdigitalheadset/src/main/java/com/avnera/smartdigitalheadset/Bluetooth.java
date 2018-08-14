/*
 * Bluetooth.java
 * Smart Digital Headset
 *
 * Created by Brian Doyle on 6/26/15
 * Copyright (c) 2015 Avnera Corporation
 *
 */
package com.avnera.smartdigitalheadset;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The Bluetooth class facilitates communication between an Android device and a Bluetooth accessory.
 *
 * @author Brian Doyle on 6/26/15.
 */
public final class Bluetooth implements BluetoothProfile.ServiceListener {

	private static final String TAG = Bluetooth.class.getSimpleName()+".lightX";
	private BluetoothA2dp												mA2DPProxy = null;
	private final Activity												mActivity;
	private	final BluetoothAdapter										mBluetoothAdapter;
	private final ConcreteBroadcastReceiver								mBroadcastReceiver;
	private final Delegate												mDelegate;
	private final HashMap<BluetoothDevice, HashMap<String, Object>>		mDeviceMap;
	private BluetoothHeadset											mHeadsetProxy = null;
	private NotificationThread											mNotificationThread;
	private final boolean												mSecureRfcomm;

	// https://www.bluetooth.org/en-us/specification/assigned-numbers/service-discovery
//	public static final UUID					kBluetoothServiceA2DP_AudioSink			= UUID.fromString( "0000110B-0000-1000-8000-00805F9B34FB" );
//	public static final UUID					kBluetoothServiceAVRCP_AVRemoteControl	= UUID.fromString( "0000110E-0000-1000-8000-00805F9B34FB" );

	/**
	 * The Bluetooth Serial Port Profile (SPP) UUID
	 */
	public static final UUID					kBluetoothServiceSPP					= UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );
//	public static final UUID					kBluetoothServiceHFP					= UUID.fromString( "0000111E-0000-1000-8000-00805F9B34FB" );
//	public static final UUID					kBluetoothServiceAvneraCustom0			= UUID.fromString( "00000000-DECA-FADE-DECA-DEAFDECACAFF" );

	/**
	 * The integer value passed to startActivityForResult() when enableBluetoothAdapter() is called.
	 * You listen for this value in your onActivityResult() method (see Android documentation for
	 * startActivityForResult( Intent, int ) and Avnera's sample code in MainActivity.java.
	 */
	public static final int						REQUEST_ENABLE_BT = 8191;				// arbitrary but < 16 bits

	private static final String					keyAction = "action";
	private static final String					keyDevice = "device";
	private static final String					keyException = "exception";
	private static final String					keyPreviousState = "previousState";
	private static final String					keyShouldConnect = "shouldConnect";
	private static final String					keySocket = "socket";
	private static final String					keyState = "state";
	private static final String					keyType = "type";

	/**
	 * Instantiate a Bluetooth object for communication between an Android device and a Bluetooth accessory.
	 * After instantiation, call start() to begin searching for devices.
	 *
	 * @param delegate The receiver of Bluetooth.Delegate messages.  This parameter cannot be null.
	 * @param activity The Android Activity to use for event registration.  This parameter cannot be null.
	 * @param secureRfcomm true if the device supports an encrypted link key (Bluetooth 2.1+ devices), false otherwise
	 * @throws IOException Thrown if BluetoothAdapter.getDefaultAdapter() fails (in which case the device does not support Bluetooth).
	 */
	public Bluetooth( Delegate delegate, Activity activity, boolean secureRfcomm ) throws IOException {
		if ( delegate == null ) throw new IllegalArgumentException( "delegate cannot be null" );
		if ( activity == null ) throw new IllegalArgumentException( "activity cannot be null" );
		if ( ( mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ) == null ) {
			throw new IOException( "No bluetooth adapter detected " );
		}
		mActivity = activity;
		mBroadcastReceiver = new ConcreteBroadcastReceiver();
		mDelegate = delegate;
		mDeviceMap = new HashMap<>();
		mSecureRfcomm = secureRfcomm;

		mBluetoothAdapter.getProfileProxy( activity, this, BluetoothProfile.A2DP );
		mBluetoothAdapter.getProfileProxy( activity, this, BluetoothProfile.HEADSET );
	}

	/**
	 * The Bluetooth.Delegate interface defines methods that will be called from a Bluetooth
	 * instance to inform an interested object (typically the entity that creates the
	 * Bluetooth class) of changes to the Bluetooth instance's state.
	 *
	 * Note:  All delegate methods are called from an ad-hoc, non-main thread.
	 */
	public interface Delegate {
		/**
		 * A Bluetooth instance calls bluetoothAdapterChangedState() to notify its delegate that
		 * a BluetoothAdapter.ACTION_STATE_CHANGED message has been received.  See the documentation
		 * for BluetoothAdapter.ACTION_STATE_CHANGED and BluetoothAdapter.EXTRA_STATE for more
		 * information on the types of values that can be passed to this function.
		 *
		 * @param bluetooth The Bluetooth instance calling bluetoothAdapterChangedState()
		 * @param currentState The value of the EXTRA_STATE field (e.g. STATE_OFF, etc.)
		 * @param previousState  The value of the EXTRA_PREVIOUS_STATE field (e.g. STATE_OFF, etc.)
		 */
		void bluetoothAdapterChangedState( Bluetooth bluetooth, int currentState, int previousState );

		/**
		 * A Bluetooth instance calls bluetoothDeviceBondStateChanged() when a BluetoothDevice.ACTION_BOND_STATE_CHANGED
		 * message is received.  When currentState equals BluetoothDevice.BOND_BONDED the peripheral device
		 * is paired and connect() can be called.
		 *
		 * @param bluetooth The Bluetooth instance calling bluetoothDeviceBondStateChanged()
		 * @param device The BluetoothDevice that for which the state is being returned.
		 * @param currentState The BluetoothDevice's current bond state
		 * @param previousState The BluetoothDevice's previous bond state
		 */
		void bluetoothDeviceBondStateChanged( Bluetooth bluetooth, BluetoothDevice device, int currentState, int previousState );

		/**
		 * A Bluetooth instance calls bluetoothDeviceConnected() when a BluetoothDevice is physically
		 * connected.  The socket should be used to create a BluetoothSocketWrapper for a LightX instance.
		 *
		 * @param bluetooth The Bluetooth instance calling bluetoothDeviceConnected()
		 * @param device The BluetoothDevice that is connected
		 * @param socket The connected socket. Create a BluetoothSocketWrapper with this socket and hand
		 *               it off to a new LightX instance.
		 */
		void bluetoothDeviceConnected( Bluetooth bluetooth, BluetoothDevice device, BluetoothSocket socket );

		/**
		 * A Bluetooth instance calls bluetoothDeviceDisconnected() when a BluetoothDevice is disconnected.
		 * A device may disconnected actively or passively.  An active disconnect is when you explicitly
		 * call disconnect() on a connected device.  A passive disconnect occurs when the remote side
		 * disconnects for some reason (for example power-down, or the device goes out of range).  When you
		 * actively disconnect, the device will remain disconnected until you call connect() again.  When
		 * a passive disconnect happens however, the device will be automatically reconnected when/if it
		 * becomes available again and an ACL connect is delivered.  If you do not wish for an ACL connect
		 * event to reconnect automatically, call disconnect() explicitly.  If you wish to try to actively
		 * reconnect, rather than waiting for an ACL connect, call connect().
		 *
		 * @param bluetooth The Bluetooth instance calling bluetoothDeviceDisconnected()
		 * @param device The BluetoothDevice that is disconnected
		 */
		void bluetoothDeviceDisconnected( Bluetooth bluetooth, BluetoothDevice device );

		/**
		 * A Bluetooth instance calls bluetoothDeviceDiscovered() when the Bluetooth instance has
		 * been put into discovery mode and a new Bluetooth device is discovered.
		 *
		 * @param bluetooth The Bluetooth instance calling bluetoothDeviceDiscovered()
		 * @param device The BluetoothDevice that was discovered
		 */
		void bluetoothDeviceDiscovered( Bluetooth bluetooth, BluetoothDevice device );

		/**
		 * If a failure to connect occurs, bluetoothDeviceFailedToConnect() will be called with an
		 * exception indicating why the connection attempt failed.  When this callback is called, the
		 * Bluetooth instance will no longer actively attempt to connect to the peer device, but it
		 * will initiate a connection if a low-level ACL connect event is received.  If you wish to
		 * disable this behavior you must explicitly call disconnect() on the Bluetooth device.
		 *
		 * @param bluetooth The Bluetooth instance calling bluetoothDeviceFailedToConnect()
		 * @param device The BluetoothDevice that is connected
		 * @param exception The reason connect() failed
		 */
		void bluetoothDeviceFailedToConnect( Bluetooth bluetooth, BluetoothDevice device, Exception exception );
	}

	private void bluetoothAdapterEnabled() {
		discoverBluetoothDevices();
	}

	/**
	 * Returns a string describing the passed bondState.  This method is primarily useful for debugging/logging.
	 *
	 * @param bondState one of the BluetoothDevice.BOND_* constants (e.g. BOND_BONDED)
	 * @return A string describing the bond state
	 */
	static public String bondStateDescription( int bondState ) {
		String				result;

		switch ( bondState ) {
			case BluetoothDevice.BOND_BONDED:		result = "BOND_BONDED";			break;
			case BluetoothDevice.BOND_BONDING:		result = "BOND_BONDING";		break;
			case BluetoothDevice.BOND_NONE:			result = "BOND_NONE";			break;

			default:								result = "UNKNOWN";				break;
		}

		return result;
	}

	/**
	 * Cancels Bluetooth discovery mode.  Automatically called when you call connect().
	 */
	public void cancelDiscovery() {
		mBluetoothAdapter.cancelDiscovery();
	}

	/**
	 * Call close() to disconnect all connected devices and shutdown this instance.
 	 */
	public void close() {
		List<BluetoothDevice>		devices = new ArrayList();
		NotificationThread			notificationThread;

		mActivity.unregisterReceiver( mBroadcastReceiver );

		synchronized( this ) {
			notificationThread = mNotificationThread;
			mNotificationThread = null;
		}

		notificationThread.close();

		synchronized( this ) {
			for ( Map.Entry<BluetoothDevice, HashMap<String, Object>> entry : mDeviceMap.entrySet() ) {
				devices.add( entry.getKey() );
			}
		}

		for ( BluetoothDevice device : devices ) {
			disconnect( device );
		}
		Logger.d(TAG,"close profile proxy");
		mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP,mA2DPProxy);
		mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET,mHeadsetProxy);
	}

	private BluetoothSocket createRfcommSocketToSPP( BluetoothDevice device ) throws IOException {
		BluetoothSocket				socket;

		if ( mSecureRfcomm ) {
			socket = device.createRfcommSocketToServiceRecord( kBluetoothServiceSPP );
		} else {
			socket = device.createInsecureRfcommSocketToServiceRecord( kBluetoothServiceSPP );
		}

		return socket;
	}

	/**
	 * Connect to a Bluetooth device.  Only devices reported via the delegate's bluetoothDeviceDiscovered()
	 * callback are valid. Calling connect will cancel any in-progress Bluetooth
	 * device discovery.
	 *
	 * @param device The Bluetooth device to connect to
	 * @throws IOException Thrown if the device does not support SPP or if createRfcommSocketToServiceRecord() fails
	 */
	public void connect( BluetoothDevice device ) throws IOException {
		HashMap<String, Object>		map;
		String						name = deviceName( device );

		Log.d( "Bluetooth.connect() called for " + name );

		cancelDiscovery();

		synchronized( this ) {
			for ( Map.Entry<BluetoothDevice, HashMap<String, Object>> entry : mDeviceMap.entrySet() ) {
				if ( ! entry.getKey().equals( device ) ) continue;

				Log.w( "Adding " + name + " to logical-connect list" );

				map = entry.getValue();
				map.put( keyShouldConnect, true );

				break;
			}
		}

		createConnectThread( device );
	}

	private void createConnectThread( BluetoothDevice device ) {
		HashMap<String, Object>		map;

		if ( device == null ) {
			synchronized( this ) {
				for ( Map.Entry<BluetoothDevice, HashMap<String, Object>> entry : mDeviceMap.entrySet() ) {
					map = entry.getValue();

					if ( map.containsKey( keyShouldConnect ) ) {
						createConnectThread( (BluetoothDevice) entry.getKey() );
					}
				}
			}

			return;
		}

		registerDevice( device );

		boolean						create = false;
		String						name = deviceName( device );
		BluetoothSocket				socket;

		synchronized( this ) {
			for ( Map.Entry<BluetoothDevice, HashMap<String, Object>> entry : mDeviceMap.entrySet() ) {
				if ( !entry.getKey().equals( device ) ) continue;

				map = entry.getValue();

				if ( !map.containsKey( keyShouldConnect ) ) break;
				if ( ( socket = (BluetoothSocket) map.get( keySocket ) ) != null && socket.isConnected() )
					break;

				create = true;

				break;
			}
		}

		if ( create ) {
			Log.d( "Creating connect thread for " + name );

			new ConnectThread( device ).start();
		}
	}

	/**
	 * Disconnect the connected Bluetooth device
	 * @param device The Bluetooth device to disconnect
	 */
	public synchronized void disconnect( BluetoothDevice device ) {
		boolean						notify = false;
		HashMap<String, Object>		map;
		BluetoothSocket				socket = null;

		Log.d( "Bluetooth.disconnect() called." );

		for ( Map.Entry<BluetoothDevice, HashMap<String, Object>> entry : mDeviceMap.entrySet() ) {
			if ( ! entry.getKey().equals( device ) ) continue;

			Log.w( "Removing " + deviceName( device ) + " from logical-connect list" );

			map = entry.getValue();
			socket = (BluetoothSocket) map.get( keySocket );
			map.remove( keySocket );
			map.remove( keyShouldConnect );
			notify = true;

			Log.d( "Bluetooth.disconnect() socket " + socket + " found for " + deviceName( device ) );

			break;
		}

		if ( notify ) notifyDelegate( DelegateCallbackType.Disconnected, device );

		if ( socket != null ) {
			// physically disconnect.  This will abort connect() on the ConnectThread.
			try {
				Log.d( "Bluetooth.disconnect() closing socket." );
				socket.close();
			} catch ( IOException e ) { }
		}
	}

	/**
	 * Get the Bluetooth device's name
	 *
	 * @param device The Bluetooth device
	 * @return The name of the Bluetooth device
	 */
	public String deviceName( BluetoothDevice device ) {
		String				name = device.getName();

		if ( name == null ) name = device.getAddress();

		return name;
	}

	/**
	 * Check to see if the specified device supports a particular Bluetooth profile.
	 *
	 * @param device The Bluetooth device to query for profile support
	 * @param bluetoothProfileUUID A Bluetooth profile UUID
	 * @return true if the device supports the Bluetooth profile, false otherwise.
	 */
	public boolean deviceSupportsProfile( BluetoothDevice device, UUID bluetoothProfileUUID ) {
		try {
			ParcelUuid[]			uuids = device.getUuids();

			if ( uuids != null ) {
				for ( ParcelUuid parcel : uuids ) {
					if ( parcel.getUuid().equals( bluetoothProfileUUID ) ) return true;
				}
			}
		} catch ( Exception e ) {
			Log.e( deviceName( device ) + ".getUuids() failed: " + e.getLocalizedMessage() );
		}

		return false;
	}

	/**
	 * Puts the Android device's BluetoothAdapter in discovery mode.  This is a high energy-consumption
	 * mode and requires the Bluetooth peripheral device to be discoverable.
	 */
	public void discoverBluetoothDevices() {
		mBluetoothAdapter.startDiscovery();

		for ( BluetoothDevice device : mBluetoothAdapter.getBondedDevices() ) {
			notifyDelegate( DelegateCallbackType.ActionBondStateChanged, device, BluetoothDevice.BOND_BONDED, BluetoothDevice.BOND_NONE );
		}
	}

	/**
	 * Returns true if the BluetoothAdapter is in discovery mode.
	 * @return true if the BluetoothAdapter is in discovery mode, false otherwise
	 */
	public boolean isDiscovering() {
		return mBluetoothAdapter.isDiscovering();
	}

	/**
	 * For debugging
	 *
	 * @param device The Bluetooth device
	 */
	private void logDeviceUUIDs( BluetoothDevice device ) {
		String				name;
		int					i, n;
		ParcelUuid[]		uuids;

		name = deviceName( device );

		try {
			uuids = device.getUuids();

			if ( ( n = uuids.length ) == 0 ) {
				Log.d( name + ".getUuids() returned 0 uuids" );
			} else {
				i = 0;

				do {
					Log.d( name + ".getUuids()[ " + i + " ]: " + uuids[ i ].getUuid().toString() );
				} while ( ++i < n );
			}
		} catch ( Exception e ) {
			Log.e( name + ".getUuids() failed: " + e.getLocalizedMessage() );
		}
	}

	// BluetoothProfile.ServiceListener Interface

	public void onServiceConnected( int profile, BluetoothProfile proxy ) {
		synchronized( this ) {
			switch ( profile ) {
				case BluetoothProfile.A2DP: {
					if ( ( mA2DPProxy = (BluetoothA2dp) proxy ) != null ) {
						Log.d( "Bluetooth A2DP proxy acquired" );

						notifyDelegate( DelegateCallbackType.ActionACLConnected );
					}
				} break;

				case BluetoothProfile.HEADSET: {
					if ( ( mHeadsetProxy = (BluetoothHeadset) proxy ) != null ) {
						Log.d( "Bluetooth Headset proxy acquired" );

						notifyDelegate( DelegateCallbackType.ActionACLConnected );
					}
				} break;

				default: break;
			}
		}
	}

	public void onServiceDisconnected( int profile ) {
		synchronized( this ) {
			switch ( profile ) {
				case BluetoothProfile.A2DP: {
					mA2DPProxy = null;

					Log.d( "Bluetooth A2DP proxy released" );
				} break;

				case BluetoothProfile.HEADSET: {
					mHeadsetProxy = null;

					Log.d( "Bluetooth Headset proxy released" );
				} break;
			}
		}
	}

	private void registerDevice( BluetoothDevice device ) {
		boolean						notify = false;

		synchronized( this ) {
			if ( ! mDeviceMap.containsKey( device ) ) {
				mDeviceMap.put( device, new HashMap<String, Object>() );
				notify = true;
			}
		}

		if ( notify ) notifyDelegate( DelegateCallbackType.Discovered, device );
	}

	/**
	 * start() performs the following actions:
	 *
	 * 1. The system BluetoothAdapter is queried to see if it is enabled.  If it is not, a request to
	 * enable the BluetoothAdapter is initiated (see notes below) and start() exits.
	 *
	 * 2.  If the Bluetooth adapter is enabled, the list of paired devices is scanned.  If any paired
	 * devices exist start() exits and the delegate will be notified via the bluetoothDeviceBonded() callback
	 * of the paired devices.
	 *
	 * 3.  If no paired devices are found, Bluetooth discovery() is initiated.  Only Bluetooth devices that
	 * are set to be discoverable (usually via user action) can be discovered.  Discovery is a high-power mode
	 * and will drain the user's battery so be sure to let the user know they should make sure their headset
	 * is discoverable.
	 *
	 * Note: In the event a request to enable the Bluetooth adapter is initiated, the user will be prompted.
	 * Upon success or failure the Activity registered with the Bluetooth instance via its constructor will
	 * have its onActivityResult() method called.  The requestCode value will be REQUEST_ENABLE_BT or
	 * RESULT_CANCELED if the enable request succeeded or failed respectively.  If the Bluetooth adapter
	 * was enabled, the process described in steps 2 and 3 above will begin automatically.
	 */
	public synchronized void start() {
		IntentFilter				filter;
		Intent						intent;

		if ( mNotificationThread == null ) {
			filter = new IntentFilter();

			filter.addAction( BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED );
			filter.addAction( BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED );

			filter.addAction( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
			filter.addAction( BluetoothAdapter.ACTION_DISCOVERY_STARTED );
			filter.addAction( BluetoothAdapter.ACTION_STATE_CHANGED );

			filter.addAction( BluetoothDevice.ACTION_ACL_CONNECTED );
			filter.addAction( BluetoothDevice.ACTION_ACL_DISCONNECTED );
			filter.addAction( BluetoothDevice.ACTION_BOND_STATE_CHANGED );
			filter.addAction( BluetoothDevice.ACTION_FOUND );
			filter.addAction( BluetoothDevice.ACTION_UUID );

			mActivity.registerReceiver( mBroadcastReceiver, filter );

			mNotificationThread = new NotificationThread();
			mNotificationThread.start();
		}

		if ( ! mBluetoothAdapter.isEnabled() ) {
			intent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );

			mActivity.startActivityForResult( intent, REQUEST_ENABLE_BT );
		} else {
			bluetoothAdapterEnabled();
		}
	}

	/**
	 * Initiate pairing (bonding) with a bluetooth device.
	 *
	 * @param device the Bluetooth device to pair to
	 * @return true if the pairing process was successfully started, false otherwise
	 */
	public boolean pair( BluetoothDevice device ) {
		boolean						result;

		if ( ( result = device.createBond() ) ) {
			Log.d( "pairing with Bluetooth device \"" + deviceName( device ) + "\" initiated" );
		} else {
			Log.e( "pairing with Bluetooth device \"" + deviceName( device ) + "\" failed to start" );
		}

		return result;
	}

	/**
	 * unpair a bluetooth device (uses private API)
	 *
	 * @param device the Bluetooth device to unpair
	 * @return true if the device was successfully unpaired, false otherwise
	 */
	protected boolean unpair( BluetoothDevice device ) {
		try {
			return (boolean) BluetoothDevice.class.getMethod("removeBond").invoke( device );
		} catch ( Exception e ) {
			return false;
		}
	}

	protected void handleActionACLDisconnected( BluetoothDevice device ) {
		boolean						a2dpConnected, headsetConnected;
		HashMap<String, Object>		map;
		String						name = deviceName(device);
		boolean						notify = false;
		BluetoothSocket				socket;

		registerDevice( device );

		Log.d( name + " received ACL disconnect event" );

		synchronized( this ) {
			for ( Map.Entry<BluetoothDevice, HashMap<String, Object>> entry : mDeviceMap.entrySet() ) {
				if ( ! entry.getKey().equals( device ) ) continue;

				map = entry.getValue();
				if ( ! map.containsKey( keyShouldConnect ) ) break;

				if ( mA2DPProxy != null ) {
					a2dpConnected = mA2DPProxy.getConnectedDevices().contains( device );

					Log.d( name + " A2DP " + ( a2dpConnected ? "is" : "is not" ) + " connected" );
				} else {
					Log.w( "A2DP proxy is null, cannot query A2DP connection state" );
					a2dpConnected = false;
				}
				if ( mHeadsetProxy != null ) {
					headsetConnected = mHeadsetProxy.getConnectedDevices().contains( device );

					Log.d( name + " Headset " + ( headsetConnected ? "is" : "is not" ) + " connected" );
				} else {
					Log.w( "Headset proxy is null, cannot query Headset connection state" );
					headsetConnected = false;
				}
				if ( ! ( a2dpConnected || headsetConnected ) ) {
					notify = true;

					if ( ( socket = (BluetoothSocket) map.get( keySocket ) ) != null && socket.isConnected() ) {
						Log.w( name + " is not connected to either A2DP or Headset profiles, closing socket" );
						try { socket.close(); } catch ( Exception e ) { }
					}
				}

				break;
			}
		}

		if ( notify ) {
			Log.d( name + " sending device disconnected notification" );

			mDelegate.bluetoothDeviceDisconnected( this, device );
		}
	}

	protected void handleActionAdapterStateChanged( int currentState, int previousState ) {
		if ( currentState == BluetoothAdapter.STATE_ON ) {
			bluetoothAdapterEnabled();
		}

		mDelegate.bluetoothAdapterChangedState( this, currentState, previousState );
	}

	protected void handleActionBondStateChanged( BluetoothDevice device, int currentState, int previousState ) {
		registerDevice( device );

		Log.d( deviceName( device ) + " bond state changed: " + bondStateDescription( previousState ) + " -> " + bondStateDescription( currentState ) );

		switch ( currentState ) {
			case BluetoothDevice.BOND_BONDED: {
				logDeviceUUIDs( device );
				createConnectThread( device );
			} break;

			case BluetoothDevice.BOND_BONDING: {
				if ( previousState == BluetoothDevice.BOND_BONDED ) {
					Log.w( "Bond state transitioned from BOND_BONDED to BOND_BONDING, this may mean the peripheral device forgot about the Android device (was its firmware flashed?  It may be necessary to unpair the headset from Android's Bluetooth settings" );
				}
			} break;
		}

		mDelegate.bluetoothDeviceBondStateChanged( this, device, currentState, previousState );
	}

	protected void handleActionFound( BluetoothDevice device ) {
		Log.d( "Discovered bluetooth device \"" + deviceName(device) + "\" at " + device.getAddress() );

		registerDevice( device );
	}

	protected String profileState( int state ) {
		switch ( state ) {
			case 0:	 return "Disconnected";
			case 1:	 return "Connecting";
			case 2:	 return "Connected";
			case 3:	 return "Disconnecting";

			default: return "Unknown state " + state;
		}
	}

	private class ConcreteBroadcastReceiver extends BroadcastReceiver {
		public void onReceive( Context context, Intent intent ) {
			switch ( intent.getAction() ) {
				case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED: {
					Log.d( "BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED received" );

					int							currentState = intent.getIntExtra( BluetoothProfile.EXTRA_STATE, -1 );
					BluetoothDevice				device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
					int							previousState = intent.getIntExtra( BluetoothProfile.EXTRA_PREVIOUS_STATE, -1 );

					if (device != null)
					    Log.d( deviceName(device) + " A2DP State is " + profileState( currentState ) + ", was " + profileState( previousState ) );
                    else
                        Log.d("device is null, currentState = "+currentState);
                    
					switch ( currentState ) {
						case BluetoothProfile.STATE_CONNECTED: {
							notifyDelegate( DelegateCallbackType.ActionACLConnected, device );
						} break;

						case BluetoothProfile.STATE_DISCONNECTED: {
							notifyDelegate( DelegateCallbackType.ActionACLDisconnected, device );
						} break;
					}
				} break;

				case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED: {
					Log.d( "BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED received" );

					int							currentState = intent.getIntExtra( BluetoothProfile.EXTRA_STATE, -1 );
					BluetoothDevice				device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
					int							previousState = intent.getIntExtra( BluetoothProfile.EXTRA_PREVIOUS_STATE, -1 );

					Log.d( deviceName(device) + " HEADSET State is " + profileState( currentState ) + ", was " + profileState( previousState ) );

					switch ( currentState ) {
						case BluetoothProfile.STATE_CONNECTED: {
							notifyDelegate( DelegateCallbackType.ActionACLConnected, device );
						} break;

						case BluetoothProfile.STATE_DISCONNECTED: {
							notifyDelegate( DelegateCallbackType.ActionACLDisconnected, device );
						} break;
					}
				} break;

				case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {
					Log.d( "BluetoothAdapter.ACTION_DISCOVERY_FINISHED received" );
				} break;

				case BluetoothAdapter.ACTION_DISCOVERY_STARTED: {
					Log.d( "BluetoothAdapter.ACTION_DISCOVERY_STARTED received" );
				} break;

				case BluetoothAdapter.ACTION_STATE_CHANGED: {
					int							currentState = intent.getIntExtra( BluetoothAdapter.EXTRA_STATE, -1 );
					int							previousState = intent.getIntExtra( BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1 );

					Log.d( "BluetoothAdapter.ACTION_STATE_CHANGED received, new state is " + currentState + ", previous state is " + previousState );

					notifyDelegate( DelegateCallbackType.ActionAdapterStateChanged, currentState, previousState );
				} break;

				// BluetoothDevice actions

				case BluetoothDevice.ACTION_ACL_CONNECTED: {
					Log.d( "BluetoothDevice.ACTION_ACL_CONNECTED received" );

					BluetoothDevice				device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );

					notifyDelegate( DelegateCallbackType.ActionACLConnected, device );
				} break;

				case BluetoothDevice.ACTION_ACL_DISCONNECTED: {
					Log.d( "BluetoothDevice.ACTION_ACL_DISCONNECTED received" );

					BluetoothDevice				device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );

					notifyDelegate( DelegateCallbackType.ActionACLDisconnected, device );
				} break;

				case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {
					Log.d( "BluetoothDevice.ACTION_BOND_STATE_CHANGED received" );

					int							currentState = intent.getIntExtra( BluetoothDevice.EXTRA_BOND_STATE, -1 );
					BluetoothDevice				device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
					int							previousState = intent.getIntExtra( BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1 );

					notifyDelegate( DelegateCallbackType.ActionBondStateChanged, device, currentState, previousState );
				} break;

				case BluetoothDevice.ACTION_FOUND: {
					BluetoothDevice		device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );

					notifyDelegate( DelegateCallbackType.ActionFound, device );
				} break;

				case BluetoothDevice.ACTION_UUID: {
					Log.d( "BluetoothDevice.ACTION_UUID received" );

					try {
						BluetoothDevice		device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
						ParcelUuid			parcel = intent.getParcelableExtra( BluetoothDevice.EXTRA_UUID );
						UUID				uuid = parcel == null ? null : parcel.getUuid();

						if ( uuid != null ) {
							Log.d( "ACTION_UUID for " + deviceName( device ) + ": " + uuid.toString() );

							if ( uuid.toString().toUpperCase().equals( kBluetoothServiceSPP ) ) {
								notifyDelegate( DelegateCallbackType.ActionACLConnected, device );
							}
						}
					} catch ( Exception e ) {
						// this seems to crash on the SVN45 firmware
						Log.e( "Failed to parse UUID in ACTION_UUID" );
					}
				} break;
			}
		}
	}

	private class ConnectThread extends Thread {
		private final BluetoothDevice			mDevice;
		private final String					mDeviceName;

		public ConnectThread( BluetoothDevice device ) {
			mDevice = device;
			mDeviceName = deviceName( device );

			cancelDiscovery();
		}

		public void run() {
			boolean								a2dpConnected, headsetConnected;
			HashMap<String, Object>				map;
			BluetoothSocket						socket = null;

			try {
				synchronized( Bluetooth.this ) {
					for ( Map.Entry<BluetoothDevice, HashMap<String, Object>> entry : mDeviceMap.entrySet() ) {
						if ( ! mDevice.equals( entry.getKey() ) ) continue;

						map = entry.getValue();

						if ( ! map.containsKey( keyShouldConnect ) ) break;

						Log.d( mDeviceName + " Connect thread - ready to open connection, performing checks" );

						if ( mDevice.getBondState() != BluetoothDevice.BOND_BONDED ) {
							Log.d( mDeviceName + " Connect thread - device is not BONDED, initiating pairing" );
							pair( mDevice );
							break;
						}
						if ( ! deviceSupportsProfile( mDevice, kBluetoothServiceSPP ) ) {
							Log.e( mDeviceName + " Connect thread - device does not support serial port profile, launching SDP" );
							mDevice.fetchUuidsWithSdp();
							break;
						}
						if ( mA2DPProxy != null ) {
							a2dpConnected = mA2DPProxy.getConnectedDevices().contains( mDevice );

							Log.d( mDeviceName + " Connect thread - device " + ( a2dpConnected ? "is" : "is not" ) + " in A2DP connected list" );
						} else {
							Log.w( "A2DP proxy is null, cannot query A2DP connection state" );
							a2dpConnected = false;
						}
						if ( mHeadsetProxy != null ) {
							headsetConnected = mHeadsetProxy.getConnectedDevices().contains( mDevice );

							Log.d( mDeviceName + " Connect thread - device " + ( headsetConnected ? "is" : "is not" ) + " in Headset connected list" );
						} else {
							Log.w( "Headset proxy is null, cannot query Headset connection state" );
							headsetConnected = false;
						}
						if ( ! ( a2dpConnected || headsetConnected ) ) {
							Log.w( mDeviceName + " Connect thread - device is not connected to either A2DP or Headset profiles, therefore will not connect to SPP" );
							break;
						}

						Log.d( mDeviceName + " Connect thread - all checks passed, opening connection" );

						if ( ( socket = (BluetoothSocket) map.get( keySocket ) ) == null ) {
							Log.d( mDeviceName + " Connect thread - creating RFCOMM socket to SPP profile" );
							socket = createRfcommSocketToSPP( mDevice );
							Log.d( mDeviceName + " Connect thread - created RFCOMM socket to SPP profile" );

							map.put( keySocket, socket );
						}

						if ( ! socket.isConnected() ) {
							Log.d( mDeviceName + " Connect thread - calling socket.connect()" );
							socket.connect();
						}

						Log.d( mDeviceName + " Connect thread - socket is connected" );

						notifyDelegate( DelegateCallbackType.Connected, mDevice, socket );

						socket = null;

						break;
					}
				}
			} catch ( Exception e ) {
				Log.e( mDeviceName + " Connect thread - failed to connect: " + e.getLocalizedMessage() );
				// if we fail to create a new socket here something is badly
				// broken and we cannot recover.  In this case, abort the
				// connection thread.
				notifyDelegate( DelegateCallbackType.FailedToConnect, mDevice, e );
			} finally {
				if ( socket != null ) try {
					Log.e( mDeviceName + " Connect thread - closing socket " + socket );
					socket.close();
				} catch ( Exception e ) { }
			}

			Log.d( mDeviceName + " Connect thread - exiting" );
		}
	}

	private enum DelegateCallbackType {
		ActionACLConnected,
		ActionACLDisconnected,
		ActionAdapterStateChanged,
		ActionBondStateChanged,
		ActionFound,

		Connected,
		Disconnected,
		Discovered,
		FailedToConnect,
	}

	private void notifyDelegate( DelegateCallbackType type, Object... arguments ) {
		Map<String, Object>			map = new HashMap<>();

		map.put( keyType, type );

		switch ( type ) {
			case ActionAdapterStateChanged: {
				map.put( keyState, arguments[ 0 ] );
				map.put( keyPreviousState, arguments[ 1 ] );
			} break;

			case ActionBondStateChanged: {
				map.put( keyDevice, arguments[ 0 ] );
				map.put( keyState, arguments[ 1 ] );
				map.put( keyPreviousState, arguments[ 2 ] );
			} break;

			case Connected: {
				map.put( keyDevice, arguments[ 0 ] );
				map.put( keySocket, arguments[ 1 ] );
			} break;

			case ActionACLConnected: {
				if ( arguments != null && arguments.length > 0 ) {
					map.put( keyDevice, arguments[ 0 ] );
				}
			} break;

			case ActionACLDisconnected:
			case ActionFound:
			case Disconnected:
			case Discovered: {
				map.put( keyDevice, arguments[ 0 ] );
			} break;

			case FailedToConnect: {
				map.put( keyDevice, arguments[ 0 ] );
				map.put( keyException, arguments[ 1] );
			} break;
		}

		synchronized( this ) {
			if ( mNotificationThread != null ) {
				mNotificationThread.add( map );
			}
		}
	}

	private enum NotificationThreadState { Running, ShuttingDown, Done }

	// the notification thread serializes delegate callbacks so we can
	// ensure proper connect/disconnect notification ordering.
	private class NotificationThread extends Thread {
		private ArrayList<Map<String, Object>>				mCalls = new ArrayList<>();
		private NotificationThreadState						mState = NotificationThreadState.Running;

		public synchronized void add( Map<String, Object> map ) {
			mCalls.add( map );
			notify();
		}

		// the state mechanism here ensures that an in-progress message to the
		// delegate will delivered before close() returns.
		public synchronized void close() {
			mState = NotificationThreadState.ShuttingDown;
			notify();

			while ( mState != NotificationThreadState.Done ) {
				try { wait(); } catch ( InterruptedException e ) { }
			}
		}

		public void run() {
			ArrayList<Map<String, Object>>		calls, tmp = null;
			BluetoothDevice						device;
			Exception							exception;
			int									previousState, state;
			BluetoothSocket						socket;

			for ( ;; ) {
				if ( tmp == null ) tmp = new ArrayList<>();

				synchronized( this ) {
					if ( mState == NotificationThreadState.ShuttingDown ) {
						mState = NotificationThreadState.Done;
						notify();
						break;
					}
					if ( mCalls.isEmpty() ) {
						try {
							wait();
						} catch ( InterruptedException e ) { }

						continue;
					}

					calls = mCalls;
					mCalls = tmp;
					tmp = null;
				}

				for ( Map<String, Object> map : calls ) {
					switch ( (DelegateCallbackType) map.get( keyType ) ) {
						case ActionACLConnected: {
							device = (BluetoothDevice) map.get( keyDevice );

							createConnectThread( device );
						} break;

						case ActionACLDisconnected: {
							device = (BluetoothDevice) map.get( keyDevice );

							handleActionACLDisconnected( device );
						} break;

						case ActionAdapterStateChanged: {
							state = (Integer) map.get( keyState );
							previousState = (Integer) map.get( keyPreviousState );

							handleActionAdapterStateChanged( state, previousState );
						}
						break;

						case ActionBondStateChanged: {
							device = (BluetoothDevice) map.get( keyDevice );
							state = (Integer) map.get( keyState );
							previousState = (Integer) map.get( keyPreviousState );

							handleActionBondStateChanged( device, state, previousState );
						}
						break;

						case ActionFound: {
							device = (BluetoothDevice) map.get( keyDevice );

							handleActionFound( device );
						} break;

						case Connected: {
							device = (BluetoothDevice) map.get( keyDevice );
							socket = (BluetoothSocket) map.get( keySocket );

							mDelegate.bluetoothDeviceConnected( Bluetooth.this, device, socket );
						}
						break;

						case Disconnected: {
							device = (BluetoothDevice) map.get( keyDevice );

							mDelegate.bluetoothDeviceDisconnected( Bluetooth.this, device );
						}
						break;

						case Discovered: {
							device = (BluetoothDevice) map.get( keyDevice );
							mDelegate.bluetoothDeviceDiscovered( Bluetooth.this, device );
						}
						break;

						case FailedToConnect: {
							device = (BluetoothDevice) map.get( keyDevice );
							exception = (Exception) map.get( keyException );

							mDelegate.bluetoothDeviceFailedToConnect( Bluetooth.this, device, exception );
						}
						break;
					}
				}
			}
		}
	}
}
