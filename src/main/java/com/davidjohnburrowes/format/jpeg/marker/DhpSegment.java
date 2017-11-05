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
import com.davidjohnburrowes.format.jpeg.support.MarkerId;

/**
 * Define Hierarchical Progression
 *
 * Defined on page B-19 to B-20 of the standard.
 */
@MarkerId(DhpSegment.MARKERID)
public class DhpSegment extends FrameSegment {
	/**
	 * Marker for this segment type
	 */
	public static final int MARKERID = 0xDE;

	/**
	 * Constructs an instance with all properties empty
	 */
	public DhpSegment() {
		super(MARKERID);
	}
}
