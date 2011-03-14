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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

/**
 * A single component as managed by a SofSegment
 */
public class SofComponentEntry {
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
	private int samplingX;

	/**
	 * Sampling value on the y axis
	 */
	private int samplingY;

	/**
	 * The quantization id
	 */
	private int quantizationTableId;

	/**
	 * @return the id for this component
	 */
	public int getId() {
		return componentId;
	}

	/**
	 * Set the id for the component
	 *
	 * @param value the id for this component (0-255)
	 */
	public void setId(int id) {
		if (!Utils.inUInt8Range(id)) {
			throw new IllegalArgumentException("id must be [0,255]. Was " + id);
		}

		componentId = id;
	}

	/**
	 * @return the sampling value for x
	 */
	public int getSamplingX() {
		return samplingX;
	}

	/**
	 * Set the sampling value for the horizontal axis
	 *
	 * @param value the sampling value for X (between 0 and 15)
	 */
	public void setSamplingX(int value) {
		if (!Utils.inUInt4Range(value)) {
			throw new IllegalArgumentException("id must be [0,15]. Was " + value);
		}

		samplingX = value;
	}

	/**
	 * @return The y sampling value
	 */
	public int getSamplingY() {
		return samplingY;
	}

	/**
	 * Set the sampling value for the vertical axis
	 *
	 * @param value the sampling value for Y (between 0 and 15)
	 */
	public void setSamplingY(int value) {
		if (!Utils.inUInt4Range(value)) {
			throw new IllegalArgumentException("id must be [0,15]. Was " + value);
		}

		samplingY = value;
	}

	/**
	 * @return The quantization table ID
	 */
	public int getQuantizationId() {
		return quantizationTableId;
	}

	/**
	 * Set the quantization table id
	 *
	 * @param id The quantization table id (between 0 and 255)
	 */
	public void setQuantizationId(int id) {
		if (!Utils.inUInt8Range(id)) {
			throw new IllegalArgumentException("id must be [0,255]. Was " + id);
		}

		quantizationTableId = id;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof SofComponentEntry)) {
			return false;
		} else {
			SofComponentEntry otherEntry = (SofComponentEntry) other;

			if ((componentId != otherEntry.getId())
					|| (samplingX != otherEntry.getSamplingX())
					|| (samplingY != otherEntry.getSamplingY())
					|| (quantizationTableId != otherEntry.getQuantizationId())) {
				return false;
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
		hash = 41 * hash + this.componentId;
		hash = 41 * hash + this.samplingX;
		hash = 41 * hash + this.samplingY;
		hash = 41 * hash + this.quantizationTableId;
		return hash;
	}

	/**
	 * Reads a component entry from the input stream.  This must be in a
	 * format as dictated by the jpeg standard.  This fully replaces the
	 * contents of this instance
	 *
	 * @param source The data input source to read from
	 * @throws IOException If any problem occurs
	 * @throws EOFException If the EOF is found while trying to read
	 */
	public void read(DataInput source) throws IOException, EOFException {
		int id = source.readUnsignedByte(); //  one source says: (1 = Y, 2 = Cb, 3 = Cr, 4 = I, 5 = Q)
		int values = source.readUnsignedByte();
		int quantId = source.readUnsignedByte();

		setId(id);
		setSamplingY(values & 0x0F);
		setSamplingX(values >> 4);
		setQuantizationId(quantId);
	}

	/**
	 * Writes this segment out to a jpeg-compliant data format on the output stream
	 * @param out The output stream to write to
	 * @throws IOException If an error occurs while writing
	 */
	public void write(DataOutput out) throws IOException {
		int sampling;
		out.writeByte(getId());

		sampling = getSamplingX() * 16;
		sampling |= getSamplingY();
		out.writeByte(sampling);
		out.writeByte(getQuantizationId());
	}

}
