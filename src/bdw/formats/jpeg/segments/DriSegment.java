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
 * Define Restart Interval
 */
public class DriSegment extends SegmentBase {
	public static final int MARKER = 0xDD;
	protected int restartInterval;

	public int getMarker() {
		return MARKER;
	}

	@Override
	public void readFromFile(RandomAccessFile file) throws IOException {
		int contentLength = file.readUnsignedShort();
		if (contentLength != 4) {
			// we have a problem
		}
//  - restart interval (high byte, low byte) in units of MCU blocks,
//    meaning that every n MCU blocks a RSTn marker can be found.
//    The first marker will be RST0, then RST1 etc, after RST7
//    repeating from RST0.

		restartInterval = file.readUnsignedShort();
	}


}
