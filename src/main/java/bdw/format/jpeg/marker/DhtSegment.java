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

import bdw.format.jpeg.component.DhtHuffmanTable;
import bdw.format.jpeg.data.TableSegment;
import bdw.format.jpeg.support.MarkerId;

/**
 * Define Huffman Table
 *
 * Defined on page B-12 to B-14 of the standard.
 */
@MarkerId(DhtSegment.MARKERID)
public class DhtSegment extends TableSegment<DhtHuffmanTable> {

	/**
	 * Standard marker for this type
	 */
	public static final int MARKERID = 0xC4;

	public DhtSegment() {
		super(MARKERID);
	}


	@Override
	protected DhtHuffmanTable createTable() {
		return new DhtHuffmanTable();
	}
}