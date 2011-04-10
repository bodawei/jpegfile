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
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
	protected void readData(DataInput input) throws IOException, InvalidJpegFormat {
		int contentLength = input.readUnsignedShort();
		if (contentLength != 4) {
			throw new InvalidJpegFormat("got the wrong length in a DRI segment");
		}

		setRestartInterval(input.readUnsignedShort());
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		super.write(stream);
		DataOutputStream dataStream;

		if (stream instanceof DataOutputStream) {
			dataStream = (DataOutputStream) stream;
		} else {
			dataStream = new DataOutputStream(stream);
		}

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
}