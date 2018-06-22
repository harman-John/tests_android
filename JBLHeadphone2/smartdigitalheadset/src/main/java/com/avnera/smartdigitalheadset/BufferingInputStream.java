/*
 * ByteBufferBackedInputStream.java
 * internal
 *
 * Created by Brian Doyle on 2/7/16
 * Copyright (c) 2016 Balance Software, Inc.
 *
 */
package com.avnera.smartdigitalheadset;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Brian Doyle
 */
public class BufferingInputStream extends InputStream {
	private class Buffer {
		public byte[]					mBuffer;
		public int						mOffset;
		public int						mLength;
		public Buffer					mNext;
	}

	protected int						mAvailable;
	protected boolean					mClosed;
	protected Buffer					mHead;
	protected Buffer					mTail;

	@Override
	public int available() {
		synchronized( this ) { return mAvailable; }
	}

	@Override
	public void close() throws IOException {
		synchronized( this ) {
			if ( mClosed ) return;

			mAvailable = 0;
			mClosed = true;
			mHead = null;
			mTail = null;
			notify();
		}
	}

	/**
	 * append() buffer to this stream's source.  Ownership of the buffer is
	 * transferred to BufferingInputStream and must not be used after calling
	 * append().
	 *
	 * @param buffer The buffer to hand off to this BufferingInputStream
	 * @param offset Distance into buffer to start reading data from
	 * @param length append length bytes starting at buffer + offset
	 * @throws IOException if the input stream is closed
	 */
	public void append( byte[] buffer, int offset, int length ) throws IOException {
		if ( buffer == null || length < 1 ) return;
		if ( offset + length > buffer.length ) throw new IllegalArgumentException( "offset + length > buffer.length" );

		Buffer b = new Buffer();

		b.mBuffer = buffer;
		b.mOffset = offset;
		b.mLength = length;

		synchronized( this ) {
			if ( mClosed ) throw new IOException( "InputStream is closed" );

			if ( mTail == null ) {
				mHead = b;
				mTail = b;
			} else {
				mTail.mNext = b;
				mTail = b;
			}

			mAvailable += length;

			notify();
		}
	}

	@Override
	public int read() throws IOException {
		byte[] buffer = new byte[ 1 ];

		return read( buffer, 0, 1 );
	}

	@Override
	public int read( byte[] buffer ) throws IOException {
		return read( buffer, 0, buffer.length );
	}

	@Override
	public int read( byte[] buffer, int offset, int length ) throws IOException {
		synchronized( this ) {
			while ( ! mClosed ) {
				if ( mHead == null ) {
					try { wait(); } catch ( Exception e ) { }
					continue;
				}

				int amountCopied = 0;

				while ( mHead != null && length > 0 ) {
					int amountToCopy = Math.min( mHead.mLength, length );

					System.arraycopy( mHead.mBuffer, mHead.mOffset, buffer, offset, amountToCopy );

					amountCopied += amountToCopy;
					mAvailable -= amountCopied;

					if ( ( mHead.mLength -= amountCopied ) == 0 ) {
						if ( ( mHead = mHead.mNext ) == null ) {
							mTail = null;
						}
					} else {
						mHead.mOffset += amountCopied;
					}

					length -= amountCopied;
					offset += amountCopied;
				}

				return amountCopied;
			}

			return -1;
		}
	}
}
