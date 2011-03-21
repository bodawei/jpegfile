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

import bdw.formats.jpeg.segments.base.SegmentBase;
import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.segments.support.SofComponent;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Start Of Frame Segment
 * There are several markers that can be used to identify instances of this segment type.
 * By default, this sets its marker to 0, so it is important that the right marker be assigned to the
 * instance of this class after creation.
 *
 * Note: One source strongly implies 0xc3-0xcb are structured differently than 0xc0-0xc2.
 */
public class SofSegment extends SegmentBase {

	/**
	 * Start of the first range of possible markers
	 */
	public static final int RANGE1_START = 0xc0;
	/**
	 * End of the first range of possible markers
	 */
	public static final int RANGE1_END = 0xc3;
	/**
	 * Start of the second range of possible markers
	 */
	public static final int RANGE2_START = 0xc5;
	/**
	 * End of the second range of possible markers
	 */
	public static final int RANGE2_END = 0xc7;
	/**
	 * Start of the third range of possible markers
	 */
	public static final int RANGE3_START = 0xc9;
	/**
	 * End of the third range of possible markers
	 */
	public static final int RANGE3_END = 0xcb;
	/**
	 * Start of the fourth range of possible markers
	 */
	public static final int RANGE4_START = 0xcd;
	/**
	 * End of the fourth range of possible markers
	 */
	public static final int RANGE4_END = 0xcf;
	/**
	 * Max size for the precision and number of components
	 */
	protected static final int UINT1_MAX_VALUE = 255;
	/**
	 * Max size for the width and height
	 */
	protected static final int UINT2_MAX_VALUE = 65535;
	/**
	 * Precision (?)
	 */
	protected int samplePrecision;
	/**
	 * Height of the image?
	 */
	protected int imageHeight;
	/**
	 * Width of the image?
	 */
	protected int imageWidth;
	/**
	 * The ordered list of components defined by this segment
	 */
	protected List<SofComponent> components;

	/**
	 * Construct
	 */
	public SofSegment() {
		setMarker(0);
		components = new ArrayList<SofComponent>();
	}

	/**
	 * @return The sample precision
	 */
	public int getSamplePrecision() {
		return samplePrecision;
	}

	/**
	 * Sets the sample precision
	 */
	public void setSamplePrecision(int precision) {
		this.paramIsUInt8(precision);

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
	 */
	public void setImageHeight(int height) {
		this.paramIsUInt16(height);

		imageHeight = height;
	}

	/**
	 * @return The width of the image
	 */
	public int getImageWidth() {
		return imageWidth;
	}

	/**
	 * @param width The width of the image. This may be between 0 and 65,536
	 */
	public void setImageWidth(int width) {
		this.paramIsUInt16(width);

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
	 * @throws IndexOutOfBoundsException If the index is negative or larger than the number of components
	 */
	public SofComponent getComponent(int index) throws IndexOutOfBoundsException {
		return components.get(index);
	}

	/**
	 * @param index The index to put the new component (others are moved "right"
	 * @param component The component to add
	 * @throws IndexOutOfBoundsException If the index is negative or larger than where can be added
	 * @throws IllegalArgumentException if component is null, or if there are already 256 entries in the set
	 */
	public void addComponent(int index, SofComponent component) throws
			IndexOutOfBoundsException {
		if (component == null) {
			throw new IllegalArgumentException("Entry may not be null");
		}

		if (components.size() >= 256) {
			throw new IllegalArgumentException("Too many components already in the list");
		}
		components.add(index, component);
	}

	/**
	 * @param index Index of the component to delete
	 * @throws IndexOutOfBoundsException If the index is out of range
	 */
	public void deleteComponent(int index) throws IndexOutOfBoundsException {
		components.remove(index);
	}

	/**
	 * Read all the data and populate this instance.
	 * This resets all contents of this segment. This means that this is like
	 * deleting all components and then setting all properties afresh.
	 *
	 * Because this will never be more than 1 kilobyte of data, we never defer
	 * reading until later.
	 * Note: If any errors occur while reading, this instance will not be
	 * changed.
	 *
	 * @param dataSource The input source to read from
	 *
	 * @throws IOException If something happens while reading
	 */
	@Override
	protected void readData(DataInput dataSource) throws IOException, InvalidJpegFormat {
		int precision;
		int height;
		int width;
		List<SofComponent> comps = new ArrayList<SofComponent>();

		try {
			int contentLength = dataSource.readUnsignedShort();

			if (contentLength < 7) {
				throw new InvalidJpegFormat("Sof segment found not enough length");
			}

			precision = dataSource.readUnsignedByte();
			height = dataSource.readUnsignedShort();
			width = dataSource.readUnsignedShort();
			int numComponents = dataSource.readUnsignedByte();

			if (contentLength != 8 + (SofComponent.DISK_SIZE * numComponents)) {
				throw new InvalidJpegFormat("Sof segment found not enough length");
			}

			// Note: According to one source, usually numComponents is:
			// 1 = grey scale
			// 3 = color YCbCr or YIQ
			// 4 = color CMYK

			for (int index = 0; index < numComponents; index++) {
				SofComponent entry = new SofComponent();
				entry.read(dataSource);
				comps.add(entry);
			}
		} catch (EOFException exception) {
			throw new InvalidJpegFormat("EOF found before Sof segment fully read");
		}

		// If we get this far, all is good. We can now commit these values:
		setSamplePrecision(precision);
		setImageHeight(height);
		setImageWidth(width);
		components.clear();
		components.addAll(comps);
	}

	/**
	 * Write this segment out to the specified output stream
	 * @param output The stream to write out to
	 * @throws IOException If any errors occur
	 */
	@Override
	public void write(OutputStream output) throws IOException {
		DataOutput out;
		if (output instanceof DataOutput) {
			out = (DataOutput) output;
		} else {
			out = new DataOutputStream(output);
		}
		out.writeShort(2 + 6 + (SofComponent.DISK_SIZE * components.size()));
		out.writeByte(getSamplePrecision());
		out.writeShort(getImageHeight());
		out.writeShort(getImageWidth());
		out.writeByte(components.size());
		for (int index = 0; index < components.size(); index++) {
			SofComponent entry = components.get(index);
			entry.write(out);
		}
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

	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof SofSegment)) {
			return false;
		} else {
			SofSegment segment = (SofSegment) other;

			if ((samplePrecision != segment.getSamplePrecision()) ||
					(imageHeight != segment.getImageHeight()) ||
					(imageWidth != segment.getImageWidth()) ||
					(components.size() != segment.getComponentCount())) {
				return false;
			}

			for (int index = 0; index < components.size(); index++) {
				SofComponent myEntry = components.get(index);
				SofComponent otherEntry = segment.getComponent(index);
				if ( ! myEntry.equals(otherEntry)) {
					return false;
				}
			}
		}
		return true;
	}
}
