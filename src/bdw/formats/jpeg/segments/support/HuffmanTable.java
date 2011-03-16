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
public class HuffmanTable {

	protected boolean isAc;	// doesn't seem to be used?
	protected int tableId;

	public void readFromFile(RandomAccessFile file) throws IOException {
		int flags = file.readUnsignedByte();
		int total = 0;

		if ((flags & 0xF0) == 0x10) {
			isAc = true;
		} else {
			isAc = false;
		}

		tableId = (flags & 0x0F);

		for (int index = 0; index < 16; index++) {
			total += file.readUnsignedByte();
		}

//		  - HT information (1 byte):
//     bit 0..3: number of HT (0..3, otherwise error)
//     bit 4   : type of HT, 0 = DC table, 1 = AC table
//     bit 5..7: not used, must be 0
//  - 16 bytes: number of symbols with codes of length 1..16, the sum of these
//    bytes is the total number of codes, which must be <= 256
//  - n bytes: table containing the symbols in order of increasing code length
//    (n = total number of codes)

		// read in a total byte array. These are the huffman codes

	}

}
