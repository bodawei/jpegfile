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

import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.ParseMode;
import bdw.formats.jpeg.segments.base.SegmentBase;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * JpgN segment type
 */
public class JpgNSegment extends SegmentBase {
	/**
	 * The first of the allowable markers that this can be used with
	 */
	public static final int FIRST_SUBTYPE = 0xF0;

	/**
	 * Last of the allowable markers this can be used with
	 */
	public static final int LAST_SUBTYPE = 0xFD;

	/**
	 * Constructs an instance with all properties empty
	 */
	public JpgNSegment(int subType) throws InvalidJpegFormat {
		if (JpgNSegment.canHandleMarker(subType)) {
			setMarker(subType);		
		} else {
			throw new InvalidJpegFormat("The subtype " + subType + " is not applicable to " + this.getClass().getSimpleName());
		}
	}

	/**
	 * Construct an instance from a stream.
	 *
	 * @param stream The stream to read from
	 * @param mode The mode to parse this in. At this time, no distinction is made between modes.
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public JpgNSegment(int subType, InputStream stream, ParseMode mode) throws IOException, InvalidJpegFormat {
		this(subType);
		super.readFromStream(stream, mode);
    }

	/**
	 * Construct an instance from a stream.
	 *
	 * @param file The file to read from
	 * @param mode The mode to parse this in. At this time, no distinction is made between modes.
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public JpgNSegment(int subType, RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
		this(subType);
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
		if ((marker >= JpgNSegment.FIRST_SUBTYPE) && (marker <= JpgNSegment.LAST_SUBTYPE)) {
			return true;
		}
		return false;
	}

	/**
	 * All JpgSegments are equal.
	 * @param other The other object to test
	 * @return If other and this are equal
	 */
	@Override
	public boolean equals(Object other) {
		return ((other == null) || !(other instanceof JpgNSegment)) ? false : true;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public int hashCode() {
		int hash = 3;
		return hash;
	}
}