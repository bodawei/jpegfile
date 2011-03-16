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

import bdw.formats.jpeg.segments.support.ScanDescriptorEntry;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Start of Scan marker
 */
public class SosSegment extends SegmentBase {

	public static final int MARKER = 0xDA;
	private List<ScanDescriptorEntry> scanDescriptors;
	private int spectralSelectionStart;
	private int spectralSelectionEnd;
	private int successiveApproximation;

	public SosSegment() {
		scanDescriptors = new ArrayList<ScanDescriptorEntry>();
		spectralSelectionStart = 0;
		spectralSelectionEnd = 0;
		successiveApproximation = 0;
	}

	@Override
	public int getMarker() {
		return SosSegment.MARKER;
	}

	@Override
	public void readFromFile(RandomAccessFile file) throws IOException {
		readData(file);
	}

	/**
	 *
	 */
	@Override
	public void readFromStream(InputStream stream) throws IOException {
		if (stream instanceof DataInputStream) {
			readData((DataInputStream) stream);
		} else {
			DataInputStream newStream = new DataInputStream(stream);
			readData(newStream);
		}
	}

	/**
	 *
	 */
	@Override
	protected void readData(DataInput input) throws IOException {
		int contentLength = input.readUnsignedShort();
		int componentCount = input.readUnsignedByte();

		if ((2 + 1 + (componentCount * 2) + 3) != contentLength) {
			throw new IllegalArgumentException("Need to report the error, and store the byte offset too.");
		}

		for (int index = 0; index < componentCount; index++) {
			ScanDescriptorEntry entry = new ScanDescriptorEntry();
			entry.readData(input);
			scanDescriptors.add(entry);
		}

		spectralSelectionStart = input.readUnsignedByte();
		spectralSelectionEnd = input.readUnsignedByte();
		successiveApproximation = input.readUnsignedByte();
	}

	public int getSpectralSelectionStart() {
		return spectralSelectionStart;
	}

	public int getSpectralSelectionEnd() {
		return spectralSelectionEnd;
	}

	public int getSuccessiveApproximation() {
		return successiveApproximation;
	}

	/**
	 * @return An iterator that will iterate over all the segments in the file
	 */
	public Iterator<ScanDescriptorEntry> iterator() {
		return scanDescriptors.listIterator();
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		super.write(stream);
		DataOutputStream dataStream;
		Iterator<ScanDescriptorEntry> iterator;

		if (stream instanceof DataOutputStream) {
			dataStream = (DataOutputStream) stream;
		} else {
			dataStream = new DataOutputStream(stream);
		}

		dataStream.writeShort(2 + 1 + (scanDescriptors.size() * 2) + 3);

		dataStream.writeByte(scanDescriptors.size());

		iterator = iterator();
		while (iterator.hasNext()) {
			ScanDescriptorEntry entry = iterator.next();
			entry.write(dataStream);
		}

		dataStream.writeByte(spectralSelectionStart);
		dataStream.writeByte(spectralSelectionEnd);
		dataStream.writeByte(successiveApproximation);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		} else if (!(other instanceof SosSegment)) {
			return false;
		} else if (other != this) {
			SosSegment castOther = (SosSegment) other;
			if ((spectralSelectionStart == castOther.getSpectralSelectionStart())
					&& (spectralSelectionEnd == castOther.getSpectralSelectionEnd())
					&& (successiveApproximation == castOther.getSuccessiveApproximation())) {
				Iterator<ScanDescriptorEntry> myIterator = iterator();
				Iterator<ScanDescriptorEntry> otherIterator = castOther.iterator();
				while (otherIterator.hasNext()) {
					if (myIterator.hasNext() == false) {
						return false;
					} else {
						ScanDescriptorEntry myEntry = myIterator.next();
						ScanDescriptorEntry otherEntry = otherIterator.next();
						if ( ! myEntry.equals(otherEntry)) {
							return false;
						}
					}
				}

				if (myIterator.hasNext()) {
					return false;
				}
			}
		}

		return true;
	}

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
