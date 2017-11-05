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

package com.davidjohnburrowes.format.jpeg.marker;

import com.davidjohnburrowes.format.jpeg.data.GenericSegment;
import com.davidjohnburrowes.format.jpeg.support.MarkerIdRange;

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
    * @param markerId The MarkerID
	 */
	public AppNSegment(int markerId) {
		super(markerId);
		if (markerId < FIRST_MARKERID || markerId > LAST_MARKERID) {
			throw new IllegalArgumentException("MarkerId out of range. Found " + markerId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBytes(byte[] bytes) {
		setByteArray(bytes);
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getBytes() {
		return getByteArray();
	}
}
