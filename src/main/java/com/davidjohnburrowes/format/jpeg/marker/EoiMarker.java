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

import com.davidjohnburrowes.format.jpeg.data.Marker;
import com.davidjohnburrowes.format.jpeg.support.MarkerId;

/**
 * The End Of Image marker
 * This contains no content
 */
@MarkerId(EoiMarker.MARKERID)
public class EoiMarker extends Marker {
	/**
	 * The end of image marker
	 */
	public static final int MARKERID = 0xD9;

	/**
	 */
	public EoiMarker() {
		super(MARKERID);
	}
}
