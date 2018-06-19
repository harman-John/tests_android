/*
 * ModuleId.java
 * Smart Digital Headset
 *
 * Created by Brian Doyle on 7/17/15
 * Copyright (c) 2015 Avnera Corporation
 *
 */
package com.avnera.smartdigitalheadset;

/**
 * @author Brian Doyle on 7/17/15.
 */
public enum ModuleId {
	None							( 0x00 ),		// no module
	Register						( 0x02 ),		// register module
	DSP								( 0x03 ),		// DSP module
	Memory							( 0x01 ),		// memory module
	SPIM							( 0x04 ),		// SPI master module
	File							( 0x05 ),		// flash file system
	Flash							( 0x06 ),		// flash memory directly
	TWIM							( 0x07 ),		// TWI master interface
	USB								( 0x08 ),		// USB interface
	JTAG							( 0x09 ),		// JTAG interface
	Log								( 0x0a ),		// Logging interface
	UART							( 0x0b ),		// UART interface
	SPIS							( 0x0c ),		// SPI slave module
	TWIS							( 0x0d ),		// TWI slave interface
	Audio							( 0x0e ),		// Audio module
	Bluetooth						( 0x0f ),		// Bluetooth module
	ANC								( 0x10 ),		// ANC module
	DVM								( 0x11 ),		// DVM module
	NVM								( 0x12 ),		// NVM module
	HostMessageHandler				( 0x14 ),		// LightX Host Message Handler module

	Application						( 0x40 ),		// application module
	Localhost						( 0x42 );		// localhost

	private final int				value;

	ModuleId( int value ) {
		if ( value < 0 || value > 0xff ) throw new IndexOutOfBoundsException();

		this.value = value;
	}

	public static ModuleId from( int value ) {
		for ( ModuleId v : ModuleId.values() ) {
			if ( value == v.value ) {
				return v;
			}
		}

		throw new RuntimeException( String.format( "invalid ModuleId value 0x%02x", value ) );
	}

	public int value() { return value; }
}
