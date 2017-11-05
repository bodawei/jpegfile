/*
 *  Copyright 2014,2017 柏大衛
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
package com.davidjohnburrowes.util;

/**
 * Utility routines for extracting nibbles from bytes, and bytes from nibbles.
 */
public class Nibble {

	/**
	 * Extract the second nibble from the lowest byte of the provided integer
	 * @param aByte An integer (bytes above the first are ignored)
	 * @return The value of the high nibble from the byte
	 */
	public static byte getUpper(int aByte) {
		return (byte) ((aByte & 0xF0) >> 4);
	}

	/**
	 * Extract the first nibble from the lowest byte of the provided integer
	 * @param aByte An integer (bytes above the first are ignored)
	 * @return The value of the low nibble from the byte
	 */
	public static byte getLower(int aByte) {
		return (byte) (aByte & 0x0F);
	}

	/**
	 * Given two nibbles, assemble them into a byte.
	 * @param highNibble An integer (bits above the first 4 are ignored)
	 * @param lowNibble  An integer (bits above the first 4 are ignored)
	 * @return a byte assembled from the two nibbles
	 */
	public static byte makeByte(int highNibble, int lowNibble) {
		highNibble = highNibble & 0x0F;
		lowNibble = lowNibble & 0x0F;

		return (byte)(highNibble << 4 | lowNibble);
	}
}
