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
package bdw.format.jpeg.data;

import bdw.format.jpeg.component.FrameComponent;
import bdw.format.jpeg.marker.DhpSegment;
import bdw.format.jpeg.support.DataBounds;
import bdw.format.jpeg.support.DataMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.io.LimitingDataInput;
import bdw.util.Size;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic superclass for SOF Segment and DHP Segment.
 */
public class FrameSegment extends MarkerSegment {
	private static final DataBounds samplePrecisionBounds = new DataBounds(
		"samplePrecision", Size.BYTE,
			8, 8,
			8, 12,
			8, 12,
			2, 16);

	private static final DataBounds imageHeightBounds =
			  new DataBounds("imageHeight", Size.SHORT, 0, 65535);

	private static final DataBounds imageWidthBounds =
			  new DataBounds("imageWidth", Size.SHORT, 1, 65535);

	private static final DataBounds componentCountBounds = new DataBounds(
		"componentCount", Size.BYTE,
			1, 255,
			1, 255,
			1, 4,
			1, 255);

	/**
	 * Precision (?)
	 */
	protected int samplePrecision = 8;
	/**
	 * Height of the image?
	 */
	protected int imageHeight = 0;
	/**
	 * Width of the image?
	 */
	protected int imageWidth = 1;

	/**
	 * The ordered list of components defined by this segment
	 */
	protected List<FrameComponent> components = new ArrayList<FrameComponent>();

	/**
	 * Constructs an instance with all properties empty
    *
    * @param markerId Id of the marker.
	 */
	public FrameSegment(int markerId) {
		super(markerId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + 6 + (FrameComponent.DISK_SIZE * components.size());
	}

	/**
	 * @return The sample precision
	 */
	public int getSamplePrecision() {
		return samplePrecision;
	}

	/**
	 * Sets the sample precision.
	 * @param precision 8, 8 or 12, or 2-16 depending on the mode.
	 */
	public void setSamplePrecision(int precision) {
		samplePrecisionBounds.throwIfInvalid(precision, getFrameMode(), getDataMode());
		throwIfPrecisionInvalid(precision);

		samplePrecision = precision;
	}

	/**
	 * @return The height of the image
	 */
	public int getImageHeight() {
		return imageHeight;
	}

	/**
	 * Sets the height of the image
	 * @param height 0-65535
	 */
	public void setImageHeight(int height) {
		imageHeightBounds.throwIfInvalid(height, getFrameMode(), getDataMode());

		imageHeight = height;
	}

	/**
	 * @return The width of the image
	 */
	public int getImageWidth() {
		return imageWidth;
	}

	/**
	 * @param width The width of the image. This may be between 1 and 65,536
	 */
	public void setImageWidth(int width) {
		imageWidthBounds.throwIfInvalid(width, getFrameMode(), getDataMode());

		imageWidth = width;
	}

	/**
	 * @return The number of components in this segment
	 */
	public int getComponentCount() {
		return components.size();
	}

	/**
	 * @param index Index of the component to be returned
	 * @return The component at the specified index
	 */
	public FrameComponent getComponent(int index) {
		return components.get(index);
	}

	public void addComponent(FrameComponent component) {
		insertComponent(this.components.size(), component);
	}
	/**
	 * @param index The index to put the new component (others are moved "right"
	 * @param component The component to add
	 */
	public void insertComponent(int index, FrameComponent component) throws
			IndexOutOfBoundsException {
		if (component == null) {
			throw new IllegalArgumentException("Entry may not be null");
		}

		componentCountBounds.throwIfInvalid(components.size() + 1, getFrameMode(), getDataMode());

		component.setDataMode(getDataMode());
		component.setFrameMode(getFrameMode());
		components.add(index, component);
	}

	/**
	 * @param index Index of the component to delete
	 */
	public void deleteComponent(int index) {
		components.remove(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();

		samplePrecisionBounds.accumulateOnViolation(getSamplePrecision(), this.getFrameMode(), results);
		imageHeightBounds.accumulateOnViolation(getImageHeight(), this.getFrameMode(), results);
		imageWidthBounds.accumulateOnViolation(getImageWidth(), this.getFrameMode(), results);
		componentCountBounds.accumulateOnViolation(components.size(), this.getFrameMode(), results);
		accumulateOnPrecisionViolation(results);

		for (FrameComponent component: components) {
			results.addAll(component.validate());
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if (!super.equals(other)) {
			return false;
		}

		FrameSegment segment = (FrameSegment) other;

		if ((getMarkerId() != segment.getMarkerId()) ||
				(getSamplePrecision() != segment.getSamplePrecision()) ||
				(getImageHeight() != segment.getImageHeight()) ||
				(getImageWidth() != segment.getImageWidth()) ||
				(getComponentCount() != segment.getComponentCount())) {
			return false;
		}

		for (int index = 0; index < components.size(); index++) {
			FrameComponent myEntry = components.get(index);
			FrameComponent otherEntry = segment.getComponent(index);
			if ( ! myEntry.equals(otherEntry)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 73 * hash + this.samplePrecision;
		hash = 73 * hash + this.imageHeight;
		hash = 73 * hash + this.imageWidth;
		hash = 73 * hash + (this.components != null ? this.components.hashCode() : 0);
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void checkModeChange() {
		super.checkModeChange();
		setSamplePrecision(getSamplePrecision());
		setImageHeight(getImageHeight());
		setImageWidth(getImageWidth());
		// Allow 0, even though this is invalid in strict mode.
		if (getComponentCount() != 0) {
			componentCountBounds.throwIfInvalid(components.size(), getFrameMode(), getDataMode());
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param dataSource The input source to read from
	 *
	 * @throws IOException If something happens while reading
	 */
	@Override
	protected void readParameters(LimitingDataInput dataSource) throws IOException {
		super.readParameters(dataSource);

		int contentLength = dataSource.getRemainingLimit();

		if (contentLength < 9) {
			throw new InvalidJpegFormat("Sof segment found not enough length");
		}

		components.clear();

		setSamplePrecision(dataSource.readUnsignedByte());
		setImageHeight(dataSource.readUnsignedShort());
		setImageWidth(dataSource.readUnsignedShort());

		int numComponents = dataSource.readUnsignedByte();
		componentCountBounds.throwIfInvalid(numComponents, getFrameMode(), getDataMode());

		if (contentLength != 6 + (FrameComponent.DISK_SIZE * numComponents)) {
			throw new InvalidJpegFormat("Sof segment found not enough length");
		}

		for (int index = 0; index < numComponents; index++) {
			FrameComponent component = new FrameComponent();
			component.setHierarchicalMode(this.getMarkerId() == DhpSegment.MARKERID);
			component.readParameters(dataSource);
			components.add(component);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stream The stream to write out to
	 * @throws IOException If any errors occur
	 */
	@Override
	protected void writeParameters(DataOutputStream stream) throws IOException {
		super.writeParameters(stream);

		if (getComponentCount() == 0) {
			throw new IllegalStateException("There must be at least one component before one can write");
		}

		stream.writeByte(getSamplePrecision());
		stream.writeShort(getImageHeight());
		stream.writeShort(getImageWidth());
		stream.writeByte(components.size());
		for (FrameComponent component: components) {
			component.writeParameters(stream);
		}
	}

	/**
	 * If there is a problem with the current precision value, add the exception
	 * to the results list
	 */
	private void accumulateOnPrecisionViolation(List<Exception> results) {
		RuntimeException result = checkPrecisionCondition(getSamplePrecision());
		if (result != null) {
			results.add(result);
		}
	}

	/**
	 * Throws an exception if there is a problem with the specified precision
	 */
	private void throwIfPrecisionInvalid(int precision) {
		RuntimeException result = checkPrecisionCondition(precision);
		if (result != null) {
			throw result;
		}
	}

	/**
	 * Checks if the specified precision is valid.
	 */
	private RuntimeException checkPrecisionCondition(int precision) {
		if (precision != 8 && precision != 12 &&
			(getFrameMode() != null && !getFrameMode().isSequentialBaseline() &&
				  !getFrameMode().isLossless()) &&
			getDataMode() == DataMode.STRICT) {
			return new IllegalArgumentException("samplePrecision mus be either 8 or 12");
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void	changeChildrenModes() {
		super.changeChildrenModes();
		for (FrameComponent component: components) {
			component.setDataMode(getDataMode());
			component.setFrameMode(getFrameMode());
			component.setHierarchicalMode(getHierarchicalMode());
		}
	}
}
