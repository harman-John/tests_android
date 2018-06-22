/*
 * Socket.java
 * Smart Digital Headset
 *
 * Created by Brian Doyle on 7/17/15
 * Copyright (c) 2015 Avnera Corporation
 *
 */
package com.avnera.smartdigitalheadset;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The Socket interface is consumed by the LightX class and is intended to be used as an abstraction
 * layer for classes that provide (or can be made to provide) socket-like semantics.  For example,
 * Android's BluetoothSocket does not utilize the same I/O methods as Android's USB classes, but both
 * can be wrapped inside an object that conforms to the Socket interface and can consequently be
 * used by LightX without it being aware of the physical transport mechanism.
 *
 * @author Brian Doyle on 7/17/15.
 */
public interface Socket {
	/**
	 * Close the socket, multiple calls to close should succeed.
	 *
	 * @throws IOException Thrown when an I/O error occurs.  LightX discards this exception.
	 */
	void close() throws IOException;

	/**
	 * Return an InputStream object for LightX (or the Socket consumer) to get data from.
	 * @return An InputStream object for LightX (or the Socket consumer) to get data from.
	 * @throws IOException Thrown when an I/O error occurs.  LightX discards this exception.
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Return an OutputStream object for LightX (or the Socket consumer) to send data to.
	 *
	 * @return An OutputStream object for LightX (or the Socket consumer) to send data to.
	 * @throws IOException Thrown when an I/O error occurs.  LightX discards this exception.
	 */
	OutputStream getOutputStream() throws IOException;
}
