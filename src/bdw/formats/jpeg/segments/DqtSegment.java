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
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Define Quantization Table
 */
public class DqtSegment extends SegmentBase {
	public static final int MARKER = 0xDB;

	protected int contentLength;

	@Override
	public int getMarker() {
		return DqtSegment.MARKER;
	}

	@Override
	public void readFromFile(RandomAccessFile file) throws IOException {
		this.contentLength = file.readUnsignedShort();
		this.file = file;
		this.fileOffset = file.getFilePointer();

		// This is an array of QuantizationTable's.  They are potentially variably sized, so
		// you'd have to read them in to know for sure how many there are.
		// naturally, after reading each, you should make sure that you've not
		// exceeded the available data.  I'm suggesting that you create a
		// wrapper around the RAF that looks like a stream and limits what
		// can be read.  You could wrap it aroudn the stream, too.

		file.skipBytes(contentLength - 2);
	}
}
