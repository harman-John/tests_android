/*
 * CRC.java
 * internal
 *
 * Created by Brian Doyle on 7/18/15
 * Copyright (c) 2015 Avnera Corporation
 *
 */
package com.avnera.smartdigitalheadset;

/**
 * @author Brian Doyle on 7/18/15.
 */
final class CRC32 {
	private static final long		kCRC32Polynomial = 0xEDB88320L;
	private static final long[]		kCRC32Table = {
		compute( 0L ),
		compute( 1L ),
		compute( 2L ),
		compute( 3L ),
		compute( 4L ),
		compute( 5L ),
		compute( 6L ),
		compute( 7L ),
		compute( 8L ),
		compute( 9L ),
		compute( 10L ),
		compute( 11L ),
		compute( 12L ),
		compute( 13L ),
		compute( 14L ),
		compute( 15L )
	};

	// calculate 32-bit crc over buffer using seed
	public static long calculate( long seed, byte[] buffer, int from, int to ) {
		for ( ; from < to; ++from ) {
			seed ^= buffer[ from ] & 0xff;
			seed  = seed >> 4 ^ kCRC32Table[ (int)(seed & 0xf) ];
			seed  = seed >> 4 ^ kCRC32Table[ (int)(seed & 0xf) ];
		}

		return seed;
	}
	public static long calculate( long seed, byte[] buffer ) { return calculate( seed, buffer, 0, buffer.length ); }

	private static long phase( long value ) {
		return ( value & 1 ) == 1 ? value >> 1 ^ kCRC32Polynomial : value >> 1;
	}
	private static long compute( long value ) {
		return phase( phase( phase( phase( value ) ) ) );
	}
}
