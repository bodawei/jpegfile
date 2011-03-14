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
package bdw.formats.jpeg.segments;

/**
 * JpgN segment type
 */
public class JpgNSegment extends SegmentBase {
	/**
	 * The first of the allowable markers that this can be used with
	 */
	public static final int START_MARKER = 0xF0;

	/**
	 * Last of the allowable markers this can be used with
	 */
	public static final int END_MARKER = 0xFD;

	/**
	 * Constructor
	 */
    public JpgNSegment() {
		setMarker(JpgNSegment.START_MARKER);
    }

	/**
	 * All JpgSegments are equal.
	 * @param other The other object to test
	 * @return If other and this are equal
	 */
	@Override
	public boolean equals(Object other) {
		return ((other == null) || !(other instanceof JpgNSegment)) ? false : true;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public int hashCode() {
		int hash = 3;
		return hash;
	}
}