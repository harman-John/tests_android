/*
 * GraphicEQPreset.java
 * internal
 *
 * Created by Brian Doyle <brian@balance-software.com on 8/1/15
 * Copyright (c) 2015 Balance Software, Inc.
 *
 */
package jbl.stc.com.entity;

/**
 * @author Brian Doyle on 8/1/15.
 */
public enum GraphicEQPreset {
	// see app_common/geq/geq_cmd.h
	Off							( 0 ),

	Jazz						( 1 ),
	Vocal						( 2 ),
	Bass						( 3 ),
	User						( 4 ),

	First						( 1 ),
	Last						( 4 ),
	NumPresets					( 4 );

	private final int			value;

	GraphicEQPreset(int value ) {
		if ( value < 0 || value > 4 ) throw new IndexOutOfBoundsException();

		this.value = value;
	}

	public static GraphicEQPreset from( int value ) {
		for ( GraphicEQPreset v : GraphicEQPreset.values() ) {
			if ( value == v.value ) {
				return v;
			}
		}

		throw new RuntimeException( String.format( "invalid GraphicEQPreset value 0x%02x", value ) );
	}

	public int value() { return value; }
}
