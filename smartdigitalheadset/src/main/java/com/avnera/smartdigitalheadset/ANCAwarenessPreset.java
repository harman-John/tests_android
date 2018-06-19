/*
 * ANCAwareness.java
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
public enum ANCAwarenessPreset {
	None						( 0 ),
	Low							( 1 ),
	Medium						( 2 ),
	High						( 3 ),

	First						( 0 ),
	Last						( 3 ),
	NumPresets					( 4 );

	private final int			value;

	ANCAwarenessPreset( int value ) {
		if ( value < 0 || value > 3 ) value = -1;

		this.value = value;
	}

	public static ANCAwarenessPreset from( int value ) {
		for ( ANCAwarenessPreset v : ANCAwarenessPreset.values() ) {
			if ( value == v.value ) {
				return v;
			}
		}

		throw new RuntimeException( String.format( "invalid ANCAwarenessPreset value 0x%02x", value ) );
	}

	public int value() { return value; }
}
