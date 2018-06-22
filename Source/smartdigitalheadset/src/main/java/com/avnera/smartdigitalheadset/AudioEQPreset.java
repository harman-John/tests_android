/*
 * AudioEQPreset.java
 * internal
 *
 * Created by Brian Doyle <brian@balance-software.com on 8/1/15
 * Copyright (c) 2015 Balance Software, Inc.
 *
 */
package com.avnera.smartdigitalheadset;

/**
 * @author Brian Doyle on 8/1/15.
 */
public enum AudioEQPreset {
	Music						( 0 ),
	Gaming						( 1 ),
	Movie						( 2 ),
	Conference					( 3 ),

	First						( 0 ),
	Last						( 3 ),
	NumPresets					( 4 );

	private final int			value;

	AudioEQPreset( int value ) {
		if ( value < 0 || value > 3 ) throw new IndexOutOfBoundsException();

		this.value = value;
	}

	public static AudioEQPreset from( int value ) {
		for ( AudioEQPreset v : AudioEQPreset.values() ) {
			if ( value == v.value ) {
				return v;
			}
		}

		throw new RuntimeException( String.format( "invalid AudioEQPreset value 0x%02x", value ) );
	}

	public int value() { return value; }
}
