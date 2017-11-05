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

package com.davidjohnburrowes.formats.jpeg.marker;

import com.davidjohnburrowes.format.jpeg.marker.DnlSegment;
import com.davidjohnburrowes.format.jpeg.support.FrameMode;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.format.jpeg.support.DataMode;
import com.davidjohnburrowes.util.Size;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DnlSegmentTest {

	private TestUtils utils;
	private DnlSegment segment;

	@Before
	public void setUp() {
		utils = new TestUtils();
		segment = new DnlSegment();
		segment.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
	}

	@Test
	public void newInstanceHasTheRightMarker() {
		assertEquals(DnlSegment.MARKERID, segment.getMarkerId());
	}

	@Test
	public void setNumberOfLines_HonorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "numberOfLines", Size.SHORT, 1, 65535));
	}

	@Test
	public void readFromStream_canReadGoodData() throws IOException {
		InputStream stream = utils.makeInputStream("00 04 FF FF");

		segment.read(stream);

		assertEquals(0xFFFF, segment.getNumberOfLines());
	}

	@Test(expected=InvalidJpegFormat.class)
	public void readFromStream_throwsOnBadInputSize() throws IOException {
		InputStream stream = utils.makeInputStream("00 06 FF FF");

		segment.read(stream);
	}

	@Test(expected=EOFException.class)
	public void readFromStream_WithBadSizeInLaxModeAndNotEnoughBytes_throwsEOF() throws IOException {
		InputStream stream = utils.makeInputStream("00 06 FF FF");
		segment.setDataMode(DataMode.LAX);

		segment.read(stream);
	}

	@Test
	public void readFromStream_WithBadSizeInLaxModeWithEnoughBytes_ReadsBadData() throws IOException {
		InputStream stream = utils.makeInputStream("00 06 FF FF 01 02 03");
		segment.setDataMode(DataMode.LAX);

		segment.read(stream);

		assertEquals(3, stream.read());
	}

	@Test
	public void write_writesSetData() throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		byte[] answer = utils.makeByteArray("FFDC 00 04 00 26");
		segment.setNumberOfLines(38);

		segment.write(stream);

		assertArrayEquals(answer, stream.toByteArray());
	}

	@Test
	public void validate_returnsNothingWhenAllGood() throws IOException {
		assertEquals(0, segment.validate().size());
	}

	@Test
	public void validate_whenNumberOfLinesBad_returnsError() throws IOException {
		segment.setDataMode(DataMode.LAX);
		segment.setNumberOfLines(0);

		assertEquals(1, segment.validate().size());
		assertEquals("numberOfLines should be between 1 and 65535. However, found: 0", segment.validate().get(0).getMessage());
	}

	@Test
	public void validate_whenSizeBad_returnsError() throws IOException {
		InputStream stream = utils.makeInputStream("00 06 00 00 01 02 03");
		segment.setDataMode(DataMode.LAX);

		segment.read(stream);

		assertEquals(2, segment.validate().size());
		assertEquals("segmentSize should be between 4 and 4. However, found: 6", segment.validate().get(0).getMessage());
	}

	@Test
	public void equals_newSegmentsEqual() throws IOException {
		assertTrue(new DnlSegment().equals(segment));
	}


	@Test
	public void equals_differentSegmentsNotEqual() throws IOException {
		DnlSegment other = new DnlSegment();

		other.setNumberOfLines(5);

		assertFalse(segment.equals(other));
	}

	@Test
	public void equals_driSegmentNotEqualToOtherClass() throws IOException {
		assertFalse(segment.equals(new Object()));
	}
}
