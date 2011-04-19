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

import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.ParseMode;
import bdw.formats.jpeg.segments.base.JpegDataStructureBase;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A list of run length headers and their Huffman coded
 * representations.  This can be for AC or DC tables, and
 * for Luminance or Chrominance (Cb and Cr).
 */
public class DhtHuffmanTable extends JpegDataStructureBase {

	/**
	 * True if this is an AC table (otherwise is DC)
	 */
	protected int isAc;

	/**
	 * ID of the table
	 */
	protected int id;

	/**
	 * The set of run length headers in this table
	 */
	protected List<DhtRunLengthHeader> headers;

	/**
	 * Constructs an instance
	 */
	public DhtHuffmanTable() {
		isAc = 0;
		id = 0;
		headers = new ArrayList<DhtRunLengthHeader>();
	}

	public boolean isValid() {
		if ((isAc & 0x0e) != 0x00) {
			return false;
		}

		return true;
	}

	/**
	 * @return True if this is an AC table, false if a DC one
	 */
	public boolean isAc() {
		return (isAc & 0x01) == 0x01;
	}

	/**
	 * @param ac true if this is an AC table, false otherwise
	 */
	public void setAc(boolean ac) {
		if (ac) {
			isAc = 0x01;
		} else {
			isAc = 0x00;
		}
	}

	/**
	 * @return id of this table (a value of 0-3)
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id The id for this table.
	 */
	public void setId(int id) {
		if ((id < 0) || (id > 3)) {
			throw new IllegalArgumentException("id must be between 0 and 3");
		}

		this.id = id;
	}

	/**
	 * @return the size of this element when written to disk
	 */
	public int getSizeOnDisk() {
		return 17 + headers.size();
	}

	/**
	 * @return The number of headers this manages
	 */
	public int getHeaderCount() {
		return headers.size();
	}

	/**
	 * @param index The index of the header to return
	 * @return Returns the Nth header (headers are always sorted, first by length, then by Huffman coding)
	 */
	public DhtRunLengthHeader getHeader(int index) {
		return headers.get(index);
	}

	/**
	 * Adds a new header here.  The header will not be accepted if
	 * its Huffman coding is not distinct from all others already
	 * in this table.  This will always sort the list of headers.
	 *
	 * @param header the header to add
	 */
	public void addHeader(DhtRunLengthHeader header) {
		if (header == null) {
			throw new IllegalArgumentException("header must not be null");
		}

		// Make sure this new header doesn't have a Huffman
		// code which is ambiguous with an existing one.
		// to validate this, we compare it with the code of
		// all the other headers in here.  If the bits of the
		// shorter match the same number of bits at the top
		// of the longer, then they are ambiguous.
		for (int index = 0; index < headers.size(); index++) {
			DhtRunLengthHeader other = headers.get(index);
			int shortBits;
			int longBits;

			if (other.getLength() < header.getLength()) {
				shortBits = other.getHuffmanCoding();
				longBits = header.getHuffmanCoding();

				longBits = longBits >> (header.getLength() - other.getLength());
			} else {
				longBits = other.getHuffmanCoding();
				shortBits = header.getHuffmanCoding();

				longBits = longBits >> (other.getLength() - header.getLength());
			}

			if (longBits == shortBits) {
				throw new IllegalArgumentException("Huffman coding is not distinct.  Will not add");
			}
		}

		headers.add(header);
		Collections.sort(headers, new HeaderComparator());
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof DhtHuffmanTable)) {
			return false;
		} else {
			DhtHuffmanTable otherTable = (DhtHuffmanTable) other;

			if (getId() != otherTable.getId()) {
				return false;
			}

			if (isAc() != otherTable.isAc()) {
				return false;
			}

			if (getHeaderCount() != otherTable.getHeaderCount()) {
				return false;
			}

			for (int index = 0; index < getHeaderCount(); index++) {
				if ( ! getHeader(index).equals(otherTable.getHeader(index))) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 83 * hash + this.isAc;
		hash = 83 * hash + this.id;
		hash = 83 * hash + (this.headers != null ? this.headers.hashCode() : 0);
		return hash;
	}

	/**
	 * @inheritdoc
	 */
	public void read(DataInput source, ParseMode strict) throws IOException, InvalidJpegFormat {
		int flags = source.readUnsignedByte();

		isAc = (flags & 0xF0) >> 4;

		if ((isAc & 0x0e) != 0x00) {
			if (strict == ParseMode.STRICT) {
				throw new InvalidJpegFormat("AC flag isn't 0 or 1, but instead is " + isAc);
			}
		}

		// TODO: What do do about bad data?
		id = (flags & 0x0F); // some say this should be 1, some say [0-3] are the only valid values

		int[] codeCounts = new int[16];

		for (int index = 0; index < 16; index++) {
			codeCounts[index] = source.readUnsignedByte();
		}

		// See http://www.impulseadventure.com/photo/jpeg-huffman-coding.html
		// This is an alternate algorithm than the one presented on that page.  The
		// end result, of course, is the same.  The heart of that description is:
		// build a binary tree, and at each level of the tree, fill all the
		// nodes as you can (left to right) using the codes stored here. When
		// a node isn't used, it becomes a parent of nodes to be used at the
		// next level.
		// This accomplishes this by observing: at level N of the tree, there
		// are 2^N nodes. If you number those nodes 0 to 2^N-1, you'll note that
		// the bit pattern of those numbers is the bit pattern that the tree
		// describes. (we don't need to actually build the tree to get the bits).
		// Fill in "nodes" here with entries from the file.  When a node
		// isn't used, we simply take the number we would have given to that
		// node, shift it by one bit (same as going down a level of the tree)
		// and keep counting from wherever we were to 2^(N+1)-1.
		// This gives us all the bit patterns without needing to build the
		// tree.
		int bits = 0;
		int mask = 1;
		for (int lengthIndex = 0; lengthIndex < 16; lengthIndex++) {
			int codesOfThisLength = codeCounts[lengthIndex];
			if (codesOfThisLength > (mask - (bits-1))) {
				throw new InvalidJpegFormat("More huffman codes of length " + lengthIndex+1 + " than spots available (" + codesOfThisLength + " needed versus " + (mask - (bits-1)) + "available)");
			}
			for (int code = 0; code < codesOfThisLength; code++) {
				if (bits > mask) {
					throw new InvalidJpegFormat("Node number larger than available space (" + bits + " versus " + mask +")");
				}
				int header = source.readUnsignedByte();
				headers.add(new DhtRunLengthHeader(bits, lengthIndex+1, (header & 0xF0) >> 4, (header & 0x0F)));
				bits ++;
			}
			bits = bits << 1;
			mask = (mask << 1) | 0x01;
		}
	}

	/**
	 * Writes this table out in a jpeg compliant format.
	 * @param out The output stream to write to
	 * @throws IOException If an error occurs while writing
	 */
	public void write(DataOutput out) throws IOException {
		int flags = 0;

		if (isAc()) {
			flags = 0x10;
		}

		flags |= id;

		out.writeByte(flags);


		int[] counts = new int[16];

		for (int index = 0; index < headers.size(); index++) {
			DhtRunLengthHeader header = headers.get(index);
			counts[header.getLength() -1] ++;
		}

		for (int index = 0; index < 16; index++) {
			out.writeByte(counts[index]);
		}

		for (int index = 0; index < headers.size(); index++) {
			DhtRunLengthHeader header = headers.get(index);
			int headerInt = header.getInitialZeroBitCount();
			headerInt = headerInt << 4;
			headerInt |= header.getFolowingDataBitCount();

			out.writeByte(headerInt);
		}
	}


	/**
	 * Convenience class used to compare two of the header classes.
	 * We consider the order to be a combination of the length and
	 * the Huffman bits (the latter, within a particular length group,
	 * are just an integer).  so combine the two numbers and do an ordinary compare
	 */
	protected class HeaderComparator implements Comparator<DhtRunLengthHeader> {

		public int compare(DhtRunLengthHeader h1, DhtRunLengthHeader h2) {
			if ((h1 == null) || (h2 == null)) {
				throw new IllegalArgumentException();
			}
			int h1Key = h1.getLength();
			h1Key = h1Key << 16;
			h1Key |= h1.getHuffmanCoding();

			int h2Key = h2.getLength();
			h2Key = h2Key << 16;
			h2Key |= h2.getHuffmanCoding();

			if (h1Key > h2Key) {
				return 1;
			} else if (h1Key < h2Key) {
				return -1;
			} else {
				return 0;
			}
		}

	}
}
