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

import com.davidjohnburrowes.format.jpeg.data.FrameSegment;
import com.davidjohnburrowes.format.jpeg.support.FrameMode;
import com.davidjohnburrowes.format.jpeg.support.MarkerIdSet;

/**
 * Start Of Frame Segment
 *
 * Defined on page B-6 of the standard.
 */
@MarkerIdSet({0xC0, 0xC1, 0xC2, 0xC3, 0xC5, 0xC6, 0xC7, 0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF})
public class SofSegment extends FrameSegment {

	/**
	 * Start of the first range of possible markers
	 */
	public static final int FIRST1_MARKERID = 0xc0;
	/**
	 * End of the first range of possible markers
	 */
	public static final int LAST1_MARKERID = 0xc3;
	/**
	 * Start of the second range of possible markers
	 */
	public static final int FIRST2_MARKERID = 0xc5;
	/**
	 * End of the second range of possible markers
	 */
	public static final int LAST2_MARKERID = 0xc7;
	/**
	 * Start of the third range of possible markers
	 */
	public static final int FIRST3_MARKERID = 0xc9;
	/**
	 * End of the third range of possible markers
	 */
	public static final int LAST3_MARKERID = 0xcb;
	/**
	 * Start of the fourth range of possible markers
	 */
	public static final int FIRST4_MARKERID = 0xcd;
	/**
	 * End of the fourth range of possible markers
	 */
	public static final int LAST4_MARKERID = 0xcf;

	/**
	 * Constructs an instance with all properties empty
    * @param markerId The Marker ID
	 */
	public SofSegment(int markerId) {
		super(markerId);

		if ((markerId < FIRST1_MARKERID || markerId > LAST1_MARKERID) &&
			(markerId < FIRST2_MARKERID || markerId > LAST2_MARKERID) &&
			(markerId < FIRST3_MARKERID || markerId > LAST3_MARKERID) &&
			(markerId < FIRST4_MARKERID || markerId > LAST4_MARKERID)) {
			throw new IllegalArgumentException("Marker id out of range. Found " + markerId);
		}

		setFrameMode(FrameMode.fromValue(markerId));
	}
}
