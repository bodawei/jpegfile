/*
 *  Copyright 2014 柏大衛
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bdw.io;


import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps a DataInput instance, and impose a limit on how many bytes may be read.
 * If this is asked to read more than the specified number of bytes, throw an
 * LimitExceeded exception.  You can think of this as imposing an artificial
 * EOF. The purpose of this is where a caller wants to make sure that a callee
 * doesn't read beyond a particular boundary.
 *
 * If a LimitExceeded exception is throw, you can be certain that all
 * all the bytes up to that limit have been read.  However, if another
 * exception is thrown (e.g. EOFException), the limit may not have been reached.
 *
 * Note: This this doesn't actually implement all the DataInput methods, but
 * only enough to support this JPEG system.
 */
public class LimitingDataInput extends InputStream implements DataInput {

	private int limit;
	private DataInput input;
	private ByteBuffer buffer;
	private int markLimit;

	/**
	 * Constructor
	 * @param input The datainput to read from
	 * @param limit The number of bytes to allow to be read
	 */
	public LimitingDataInput(DataInput input, int limit) {
		if (limit < 0) {
			throw new IllegalArgumentException("Limit must be positive");
		}
		if (input == null) {
			throw new IllegalArgumentException("input must be non-null");
		}

		this.limit = limit;
		this.input = input;
		this.buffer = new ByteBuffer();
	}

	/**
	 * Put a mark on the input stream.  This allows one to read up to size bytes
	 * and then reset() back to the current point, and read those bytes again.
	 *
	 * @param size The number of bytes to preserve.
	 */
	@Override
	public void mark(int size) {
		markLimit = limit;
		buffer.mark(size);
	}

	/**
	 * Roll back to the position marked with mark().  Note that if mark() has
	 * not been called, an exception may be thrown.
	 */
	@Override
	public void reset() throws IOException {
		buffer.reset();
		limit = markLimit;
	}

	/**
	 * @return true this supports a mark.
	 */
	@Override
	public boolean markSupported() {
		return true;
	}

	/**
	 * @return The number of bytes that this may still read
	 */
	public int getRemainingLimit() {
		return limit;
	}

	/**
	 * Read as many bytes as are in the byte array
	 */
	@Override
	public void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	/**
	 * Reads len bytes after skipping "off" bytes from the underlying DataInput.
	 */
	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		skipBytes(off);

		for (int index = 0; index < len; index++) {
			b[index] = readByte();
		}
	}

	/**
	 * Read and discard "n" bytes from the input
	 */
	@Override
	public int skipBytes(int n) throws IOException {
		for (int index = 0; index < n; index++) {
			readByte();
		}

		return n;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean readBoolean() throws IOException {
		if (limit < 1) {
			throw new LimitExceeded("Reading boolean exceeded read limit", 1, this);
		}
		byte result = readByte();

		return (result == 0) ? false : true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		int foo = (0xFF & readByte());
		return foo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte readByte() throws IOException {
		if (limit < 1) {
			throw new LimitExceeded("Reading byte exceeded read limit", 1, this);
		}

		byte result;

		if (buffer.mustRead()) {
			result = buffer.readByte();
		} else {
			result = input.readByte();

			if (buffer.canAdd()) {
				buffer.addByte(result);
			}
		}

		limit --;

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int readUnsignedByte() throws IOException {
		if (limit < 1) {
			throw new LimitExceeded("Reading unsigned byte exceeded read limit", 1, this);
		}

		byte aByte = readByte();

		return aByte & 0xFF ;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short readShort() throws IOException {
		if (limit < 2) {
			input.skipBytes(limit);
			int temp = limit;
			limit = 0;
			throw new LimitExceeded("Reading short exceeded limit", 2 - temp, this);
		}

		byte byteOne = readByte();
		byte byteTwo = readByte();

		return (short)((byteOne << 8) | (byteTwo & 0xff));
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int readUnsignedShort() throws IOException {
		if (limit < 2) {
			input.skipBytes(limit);
			int temp = limit;
			limit = 0;
			throw new LimitExceeded("Reading unsigned short exceeded limit", 2 - temp, this);
		}

		byte byteOne = readByte();
		byte byteTwo = readByte();

		int temp =  ((int) 0) | (((byteOne & 0xff) << 8) | (byteTwo & 0xff));
		return temp;
	}

	/**
	 * NOTE: This is not supported at this time
	 * @throws UnsupportedOperationException If this is directly called.
	 */
	@Override
	public char readChar() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * NOTE: This is not supported at this time
	 * @throws UnsupportedOperationException If this is directly called.
	 */
	@Override
	public int readInt() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * NOTE: This is not supported at this time
	 * @throws UnsupportedOperationException If this is directly called.
	 */
	@Override
	public long readLong() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * NOTE: This is not supported at this time
	 * @throws UnsupportedOperationException If this is directly called.
	 */
	@Override
	public float readFloat() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * NOTE: This is not supported at this time
	 * @throws UnsupportedOperationException If this is directly called.
	 */
	@Override
	public double readDouble() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * NOTE: This is not supported at this time
	 * @throws UnsupportedOperationException If this is directly called.
	 */
	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * NOTE: This is not supported at this time
	 * @throws UnsupportedOperationException If this is directly called.
	 */
	@Override
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
