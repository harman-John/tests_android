/*
 * Utility.java
 * internal
 *
 * Created by Brian Doyle on 8/1/15
 * Copyright (c) 2015 Balance Software, Inc.
 *
 */
package com.avnera.smartdigitalheadset;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Various utility methods (currently little-endian value conversion functions for LightX arguments)
 *
 * @author Brian Doyle on 8/1/15.
 */
public class Utility {
	private static final String TAG = Utility.class.getSimpleName();

	/**
	 * getBoolean() reads 4 bytes starting at buffer[ offset ], converts them from an unsigned
	 * 32-bit little-endian integer representation, and returns the value as a boolean.
	 *
	 * @param buffer The buffer to read the boolean value from
	 * @param offset The buffer offset to start reading from.  The buffer must contain at least
	 *               4 bytes from offset.
	 * @return The boolean value from the buffer
	 */
	public static boolean getBoolean( byte[] buffer, int offset ) {
		return getUnsignedInt( buffer, offset ) != 0;
	}

	/**
	 * getFloat() reads 4 bytes starting at buffer[ offset ], converts them from a
	 * 32-bit little-endian float representation, and returns the value as a float.
	 *
	 * @param buffer The buffer to read the float value from
	 * @param offset The buffer offset to start reading from.  The buffer must contain at least
	 *               4 bytes from offset.
	 * @return The boolean value from the buffer
	 */
	public static float getFloat( byte[] buffer, int offset ) {
		return ByteBuffer.wrap( buffer, offset, 4 ).order( ByteOrder.LITTLE_ENDIAN ).getFloat();
	}

	/**
	 * getInt() reads 4 bytes starting at buffer[ offset ], converts them from an signed
	 * 32-bit little-endian integer representation, and returns the value as an int.
	 *
	 * @param buffer The buffer to read the boolean value from
	 * @param offset The buffer offset to start reading from.  The buffer must contain at least
	 *               4 bytes from offset.
	 * @return The boolean value from the buffer
	 */
	public static int getInt( byte[] buffer, int offset ) {
		return (int) getUnsignedInt( buffer, offset );
	}

	/**
	 * getUnsignedInt() reads 4 bytes starting at buffer[ offset ] and converts them from an unsigned
	 * 32-bit little-endian integer representation into a long in host byte order.
	 *
	 * @param buffer The buffer to read the 32-bit unsigned little-endian integer from
	 * @param offset The buffer offset to start reading from.  The buffer must contain at least
	 *               4 bytes from offset.
	 * @return The unsigned 32-bit integer value from the buffer converted to host byte order
	 */
	public static long getUnsignedInt( byte[] buffer, int offset ) {
		long						result;
		if (buffer == null){
			Logger.d(TAG,"buffer is null , so return 0");
			return 0;
		}
		result  =   buffer[ offset++ ] & 0xffL;
		result |= ( buffer[ offset++ ] & 0xffL ) << 8;
		result |= ( buffer[ offset++ ] & 0xffL ) << 16;
		result |= ( buffer[ offset   ] & 0xffL ) << 24;

		return result;
	}

	/**
	 * putBoolean() is a utility method that converts a boolean value into a four-byte little-endian
	 * representation, then stores the result into the provided buffer over the range buffer[ offset ] ..
	 * buffer[ offset + 3 ]
	 *
	 * @param value The boolean value to store
	 * @param buffer The buffer to write the boolean to, in little endian byte order
	 * @param offset The buffer offset to start storing the value at.  buffer must be contain at least
	 *               4 bytes from offset.
	 */
	public static void putBoolean( boolean value, byte[] buffer, int offset ) {
		putUnsignedInt( value ? 1 : 0, buffer, offset );
	}

	/**
	 * putFloat() is a utility method that converts a float value (32-bits on
	 * Java platforms) into a four-byte little-endian representation, then stores the result into the
	 * provided buffer over the range buffer[ offset ] .. buffer[ offset + 3 ]
	 *
	 * @param value The 32-bit float value to store
	 * @param buffer The buffer to write the integer to, in little endian byte order
	 * @param offset The buffer offset to start storing the value at
	 */
	public static void putFloat( float value, byte[] buffer, int offset ) {
		ByteBuffer.allocate( 4 ).order( ByteOrder.LITTLE_ENDIAN ).putFloat( value ).get( buffer, offset, 4 );
	}

	/**
	 * putInt() is a utility method that converts the low 32-bits of the long 'value'
	 * argument into little-endian representation, then stores the result into the provided buffer
	 * over the range buffer[ offset ] .. buffer[ offset + 3 ].
	 *
	 * @param value The 32-bit value to store.
	 * @param buffer The buffer to write the integer to.  value will be stored in little endian byte order.
	 * @param offset The buffer offset to start storing the value at.  buffer must be contain at least
	 *               4 bytes from offset.
	 */
	public static void putInt( int value, byte[] buffer, int offset ) {
		putUnsignedInt( value, buffer, offset );
	}

	/**
	 * PutUnsignedInt() is a utility method that converts the low 32-bits of the long 'value'
	 * argument into little-endian representation, then stores the result into the provided buffer
	 * over the range buffer[ offset ] .. buffer[ offset + 3 ].
	 *
	 * @param value The 32-bit value to store.
	 * @param buffer The buffer to write the integer to.  value will be stored in little endian byte order.
	 * @param offset The buffer offset to start storing the value at.  buffer must be contain at least
	 *               4 bytes from offset.
	 */
	public static void putUnsignedInt( long value, byte[] buffer, int offset ) {
		buffer[ offset++ ] = (byte)(value       & 0xff);
		buffer[ offset++ ] = (byte)(value >>  8 & 0xff);
		buffer[ offset++ ] = (byte)(value >> 16 & 0xff);
		buffer[ offset   ] = (byte)(value >> 24 & 0xff);
	}

	/**
	 * stringFromASCIICString() returns a String from a US_ASCII encoded, null-terminated buffer.
	 *
	 * @param buffer The buffer to construct the string from
	 * @return A String or null if buffer is null on entry
	 */
	public static String stringFromASCIICString( byte[] buffer ) {
		int							i, n;
		String						s = null;

		if ( buffer != null ) {
			for ( i = 0, n = buffer.length; i < n; ++i ) {
				if ( buffer[ i ] == 0 ) break;
			}

			s = new String( buffer, 0, i, StandardCharsets.US_ASCII );
		}

		return s;
	}
}
