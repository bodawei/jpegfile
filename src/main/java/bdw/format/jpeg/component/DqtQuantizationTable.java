/*
 *  Copyright 2014 柏大衛
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

package bdw.format.jpeg.component;

import bdw.format.jpeg.data.Component;
import bdw.format.jpeg.support.DataBounds;
import bdw.util.Nibble;
import bdw.util.Size;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * A table of quantization entries used by the DqtSegment
 *
 * Defined on page B-11 and B-12 of the standard as part of the
 * Quantization Table segment.
 */
public class DqtQuantizationTable extends Component  {
	private static final int SUBTABLE_LENGTH = 64;
	private static final DataBounds elementPrecision = new DataBounds(
		"elementPrecision", Size.NIBBLE,
			0, 0,
			0, 1,
			0, 1,
			0,15);
	private static final DataBounds quantTableIdBounds = new DataBounds(
		"quantTableId", Size.NIBBLE,
			0, 3,
			0, 3,
			0, 3,
			0, 15);
	private static final DataBounds oneByteElement = new DataBounds(
		"element", Size.BYTE,
			1, 255,
			1, 255,
			1, 255,
			0, 255);
	private static final DataBounds twoByteElement = new DataBounds(
		"element", Size.SHORT,
			1, 255,
			1, 65535,
			1, 65535,
			0, 65535);

	private Integer precision;
	private byte quantizationTableId;
	private int[] table;
	private boolean checking;

	/*
	 * Initializes the table with values that work for all frame modes.
	 */
	public DqtQuantizationTable() {
		precision = null;
		quantizationTableId = 0;
		table = new int[SUBTABLE_LENGTH];
		checking = false;

		for (int index = 0; index < SUBTABLE_LENGTH; index++) {
			table[index] = 1;
		}
	}

	/**
	 * @return the precision of the elements in this table
	 */
	public Integer getElementPrecision() {
		return precision;
	}

	/**
	 * The precision of elements in the table. This can be any of the values
	 * specified below, or it can be null. Null indicates that the precision of
	 * the elements will be determined only when actually needed. IF the table
	 * contains all values below 255, then the precision is implicitly 0, while
	 * if it contains elements over 255, the precision is 1.
	 *
	 * A precision of 0 requires elements to be one (unsigned) byte long, while
	 * a precision of 1 requires elements to be two (unsigned) bytes long.
	 *
	 * This is much like the frame mode, in that it is best to set it before
	 * changing other values, since if the table already contains 65535 as a
	 * value, trying to change this to 0 will simply throw an exception.
	 *
	 * Values for this can be 0 or 1, except in baseline mode where only 0 is
	 * allowed, and lossless mode where this is ignored (any value can be set)
	 *
	 * @param precision 0, 1, null, or any value depending on the frame mode.
	 */
	public void setElementPrecision(Integer precision) {
		if (precision == null) {
			this.precision = null;
			return;
		}

		elementPrecision.throwIfInvalid(precision, this.getFrameMode(), this.getDataMode());

		Integer oldPrecision = this.precision;
		this.precision = precision;
		try {
			checkModeChange();
		} catch (RuntimeException e) {
			this.precision = oldPrecision;
			throw e;
		}
	}

	/**
	 * The quantization table identifier for this table.  Ordinarily this can
	 * only be in the range [0,3], however if the frame mode is lossless, then
	 * this can have any value that will fit in a nibble.
	 *
	 * @param id The table id. 0-3 or 0-15.
	 */
	public void setTableId(int id) {
		quantTableIdBounds.throwIfInvalid(id, this.getFrameMode(), this.getDataMode());

		this.quantizationTableId = (byte) id;
	}

	/**
	 * @return The quantization table id of this table
	 */
	public int getTableId() {
		return quantizationTableId;
	}

	/**
	 * Sets a value in the quantization table.  Value sizes are dictated by
	 * the element precision. See the elementPrecision property setter for more
	 * details.
	 *
	 * @param index The index into the quantization table (0-63)
	 * @param value The quantization value
	 */
	public void setElement(int index, int value) {
		if ((index < 0) || (index >= SUBTABLE_LENGTH)) {
			throw new IndexOutOfBoundsException("index must be [0,63]");
		}

		if (precisionIsOneByte()) {
			oneByteElement.throwIfInvalid(value, this.getFrameMode(), this.getDataMode());
		} else {
			twoByteElement.throwIfInvalid(value, this.getFrameMode(), this.getDataMode());
		}

		table[index] = value;
	}

	/**
	 * @param index index into the table (0-63)
	 * @return The quantization entry at the index'th position in this table
	 */
	public int getElement(int index) {
		if ((index < 0) || (index >= SUBTABLE_LENGTH)) {
			throw new IndexOutOfBoundsException("index must be [0,63]");
		}

		return table[index];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + 1 + (actualPrecisionIsOneByte() ? 64 : 128);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readParameters(DataInput source) throws IOException {
		super.readParameters(source);
		int flags = source.readUnsignedByte();
		setElementPrecision((int)Nibble.getUpper(flags));

		setTableId(Nibble.getLower(flags));

		for (int index = 0; index < SUBTABLE_LENGTH; index++) {
			if (precisionIsOneByte()) {
				this.setElement(index, source.readUnsignedByte());
			} else {
				this.setElement(index, source.readUnsignedShort());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeParameters(DataOutputStream output) throws IOException {
		super.writeParameters(output);

		boolean precisionIsOne = actualPrecisionIsOneByte();

		output.writeByte(Nibble.makeByte(precisionIsOne ? 0 : 1, getTableId()));

		for (int index = 0; index < SUBTABLE_LENGTH; index++) {
			if (precisionIsOne) {
				output.writeByte(getElement(index));
			} else {
				output.writeShort(getElement(index));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();

		if (getElementPrecision() != null) {
			elementPrecision.accumulateOnViolation(getElementPrecision(), getFrameMode(), results);
		}
		quantTableIdBounds.accumulateOnViolation(getTableId(), getFrameMode(), results);

		for (int index = 0; index < SUBTABLE_LENGTH; index++) {
			if (precisionIsOneByte()) {
				oneByteElement.throwIfInvalid(getElement(index), this.getFrameMode(), this.getDataMode());
			} else {
				twoByteElement.throwIfInvalid(getElement(index), this.getFrameMode(), this.getDataMode());
			}
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof DqtQuantizationTable)) {
			return false;
		}

		DqtQuantizationTable otherTable = (DqtQuantizationTable) other;

		if (getTableId() != otherTable.getTableId()) {
			return false;
		} else {
			for (int index = 0; index < 64; index++) {
				if (getElement(index) != otherTable.getElement(index)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 19 * hash + (this.precision != null ? this.precision.hashCode() : 0);
		hash = 19 * hash + this.quantizationTableId;
		hash = 19 * hash + Arrays.hashCode(this.table);
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void checkModeChange() {
		super.checkModeChange();
		if (checking) {
			return;
		}

		checking = true;

		try {
			setElementPrecision(getElementPrecision());
			setTableId(getTableId());
			for (int index = 0; index < SUBTABLE_LENGTH; index++) {
				setElement(index, getElement(index));
			}
		} finally {
			checking = false;
		}
	}

	/*
	 * @return true if the data in the table can all be stored in 1 byte values.
	 */
	private boolean actualPrecisionIsOneByte() {
		if (getElementPrecision() != null) {
			return precisionIsOneByte();
		}

		for (int index = 0; index < SUBTABLE_LENGTH; index++) {
			if (getElement(index) > 255) {
				return false;
			}
		}

		return true;
	}

	/*
	 * @return true if current precision indicates this has one byte values.
	 */
	private boolean precisionIsOneByte() {
		return (getElementPrecision() != null && getElementPrecision() == 0);
	}
}
