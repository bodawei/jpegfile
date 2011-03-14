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

package bdw.formats.jpeg.segments.support;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author bodawei
 */
public class QuantizationTable {
	private int unitSize;
	private int id;

	public void readFromFile(RandomAccessFile file) throws IOException {
		int flags = file.readUnsignedByte();
		if ((flags & 0xF0) == 0) {
			unitSize = 1;
		} else {
			unitSize = 2;
		}

		id = (flags & 0x0F);

//		  - QT information (1 byte):
//     bit 0..3: number of QT (0..3, otherwise error)
//     bit 4..7: precision of QT, 0 = 8 bit, otherwise 16 bit
//  - n bytes QT, n = 64*(precision+1)
//  - For precision=1 (16 bit), the order is high-low for each of the 64 words.

		// read in a 64 byte * unitSize block of entries
	}

}
