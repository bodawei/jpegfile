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

import java.io.IOException;

/**
 * This utility provides a scheme for managing a buffer of bytes.  It has a
 * very specific usage pattern.
 *
 * At the high level, it has the same mark() and reset() scheme as an
 * InputStream.  Call mark(N), and this will remember up to the next N bytes.
 * If at any point during during those N bytes one calls reset(), then those
 * bytes can, sequentially, be retrieved.
 *
 * However, this is not a stream, but instead is to be used by stream-like
 * things.  In those cases, those users would make use of this like: Call
 * mustRead(). If that returns true, then there are buffered bytes that must
 * be read.  If that is false, then one must retrieve bytes from some the
 * original source. In the latter case one would then call mustAdd(), and if
 * that is true, then the retrieved byte must be added.  The intent of this
 * slightly hard to wrap-ones-mind-around interface is to allow a client to
 * not worry whether it is in "buffering" mode or not. These calls will all
 * maintain state for the caller, so it can do the right thing.
 *
 * Calling mark() again while one is still mark()ing is fine. This simply starts
 * a new buffering at this point (any previous bytes are discarded). This can
 * result in some odd cases where the amount of data the buffer is buffering
 * is larger than the amount requested (e.g. mark(10), addByte() 10 times,
 * reset(), mark(2).  In that case, this is still managing a buffer of 10
 * bytes.
 *
 * Calling readByte() past the end of the buffer will simply silently shut
 * down the buffering, and calling mustRead() will then return false.
 *
 * Calling addByte() past the end of the asked-for mark()'ed bytes will also
 * silently shut down the buffering. Again, calling mustRead() or mustAdd()
 * will both return false.
 *
 * Calling reset() when buffering isn't happening will throw an exception.
 */
public class ByteBuffer {
	private byte[] buffer = null;
	private int bufferedByteCount = 0;
	private int bufferIndex = 0;
	private int promisedLength = 0;
	private boolean consumingBuffer;

	/*
	 * Start accepting bytes to be buffered, or (if we are already buffering)
	 * discard older bytes and preserve existing and subsequently new ones.
	 */
	public void mark(int length) {
		int bytesRemaining = bufferedByteCount - bufferIndex;
		byte[] newBuffer = new byte[Math.max(length, bytesRemaining)];

		if (bytesRemaining != 0) {
			int destIndex = 0;
			for (int srcIndx = bufferIndex; srcIndx < bufferedByteCount; srcIndx++) {
				newBuffer[destIndex] = buffer[srcIndx];
				destIndex++;
			}

			consumingBuffer = true;
		}

		promisedLength = length;
		bufferedByteCount = bytesRemaining;
		bufferIndex = 0;
		buffer = newBuffer;
	}

	/*
	 * Switch back to the beginning of any buffered bytes.
	 */
	public void reset() throws IOException {
		if (buffer == null) {
			throw new IOException("Tried to reset when there is (no longer) any buffering happening.");
		}

		if (bufferIndex <= promisedLength) {
			bufferIndex = 0;
			consumingBuffer = (bufferedByteCount != 0);
		}
	}

	/*
	 * This buffer is ready to accept new bytes. This means this is actively
	 * in the middle of a mark().
	 */
	public boolean canAdd() {
		return !consumingBuffer && buffer != null;
	}

	/*
	 * Adds a byte to the buffer.  If this would exceed the length specified
	 * in mark(), this will then silently stop buffering.
	 */
	public void addByte(byte aByte) {
		if (consumingBuffer) {
			throw new IllegalStateException("Cannot add when reading");
		}
		if (buffer == null) {
			throw new IllegalStateException("Cannot add before marking");
		}

		if (bufferIndex >= buffer.length) {
			buffer = null;
			bufferIndex = 0;
			bufferedByteCount = 0;
			return;
		}

		buffer[bufferIndex] = aByte;
		bufferIndex++;
		bufferedByteCount ++;
	}

	/*
	 * Indicates whether one should read bytes from this buffer rather than
	 * the original data source.
	 */
	public boolean mustRead() {
		return consumingBuffer;
	}

	/*
	 * Reads a byte from this buffer.  If doing so exhusts the available data,
	 * then shut down buffering.
	 */
	public byte readByte() {
		if (!consumingBuffer) {
			throw new IllegalStateException("Nothing to read from buffer");
		}

		byte aByte = buffer[bufferIndex];

		bufferIndex++;

		if (bufferIndex == bufferedByteCount) {
			consumingBuffer = false;
			if (bufferIndex == buffer.length) {
				buffer = null;
				bufferIndex = 0;
				bufferedByteCount = 0;
			}
		}

		return aByte;
	}
}
