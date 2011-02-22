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

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 */
public class App0Segment extends SegmentBase {
	/**
	 * The marker code corresponding to this type of segment
	 */
	public static final int MARKER = 0xE0;


	protected String identifier;
	protected int versionMajor;
	protected int versionMinor;
	protected int units;
	protected int xDensity;
	protected int yDensity;
	protected int xThumbnail;
	protected int yThumbnail;

	public App0Segment() {
		identifier = null;
		versionMajor = 0;
		versionMinor = 0;
		units = 0;
		xDensity = 0;
		yDensity = 0;
		xThumbnail = 0;
		yThumbnail = 0;
	}

	@Override
	public int getMarker() {
		return App0Segment.MARKER;
	}

	/**
	 *
	 */
	@Override
	public void readFromFile(RandomAccessFile file) throws IOException {
		int stringLength;
		StringBuilder builder = new StringBuilder();
		int contentLength = file.readUnsignedShort();
		stringLength = contentLength - 2 - 9;

		for (int index = 0; index < stringLength; index++) {
			int aChar = file.readUnsignedByte();
			if (index != stringLength -1) {
				builder.append(aChar);
			} else {
				if (aChar != 0x00) {
					throw new IllegalArgumentException();
				}
			}
		}
		identifier = builder.toString();
		versionMajor = file.readUnsignedByte();
		versionMinor = file.readUnsignedByte();
		units = file.readUnsignedByte();
		xDensity = file.readUnsignedShort();
		yDensity = file.readUnsignedShort();
		xThumbnail = file.readUnsignedByte();
		yThumbnail = file.readUnsignedByte();
	}


}
