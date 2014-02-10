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
 * A single component as managed by a SofSegment
 */
public class FrameComponent extends Component {
	private static final DataBounds componentIdBounds = new DataBounds(
		"componentId", Size.BYTE, 0, 255);
	private static final DataBounds horizontalBounds = new DataBounds(
		"horizontalScaling", Size.NIBBLE, 1, 4);
	private static final DataBounds verticalBounds = new DataBounds(
		"verticalScaling", Size.NIBBLE, 1, 4);
	private static final DataBounds quantizationBounds = new DataBounds(
		"quantizationSelector", Size.BYTE,
			  0, 3,
			  0, 3,
			  0, 3,
			  0, 0);
	private static final DataBounds horizontalQuantizationBounds = new DataBounds(
		"quantizationSelector", Size.BYTE, 0, 0);

	/**
	 * Convenience value for how large this data is if written to disk (bytes)
	 */
	public static final int DISK_SIZE = 3;

	/**
	 * ID for this component
	 */
	private int componentId;

	/**
	 * The sampling value on the x axis
	 */
	private int horScalingFactor = 1;

	/**
	 * Sampling value on the y axis
	 */
	private int vertScalingFactor = 1;

	/**
	 * The quantization id
	 */
	private int quantTableSelector;

	/**
	 * Standard constructor
	 */
	public FrameComponent() {
	}

	/**
	 * Set the id for the component
	 *
	 * @param value the id for this component (0-255)
	 */
	public void setComponentId(int value) {
		componentIdBounds.throwIfInvalid(value, getFrameMode(), getDataMode());
		componentId = value;
	}

	/**
	 * @return the id for this component
	 */
	public int getComponentId() {
		return componentId;
	}

	/**
	 * Set the sampling value for the horizontal axis
	 *
	 * @param value the sampling value for X (between 1 and 4)
	 */
	public void setHorizontalScaling(int value) {
		horizontalBounds.throwIfInvalid(value, getFrameMode(), getDataMode());
		horScalingFactor = value;
	}

	/**
	 * @return the sampling value for the horizontal axis
	 */
	public int getHorizontalScaling() {
		return horScalingFactor;
	}

	/**
	 * Set the sampling value for the vertical axis
	 *
	 * @param value the sampling value for Y (between 1 and 4)
	 */
	public void setVerticalScaling(int value) {
		verticalBounds.throwIfInvalid(value, getFrameMode(), getDataMode());
		vertScalingFactor = value;
	}

	/**
	 * @return The y sampling value
	 */
	public int getVerticalScaling() {
		return vertScalingFactor;
	}

	/**
	 * Set the quantization table id
	 *
	 * @param value The quantization table id (between 0 and 3)
	 */
	public void setQuantizationSelector(int value) {
		if (getHierarchicalMode()) {
			horizontalQuantizationBounds.throwIfInvalid(value, getFrameMode(), getDataMode());
		} else {
			quantizationBounds.throwIfInvalid(value, getFrameMode(), getDataMode());
		}
		quantTableSelector = value;
	}

	/**
	 * @return The quantization table ID
	 */
	public int getQuantizationSelector() {
		return quantTableSelector;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + 3;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void readParameters(DataInput input) throws IOException {
		super.readParameters(input);
		setComponentId(input.readUnsignedByte());
		int values = input.readUnsignedByte();
		setHorizontalScaling(Nibble.getUpper(values));
		setVerticalScaling(Nibble.getLower(values));
		setQuantizationSelector(input.readUnsignedByte());
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void writeParameters(DataOutputStream output) throws IOException {
		super.writeParameters(output);
		output.writeByte(getComponentId());
		output.writeByte(Nibble.makeByte(getHorizontalScaling(), getVerticalScaling()));
		output.writeByte(getQuantizationSelector());
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();

		componentIdBounds.accumulateOnViolation(getComponentId(), getFrameMode(), results);
		horizontalBounds.accumulateOnViolation(getHorizontalScaling(), getFrameMode(), results);
		verticalBounds.accumulateOnViolation(getVerticalScaling(), getFrameMode(), results);
		quantizationBounds.accumulateOnViolation(getQuantizationSelector(), getFrameMode(), results);

		return results;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof FrameComponent)) {
			return false;
		}
		FrameComponent otherEntry = (FrameComponent) other;

		return (getHierarchicalMode() == otherEntry.getHierarchicalMode() &&
			getComponentId() == otherEntry.getComponentId() &&
			getHorizontalScaling() == otherEntry.getHorizontalScaling() &&
			getVerticalScaling() == otherEntry.getVerticalScaling() &&
			getQuantizationSelector() == otherEntry.getQuantizationSelector());
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + this.componentId;
		hash = 41 * hash + this.horScalingFactor;
		hash = 41 * hash + this.vertScalingFactor;
		hash = 41 * hash + this.quantTableSelector;
		return hash;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void checkModeChange() {
		super.checkModeChange();

		setComponentId(getComponentId());
		setHorizontalScaling(getHorizontalScaling());
		setVerticalScaling(getVerticalScaling());
		setQuantizationSelector(getQuantizationSelector());
	}
}
