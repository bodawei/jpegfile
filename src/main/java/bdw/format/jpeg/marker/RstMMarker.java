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

import bdw.format.jpeg.data.Marker;
import bdw.format.jpeg.support.MarkerIdRange;

/**
 * "Restart with modulo 8 count 'm'" marker
 *
 * This is defined on page B-2 of the standard. More details on B-5
 */
@MarkerIdRange(first=RstMMarker.FIRST_MARKERID, last=RstMMarker.LAST_MARKERID)
public class RstMMarker extends Marker {
	/**
	 * First of the markers that are used with this
	 */
   public static final int FIRST_MARKERID = 0xd0;

	/**
	 * Last of the markers that are used with this
	 */
   public static final int LAST_MARKERID = 0xd7;

	/**
	 * Constructor
    * @param markerId The Marker ID
	 */
	public RstMMarker(int markerId) {
		super(markerId);

		if (markerId < FIRST_MARKERID || markerId > LAST_MARKERID) {
			throw new IllegalArgumentException("Marker out of range. Found " + markerId);
		}
	}
}
