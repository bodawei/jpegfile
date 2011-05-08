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
import bdw.formats.jpeg.segments.support.SosDescriptor;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Start of Scan marker
 */
public class SosSegment extends SegmentBase {

	/**
	 * The marker that this type accepts
	 */
	public static final int MARKER = 0xDA;

	/**
	 * Array of ScanDescriptors
	 */
	private List<SosDescriptor> scanDescriptors;

	/**
	 * Start of spectral selection
	 */
	private int spectralSelectionStart;

	/**
	 * End of spectral selection
	 */
	private int spectralSelectionEnd;

	/**
	 * Successive approximation
	 */
	private int successiveApproximation;

	/**
	 * Constructs an instance with all properties empty
	 */
	public SosSegment() {
		scanDescriptors = new ArrayList<SosDescriptor>();
		spectralSelectionStart = 0;
		spectralSelectionEnd = 0;
		successiveApproximation = 0;
		setMarker(SosSegment.MARKER);
	}

	/**
	 * @return the start of the spectral selection
	 */
	public int getSpectralSelectionStart() {
		return spectralSelectionStart;
	}

	/**
	 * Sets the start spectral selection value
	 * @param start a value between 0 and 255 indicating the start
	 */
	public void setSpectralSelectionStart(int start) {
		paramIsUInt8(start);

		spectralSelectionStart = start;
	}

	/**
	 * @return the end of the spectral selection
	 */
	public int getSpectralSelectionEnd() {
		return spectralSelectionEnd;
	}

	/**
	 * Sets the end spectral selection value
	 * @param start a value between 0 and 255 indicating the start
	 */
	public void setSpectralSelectionEnd(int end) {
		paramIsUInt8(end);

		spectralSelectionEnd = end;
	}

	/**
	 * @return the successive approximation value
	 */
	public int getSuccessiveApproximation() {
		return successiveApproximation;
	}

	/**
	 * Sets the end spectral selection value
	 * @param start a value between 0 and 255 indicating the start
	 */
	public void setSuccessiveApproximation(int approx) {
		paramIsUInt8(approx);

		successiveApproximation = approx;
	}

	/**
	 * @return The number of descriptors in this segment
	 */
	public int getDescriptorCount() {
		return scanDescriptors.size();
	}

	/**
	 * @param index Index of the descriptor to be returned
	 * @return The descriptor at the specified index
	 * @throws IndexOutOfBoundsException If the index is negative or larger than the number of components
	 */
	public SosDescriptor getDescriptor(int index) throws IndexOutOfBoundsException {
		return scanDescriptors.get(index);
	}

	/**
	 * @param index The index to put the new descriptor (others are moved "right"
	 * @param component The descriptor to add
	 * @throws IndexOutOfBoundsException If the index is negative or larger than where can be added
	 * @throws IllegalArgumentException if component is null, or if there are already 256 entries in the set
	 */
	public void addDescriptor(int index, SosDescriptor descriptor) throws
			IndexOutOfBoundsException {
		if (descriptor == null) {
			throw new IllegalArgumentException("Entry may not be null");
		}

		if (scanDescriptors.size() >= 256) {
			throw new IllegalArgumentException("Too many descriptors already in the list");
		}
		scanDescriptors.add(index, descriptor);
	}

	/**
	 * @param index Index of the descriptor to delete
	 * @throws IndexOutOfBoundsException If the index is out of range
	 */
	public void deleteDescriptor(int index) throws IndexOutOfBoundsException {
		scanDescriptors.remove(index);
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void readData(DataInput input) throws IOException {
		int contentLength = input.readUnsignedShort();
		int componentCount = input.readUnsignedByte();

		if ((2 + 1 + (componentCount * 2) + 3) != contentLength) {
			throw new IllegalArgumentException("Sos Got " + (2 + 1 + (componentCount * 2) + 3) + " bytes, but expected " + contentLength);
		}

		for (int index = 0; index < componentCount; index++) {
			SosDescriptor entry = new SosDescriptor();
			entry.readData(input);
			addDescriptor(index, entry);
		}

		setSpectralSelectionStart(input.readUnsignedByte());
		setSpectralSelectionEnd(input.readUnsignedByte());
		setSuccessiveApproximation(input.readUnsignedByte());
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		super.write(stream);
		DataOutputStream dataStream;

		if (stream instanceof DataOutputStream) {
			dataStream = (DataOutputStream) stream;
		} else {
			dataStream = new DataOutputStream(stream);
		}

		dataStream.writeShort(2 + 1 + (getDescriptorCount() * 2) + 3);

		dataStream.writeByte(getDescriptorCount());

		for (int index = 0; index < getDescriptorCount(); index++) {
			SosDescriptor entry = getDescriptor(index);
			entry.write(dataStream);
		}

		dataStream.writeByte(getSpectralSelectionStart());
		dataStream.writeByte(getSpectralSelectionEnd());
		dataStream.writeByte(getSuccessiveApproximation());
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		} else if (!(other instanceof SosSegment)) {
			return false;
		} else if (other != this) {
			SosSegment castOther = (SosSegment) other;
			if ((getSpectralSelectionStart() == castOther.getSpectralSelectionStart())
					&& (getSpectralSelectionEnd() == castOther.getSpectralSelectionEnd())
					&& (getSuccessiveApproximation() == castOther.getSuccessiveApproximation())) {
				if (getDescriptorCount() != castOther.getDescriptorCount()) {
					return false;
				}
				for (int index = 0; index < getDescriptorCount(); index++) {
					SosDescriptor myEntry = getDescriptor(index);
					SosDescriptor otherEntry = castOther.getDescriptor(index);
					if ( ! myEntry.equals(otherEntry)) {
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
		int hash = 7;
		hash = 37 * hash + (this.scanDescriptors != null ? this.scanDescriptors.hashCode() : 0);
		hash = 37 * hash + this.spectralSelectionStart;
		hash = 37 * hash + this.spectralSelectionEnd;
		hash = 37 * hash + this.successiveApproximation;
		return hash;
	}

}
