/*
 * BluetoothSocketWrapper.java
 * Smart Digital Headset
 *
 * Created by Brian Doyle on 7/17/15
 * Copyright (c) 2015 Avnera Corporation
 *
 */
package com.avnera.smartdigitalheadset;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The BluetoothSocketWrapper class converts an Android BluetoothSocket instance into a
 * LightX-compatible Socket instance.
 *
 * @author Brian Doyle on 7/17/15.
 */
public final class BluetoothSocketWrapper implements Socket {
	BluetoothSocket					mSocket;

	public BluetoothSocketWrapper( BluetoothSocket socket ) {
		if ( ( mSocket = socket ) == null ) throw new IllegalArgumentException( "socket cannot be null" );
	}

	@Override
	public void close() throws IOException { mSocket.close(); }

	@Override
	public InputStream getInputStream() throws IOException { return mSocket.getInputStream(); }

	@Override
	public OutputStream getOutputStream() throws IOException { return mSocket.getOutputStream(); }
}
