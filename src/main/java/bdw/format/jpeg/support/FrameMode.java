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
package bdw.format.jpeg.support;

/**
 * An enumeration of the 13 different frame modes allowed in a JPEG file, as
 * well as convenience routines for identifying general categories of frame
 * modes.
 */
public enum FrameMode {
	HUFF_BASELINE_SEQUENTIAL_DCT(0xc0),
	HUFF_EXTENDED_SEQUENTIAL_DCT(0xc1),
	HUFF_PROGRESSIVE_DCT(0xc2),
	HUFF_LOSSLESS(0xc3),
	DIFF_HUFF_EXTENDED_SEQUENTIAL_DCT(0xc5),
	DIFF_HUFF_PROGRESSIVE_DCT(0xc6),
	DIFF_HUFF_SPATIAL(0xc7),
	AC_EXTENDED_SEQUENTIAL_DCT(0xc9),
	AC_PROGRESSIVE_DCT(0xca),
	AC_LOSSLESS(0xcb),
	DIFF_AC_EXTENDED_SEQUENTIAL_DCT(0xcd),
	DIFF_AC_PROGRESSIVE_DCT(0xce),
	DIFF_AC_SPATIAL(0xcf);

	private int value;

	/**
	 * Constructor
	 * @param code The fram mode value
	 */
	private FrameMode(int code) {
		value = code;
	}

	/**
	 * @return The integer value for this mode
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return true if this frame mode is a sequential baseline mode.
	 */
	public boolean isSequentialBaseline() {
		return this == HUFF_BASELINE_SEQUENTIAL_DCT;
	}

	/**
	 * @return true if this is an sequential extended frame mode (whether
	 * differential or not, huffman or ac encoded)
	 */
	public boolean isSequentialExtended() {
		return this == HUFF_EXTENDED_SEQUENTIAL_DCT ||
				  this == AC_EXTENDED_SEQUENTIAL_DCT ||
				  this == DIFF_HUFF_EXTENDED_SEQUENTIAL_DCT ||
				  this == DIFF_AC_EXTENDED_SEQUENTIAL_DCT;
	}

	/**
	 * @return true if this is an progressive frame mode (whether
	 * differential or not, huffman or ac encoded)
	 */
	public boolean isProgressive() {
		return this == HUFF_PROGRESSIVE_DCT ||
				  this == AC_PROGRESSIVE_DCT ||
				  this == DIFF_HUFF_PROGRESSIVE_DCT ||
				  this == DIFF_AC_PROGRESSIVE_DCT;
	}

	/**
	 * @return true if this is a lossless frame mode
	 */
	public boolean isLossless() {
		return this == HUFF_LOSSLESS ||
				  this == AC_LOSSLESS ||
				  this == DIFF_HUFF_SPATIAL ||
				  this == DIFF_AC_SPATIAL;
	}

	/**
	 * @param value The value to be converted into a FrameMode enum value
	 * @return The enum value corresponding to the provided value. Will return
	 * null if the provided value doesn't match any.
	 */
	public static FrameMode fromValue(int value) {
		if (value == HUFF_BASELINE_SEQUENTIAL_DCT.getValue()) {
			return HUFF_BASELINE_SEQUENTIAL_DCT;
		} else if (value == HUFF_EXTENDED_SEQUENTIAL_DCT.getValue()) {
			return HUFF_EXTENDED_SEQUENTIAL_DCT;
		} else if (value == HUFF_PROGRESSIVE_DCT.getValue()) {
			return HUFF_PROGRESSIVE_DCT;
		} else if (value == HUFF_LOSSLESS.getValue()) {
			return HUFF_LOSSLESS;
		} else if (value == AC_EXTENDED_SEQUENTIAL_DCT.getValue()) {
			return AC_EXTENDED_SEQUENTIAL_DCT;
		} else if (value == AC_PROGRESSIVE_DCT.getValue()) {
			return AC_PROGRESSIVE_DCT;
		} else if (value == AC_LOSSLESS.getValue()) {
			return AC_LOSSLESS;
		} else if (value == DIFF_HUFF_EXTENDED_SEQUENTIAL_DCT.getValue()) {
			return DIFF_HUFF_EXTENDED_SEQUENTIAL_DCT;
		} else if (value == DIFF_HUFF_PROGRESSIVE_DCT.getValue()) {
			return DIFF_HUFF_PROGRESSIVE_DCT;
		} else if (value == DIFF_HUFF_SPATIAL.getValue()) {
			return DIFF_HUFF_SPATIAL;
		} else if (value == DIFF_AC_EXTENDED_SEQUENTIAL_DCT.getValue()) {
			return DIFF_AC_EXTENDED_SEQUENTIAL_DCT;
		} else if (value == DIFF_AC_PROGRESSIVE_DCT.getValue()) {
			return DIFF_AC_PROGRESSIVE_DCT;
		} else if (value == DIFF_AC_SPATIAL.getValue()) {
			return DIFF_AC_SPATIAL;
		}
		return null;
	}

}
