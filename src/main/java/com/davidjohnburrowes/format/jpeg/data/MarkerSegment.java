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
import com.davidjohnburrowes.io.LimitingDataInput;
import com.davidjohnburrowes.util.Util;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

/*
 * Base class for all segments that have parameters after the marker.
 * MarkerSegments are all assumed to have a two byte size field after the 0xFF
 * and markerId.  The read() routines will read in this size, and the write()
 * routine will write it back out. In addition, in some cases it is
 * theoretically possible to have extra bytes in the segment after the defined
 * data has been read in. in this case, this will finish the reading process
 * by reading these bytes in and storing them in the trailingBytes property.
 * This will be accepted, however, only in LAX mode.
 */
public class MarkerSegment extends Marker {
	/*
	 * Any trailing bytes associated with this segment.
	 */
	private byte[] trailingBytes = null;

	/*
	 * Constructor!
	 */
	public MarkerSegment(int markerId) {
		super(markerId);
	}

	/*
	 * Allows one to specify a set of "junk" bytes that come after the data
	 * portion on a segment.
	 */
	public void setTrailingBytes(byte[] bytes) {
		trailingBytes = bytes;
	}

	public byte[] getTrailingBytes() {
		return trailingBytes == null ? new byte[0] : trailingBytes;
	}

	/**
	 * {@inheritDoc}
	 * @return The number of bytes this segment takes up on disk
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + 2 + getTrailingBytes().length; // the size bytes plus trailing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void read(RandomAccessFile file) throws IOException {
		super.read(file);

		int byteCount = file.readUnsignedShort();
		LimitingDataInput ldi = new LimitingDataInput(file, byteCount - 2);

		readParameters(ldi);

		if (ldi.getRemainingLimit() != 0 && getDataMode() == DataMode.STRICT) {
			throw new InvalidJpegFormat("" + ldi.getRemainingLimit() + " too many bytes trailing.");
		} else {
			readTrailingBytes(ldi);
		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * Reads the segment size, then any other parameters, and finally any trailing
	 * bytes.
	 *
	 * @param stream The file to read from (not null)
	 *
	 * @throws IOException If something really unexpected happens when reading.
	 */
	@Override
	public void read(InputStream stream) throws IOException {
		super.read(stream);

		DataInput wrapped = Util.wrapAsDataInput(stream);
		int byteCount = wrapped.readUnsignedShort();
		LimitingDataInput ldi = new LimitingDataInput(wrapped, byteCount - 2);

		readParameters(ldi);

		if (ldi.getRemainingLimit() != 0 && getDataMode() == DataMode.STRICT) {
			throw new InvalidJpegFormat("" + ldi.getRemainingLimit() + " too many bytes trailing.");
		} else {
			readTrailingBytes(ldi);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Writes out the size, then any other parameters for the segment, and then
	 * the trailing bytes.
	 *
	 * @param stream The stream to write to
	 * @throws IOException If an io problem happens.
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		super.write(stream);

		DataOutputStream dataOut = Util.wrapAsDataOutput(stream);

		dataOut.writeShort(getParameterSizeOnDisk());

		this.writeParameters(dataOut);

		if (getTrailingBytes().length != 0) {
			dataOut.write(getTrailingBytes());
		}
	}

	/*
	 * {@inheritDoc}
	 *
	 * Clears any trailing bytes.
	 */
	@Override
	public void clearPassthrough() {
		super.clearPassthrough();
		setTrailingBytes(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		return (super.equals(other) &&
			Arrays.equals(this.getTrailingBytes(), ((MarkerSegment)other).getTrailingBytes()));
	}

	/**
	 * Convenience routine called by both read() routines to parse their data.
	 * Subclasses will generally want to override this, rather than the two
	 * read() routines.
	 *
	 * @param dataSource The input source to read from
	 *
	 * @throws IOException If the data source vanishes, is closed by someone else
	 *		or is otherwise bad.
	 */
	protected void readParameters(LimitingDataInput dataSource) throws IOException {
	}

	/*
	 * Subclasses should override this to write data from the segument.
	 *
	 * @param stream The stream to write to
	 *
	 * @throws IOException If the stream vanishes, is closed by someone else
	 *		or is otherwise bad.
	 */
	protected void writeParameters(DataOutputStream stream) throws IOException {
	}

	/**
	 * Read any bytes remaining in the limitingDataInput.
	 */
	private void readTrailingBytes(LimitingDataInput dataSource) throws IOException {
		if (dataSource.getRemainingLimit() == 0) {
			return;
		}
		byte[] bytes = new byte[dataSource.getRemainingLimit()];
		dataSource.readFully(bytes);
		this.setTrailingBytes(bytes);
	}
}
