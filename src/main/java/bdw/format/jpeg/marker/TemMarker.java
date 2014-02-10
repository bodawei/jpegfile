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
import bdw.format.jpeg.support.MarkerId;

/**
 * "For temporary private use in arithmetic coding" marker.
 * This isn't supposed to appear in final JPEG streams (it is put in them during
 * processing, and removed during a post-processing phase).
 *
 * Defined on page B-3 of the standard.
 */
@MarkerId(TemMarker.MARKERID)
public class TemMarker extends Marker {
	/**
	 * MarkerId for this type of marker
	 */
	public static final int MARKERID = 0x01;

	/**
	 */
	public TemMarker() {
		super(MARKERID);
	}
}
