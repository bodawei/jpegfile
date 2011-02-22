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
 * Expand reference components
 */
public class ExpSegment extends SegmentBase {

	public static final int MARKER = 0xDF;
	protected int contentLength;

	public int getMarker() {
		return MARKER;
	}

	@Override
	public void readFromFile(RandomAccessFile file) throws IOException {
		this.contentLength = file.readUnsignedShort();
		this.file = file;
		this.fileOffset = file.getFilePointer();

		// read an array of huffman blocks

		file.skipBytes(contentLength - 2);
	}

}
