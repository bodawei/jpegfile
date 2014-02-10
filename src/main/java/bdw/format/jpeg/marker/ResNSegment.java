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
package bdw.format.jpeg.marker;

import bdw.format.jpeg.data.GenericSegment;
import bdw.format.jpeg.support.MarkerIdRange;

/**
 * "Reserved"
 *
 * Defined on page B-3 of the standard.
 */
@MarkerIdRange(first=ResNSegment.FIRST_MARKERID, last=ResNSegment.LAST_MARKERID)
public class ResNSegment extends GenericSegment {
	/**
	 * The first of the allowable markers that this can be used with
	 */
	public static final int FIRST_MARKERID = 0x02;

	/**
	 * Last of the allowable markers this can be used with
	 */
	public static final int LAST_MARKERID = 0xBF;

	/**
	 * Constructs an instance with all properties empty
	 */
	public ResNSegment(int marker) {
		super(marker);
		if (marker < FIRST_MARKERID || marker > LAST_MARKERID) {
			throw new IllegalArgumentException("Marker out of range. Found " + marker);
		}
	}

	/**
	 * @param bytes The set of bytes this segment is managing
	 */
	public void setBytes(byte[] bytes) {
		setByteArray(bytes);
	}

	public byte[] getBytes() {
		return getByteArray();
	}
}