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

package bdw.format.jpeg.segment;

import bdw.format.jpeg.data.MultiSegment;
import bdw.format.jpeg.support.MarkerRange;

/**
 * "Restart" segment
 */
@MarkerRange(first=RstSegment.FIRST_MARKER, last=RstSegment.LAST_MARKER)
public class RstSegment extends MultiSegment {
	/**
	 * First of the conventional markers that are used with this
	 */
    public static final int FIRST_MARKER = 0xd0;

	/**
	 * Last of the markers that are used with this
	 */
    public static final int LAST_MARKER = 0xd7;

	
	public RstSegment(int marker) {
		super(marker);
	}

	/**
	 * All RstSegments are equal.
	 * @param other The other object to test
	 * @return If other and this are equal
	 */
	@Override
	public boolean equals(Object other) {
		return ((other == null) || !(other instanceof RstSegment)) ? false : true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		return hash;
	}
}
