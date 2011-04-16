/*
 *  Copyright 2011 æŸ�å¤§è¡›
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

package bdw.util;

import java.util.Arrays;

/**
 * This is something like a string builder, but instead builds an array
 * of bytes.
 * At this moment, this is a minimal API, and it really needs to be extended
 */
public class ByteArrayBuilder {
	// Realistically, this implementation is probably not good, as it
	// involves a lot of realloc()'s.  Better, I think, if it just kept
	// an array of array chunks.  But, the implementation should not be
	// visible from outside this.

	/**
	 * Amount to realloc the buffer by
	 */
	private static final int REALLOC_SIZE = 1024;
	/**
	 * The buffer we use to keep the array of bytes.
	 */
	private byte[] buffer;

	/**
	 * Number of data bytes in the buffer (buffer.length > dataLength)
	 */
	private int dataLength;

	/**
	 * Builds an instance (no duh)
	 */
	public ByteArrayBuilder() {
		buffer = new byte[1024];
		dataLength = 0;
	}

	/**
	 * @return The size of the data in this builder
	 */
	public int getSize() {
		return dataLength;
	}

	/**
	 * Adds the byte to the end of the builder's buffer
	 * @param value Byte to append
	 */
	public void append(byte value) {
		if (dataLength >= buffer.length) {
			reallocBuffer();
		}
		buffer[dataLength] = value;
		dataLength++;
	}

	/**
	 * Append an integer. Note that this integer must be in the range of a
	 * byte.
	 * @param value the value to append
	 */
	public void append(int value) {
		if ((value < Byte.MIN_VALUE) || (value > Byte.MAX_VALUE)) {
			throw new IllegalArgumentException("Int must be in range of a byte");
		}

		append((byte) value);
	}

	/**
	 * Removes one byte from the end of the buffer. If there are no bytes,
	 * does nothing.
	 */
	public void deAppend() {
		if (dataLength > 0) {
			dataLength --;
		}
	}
	/**
	 * @param index The location of the byte to return
	 * @return The byte at the specified index
	 */
	public byte getByteAt(int index) {
		if ((index < 0) || (index > dataLength)) {
			throw new IndexOutOfBoundsException("Index too large or negative");
		}
		return buffer[index];
	}

	/**
	 * Sets the value of a byte at the specified index to the specified value
	 * If this is beyond the end of the current length, the buffer is resized to
	 * accomodate the position.
	 *
	 * @param index Position to put the new byte value
	 * @param value The byte value to be added
	 */
	public void setByteAt(int index, byte value) {
		if ((value < Byte.MIN_VALUE) || (value > Byte.MAX_VALUE)) {
			throw new IllegalArgumentException("Int must be in range of a byte");
		}
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index was negative");
		}

		if (index >= buffer.length) {
			buffer = Arrays.copyOf(buffer, index+1);
		}

		if (index + 1 > dataLength) {
			dataLength = index + 1;
		}

		buffer[index] = value;
	}

	/**
	 * Sets the value of a byte at the specified index to the specified value
	 * If this is beyond the end of the current length, the buffer is resized to
	 * accomodate the position.
	 *
	 * @param index Position to put the new byte value
	 * @param value The byte value to be added. Must be in the range of a byte!
	 */
	public void setByteAt(int index, int value) {
		setByteAt(index, (byte) value);
	}

	/**
	 * Make the buffer larger, if needed.
	 */
	private void reallocBuffer() {
		buffer = Arrays.copyOf(buffer, buffer.length + ByteArrayBuilder.REALLOC_SIZE);
	}
}
