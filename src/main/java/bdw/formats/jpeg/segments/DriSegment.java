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
package bdw.formats.jpeg.segments;

import bdw.formats.jpeg.segments.base.SegmentBase;
import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.ParseMode;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Define Restart Interval
 * According to one source:
 * [16 it value] in units of MCU blocks,
 * meaning that every n MCU blocks a RSTn marker can be found.
 * The first marker will be RST0, then RST1 etc, after RST7
 * repeating from RST0.
 * Another source says that if the value is 0, there will be no rst segments
 */
public class DriSegment extends SegmentBase {

	/**
	 * The only allowable marker for this segment type
	 */
	public static final int MARKER = 0xDD;

	/**
	 * The restart interval this segment is managing
	 */
	private int restartInterval;

	/**
	 * Constructor
	 */
	public DriSegment() {
		restartInterval = 0;
		setMarker(DriSegment.MARKER);
	}

	/**
	 * Construct an instance from a stream, parsing it strictly.
	 *
	 * @param stream The stream to read from
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
    public DriSegment(InputStream stream) throws IOException, InvalidJpegFormat {
		this(stream, ParseMode.STRICT);
    }

	/**
	 * Construct an instance from a stream.
	 *
	 * @param stream The stream to read from
	 * @param mode The mode to parse this in. At this time, no distinction is made between modes.
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public DriSegment(InputStream stream, ParseMode mode) throws IOException, InvalidJpegFormat {
		this();
		super.readFromStream(stream, mode);
    }

	/**
	 * Construct an instance from a stream. Parses it strictly
	 *
	 * @param file The file to read from
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
    public DriSegment(RandomAccessFile file) throws IOException, InvalidJpegFormat {
		this(file, ParseMode.STRICT);
    }

	/**
	 * Construct an instance from a stream.
	 *
	 * @param file The file to read from
	 * @param mode The mode to parse this in. At this time, no distinction is made between modes.
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public DriSegment(RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
		this();
		super.readFromFile(file, mode);
    }

	/**
	 * Checks whether instances of this class should be constructed
	 * with the specified marker.
	 *
	 * @param marker The marker to check.
	 * @return true if this conventionally can be associated with that marker.
	 */
	public static boolean canHandleMarker(int marker) {
		if (marker == DriSegment.MARKER) {
			return true;
		}
		return false;
	}

	/**
	 * @return The interval value
	 */
	public int getRestartInterval() {
		return restartInterval;
	}

	/**
	 * @param interval The interval [0-65535]
	 */
	public void setRestartInterval(int interval) {
		paramIsUInt16(interval);

		restartInterval = interval;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		super.write(stream);
		DataOutputStream dataStream = super.wrapAsDataOutputStream(stream);

		dataStream.writeShort(4);

		dataStream.writeShort(getRestartInterval());
	}

	/**
	 * Two DriSegments are equal if they have the same restart interval
	 * @param other The other object to test
	 * @return If other and this are equal
	 */
	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof DriSegment)) {
			return false;
		}

		DriSegment driOther = (DriSegment) other;

		if (getRestartInterval() != driOther.getRestartInterval()) {
			return false;
		}

		return true;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + this.restartInterval;
		return hash;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void readData(DataInput input, ParseMode mode) throws IOException, InvalidJpegFormat {
		int contentLength = input.readUnsignedShort();
		if (contentLength != 4) {
			throw new InvalidJpegFormat("got the wrong length in a DRI segment");
		}

		setRestartInterval(input.readUnsignedShort());
		// VALID CHECK: Don't know if there's a valid range of values here.
	}

}
