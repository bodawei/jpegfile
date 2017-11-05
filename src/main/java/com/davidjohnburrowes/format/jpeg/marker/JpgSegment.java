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
import com.davidjohnburrowes.format.jpeg.support.MarkerId;

/**
 * "Reserved for JPG extensions"
 *
 * Defined on page B-2 of the standard.
 */
@MarkerId(JpgSegment.MARKERID)
public class JpgSegment extends GenericSegment {

	/**
	 * Marker for this kind of segment
	 */
	public static final int MARKERID = 0xC8;

	/**
	 * Constructor! What a surprise!
	 */
	public JpgSegment() {
		super(MARKERID);
	}

	/**
	 * @param bytes the bytes that this segment is managing
	 */
	public void setBytes(byte[] bytes) {
		setByteArray(bytes);
	}

	public byte[] getBytes() {
		return getByteArray();
	}
}
