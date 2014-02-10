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
 * Application data
 *
 * Defined on page B-17 of the standard.
 */
@MarkerIdRange(first=AppNSegment.FIRST_MARKERID, last=AppNSegment.LAST_MARKERID)
public class AppNSegment extends GenericSegment {
	/**
	 * The first marker id
	 */
	public static final int FIRST_MARKERID = 0xE0;

	/**
	 * The last marker id
	 */
	public static final int LAST_MARKERID = 0xEF;

	/**
	 * Constructs an instance with all properties empty
	 */
	public AppNSegment(int markerId) {
		super(markerId);
		if (markerId < FIRST_MARKERID || markerId > LAST_MARKERID) {
			throw new IllegalArgumentException("MarkerId out of range. Found " + markerId);
		}
	}

	/**
	 * @inheritdoc
	 */
	public void setBytes(byte[] bytes) {
		setByteArray(bytes);
	}

	/**
	 * @inheritdoc
	 */
	public byte[] getBytes() {
		return getByteArray();
	}
}
