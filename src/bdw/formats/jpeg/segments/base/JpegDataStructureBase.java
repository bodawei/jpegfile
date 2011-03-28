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

package bdw.formats.jpeg.segments.base;

/**
 * Serves as a superclass for Segments and entries providing parameter checking
 * routines
 */
public class JpegDataStructureBase {

	/**
	 * Validates that the parameter is in in the range of an unsigned
	 * four bit integer.  Throws an exception if it isn't.
	 *
	 * @param value The integer to check
	 * @throws IllegalArgumentException If the parameter isn't in range
	 */
	public void paramIsUInt4(int value) throws IllegalArgumentException {
		if ((value < 0) || (value >= 16)) {
			throw new IllegalArgumentException("Parameter not in range of a 4 bit integer");
		}
	}

	/**
	 * Validates that the parameter is in in the range of an unsigned
	 * eight bit integer.  Throws an exception if it isn't.
	 *
	 * @param value The integer to check
	 * @throws IllegalArgumentException If the parameter isn't in range
	 */
	public void paramIsUInt8(int value) throws IllegalArgumentException {
		if ((value < 0) || (value >= 256)) {
			throw new IllegalArgumentException("Parameter not in range of an 8 bit integer");
		}
	}

	/**
	 * Validates that the parameter is in in the range of an unsigned
	 * 16 bit integer.  Throws an exception if it isn't.
	 *
	 * @param value The integer to check
	 * @throws IllegalArgumentException If the parameter isn't in range
	 */
	public void paramIsUInt16(int value) throws IllegalArgumentException {
		if ((value < 0) || (value >= 65536)) {
			throw new IllegalArgumentException("Parameter not in range of a 16 bit integer");
		}
	}

}
