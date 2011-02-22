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
 * Start Of Frame
 * @author dburrowes
 */
public abstract class SofSegmentBase extends SegmentBase {

	protected int contentLength;
	protected int samplePrecision;
	protected int imageHeight;
	protected int imageWidth;
	protected int numComponents;

	@Override
	public void readFromFile(RandomAccessFile file) throws IOException {
		this.contentLength = file.readUnsignedShort();
		this.file = file;
		this.fileOffset = file.getFilePointer();

		samplePrecision = file.readUnsignedByte();
		imageHeight = file.readUnsignedShort();
		imageWidth = file.readUnsignedShort();
		numComponents = file.readByte();

		// read an array of components

		file.skipBytes(contentLength - 2 - 6); // 2 for size itself, 6 for the fixed ata
	}

}
