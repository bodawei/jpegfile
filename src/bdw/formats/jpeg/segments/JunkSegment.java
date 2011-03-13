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

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *Contains data up to the next 0xff0x?? (where ?? is not 00)
 */
public class JunkSegment extends SegmentBase {

    public static int MARKER = 0xFFFF;
    private long contentLength;

    @Override
    public int getMarker() {
	return JunkSegment.MARKER;
    }

    @Override
    public void readFromFile(RandomAccessFile file) throws IOException {
	this.file = file;
	this.fileOffset = file.getFilePointer();

	try {
	    while (true) {
	    int aByte = file.readUnsignedByte();
	    if (aByte == 0xff) {
		int markerByte = file.readUnsignedByte();
		if (markerByte != 0x00) {   // possibly have a substantive marker.
		    contentLength = file.getFilePointer() - this.fileOffset;
		    file.seek(file.getFilePointer() - 2);
		    return;
		}
	    }
	    }
	} catch (EOFException exception) {
	}

	contentLength = file.getFilePointer() - this.fileOffset;
    }
}
