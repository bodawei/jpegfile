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

package bdw.formats.utf8;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads variable width integers off of an input stream, and returns them as integers
 * This is the same integer format underlying UTF-8.  See http://tools.ietf.org/html/rfc3629 and http://en.wikipedia.org/wiki/UTF-8
 * However, this handles integers from 0 up to Integer.MAX_INT (which is larger than what UTF-8 allows
 * In Strict mode (default is true), although the integer '1' can be represented with 6 bytes, this will reject it because it isn't the minimal representation.
 */
public class VarIntParser {
	private static final int B0000_0000 = 0x00;
	private static final int B1000_0000 = 0x80;
	private static final int B1100_0000 = 0xC0;
	private static final int B1110_0000 = 0xE0;
	private static final int B1111_0000 = 0xF0;
	private static final int B1111_1000 = 0xF8;
	private static final int B1111_1100 = 0xFC;
	private static final int B1111_1110 = 0xFE;
	private static final int CONTINUATION_MASK = B1100_0000;
	private static final int CONTINUATION_BITS = B1000_0000;
	
	private InputStream stream;
	private boolean strict;
	
	/**
	 * Construct an instance with strict mode on
	 * @param stream the stream to read from
	 */
	public VarIntParser(InputStream stream) {
		if (stream.markSupported()) {
			this.stream = stream;
		} else {
			this.stream = new BufferedInputStream(stream, 1);
		}
		this.strict = true;
	}

	/**
	 * @param value the value for the strict mode
	 */
	public void setStrict(boolean value) {
		this.strict = value;
	}

	/**
	 * @return the strict mode
	 */
	public boolean getStrict() {
		return this.strict;
	}

	/**
	 * @return True if this is at the end of the input stream
	 * @throws IOException If something goes wrong when touching the input stream
	 */
	public boolean atEos() throws IOException {
		this.stream.mark(1);
		int aByte = this.stream.read();
		this.stream.reset();

		return (aByte == -1);
	}

	/**
	 * Gets the next integer from the input stream
	 * @return The next integer
	 * @throws IOException If something, in general, goes wrong on the input stream
	 * @throws MalformedInt If the data in the stream doesn't properly represent an integer (if strict mode is on, this will include non-minimally represented integers)
	 * @throws EOFException If this tries to read past the end of file
	 */
	public int readInt() throws EOFException, IOException, MalformedIntException {
		int result;
		int leadByte = this.stream.read();

		assert(leadByte <= 255);

		if (leadByte == -1) {
			throw new EOFException();
		}
		
		// See the standards for what these masks mean, in general check to see if the header bits are set properly, and if so, then read in the trailing bits
		
		if ((leadByte & B1000_0000) == B0000_0000) {
			result = leadByte;
		} else if ((leadByte & B1110_0000) == B1100_0000) {
			result = this.readTailBytes(1, (leadByte & ~B1110_0000));
			this.checkStrict(result, 0x7F, 0x7FF);
		} else if ((leadByte & B1111_0000) == B1110_0000) {
			result = this.readTailBytes(2, (leadByte & ~B1111_0000));
			this.checkStrict(result, 0x7FF, 0xFFFF);
		} else if ((leadByte & B1111_1000) == B1111_0000) {
			result = this.readTailBytes(3, (leadByte & ~B1111_1000));
			this.checkStrict(result, 0xFFFF, 0x1FFFFF);
		} else if ((leadByte & B1111_1100) == B1111_1000) {
			result = this.readTailBytes(4, (leadByte & ~B1111_1100));
			this.checkStrict(result, 0x1FFFFF, 0x3FFFFFF);
		} else if ((leadByte & B1111_1110) == B1111_1100) {
			result = this.readTailBytes(5, (leadByte & ~B1111_1110));
			this.checkStrict(result, 0x3FFFFFF, 0x7FFFFFFF);
		} else {
			throw new MalformedIntException("Invalid header byte", leadByte);
		}
		return result;
	}

	/**
	 * Validates that the integer is representing a number in the range (lower, upper]
	 * 
	 * @param theInt The integer to return
	 * @param lower the lower end of the range
	 * @param upper the upper end of the range
	 * @throws MalformedInt If the integer is out of range
	 */
	private void checkStrict(int theInt, int lower, int upper) throws MalformedIntException {
		if (this.strict) {
			if ((theInt <= lower) || (theInt > upper)) {
				throw new MalformedIntException("Not a minimal representation", theInt);
			}
		}
	}

	/**
	 * Read numBytes trailing bytes.  Each must have the top two bits set to 0b10xxxxxx
	 * 
	 * @param numBytes Number of bytes to read in
	 * @param start The uppermost bits of the integer to form, fromt he leading byte
	 * @return the integer formed from the data bits of each byte
	 * @throws IOException General IO problem
	 * @throws MalformedInt The data in the stream didn't represent the integer (e.g. we couldn't read numBytes bytes, because we reached eos, or a byte without 0b10xxxxxx
	 */
	private int readTailBytes(int numBytes, int start) throws IOException, MalformedIntException {
		int total = start;
		
		for (int index = 0; index < numBytes; index++) {
			int aByte;
			
			this.stream.mark(1);
			aByte = this.stream.read();

			if (aByte == -1) {
				throw new MalformedIntException("Integer terminated early (" + (index+1) + " bytes read, " + (numBytes+1) + " expected)");
			}
			
			if ((aByte & CONTINUATION_MASK) != CONTINUATION_BITS) {
				this.stream.reset();
				throw new MalformedIntException("Integer terminated early. Found byte without continuation bits.", aByte);
			}
			
			total = total << 6;
			total = total | (aByte & ~CONTINUATION_MASK);
		}
		
		return total;
	}

}
