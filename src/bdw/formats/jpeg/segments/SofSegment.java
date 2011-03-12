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
 */
public class SofSegment extends MultiMarkerSegmentBase {
    public static final int RANGE1_START = 0xc0;
    public static final int RANGE1_END = 0xc3;

    public static final int RANGE2_START = 0xc5;
    public static final int RANGE2_END = 0xc7;

    public static final int RANGE3_START = 0xc9;
    public static final int RANGE3_END = 0xcb;

    public static final int RANGE4_START = 0xcd;
    public static final int RANGE4_END = 0xcf;
    
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
		numComponents = file.readByte(); //usually 1 = grey scaled, 3 = color YCbCr or YIQ, 4 = color CMYK)

		// read an array of components

		file.skipBytes(contentLength - 2 - 6); // 2 for size itself, 6 for the fixed ata
	}


}
