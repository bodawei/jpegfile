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

package bdw.formats.jpeg.support;

import bdw.format.jpeg.segment.support.DhtRunLengthHeader;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class DhtRunLengthHeaderTest {

	@Before
    public void setUp() {
    }

	@Test
	public void constructor_BuildsMask() {
		DhtRunLengthHeader header = new DhtRunLengthHeader(0x11, 5, 5, 5);

		assertEquals("mask", 0x1f, header.getHuffmanCodingBitMask());
	}

	@Test
	public void getFolowingDataBitCount_ReturnsConstructedValue() {
		DhtRunLengthHeader header = new DhtRunLengthHeader(0x11, 5, 6, 7);

		assertEquals("following bit count", 7, header.getFolowingDataBitCount());
	}

	@Test
	public void getInitialZeroBitCount_ReturnsConstructedValue() {
		DhtRunLengthHeader header = new DhtRunLengthHeader(0x11, 5, 6, 7);

		assertEquals("zero bit count", 6, header.getInitialZeroBitCount());
	}

	@Test
	public void getHuffmanCoding_ReturnsConstructedValue() {
		DhtRunLengthHeader header = new DhtRunLengthHeader(0x11, 5, 6, 7);

		assertEquals("huffmann coding", 0x11, header.getHuffmanCoding());
	}
}