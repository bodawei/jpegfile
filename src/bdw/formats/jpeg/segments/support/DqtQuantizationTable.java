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
import bdw.formats.jpeg.segments.base.JpegDataStructureBase;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

/**
 * A table of quantization entries used by the DqtSegment
 */
public class DqtQuantizationTable extends JpegDataStructureBase  {
	/**
	 * Preserves the unit size read from a jpeg file.  This is not used if
	 * this table is built by hand rather than by reading from a file.
	 */
	private int unitSize;

	/**
	 * The id of the table.
	 */
	private int id;

	/**
	 * The table of 64 quantization values.
	 */
	private int[] table;

	/**
	 * Construct an instance with an id of 0 and 0 in all quantization values.
	 */
	public DqtQuantizationTable() {
		table = new int[64];
		unitSize = 0;
		id = 0;
	}

	/**
	 * @return The id of this table
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id The id of this table (must be between 0 and 63, though some sources indicate anything more than 3 is an error)
	 */
	public void setId(int id) {
		paramIsUInt4(id);

		this.id = id;
	}

	/**
	 * @return The Size of this when written to a jpeg file
	 */
	public int getSizeOnDisk() {
		if (getEntyOutputSize() == 0) {
			return 1 + (1 * 64);
		} else {
			return 1 + (2 * 64);
		}
	}

	/**
	 * @param index index into the table (0-63)
	 * @return The quantization entry at the index'th position in this table
	 */
	public int getEntry(int index) {
		if ((index < 0) || (index > 63)) {
			throw new IndexOutOfBoundsException("index must be [0,63]");
		}

		return table[index];
	}

	/**
	 * @param index The index into the quantization table (0-63)
	 * @param value The quantization value (0-65535)
	 */
	public void setEntry(int index, int value) {
		paramIsUInt16(value);

		if ((index < 0) || (index > 63)) {
			throw new IndexOutOfBoundsException("index must be [0,63]");
		}

		table[index] = value;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof DqtQuantizationTable)) {
			return false;
		} else {
			DqtQuantizationTable otherTable = (DqtQuantizationTable) other;

			if (id != otherTable.getId()) {
				return false;
			} else {
				for (int index = 0; index < 64; index++) {
					if (getEntry(index) != otherTable.getEntry(index)) {
						return false;
					}
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
		int hash = 5;
		hash = 41 * hash + this.id;
		hash = 41 * hash + Arrays.hashCode(this.table);
		return hash;
	}

	/**
	 * Read in an instance from a data source. Note, this "blindly" reads the
	 * next 65 or 129 bytes.
	 *
	 * @param source The source of data to read the instance from
	 * @throws IOException  If an IO exception occurs
	 * @throws InvalidJpegFormat If something is wrong in the format (mainly EOF)
	 */
	public void read(DataInput source) throws IOException, InvalidJpegFormat {
		try {
			int flags = source.readUnsignedByte();
			unitSize = ((flags & 0xF0) >> 4);
			// some indications may be that different unit sizes have some meaning
			// Not clear about that, so not tracking that

			// Note: Multiple sources say 0-3 are ok, but
			// larger are an error.  Right now I don't know enough why, so not erroring.
			id = (flags & 0x0F);

			for (int index = 0; index < 64; index++) {
				if (unitSize == 0) {
					this.setEntry(index, source.readUnsignedByte());
				} else {
					this.setEntry(index, source.readUnsignedShort());
				}
			}
		} catch (EOFException e) {
			throw new InvalidJpegFormat("Ran out of bytes for the quantization table");
		}
	}

	/**
	 * Writes this table out in a jpeg compliant format.
	 * @param out The output stream to write to
	 * @throws IOException If an error occurs while writing
	 */
	public void write(DataOutput out) throws IOException {
		int outputSize = getEntyOutputSize();

		out.write((outputSize << 4) | getId());

		for (int index = 0; index < table.length; index++) {
			if (outputSize == 0) {
				out.write(getEntry(index));
			} else {
				out.writeShort(getEntry(index));
			}
		}
	}

	/**
	 * Calculates two things and returns them combined into a single
	 * value.
	 * 1: should we write out 1 or 2 byte entries
	 * 2: What we should put in the top 4 bits of the first byte.
	 *  	We try to preserve whatever we might have read in, but
	 *		if the data in our table doesn't match that, use what
	 * 		the table indicates.
	 * @return  Returns 0 if only one byte to be used, or a non-zero if 2 bytess
	 */
	protected int getEntyOutputSize() {
		boolean canUseOneByte = true;
		int outputSize = unitSize;

		for (int index = 0; index < table.length; index++) {
			if (table[index] > 255) {
				canUseOneByte = false;
			}
		}

		if ((canUseOneByte != true) && (unitSize == 0)) {
			outputSize = 1;
		} else if ((canUseOneByte == true) && (unitSize > 0)) {
			outputSize = 0;
		}

		return outputSize;
	}
}
