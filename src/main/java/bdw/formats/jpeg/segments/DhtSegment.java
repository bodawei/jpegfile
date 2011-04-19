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

import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.segments.base.SegmentBase;
import bdw.formats.jpeg.segments.support.DhtHuffmanTable;
import bdw.formats.jpeg.segments.support.DqtQuantizationTable;
import bdw.io.LimitExceeded;
import bdw.io.LimitingDataInput;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Define Huffman Table
 */
public class DhtSegment extends SegmentBase {

	/**
	 * Standard marker for this type
	 */
	public static final int MARKER = 0xC4;

	/**
	 * list of quantization tables
	 */
	protected List<DhtHuffmanTable> tables;

	/**
	 * Constructor
	 */
	public DhtSegment() {
		setMarker(DhtSegment.MARKER);
		tables = new ArrayList<DhtHuffmanTable>();
	}

	/**
	 * @return the number of tables in this segment
	 */
	public int getTableCount() {
		return tables.size();
	}

	/**
	 * @param index The index of the table to retrieve
	 * @return The table at the index'th position
	 */
	public DhtHuffmanTable getTable(int index) {
		return tables.get(index);
	}

	/**
	 * @param index The index to insert the specified table
	 * @param table The table to be inserted at the index'th position
	 */
	public void insertTable(int index, DhtHuffmanTable table) {
		tables.add(index, table);
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void readData(DataInput input) throws IOException, InvalidJpegFormat {
		int contentLength = input.readUnsignedShort();
		LimitingDataInput limited = new LimitingDataInput(input, contentLength -2);
		int index = 0;

		while (limited.getRemainingLimit() != 0) {
			DhtHuffmanTable table = new DhtHuffmanTable();

			try {
				table.read(limited, strict);
			} catch (LimitExceeded e) {
				throw new InvalidJpegFormat("Dht segment length doesn't match actual data");
			}
			insertTable(index, table);
			index++;
		}

	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		super.write(stream);
		DataOutputStream dataStream;
		int length = 0;

		if (stream instanceof DataOutputStream) {
			dataStream = (DataOutputStream) stream;
		} else {
			dataStream = new DataOutputStream(stream);
		}

		for (int index = 0; index < getTableCount(); index++) {
			length += getTable(index).getSizeOnDisk();
		}

		dataStream.writeShort(length + 2);

		for (int index = 0; index < getTableCount(); index++) {
			getTable(index).write(dataStream);
		}
	}

	/**
	 * Equals here means both have the same count of tables, and table N here
	 * is equal to table N in the other
	 */
	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof DhtSegment)) {
			return false;
		} else {
			DhtSegment castOther = (DhtSegment) other;

			if (getTableCount() != castOther.getTableCount()) {
				return false;
			}

			for (int index = 0; index < getTableCount(); index++) {
				if ( ! getTable(index).equals(castOther.getTable(index))) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + (this.tables != null ? this.tables.hashCode() : 0);
		return hash;
	}


}
