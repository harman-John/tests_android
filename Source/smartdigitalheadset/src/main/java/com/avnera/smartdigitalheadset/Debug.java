/*
 * Debug.java
 * Smart Digital Headset
 *
 * Created by Brian Doyle on 7/17/15
 * Copyright (c) 2015 Avnera Corporation
 *
 */
package com.avnera.smartdigitalheadset;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Brian Doyle on 7/17/15.
 */
public final class Debug {

	static int width = 8;

	/**
	 * hexify() generates strings representing the contents of byte[] buffers in hexadecimal.
	 * This is useful for debugging.  Output looks like:
	 * <pre>
	 * 000000: FF 5A 00 00 00 93 0F 40 81 00 00 00 00 28 80 00
	 * 000010: 00 00 02 00 01 02 1C 50 02 00 53 02 01 14 38 FF
	 * etc.
	 * </pre>
	 *
	 * @param bytes The buffer to dump
	 * @param baseAddress The base address to add to the offset display (leftmost column)
	 * @param from Offset in bytes from the start of buffer to start dumping from
	 * @param to Offset in bytes from the start of the buffer to dump to (exclusive)
	 * @return A string containing the dumped data
	 */
	public static String hexify( byte[] bytes, int baseAddress, int from, int to ) {
		int							i, j, n, o;
		StringBuilder				s = new StringBuilder();

		for ( i = from, n = to; i < n; i += j ) {
			if ( ( o = n - i ) > width ) o = width;

			s.append( String.format( "%06X:", baseAddress + i ) );

			for ( j = 0; j < o; ++j ) {
				s.append( String.format( " %02X", bytes[ i + j ] ) );
			}

			s.append( "\n" );
		}

		return s.toString();
	}

	/**
	 * Convenience method of hexify() using a baseAddress of 0
	 *
	 * @param bytes The buffer to dump
	 * @param from Offset in bytes from the start of buffer to start dumping from
	 * @param to Offset in bytes from the start of the buffer to dump to (exclusive)
	 * @return A string containing the dumped data
	 */
	public static String hexify( byte[] bytes, int from, int to ) { return hexify( bytes, 0, from, to ); }
	/**
	 * Convenience method of hexify() using a from of 0 and to of buffer.length
	 *
	 * @param bytes The buffer to dump
	 * @param baseAddress The base address to add to the offset display (leftmost column)
	 * @return A string containing the dumped data
	 */
	public static String hexify( byte[] bytes, int baseAddress ) { return hexify( bytes, baseAddress, 0, bytes.length ); }
	/**
	 * Convenience method of hexify() using a baseAddress of 0, from of 0, and to of buffer.length
	 *
	 * @param bytes The buffer to dump
	 * @return A string containing the dumped data
	 */
	public static String hexify( byte[] bytes ) { return hexify( bytes, 0, 0, bytes.length ); }

	/**
	 * Convert an Exception's (or any Throwable's) stack trace to a string suitable for logging.
	 * @param t The throwable to obtain the stack trace from
	 * @return A string representation of the stack trace
	 */
	public static String stackTrace( Throwable t ) {
		StringWriter				writer = new StringWriter();

		t.printStackTrace( new PrintWriter( writer ) );

		return writer.toString();
	}

}
