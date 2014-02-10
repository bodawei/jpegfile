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
import java.util.List;

/**
 * A portion of the SOS Segment
 *
 * Note: This is defined on pages B-8 and B-10 of the standard.
 */
public class SosComponentSpec extends Component {
	private static final DataBounds componentSelectorBounds = new DataBounds(
		"componentSelector", Size.BYTE, 0, 255);
	private static final DataBounds dcTableSelectorBounds = new DataBounds(
		"dcTableSelector", Size.NIBBLE,
			  0, 1,
			  0, 3,
			  0, 3,
			  0, 3);
	private static final DataBounds acTableSelectorBounds = new DataBounds(
		"acTableSelector", Size.NIBBLE,
			  0, 1,
			  0, 3,
			  0, 3,
			  0, 0);

	private int componentSelector;
	private int dcTableSelector;
	private int acTableSelector;

	/**
	 * Constructs an instance with all values set to 0
	 */
	public SosComponentSpec() {
		componentSelector = 0;
		dcTableSelector = 0;
		acTableSelector = 0;
	}

	/**
	 * @param selector Sets the selector value.
	 */
	public void setComponentSelector(int selector) {
		componentSelectorBounds.throwIfInvalid(selector, getFrameMode(), getDataMode());

		this.componentSelector = selector;
	}

	public int getComponentSelector() {
		return componentSelector;
	}

	/**
	 * @param selector Sets the DC table selector value.
	 */
	public void setDcTableSelector(int selector) {
		dcTableSelectorBounds.throwIfInvalid(selector, getFrameMode(), getDataMode());

		this.dcTableSelector = selector;
	}

	public int getDcTableSelector() {
		return dcTableSelector;
	}

	/**
	 * @param selector Sets the AC table selector value.
	 */
	public void setAcTableSelector(int selector) {
		acTableSelectorBounds.throwIfInvalid(selector, getFrameMode(), getDataMode());

		this.acTableSelector = selector;
	}

	public int getAcTableSelector() {
		return acTableSelector;
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
	public void readParameters(DataInput source) throws IOException {
		super.readParameters(source);
		setComponentSelector(source.readUnsignedByte());
		int tableSelectors = source.readUnsignedByte();

		setDcTableSelector(Nibble.getUpper(tableSelectors));
		setAcTableSelector(Nibble.getLower(tableSelectors));
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void writeParameters(DataOutputStream stream) throws IOException {
		super.writeParameters(stream);

		stream.writeByte(getComponentSelector());
		stream.writeByte(Nibble.makeByte(getDcTableSelector(), getAcTableSelector()));
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();

		componentSelectorBounds.accumulateOnViolation(getComponentSelector(), getFrameMode(), results);
		dcTableSelectorBounds.accumulateOnViolation(getDcTableSelector(), getFrameMode(), results);
		acTableSelectorBounds.accumulateOnViolation(getAcTableSelector(), getFrameMode(), results);

		return results;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public boolean equals(Object other) {
		if ((other == null) || (!(other instanceof SosComponentSpec))) {
			return false;
		}

		SosComponentSpec castOther = (SosComponentSpec) other;

		return (getComponentSelector() == castOther.getComponentSelector() &&
				getDcTableSelector() == castOther.getDcTableSelector() &&
				getAcTableSelector() == castOther.getAcTableSelector());
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + this.componentSelector;
		hash = 29 * hash + this.acTableSelector;
		hash = 29 * hash + this.dcTableSelector;
		return hash;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void checkModeChange() {
		super.checkModeChange();

		setComponentSelector(getComponentSelector());
		setDcTableSelector(getDcTableSelector());
		setAcTableSelector(getAcTableSelector());
	}
}
