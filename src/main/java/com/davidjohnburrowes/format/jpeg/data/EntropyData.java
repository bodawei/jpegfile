/*
 *  Copyright 2014,2017 柏大衛
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
package com.davidjohnburrowes.format.jpeg.data;

import com.davidjohnburrowes.format.jpeg.support.DataMode;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.util.ByteArrayBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a series of Entropy Data bytes from a JPEG data stream.
 * This copes with one special case, which is when the last byte found is a
 * 0xFF. In any reasonably formed file, it shouldn't be (it should, in fact,
 * be the introduction to another marker).  Nonetheless, if for some reason
 * this is jpeg data without a trailing marker, this will simply store the
 * 0xFF and write it back out.  If clearPassthrough() is called, this indication
 * will be reset, and the 0xFF will be written out as 0xFF and 0x00
 */
public class EntropyData extends DataItem {
	private ByteArrayBuilder builder;
	private boolean trailingFF;
	private byte[] data;

	/*
	 * Ordinary constructor.
	 */
	public EntropyData() {
		builder = new ByteArrayBuilder();
		trailingFF = false;
	}

	/**
	 * @return a byte array of the entropy coded data
	 */
	public byte[] getData() {
		if (data == null) {
			data = builder.toArray();
		}

		return data;
	}

	/**
	 * Note that this takes the bytes literally as presented. These should not
	 * have 0xFF bytes escaped with a 0x00 byte as happens on disk.
	 * @param data the data to be stored as entropy coded data
	 */
	public void setData(byte[] data) {
		this.data = data;
		builder = new ByteArrayBuilder();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void read(RandomAccessFile file) throws IOException {
		ByteArrayBuilder localBuilder = new ByteArrayBuilder();
		long fileLength = file.length();

		while (true) {
			long startingPos = file.getFilePointer();
			int aByte;

			if (fileLength < startingPos + 1) {
				break;
			}

			aByte = file.readByte();

			if (aByte == -1) {
				if (fileLength < startingPos + 2) {
					localBuilder.append((byte)aByte);
					if (getDataMode() == DataMode.STRICT) {
						throw makeTrailingFfException();
					}
					trailingFF = true;
					break;
				}

				int markerByte = file.readByte();

				if (markerByte == 0x00) {
					localBuilder.append((byte)aByte);
				} else {
					file.seek(startingPos);
					break;
				}
			} else {
				localBuilder.append((byte)aByte);
			}
		}

		builder = localBuilder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void read(InputStream stream) throws IOException {
		int aByte;
		boolean keepGoing = true;
		ByteArrayBuilder localBuilder = new ByteArrayBuilder();

		while (keepGoing) {
			stream.mark(2);	// allow for rollback if we find 0xFF 0xXX

			aByte = stream.read();
			switch(aByte) {
				case -1:	// eof
					keepGoing = false;
					break;
				case 0xFF:
					int markerByte = stream.read();

					switch (markerByte) {
						case -1:
							localBuilder.append((byte)aByte);
							keepGoing = false;
							if (getDataMode() == DataMode.STRICT) {
								throw makeTrailingFfException();
							}
							trailingFF = true;
							break;
						case 0x00:
							localBuilder.append((byte)aByte);
							break;
						default:
							stream.reset();
							keepGoing = false;
							break;
					}
					break;
				default:
					localBuilder.append((byte)aByte);
					break;
			}
		}

		builder = localBuilder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		super.write(stream);
		byte[] bytes = getData();

		for (int index = 0; index < bytes.length; index++) {
			stream.write(bytes[index]);
			if (bytes[index] == (byte)0xFF &&
					  (index != bytes.length - 1 ||
					  (index == bytes.length - 1 && !trailingFF))) {
				stream.write(0);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();

		RuntimeException e = checkValidityCondition(DataMode.STRICT);
		if (e != null) {
			results.add(e);
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 * If read() found a trailing 0xFF, this will unset that, so that writing it
	 * back out will write 0xFF 0x00
	 */
	@Override
	public void clearPassthrough() {
		super.clearPassthrough();
		trailingFF = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null || (! (other instanceof EntropyData))) {
			return false;
		}
		return Arrays.equals(this.getData(), ((EntropyData)other).getData());
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 67 * hash + (this.trailingFF ? 1 : 0);
		hash = 67 * hash + Arrays.hashCode(this.getData());
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void checkModeChange() {
		super.checkModeChange();

		RuntimeException e = checkValidityCondition(getDataMode());
		if (e != null) {
			throw e;
		}
	}

	/**
	 * Checks whether this is STRICTly valid (if there is no trailing 0xFF)
	 */
	private RuntimeException checkValidityCondition(DataMode mode) {
		if (mode == DataMode.STRICT && trailingFF) {
			return makeTrailingFfException();
		}

		return null;
	}

	/**
	 * Convenience routine.  Returns an exception which can be used any time we
	 * need to report a trailing 0xFF
	 */
	private RuntimeException makeTrailingFfException() {
		return new InvalidJpegFormat("Entropy coded data has 0xFF as the final byte without the expected 0x00 padding afterwards.");
	}
}