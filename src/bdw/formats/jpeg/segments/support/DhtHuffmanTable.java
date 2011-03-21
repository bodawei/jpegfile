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

import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.segments.base.JpegDataBase;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A table of huffman encoding values
 */
public class DhtHuffmanTable extends JpegDataBase {

	protected boolean isAc;	// doesn't seem to be used?
	protected int tableId;
	protected List<byte[]> entries;

	public DhtHuffmanTable() {
		isAc = false;
		tableId = 0;
		entries = new ArrayList<byte[]>(16);
	}

	public boolean isAc() {
		return false;
	}
	
	public void setAc(boolean ac) {
		isAc = ac;
	}
	
	public int getId() {
		return tableId;
	}
	
	public void setId(int id) {
		paramIsUInt4(id);
		
		tableId = id;
	}
	
	public int getEntryCount() {
		return entries.size();
	}
	
	public byte[] getEntry(int index) {
		return entries.get(index);
	}
	
	public void setEntry(int index, byte[] data) {
		if (data.length > 16) {
			throw new IllegalArgumentException("data can not be longer than 16 bytes");
		}
		entries.set(index, data);
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof DqtQuantizationTable)) {
			return false;
		} else {
			DhtHuffmanTable otherTable = (DhtHuffmanTable) other;

			if (getId() != otherTable.getId()) {
				return false;
			} else {
			}
		}
		return true;
	}

	public void read(DataInput source) throws IOException, InvalidJpegFormat {
		int flags = source.readUnsignedByte();
		int total = 0;

		if ((flags & 0xF0) == 0x10) {
			isAc = true;
		} else {
			isAc = false;
		}

		tableId = (flags & 0x0F);

		for (int index = 0; index < 16; index++) {
			total += source.readUnsignedByte();
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

	/**
	 * Writes this table out in a jpeg compliant format.
	 * @param out The output stream to write to
	 * @throws IOException If an error occurs while writing
	 */
	public void write(DataOutput out) throws IOException {
	}
}
