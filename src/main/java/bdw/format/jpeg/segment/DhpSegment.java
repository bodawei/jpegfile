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
package bdw.format.jpeg.segment;

import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.ParseMode;
import bdw.format.jpeg.segment.base.SegmentBase;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Define Hierarchical Progression
 */
public class DhpSegment extends SegmentBase {
	/**
	 * Marker for this segment type
	 */
	public static final int SUBTYPE = 0xDE;

	/**
	 * Construct
	 */
	public DhpSegment() {
		setMarker(DhpSegment.SUBTYPE);
	}

	/**
	 * Constructs an instance with all properties empty
	 */
	public DhpSegment(int subType) throws InvalidJpegFormat {
		if (DhpSegment.canHandleMarker(subType)) {
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
	public DhpSegment(int subType, InputStream stream, ParseMode mode) throws IOException, InvalidJpegFormat {
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
	public DhpSegment(int subType, RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
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
		if (marker == DhpSegment.SUBTYPE) {
			return true;
		}
		return false;
	}

	/**
	 * All SoiSegments are equal.
	 * @param other The other object to test
	 * @return If other and this are equal
	 */
	@Override
	public boolean equals(Object other) {
		return ((other == null) || !(other instanceof DhpSegment)) ? false : true;
	}

	/**
	 * @inheritdoc
	 * @return
	 */
	@Override
	public int hashCode() {
		int hash = 5;
		return hash;
	}
}
