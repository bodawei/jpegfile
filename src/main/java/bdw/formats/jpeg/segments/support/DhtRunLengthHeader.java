package bdw.formats.jpeg.segments.support;

import bdw.formats.jpeg.segments.base.JpegDataStructureBase;

/**
 * This value class represents a run length header from the DhtSegment.
 * Specifically, in a JPEG file, the run length header is two nibbles,
 * where the first represents how many zeros have been elided, and the
 * second represents how many bits of data follow. However,
 * this header, in the data, is represented by a Huffman coded sequence
 * of bits.  So, the header 0x4a (4 zeros, 10 bits of data following)
 * might be represented as 0b101.
 * This object represents this whole mass of data.  It captures
 * the bit sequence that is used to represent this the run length
 * header (and a length and a mask which can be used to help do
 * calculations with that) as well as the number of zeros and following
 * data.
 */
public class DhtRunLengthHeader extends JpegDataStructureBase {

	/**
	 * The Huffman coded bits
	 */
	protected int huffmanCode;
	
	/**
	 * A mask that can be used, with an & operation, the data bits
	 */
	protected int mask;
	
	/**
	 * The length, in bits, of the huffman code
	 */
	protected int bitsLength;
	
	/**
	 * The number of zero bits this header indicates have been skipped
	 */
	protected int zeroBitCount;

	/**
	 * The number of data bits following this header.
	 */
	protected int dataBitCount;

	/**
	 * Constructs an instance
	 * @param huffmanCode The bit sequence of the huffman encoding of this header (0-65536)
	 * @param length The number of bits in the huffman data (1-16)
	 * @param zeroBitCount The number of zeros this header indicates have been skipped (0-15)
	 * @param dataBitCount The number of data bits following the header (0-15)
	 */
	public DhtRunLengthHeader(int huffmanCode, int length, int zeroBitCount, int dataBitCount) {
		this.paramIsUInt16(huffmanCode);
		if ((length > 16) || (length <= 0)) {
			throw new IllegalArgumentException("length must be 1-16");
		}
		this.paramIsUInt4(zeroBitCount);
		this.paramIsUInt4(dataBitCount);

		this.huffmanCode = huffmanCode;
		this.bitsLength = length;
		this.zeroBitCount = zeroBitCount;
		this.dataBitCount = dataBitCount;
		this.mask = makeMask(length);
	}

	/**
	 * @return The number of 0 bits skipped by this header
	 */
	public int getInitialZeroBitCount() {
		return zeroBitCount;
	}

	/**
	 * @return The number of data bits following this header
	 */
	public int getFolowingDataBitCount() {
		return dataBitCount;
	}

	/**
	 * @return A bitmask that includes all bits in the huffman coding. Thus,
	 * if the huffman coding is 0x05 (0b0000 0101), the mask would be 0x07 (0b0000 0111)
	 */
	public int getHuffmanCodingBitMask() {
		return mask;
	}

	/**
	 * @return The set of bits representing the huffman coding of this header
	 */
	public int getHuffmanCoding() {
		return huffmanCode;
	}

	/**
	 * @return The length of the Huffman coded data
	 */
	public int getLength() {
		return bitsLength;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof DhtRunLengthHeader)) {
			return false;
		} else {
			DhtRunLengthHeader otherHeader = (DhtRunLengthHeader) other;
			if (otherHeader.getFolowingDataBitCount() != getFolowingDataBitCount()) {
				return false;
			}

			if (otherHeader.getInitialZeroBitCount() != getInitialZeroBitCount()) {
				return false;
			}

			if (otherHeader.getHuffmanCoding() != getHuffmanCoding()) {
				return false;
			}

			if (otherHeader.getHuffmanCodingBitMask() != getHuffmanCodingBitMask()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Builds a mask full of length-bit 1's.
	 *
	 * @param length The number of bits in the mask
	 * @return A constructed mask
	 */
	private int makeMask(int length) {
		int newMask = 1;
		for (int index = 1; index < length; index++) {
			newMask = (newMask << 1) | 1;
		}

		return newMask;
	}
}
