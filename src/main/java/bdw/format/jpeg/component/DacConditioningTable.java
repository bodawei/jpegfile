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
import bdw.format.jpeg.support.DataMode;
import bdw.util.Nibble;
import bdw.util.Size;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * An individual Define Arithmetic coding conditioning table
 *
 * Note: This is defined on pages B-14 - B-15 of the standard.
 */
public class DacConditioningTable extends Component {
	private static final DataBounds tableClassBounds = new DataBounds(
		"tableClass", Size.NIBBLE,
			0, Size.NIBBLE.getMax(),
			0, 1,
			0, 1,
			0, 0);
	private static final DataBounds tableIdBounds = new DataBounds(
		"tableId", Size.NIBBLE,
			0, Size.NIBBLE.getMax(),
			0, 3,
			0, 3,
			0, 3);
	private static final DataBounds tableValueBounds = new DataBounds(
		"tableId", Size.BYTE,
			0, Size.BYTE.getMax(),
			0, 255,
			0, 255,
			0, 255);

	private int tableClass;
	private int tableId;
	private int tableValue;

	/**
	 * Constructs an instance.
	 */
	public DacConditioningTable() {
		tableClass = 0;
		tableId = 0;
		tableValue = 1;
	}

	/**
	 * @param newClass the class for this table
	 */
	public void setTableClass(int newClass) {
		tableClassBounds.throwIfInvalid(newClass, getFrameMode(), getDataMode());
		throwIfSpecialConditionInvalid(newClass, getTableValue());

		tableClass = newClass;
	}

	public int getTableClass() {
		return tableClass;
	}

	/**
	 * @param newId Id for this table
	 */
	public void setTableId(int newId) {
		tableIdBounds.throwIfInvalid(newId, getFrameMode(), getDataMode());
		tableId = newId;
	}

	public int getTableId() {
		return tableId;
	}

	/**
	 * @param newValue Value for this table
	 */
	public void setTableValue(int newValue) {
		tableValueBounds.throwIfInvalid(newValue, getFrameMode(), getDataMode());
		throwIfSpecialConditionInvalid(getTableClass(), newValue);

		tableValue = newValue;
	}

	public int getTableValue() {
		return tableValue;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + 2;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void readParameters(DataInput input) throws IOException {
		super.readParameters(input);
		int flags = input.readUnsignedByte();
		setTableClass((int)Nibble.getUpper(flags));
		setTableId(Nibble.getLower(flags));
		setTableValue(input.readUnsignedByte());
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void writeParameters(DataOutputStream output) throws IOException {
		super.writeParameters(output);

		output.write(Nibble.makeByte(getTableClass(), getTableId()));
		output.write((byte)getTableValue());
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();

		tableClassBounds.accumulateOnViolation(getTableClass(), getFrameMode(), results);
		tableIdBounds.accumulateOnViolation(getTableId(), getFrameMode(), results);
		tableValueBounds.accumulateOnViolation(getTableValue(), getFrameMode(), results);
		accumulateOnSpecialConditionViolation(results);

		return results;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof DacConditioningTable)) {
			return false;
		}

		DacConditioningTable otherTable = (DacConditioningTable) other;

		if (getTableClass() != otherTable.getTableClass() ||
				getTableId() != otherTable.getTableId() ||
				getTableValue() != otherTable.getTableValue()) {
			return false;
		}

		return true;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public int hashCode() {
		int hash = 3;
		hash = 17 * hash + this.tableClass;
		hash = 17 * hash + this.tableId;
		hash = 17 * hash + this.tableValue;
		return hash;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void checkModeChange() {
		super.checkModeChange();

		setTableClass(getTableClass());
		setTableId(getTableId());
		setTableValue(getTableValue());
	}

	/**
	 * Adds exceptions for special conditions
	 */
	private void accumulateOnSpecialConditionViolation(List<Exception> results) {
		RuntimeException result = checkSpecialCondition(getTableClass(), getTableValue());
		if (result != null) {
			results.add(result);
		}
	}

	/**
	 * Throws an exception on special condition
	 */
	private void throwIfSpecialConditionInvalid(int theClass, int theValue) {
		RuntimeException result = checkSpecialCondition(theClass, theValue);
		if (result != null) {
			throw result;
		}
	}

	/**
	 * We can't check one condition with DataBounds, and this is in certain
	 * frame modes, the table value has a slightly different range when the class
	 * is 1. In these cases, we will generate an exception which can be thrown
	 * or added.  See page B-15 of the standard.
	 */
	private RuntimeException checkSpecialCondition(int theClass, int theValue) {
		if (theClass == 1 &&
			(theValue == 0 || theValue > 63 ) &&
			(getFrameMode().isSequentialExtended() || getFrameMode().isProgressive() || getFrameMode() != null) &&
			getDataMode() == DataMode.STRICT) {
			return new IllegalArgumentException("When table class is 1, max value is 63");
		}
		return null;
	}
}
