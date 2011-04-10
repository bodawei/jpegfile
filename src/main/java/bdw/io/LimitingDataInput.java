/*
 *  Copyright 2011 柏大衛
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

/**
 * Wraps another instance of a DataInput interface, but limit how many
 * bytes it may be used to read.  If this is asked to read more than the
 * specified number of bytes, throw an LimitExceeded exception.
 *
 * If a LimitExceeded exception is throw, you can be certain that all
 * all the bytes up to that limit have been read.  However, if another
 * exception is thrown (e.g. eof), the limit that this imposes may not
 * be in alignment with the number of bytes read. (ICK) To be safe, do
 * not use this instance again if any exception has been thrown.
 *
 * Note: This only wraps the methods defined by the DataInput interface,
 * and not things like read() which may be on a class that implements this.
 */
public class LimitingDataInput implements DataInput {
	
	private int limit;
	private DataInput input;

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

	}

	/**
	 * @return The number of bytes that this may still read
	 */
	public int getRemainingLimit() {
		return limit;
	}

	/**
	 * NOTE: This is not supported at this time
	 * @throws UnsupportedOperationException
	 */
	public void readFully(byte[] b) throws IOException {

		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * NOTE: This is not supported at this time
	 * @throws UnsupportedOperationException
	 */
	public void readFully(byte[] b, int off, int len) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * NOTE: This is not supported at this time
	 * @throws UnsupportedOperationException
	 */
	public int skipBytes(int n) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * @inheritdoc
	 */
	public boolean readBoolean() throws IOException {
		if (limit < 1) {
			throw new LimitExceeded("Reading boolean exceeded read limit", 1, this, null);
		} else {
			boolean result = input.readBoolean();
			limit --;
			return result;
		}
	}

	/**
	 * @inheritdoc
	 */
	public byte readByte() throws IOException {
		if (limit < 1) {
			throw new LimitExceeded("Reading byte exceeded read limit", 1, this, null);
		} else {
			byte result = input.readByte();
			limit --;
			return result;
		}
	}

	/**
	 * @inheritdoc
	 */
	public int readUnsignedByte() throws IOException {
		if (limit < 1) {
			throw new LimitExceeded("Reading unsigned byte exceeded read limit", 1, this, null);
		} else {
			int result = input.readUnsignedByte();
			limit --;
			return result;
		}
	}

	/**
	 * @inheritdoc
	 */
	public short readShort() throws IOException {
		if (limit < 2) {
			input.skipBytes(limit);
			int temp = limit;
			limit = 0;
			throw new LimitExceeded("Reading short exceeded limit", 2 - temp, this, null);
		} else {
			short result = input.readShort();
			limit -= 2;
			return result;
		}
	}


	/**
	 * @inheritdoc
	 */
	public int readUnsignedShort() throws IOException {
		if (limit < 2) {
			input.skipBytes(limit);
			int temp = limit;
			limit = 0;
			throw new LimitExceeded("Reading unsigned short exceeded limit", 2 - temp, this, null);
		} else {
			int result = input.readUnsignedShort();
			limit -= 2;
			return result;
		}
	}

	/**
	 * @inheritdoc
	 */
	public char readChar() throws IOException {
		if (limit < 2) {
			input.skipBytes(limit);
			int temp = limit;
			limit = 0;
			throw new LimitExceeded("Reading char exceeded limit", 2 - temp, this, null);
		} else {
			char result = input.readChar();
			limit -= 2;
			return result;
		}
	}

	/**
	 * @inheritdoc
	 */
	public int readInt() throws IOException {
		if (limit < 4) {
			input.skipBytes(limit);
			int temp = limit;
			limit = 0;
			throw new LimitExceeded("Reading int exceeded limit", 4 - temp, this, null);
		} else {
			int result = input.readInt();
			limit -= 4;
			return result;
		}
	}

	/**
	 * @inheritdoc
	 */
	public long readLong() throws IOException {
		if (limit < 8) {
			input.skipBytes(limit);
			int temp = limit;
			limit = 0;
			throw new LimitExceeded("Reading long exceeded limit", 8 - temp, this, null);
		} else {
			long result = input.readLong();
			limit -= 8;
			return result;
		}
	}

	/**
	 * @inheritdoc
	 */
	public float readFloat() throws IOException {
		if (limit < 4) {
			input.skipBytes(limit);
			int temp = limit;
			limit = 0;
			throw new LimitExceeded("Reading float exceeded limit", 4 - temp, this, null);
		} else {
			float result = input.readFloat();
			limit -= 4;
			return result;
		}
	}

	/**
	 * @inheritdoc
	 */
	public double readDouble() throws IOException {
		if (limit < 8) {
			input.skipBytes(limit);
			int temp = limit;
			limit = 0;
			throw new LimitExceeded("Reading double exceeded limit", 8 - temp, this, null);
		} else {
			double result = input.readDouble();
			limit -= 8;
			return result;
		}
	}

	/**
	 * NOTE: This is not supported at this time
	 * @throws UnsupportedOperationException
	 */
	public String readLine() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * NOTE: This is not supported at this time
	 * @throws UnsupportedOperationException
	 */
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
