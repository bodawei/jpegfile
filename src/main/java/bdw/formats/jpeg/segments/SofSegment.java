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
import bdw.formats.jpeg.ParseMode;
import bdw.formats.jpeg.segments.support.SofComponent;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
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
	public static final int FIRST1_SUBTYPE = 0xc0;
	/**
	 * End of the first range of possible markers
	 */
	public static final int LAST1_SUBTYPE = 0xc3;
	/**
	 * Start of the second range of possible markers
	 */
	public static final int FIRST2_SUBTYPE = 0xc5;
	/**
	 * End of the second range of possible markers
	 */
	public static final int LAST2_SUBTYPE = 0xc7;
	/**
	 * Start of the third range of possible markers
	 */
	public static final int FIRST3_SUBTYPE = 0xc9;
	/**
	 * End of the third range of possible markers
	 */
	public static final int LAST3_SUBTYPE = 0xcb;
	/**
	 * Start of the fourth range of possible markers
	 */
	public static final int FIRST4_SUBTYPE = 0xcd;
	/**
	 * End of the fourth range of possible markers
	 */
	public static final int LAST4_SUBTYPE = 0xcf;

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
	 * Constructs an instance with all properties empty
	 */
	public SofSegment(int subType) throws InvalidJpegFormat {
		components = new ArrayList<SofComponent>();
		if (SofSegment.canHandleMarker(subType)) {
			setMarker(subType);		
		} else {
			throw new InvalidJpegFormat("The subtype " + subType + " is not applicable to " + this.getClass().getSimpleName());
		}
	}

	/**
	 * Construct an instance from a stream.
	 *
	 * @param stream The stream to read from
	 * @param mode The mode to parse this in. At this time, no distinction is made between modes.
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public SofSegment(int subType, InputStream stream, ParseMode mode) throws IOException, InvalidJpegFormat {
		this(subType);
		super.readFromStream(stream, mode);
    }

	/**
	 * Construct an instance from a stream.
	 *
	 * @param file The file to read from
	 * @param mode The mode to parse this in. At this time, no distinction is made between modes.
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public SofSegment(int subType, RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
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
		if (((marker >= SofSegment.FIRST1_SUBTYPE) && (marker <= SofSegment.LAST1_SUBTYPE)) ||
			((marker >= SofSegment.FIRST2_SUBTYPE) && (marker <= SofSegment.LAST2_SUBTYPE)) ||	
			((marker >= SofSegment.FIRST3_SUBTYPE) && (marker <= SofSegment.LAST3_SUBTYPE)) ||
			((marker >= SofSegment.FIRST4_SUBTYPE) && (marker <= SofSegment.LAST4_SUBTYPE))) {
			return true;
		}
		return false;
	}

	/**
	 * @return true if the segment is strictly valid
	 */
	@Override
	public boolean isValid() {
		if (! super.isValid()) {
			return false;
		} else {
			if ((components.size() == 0) ||
				(components.size() == 2) ||
				(components.size() > 4)) {
				return false;
			}

			for (SofComponent component : components) {
				if ( ! component.isValid()) {
					return false;
				}
			}
		}
		return true;
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
	 * Write this segment out to the specified output stream
	 * @param output The stream to write out to
	 * @throws IOException If any errors occur
	 */
	@Override
	public void write(OutputStream output) throws IOException {
		DataOutput out = super.wrapAsDataOutputStream(output);

		if ( ! isValid()) {
			throw new IOException("The Number of components is not valid, or the components themselves are not valid");
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
	protected void readData(DataInput dataSource, ParseMode mode) throws IOException, InvalidJpegFormat {
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

			if ((numComponents == 0) ||
				(numComponents == 2) ||
				(numComponents > 4)) {
				if (mode == ParseMode.STRICT) {
					throw new InvalidJpegFormat("Sof segment found with " + numComponents + " components. This should be 1, 3 or 4");
				} else {
					setValid(false);
				}
			}

			for (int index = 0; index < numComponents; index++) {
				SofComponent entry = new SofComponent();
				entry.read(dataSource, mode);
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
}
