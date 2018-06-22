/*
 * Command.java
 * sdhm
 * 
 * Created by Brian Doyle on 06/21/2015
 * Copyright (c) 2015 Avnera Corporation All rights reserved.
 */
package com.avnera.smartdigitalheadset;

/**
 *
 * @author Brian Doyle
 */
final class Packet {
	public byte[]				buffer;
	public ModuleId				destination;
	public boolean				hasBuffer;
	public boolean				highPriority;
	public Opcode				opcode;
	public ModuleId				source;
	public Status				status;
	public int					timeoutMs;
	public Type					type;

	protected static final int	kBluetoothChecksumSize = 1;
	protected static final int	kBluetoothTransportHeaderSize = 6;
	public static final int		kPacketHeaderSize = 12;
	public static final int		kMaximumBufferSize = 256;	// bluetooth
	public static final int		kOpcodeOffset = 2;
	public static final int		kParam1Offset = 8;

	private long				param0;
	private long				param1;

	// Static methods

	public static Packet assemble( byte[] data, int from, int to, int[] consumed ) {
		int						bufferLength, dataRemaining, start;
		Packet					packet = new Packet();

		if ( to - from < kPacketHeaderSize || to > data.length ) return null;

		start = from;

		packet.source = ModuleId.from( data[ from ] );
		packet.destination = ModuleId.from( data[ ++from ] );
		packet.opcode = Opcode.from( data[ ++from ] & Opcode.Mask.value() );
		packet.hasBuffer = ( data[ from ] & Opcode.BufferFlag.value() ) != 0;
		packet.highPriority = ( data[ from ] & Opcode.HighPriorityFlag.value() ) != 0;
		packet.status = Status.from( data[ ++from ] & Status.Mask.value() );
		packet.type = Type.from( data[ from ] & Type.Mask.value() );
		packet.param0  =   data[ ++from ] & 0xffL;
		packet.param0 |= ( data[ ++from ] & 0xffL ) <<  8;
		packet.param0 |= ( data[ ++from ] & 0xffL ) << 16;
		packet.param0 |= ( data[ ++from ] & 0xffL ) << 24;
		packet.param1  =   data[ ++from ] & 0xffL;
		packet.param1 |= ( data[ ++from ] & 0xffL ) <<  8;
		packet.param1 |= ( data[ ++from ] & 0xffL ) << 16;
		packet.param1 |= ( data[ ++from ] & 0xffL ) << 24;

		if ( packet.hasBuffer ) {
			bufferLength = (int)( packet.param1 & 0xffffffffL );
			dataRemaining = to - ++from;

			if ( dataRemaining < bufferLength ) {
//				return null;
//				why is this here?  did this happen on bluetooth?
				// If below line is commented out some command sequences fail on Bluetooth
				// (e.g. enterBootloader and WriteFirmware. USB works ok with line uncommented (CC)
				packet.param1 = bufferLength = dataRemaining;
			}

			packet.buffer = new byte[ bufferLength ];
			System.arraycopy( data, from, packet.buffer, 0, bufferLength );
			from += bufferLength;
		}

		if ( consumed != null && consumed.length > 0 ) consumed[ 0 ] = from - start;

		return packet;
	}

	// Instance Methods

	public int address() { return (int)( param0 & 0xffffff ); }

	public String bufferString() { return Utility.stringFromASCIICString( buffer ); }

	public static byte checksum( byte[] data, int from, int to ) {
		byte					checksum;

		for ( checksum = 0; from < to; ++from ) {
			checksum += data[ from ];
		}

		return (byte) -checksum;
	}

	public Command command() { return Command.from( (int) ( ( param0 & 0xff000000L ) >> 24 ) ); }
	public boolean isPush() { return command().value() >= Command.PushFirst.value() && command().value() <= Command.PushLast.value(); }

	public int checksumSize() { return source == ModuleId.Bluetooth ? kBluetoothChecksumSize : 0; }
	public int minimumPacketSize() { return transportHeaderSize() + kPacketHeaderSize + checksumSize(); }
	public int transportHeaderSize() { return source == ModuleId.Bluetooth ? kBluetoothTransportHeaderSize : 0; }

	public byte[] packetize() {
		int						checksumSize, dataLength, packetLength, payloadLength, transportHeaderSize;
		byte[]					packet, payload;

		checksumSize = checksumSize();
		payload = payload();
		payloadLength = ( payload == null ? 0 : payload.length );
		transportHeaderSize = transportHeaderSize();
		dataLength = transportHeaderSize + payloadLength;
		packetLength = dataLength + checksumSize;
		packet = new byte[ packetLength ];

		// transport header
		switch ( source ) {
			case Bluetooth: {
				packet[ 0 ] = (byte) 0xff;
				packet[ 1 ] = (byte) 0x5a;
				packet[ 2 ] = (byte) 0x00;
				packet[ 3 ] = (byte) 0x00;
				packet[ 4 ] = (byte) ( packetLength >> 8 & 0xff );   // total size of packet MSB
				packet[ 5 ] = (byte) ( packetLength & 0xff );        // total size of packet LSB
			} break;
		}

		System.arraycopy( payload, 0, packet, transportHeaderSize, payloadLength );

		if ( checksumSize == 1 ) {
			packet[ dataLength ] = checksum( packet, 0, dataLength );
		}

		return packet;
	}
	public long param0() { return param0 & 0xffffffffL; }
	public long param1() { return param1 & 0xffffffffL; }

	public byte[] payload() {
		int						bufferLength;
		long					param;
		byte[]					payload;

		bufferLength = buffer == null ? 0 : buffer.length;
		payload = new byte[ kPacketHeaderSize + bufferLength ];

		if ( source == ModuleId.USB && type == null ) {
			type = Type.Nonblocking;
		}

		payload[  0 ]  = (byte)(source.value() & 0xff);
		payload[  1 ]  = (byte)(destination.value() & 0xff);
		payload[  2 ]  = (byte)(opcode.value() & 0xff);
		payload[  2 ] |= highPriority ? Opcode.HighPriorityFlag.value() : 0;
		payload[  2 ] |= hasBuffer ? Opcode.BufferFlag.value() : 0;
		payload[  3 ]  = (byte)(( status == null ? Status.Ok.value() : status.value() ) & 0xff);
		payload[  3 ] |= type == null ? Type.Result.value() : type.value();
		payload[  4 ]  = (byte)( param0       & 0xff );
		payload[  5 ]  = (byte)( param0 >>  8 & 0xff );
		payload[  6 ]  = (byte)( param0 >> 16 & 0xff );
		payload[  7 ]  = (byte)( param0 >> 24 & 0xff );

		if ( hasBuffer && buffer == null ) buffer = new byte[ 0 ];

		if ( buffer != null ) {
			param = buffer.length;

			System.arraycopy( buffer, 0, payload, kPacketHeaderSize, bufferLength );
		} else {
			param = param1;
		}

		payload[  8 ] = (byte)( param       & 0xff );
		payload[  9 ] = (byte)( param >>  8 & 0xff );
		payload[ 10 ] = (byte)( param >> 16 & 0xff );
		payload[ 11 ] = (byte)( param >> 24 & 0xff );

		return payload;
	}

	public static Packet request( ModuleId source, Opcode opcode, Command command, int timeoutMs, int address, byte[] buffer ) {
		Packet					packet = new Packet();

		if ( buffer == null ) buffer = new byte[ kMaximumBufferSize ];
		else if ( buffer.length > kMaximumBufferSize ) throw new IllegalArgumentException( "buffer cannot exceed " + kMaximumBufferSize + " bytes" );

		packet.source = source;
		packet.destination = ModuleId.Application;
		packet.opcode = opcode;
		packet.hasBuffer = true;
		packet.buffer = buffer;
		packet.timeoutMs = timeoutMs;
		packet.setAddress( address );
		packet.setCommand( command );

		return packet;
	}

	public static Packet readRequest( ModuleId source, Command command, int timeoutMs, int address, byte[] buffer ) {
		return request( source, Opcode.Read, command, timeoutMs, address, buffer );
	}

	public static Packet writeRequest( ModuleId source, Command command, int timeoutMs, int address, byte[] buffer ) {
		return request( source, Opcode.Write, command, timeoutMs, address, buffer );
	}

	public void setAddress( int address ) {
		if ( address < 0 || address > 0xffffff ) throw new IndexOutOfBoundsException();

		param0 |= address;
	}
	public void setCommand( Command command ) {
		param0 |= ( command.value() & 0xff ) << 24;
	}
	public void setFlashPartitionId( FlashPartitionId flashPartitionId ) { param0 |= ( flashPartitionId.value() & 0xff ) << 24; }
	public void setParam0( long value ) {
		if ( value < 0 || value > 0xffffffffL ) throw new IndexOutOfBoundsException();

		param0 = value;
	}
	public void setParam1( long value ) {
		if ( value < 0 || value > 0xffffffffL ) throw new IndexOutOfBoundsException();

		param1 = value;
	}

	public String toString() {
		String						bufferValue = bufferString();

		return String.format( "src: %s, dst: %s, opcode: %s, buffer?: %b, priority?: %b, status: %s, type: %s, command: %s, address: 0x%06X, buffer: %s",
			source, destination, opcode, hasBuffer, highPriority, status, type, command(), address(), ( bufferValue == null ? "null" : "\"" + bufferValue + "\"" )
		);
	}

	public static boolean verifyChecksum( byte[] packet, int from, int to ) {
		--to;

		return checksum( packet, from, to ) == packet[ to ];
	}

	public enum FlashPartitionId {
		Bootloader						( 0x00 ),
		BootData						( 0x01 ),
		FactoryImage					( 0x02 ),
		UpdateImage						( 0x03 ),
		Resource						( 0x04 );

		private final int				value;

		FlashPartitionId( int value ) {
			if ( value < 0 || value > 0xff ) throw new IndexOutOfBoundsException();

			this.value = (byte) value;
		}

		public static FlashPartitionId from( int value ) {
			for ( FlashPartitionId v : FlashPartitionId.values() ) {
				if ( value == v.value ) {
					return v;
				}
			}

			throw new RuntimeException( String.format( "invalid FlashPartitionId value 0x%02x", value ) );
		}

		public int value() { return value; }
	}

	public enum Opcode {
		None							( 0x00 ),		// none
		Read							( 0x01 ),		// read operation
		Write							( 0x02 ),		// write operation
		Set								( 0x03 ),		// set parameter operation
		Get								( 0x04 ),		// get parameter operation
		Request							( 0x05 ),		// request access to a port operation

		Mask							( 0x3f ),		// command opcode mask

		HighPriorityFlag				( 0x40 ),
		BufferFlag						( 0x80 );		// flag for this command referencing a data buffer

		private final int				value;

		Opcode( int value ) {
			if ( value < 0 || value > 0xff ) throw new IndexOutOfBoundsException();

			this.value = (byte) value;
		}

		public static Opcode from( int value ) {
			for ( Opcode v : Opcode.values() ) {
				if ( value == v.value ) {
					return v;
				}
			}

			throw new RuntimeException( String.format( "invalid Opcode value 0x%02x", value ) );
		}

		public int value() { return value; }
	}

	public enum Status {
		Ok								( 0x00 ),
		Fail							( 0x01 ),
		Pending							( 0x02 ),				// TODO: ask avnera what can return this
		NotFound						( 0x03 ),
		NotSupported					( 0x04 ),
		ExceededLimits					( 0x05 ),
		NotReady						( 0x06 ),
		CommFailure						( 0x07 ),

		Complete						( Ok.value() ),			// convenience naming, indicates a transaction is complete
		Continue						( Pending.value() ),	// convenience naming, indicates a transaction is continuing

		Mask							( 0x3f );		// mask to apply the packet result type/status code

		private final int				value;

		Status( int value ) {
			if ( value < 0 || value > 0xff ) throw new IndexOutOfBoundsException();

			this.value = (byte) value;
		}

		public static Status from( int value ) {
			for ( Status v : Status.values() ) {
				if ( value == v.value ) {
					return v;
				}
			}

			throw new RuntimeException( String.format( "invalid Status value 0x%02x", value ) );
		}

		public int value() { return value; }
	}

	public enum Type {
		Result							( 0x00 ),
		Nonblocking						( 0x40 ),
		Blocking						( 0x80 ),

		Mask							( 0xc0 );		// mask to apply the packet result type/status code

		private final int				value;

		Type( int value ) {
			if ( value < 0 || value > 0xff ) throw new IndexOutOfBoundsException();

			this.value = (byte) value;
		}

		public static Type from( int value ) {
			for ( Type v : Type.values() ) {
				if ( value == v.value ) {
					return v;
				}
			}

			throw new RuntimeException( String.format( "invalid Type value 0x%02x", value ) );
		}

		public int value() { return value; }
	}
}
