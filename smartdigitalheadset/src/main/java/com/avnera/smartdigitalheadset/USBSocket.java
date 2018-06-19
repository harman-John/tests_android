/*
 * USBHIDSocket.java
 * internal
 *
 * Created by Brian Doyle on 1/27/16
 * Copyright (c) 2016 Balance Software, Inc.
 *
 */
package com.avnera.smartdigitalheadset;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class USBSocket implements Socket {
	final int				kControlTransferF0HeaderSize = 2;
	final int				kControlTransferF1HeaderSize = 1;
	final int				kHeaderSize = kControlTransferF0HeaderSize + Packet.kPacketHeaderSize;

	boolean					mPacketPending=false;
	boolean					mClosed;
	UsbDeviceConnection		mConnection;
	UsbInterface			mInterface;
	BufferingInputStream	mInputStream;
	USBHIDOutputStream		mOutputStream;
	USBHIDPollThread		mPollThread;

	public USBSocket( UsbDeviceConnection connection, UsbInterface usbInterface) throws IOException {
		mInterface = usbInterface;
		mConnection = connection;
		mInputStream = new BufferingInputStream();
		mOutputStream = new USBHIDOutputStream();
		mPollThread = new USBHIDPollThread();
		mPollThread.start();
	}

	/**
	 * Close the socket, multiple calls to close should succeed.
	 *
	 * @throws IOException Thrown when an I/O error occurs.  LightX discards this exception.
	 */
	@Override
	public void close() throws IOException {
		synchronized( this ) {
			if ( mClosed ) return;

			mClosed = true;
			notify();
		}

		try { mInputStream.close(); } catch ( Exception e ) { }
		try { mOutputStream.close(); } catch ( Exception e ) { }

		mPollThread.close();
	}

	@Override
	public InputStream getInputStream() throws IOException { return mInputStream; }

	@Override
	public OutputStream getOutputStream() throws IOException { return mOutputStream; }

	private class USBHIDPollThread extends Thread {
		synchronized void close() {
			mClosed = true;
			notify();
		}

		public void run() {
			for ( ;; ) {
				try {
					// TODO: I've implemented this as a continuously polling loop because
					// I'm thinking for now push events can just be queued up by the hardware
					// as a normal packet with the push bit set.  This loop will catch that.
					// If push events *won't* be delivered this way, then this loop could
					// be gated with a condition that waits for a request command to be sent
					// before the poll loop starts.
					//
					// The best solution though is to implement an HID interrupt scheme so
					// polling isn't required at all.
					//
					// I'm not sure if this 50ms poll will result in higher than normal
					// battery usage, so this should be tested.
					synchronized( this ) {
						wait( 50 );
						if ( mClosed ) break;
					}
					if( mPacketPending ) {
						readInputStream(LightX.getFirmwareIsUpdating());
					}
				} catch ( InterruptedException e ) {
				} catch ( Exception e ) {
					Log.e( "Exception in USB poll thread: " + e.getLocalizedMessage() );
					try { USBSocket.this.close(); } catch ( Exception e0 ) { }
					break;
				}
			}
		}

		public void readInputStream(boolean holdInterface) throws IOException {
			final int	kDataSize = 256;
			final int	kTimeoutMs = 5000;

			int			amountRead;
			byte[]		data = new byte[ kControlTransferF0HeaderSize + Packet.kPacketHeaderSize + kDataSize ];

			data[ 0 ] = (byte) 0xF0;
			amountRead = mConnection.controlTransfer( 0xA1, 0x01, 0x03F0, 0, data, kHeaderSize, kTimeoutMs );

			if ( amountRead < 0 ) throw new IOException( "USB control transfer read (0xF0) failed" );
			if ( amountRead != kHeaderSize ) throw new IOException( "USB control transfer read (0xF0) returned unexpected size (" + amountRead + " bytes)" );
			if ( data[ 0 ] != (byte) 0xF0 ) throw new IOException( "USB control transfer read (0xF0) returned unexpected data:\n" + Debug.hexify( data ) );
			if ( data[ 1 ] == 0 ) { return; }

//			Log.d( "read " + amountRead + " bytes from 0xF0:\n" + Debug.hexify( data, 0, amountRead ) );

			final int	kOpcodeOffset = kControlTransferF0HeaderSize + Packet.kOpcodeOffset;
			final int	kParam1Offset = kControlTransferF0HeaderSize + Packet.kParam1Offset;

			boolean		hasBuffer = ( data[ kOpcodeOffset ] & Packet.Opcode.BufferFlag.value() ) != 0;
			long		bufferSize = Utility.getUnsignedInt( data, kParam1Offset );
			int			length = Packet.kPacketHeaderSize;

			if ( hasBuffer && bufferSize > 0 ) {
				final int	kControlTransferF1DataSize = kControlTransferF1HeaderSize + kDataSize;
				final int	kLastHeaderByteOffset = kControlTransferF0HeaderSize + Packet.kPacketHeaderSize - 1;
				byte		lastHeaderByte = data[ kLastHeaderByteOffset ];

				data[ kLastHeaderByteOffset ] = (byte) 0xF1;
				amountRead = mConnection.controlTransfer( 0xA1, 0x01, 0x03F1, 0, data, kLastHeaderByteOffset, kControlTransferF1DataSize, kTimeoutMs );
				if (!holdInterface){
					mConnection.releaseInterface(mInterface);
				}

				if ( amountRead < 0 ) throw new IOException( "USB control transfer read (0xF1) failed" );

//				Log.d( "read " + amountRead + " bytes from 0xF1:\n" + Debug.hexify( data, kLastHeaderByteOffset, amountRead ) );

				data[ kLastHeaderByteOffset ] = lastHeaderByte;

				amountRead -= 1;

				if ( bufferSize > amountRead ) {
					Log.e( "USB control transfer read (0xF0) returns bufferSize != amountRead (" + bufferSize + " != " + amountRead + ")" );
					bufferSize = amountRead;
					Utility.putUnsignedInt( bufferSize, data, kParam1Offset );
				}

				length += bufferSize;
			}

			mInputStream.append( data, kControlTransferF0HeaderSize, length );
			mPacketPending=false;
		}
	}

	private class USBHIDOutputStream extends OutputStream {
		boolean			mClosed;

		@Override
		public synchronized void close() throws IOException {
			mClosed = true;
		}

		@Override
		public void write( int oneByte ) throws IOException {
			byte[] buffer = { (byte)(oneByte & 0xff) };

			write( buffer, 0, 1 );
		}

		@Override
		public void write(byte[] buffer) throws IOException {
			write( buffer, 0, buffer.length );
		}

		@Override
		public synchronized void write( byte[] buffer, int offset, int length ) throws IOException {
			if ( buffer == null || buffer.length == 0 || length < 1 ) return;
			if ( buffer.length - offset < length ) throw new IllegalArgumentException( "length > available buffer data" );

			final int		kDataSize = 256;
			final int		kTimeoutMs = 5000;

			byte[]			data = new byte[ kControlTransferF1HeaderSize + kDataSize ];

			while ( length > 0 ) {
				synchronized( this ) {
					if ( mClosed ) throw new IOException( "USBSocket closed" );
				}

				int			amountWritten;
				long		bufferSize = 0;
				boolean		hasBuffer = ( buffer[ Packet.kOpcodeOffset ] & Packet.Opcode.BufferFlag.value() ) != 0;

				if ( length < Packet.kPacketHeaderSize ) throw new IllegalArgumentException( "write buffer does not contain a packet header" );

				if ( hasBuffer ) {
					bufferSize = Utility.getUnsignedInt( buffer, Packet.kParam1Offset );

					if ( bufferSize > kDataSize ) throw new IllegalArgumentException( "packet buffer cannot exceed " + kDataSize + " bytes" );
					if ( bufferSize > 0 ) {
						if ( Packet.kPacketHeaderSize + bufferSize > length ) {
							throw new IllegalArgumentException( "buffer does not contain all packet data" );
						}

						data[ 0 ] = (byte) 0xF1;
						System.arraycopy( buffer, offset + Packet.kPacketHeaderSize, data, kControlTransferF1HeaderSize, (int) bufferSize );
						Arrays.fill( data, kControlTransferF1HeaderSize + (int) bufferSize, data.length, (byte) 0 );

						mConnection.claimInterface(mInterface,true);
						amountWritten = mConnection.controlTransfer( 0x21, 0x09, 0x03F1, 0, data, data.length, kTimeoutMs );

						if ( amountWritten != data.length ) {
							throw new IOException( "Failed to write data to USB control transfer endpoint (0xF1)" );
						} else {
//							Log.d( "wrote to 0xF1 control endpoint:\n" + Debug.hexify( data ) );
						}
					}
				}

				data[ 0 ] = (byte) 0xF0;
				data[ 1 ] = 0;
				System.arraycopy( buffer, offset, data, kControlTransferF0HeaderSize, Packet.kPacketHeaderSize );

				amountWritten = mConnection.controlTransfer( 0x21, 0x09, 0x03F0, 0, data, kHeaderSize, kTimeoutMs );

				if ( amountWritten != kHeaderSize ) {
					Log.e( "control transfer 0xF0 write failed" );
					throw new IOException( "Failed to write data to USB control transfer endpoint (0xF0)" );
				} else {
//					Log.d( "wrote to 0xF0 control endpoint:\n" + Debug.hexify( data ) );
				}

				offset += Packet.kPacketHeaderSize + bufferSize;
				length -= Packet.kPacketHeaderSize + bufferSize;
			}
			mPacketPending=true;
		}
	}
}
