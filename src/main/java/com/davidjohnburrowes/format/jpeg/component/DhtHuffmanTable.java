/*
 *  Copyright 2014,2017 柏大衛
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
package com.davidjohnburrowes.format.jpeg.component;

import com.davidjohnburrowes.format.jpeg.data.Component;
import com.davidjohnburrowes.format.jpeg.support.DataBounds;
import com.davidjohnburrowes.util.Nibble;
import com.davidjohnburrowes.util.Size;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A list of Huffman values
 *
 * Defined on page B-12 to B-14 of the standard.
 */
public class DhtHuffmanTable extends Component {
	private static final int NUM_CODE_COUNTS = 16;
	private static final DataBounds tableClassBounds = new DataBounds(
		"tableClass", Size.NIBBLE,
			0, 1,
			0, 1,
			0, 1,
			0, 0);
	private static final DataBounds tableIdBounds = new DataBounds(
		"tableId", Size.NIBBLE,
			0, 1,
			0, 3,
			0, 3,
			0, 3);
	private static final DataBounds elementSizeBounds = new DataBounds(
		"elementSize", Size.BYTE,
			0, 255);
	private static final DataBounds valueBounds = new DataBounds(
		"code", Size.BYTE,
			0, 255);

	/**
	 * True if this is an AC table (otherwise is DC)
	 */
	protected byte tableClass;

	/**
	 * ID of the table
	 */
	protected byte tableId;

	/**
	 * The set of run length headers in this table
	 */
	protected List<short[]> elements;


	public DhtHuffmanTable() {
		tableClass = 0;
		tableId = 0;
		elements = new ArrayList<short[]>();
		for (int index = 0; index < NUM_CODE_COUNTS; index++) {
			elements.add(new short[0]);
		}
	}

	/**
	 * @param newClass of the table
	 */
	public void setTableClass(int newClass) {
		tableClassBounds.throwIfInvalid(newClass, this.getFrameMode(), this.getDataMode());

		tableClass = (byte) newClass;
	}

	/**
	 * @return Class of the table (0 == DC, 1 == AC)
	 */
	public byte getTableClass() {
		return tableClass;
	}

	/**
	 * @param newId The id for this table.
	 */
	public void setTableId(int newId) {
		tableIdBounds.throwIfInvalid(newId, this.getFrameMode(), this.getDataMode());

		this.tableId = (byte)newId;
	}

	/**
	 * @return id of this table (a value of 0-3)
	 */
	public int getTableId() {
		return tableId;
	}

	/**
	 * Sets an element at the specified index. An element is, it self, an array
	 * of byte values (stored as shorts because they are unsigned values)
	 * @param index The index of the element to add
	 * @param element The element to add. Note that the items in the element array
	 *				must be in the range 0-255
	 */
	public void setElement(int index, short[] element) {
		if (index >= NUM_CODE_COUNTS) {
			throw new IllegalArgumentException("Cant have more than 16 elements");
		}

		if (element == null) {
			throw new IllegalArgumentException("Cant set an elements to null");
		}

		elementSizeBounds.throwIfInvalid(element.length, this.getFrameMode(), this.getDataMode());

		for (int subIndex = 0; subIndex < element.length; subIndex++) {
			if (element[subIndex] > 255 || element[subIndex] < 0) {
				throw new IllegalArgumentException("Items in the element array must be in the range 0-255");
			}
		}

		elements.set(index, element);
	}

	/**
	 *
	 * @param index The index of the element to retrieve
	 * @return The element.
	 */
	public short[] getElement(int index) {
		if (index >= NUM_CODE_COUNTS) {
			throw new IllegalArgumentException("Cant have more than 16 elements");
		}

		return elements.get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSizeOnDisk() {
		int elemntsSize = 0;
		for (short[] element : elements) {
			elemntsSize += element.length;
		}

		return 17 + elemntsSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readParameters(DataInput source) throws IOException {
		super.readParameters(source);
		int flags = source.readUnsignedByte();

		setTableClass((int)Nibble.getUpper(flags));
		setTableId(Nibble.getLower(flags));

		short[] codeCounts = new short[NUM_CODE_COUNTS];

		for (int index = 0; index < NUM_CODE_COUNTS; index++) {
			codeCounts[index] = (short) source.readUnsignedByte();
		}

		for (int index = 0; index < NUM_CODE_COUNTS; index++) {
			byte[] huffmanEntries = new byte[codeCounts[index]];
			source.readFully(huffmanEntries);

			// convert the raw bytes to shorts
			short[] actualEntries = new short[codeCounts[index]];
			for (int subIndex = 0; subIndex < codeCounts[index]; subIndex ++) {
				actualEntries[subIndex] = (short) (0xFF & ((short) huffmanEntries[subIndex]));
			}

			setElement(index, actualEntries);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeParameters(DataOutputStream output) throws IOException {
		output.writeByte(Nibble.makeByte(getTableClass(), getTableId()));

		for (int index = 0; index < NUM_CODE_COUNTS; index++) {
			output.writeByte(getElement(index).length);
		}

		for (short[] element : elements) {
			for (int subIndex = 0; subIndex < element.length; subIndex++) {
				output.write((byte)(0xFF & element[subIndex]));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();

		tableClassBounds.accumulateOnViolation(getTableClass(), getFrameMode(), results);
		tableIdBounds.accumulateOnViolation(getTableId(), getFrameMode(), results);

		for (short[] element : elements) {
			elementSizeBounds.accumulateOnViolation(element.length, this.getFrameMode(), results);
			for (short value : element) {
				valueBounds.accumulateOnViolation(value, getFrameMode(), results);
			}
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof DhtHuffmanTable)) {
			return false;
		} else {
			DhtHuffmanTable otherTable = (DhtHuffmanTable) other;

			if (getTableId() != otherTable.getTableId()) {
				return false;
			}

			if (getTableClass() != otherTable.getTableClass()) {
				return false;
			}

			for (int index = 0; index < NUM_CODE_COUNTS; index++) {
				if ( ! Arrays.equals(getElement(index), otherTable.getElement(index))) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 83 * hash + this.tableClass;
		hash = 83 * hash + this.tableId;
		hash = 83 * hash + this.elements.hashCode();
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void checkModeChange() {
		super.checkModeChange();

		setTableClass(getTableClass());
		setTableId(getTableId());
		for (int index = 0; index < NUM_CODE_COUNTS; index++) {
			setElement(index, getElement(index));
		}
	}
}
