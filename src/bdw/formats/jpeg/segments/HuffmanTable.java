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

		// read in a total byte array. These are the huffman codes

	}

}
