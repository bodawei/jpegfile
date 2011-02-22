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
 * Start of Scan marker
 */
public class SosSegment extends SegmentBase {
	public static final int MARKER = 0xDA;

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

		// int componentCount = file.readUnsignedByte();

		// read componentCount ScanDescriptorTable entries

		// spectralSelectionStart = file.readUnsignedByte();
		// spectralSectionEnt = file.readUnsignedByte();
		// successiveApproximation = file.readUnsignedByte();

		file.skipBytes(contentLength - 2);
	}
}
