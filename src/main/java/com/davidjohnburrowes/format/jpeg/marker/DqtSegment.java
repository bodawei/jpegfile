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

import com.davidjohnburrowes.format.jpeg.component.DqtQuantizationTable;
import com.davidjohnburrowes.format.jpeg.data.TableSegment;
import com.davidjohnburrowes.format.jpeg.support.MarkerId;

/**
 * Define Quantization Table
 *
 * Defined on page B-11 to B-12 of the standard.
 */
@MarkerId(DqtSegment.MARKERID)
public class DqtSegment extends TableSegment<DqtQuantizationTable> {
	/**
	 * Standard marker for this type
	 */
	public static final int MARKERID = 0xDB;

	/**
	 * Standard constructor
	 */
	public DqtSegment() {
		super(MARKERID);
	}

	@Override
	protected DqtQuantizationTable createTable() {
		return new DqtQuantizationTable();
	}
}
