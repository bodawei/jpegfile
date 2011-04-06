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

import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.segments.base.SegmentBase;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class AppNSegment extends SegmentBase {
	public static final int START_MARKER = 0xE0;
	public static final int END_MARKER = 0xEF;

	protected byte[] data;
	public AppNSegment() {

	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void readFromStream(InputStream stream) throws IOException, InvalidJpegFormat {
		int contentLength = stream.readUnsignedShort();
		byte[] buffer = new byte[contentLength];

		for (int index = 0; index < contentLength; index++) {
			buffer[index] = (byte)stream.readByte();
		}

		data = buffer;
	}

	@Override
	public void readFromFile(RandomAccessFile file) throws IOException, InvalidJpegFormat {
		raFile = file;
		fileOffset = file.getFilePointer();

		int contentLength = stream.readUnsignedShort();
		byte[] buffer;

		if (contentLength > 024) {

		} else {
			buffer = new byte[contentLength]; // if there's less than 1K of data, store it
			int count = 0;

			while (true) {
				buffer[count] = file.readByte();;
				count++;
			}
		}

			data = buffer;
			raFile = null;
			fileOffset = 0;
			dataRead = true;
		}
	}
}
