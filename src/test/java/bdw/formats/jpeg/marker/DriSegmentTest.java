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
package bdw.formats.jpeg.marker;

import bdw.format.jpeg.marker.DriSegment;
import bdw.format.jpeg.support.FrameMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.DataMode;
import bdw.util.Size;
import bdw.formats.jpeg.test.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DriSegmentTest {

	private TestUtils utils;
	private DriSegment segment;

	@Before
	public void setUp() {
		utils = new TestUtils();
		segment = new DriSegment();
		segment.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
	}

	@Test
	public void newInstanceHasTheRightMarker() {
		assertEquals(DriSegment.MARKERID, segment.getMarkerId());
	}

	@Test
	public void setRestatInterval_HonorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "restartInterval", Size.SHORT, 0, 65535));
	}

	@Test
	public void readFromStream_canReadGoodData() throws IOException {
		InputStream stream = utils.makeInputStream("00 04 FF FF");

		segment.read(stream);

		assertEquals("Restart Interval", 0xFFFF, segment.getRestartInterval());
	}

	@Test(expected=InvalidJpegFormat.class)
	public void readFromStream_throwsOnBadInputSize() throws IOException {
		InputStream stream = utils.makeInputStream("00 06 FF FF");

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
		byte[] answer = utils.makeByteArray("FFDD 00 04 00 26");
		segment.setRestartInterval(38);

		segment.write(stream);

		assertArrayEquals(answer, stream.toByteArray());
	}

	@Test
	public void validate_withNewInstance_hasNoProblems() throws IOException {
		assertEquals(0, segment.validate().size());
	}

	@Test
	public void validate_whenSizeBad_returnsError() throws IOException {
		InputStream stream = utils.makeInputStream("00 06 00 00 01 02 03");
		segment.setDataMode(DataMode.LAX);

		segment.read(stream);

		assertEquals(1, segment.validate().size());
		assertEquals("segmentSize should be between 4 and 4. However, found: 6", segment.validate().get(0).getMessage());
	}

	@Test
	public void equals_newSegmentsEqual() throws IOException {
		assertTrue(segment.equals(new DriSegment()));
	}


	@Test
	public void equals_differentSegmentsNotEqual() throws IOException {
		DriSegment other = new DriSegment();

		other.setRestartInterval(5);

		assertFalse(segment.equals(other));
	}

	@Test
	public void equals_driSegmentNotEqualToOtherClass() throws IOException {
		assertFalse(segment.equals(new Object()));
	}
}