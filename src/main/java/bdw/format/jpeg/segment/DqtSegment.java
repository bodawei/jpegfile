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

package bdw.format.jpeg.segment;

import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.ParseMode;
import bdw.format.jpeg.segment.base.SegmentBase;
import bdw.format.jpeg.segment.support.DqtQuantizationTable;
import bdw.io.LimitExceeded;
import bdw.io.LimitingDataInput;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Define Quantization Table
 */
public class DqtSegment extends SegmentBase {
	/**
	 * Standard marker for this type
	 */
	public static final int SUBTYPE = 0xDB;

	/**
	 * list of quantization tables
	 */
	protected List<DqtQuantizationTable> tables;
	
	/**
	 * Constructor
	 */
	public DqtSegment() {
		setMarker(DqtSegment.SUBTYPE);
		tables = new ArrayList<DqtQuantizationTable>();
	}

	/**
	 * Constructs an instance with all properties empty
	 */
	public DqtSegment(int subType) throws InvalidJpegFormat {
		this();
		if (DqtSegment.canHandleMarker(subType)) {
			setMarker(subType);		
		} else {
			throw new InvalidJpegFormat("The subtype " + subType + " is not applicable to " + this.getClass().getSimpleName());
		}
	}

	/**
	 * Construct an instance from a stream.
	 *
	 * @param stream The stream to read from
	 * @param mode The mode to parse this in.
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public DqtSegment(int subType, InputStream stream, ParseMode mode) throws IOException, InvalidJpegFormat {
		this(subType);
		super.readFromStream(stream, mode);
    }

	/**
	 * Construct an instance from a stream.
	 *
	 * @param file The file to read from
	 * @param mode The mode to parse this in. 
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public DqtSegment(int subType, RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
		this(subType);
		super.readFromFile(file, mode);
    }

	/**
	 * Checks whether instances of this class should be constructed
	 * with the specified marker.
	 *
	 * @param marker The marker to check.
	 * @return true if this conventionally can be associated with that marker.
	 */
	public static boolean canHandleMarker(int marker) {
		if (marker == DqtSegment.SUBTYPE) {
			return true;
		}
		return false;
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
	public DqtQuantizationTable getTable(int index) {
		return tables.get(index);
	}

	/**
	 * @param index The index to insert the specified table
	 * @param table The table to be inserted at the index'th position
	 */
	public void insertTable(int index, DqtQuantizationTable table) {
		tables.add(index, table);
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		super.write(stream);
		DataOutputStream dataStream = super.wrapAsDataOutputStream(stream);
		int length = 0;

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
		if ((other == null) || !(other instanceof DqtSegment)) {
			return false;
		} else {
			DqtSegment castOther = (DqtSegment) other;

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

	/**
	 * @inheritdoc
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 61 * hash + (this.tables != null ? this.tables.hashCode() : 0);
		return hash;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void readData(DataInput input, ParseMode mode) throws IOException, InvalidJpegFormat {
		int contentLength = input.readUnsignedShort();
		LimitingDataInput limited = new LimitingDataInput(input, contentLength -2);
		int index = 0;
		
		while (limited.getRemainingLimit() != 0) {
			DqtQuantizationTable table = new DqtQuantizationTable();

			try {
				table.read(limited);
			} catch (LimitExceeded e) {
				throw new InvalidJpegFormat("Dqt segment length doesn't match actual data");
			}
			insertTable(index, table);
			index++;
		}

	}
}
