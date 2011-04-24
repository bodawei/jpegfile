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

import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.TestUtils;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bodawei
 */
public class DriSegmentTest {

	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
	}


	@Test
	public void newInstanceHasTheRightMarker() {
		assertEquals(DriSegment.MARKER, new DriSegment().getMarker());
	}

	@Test
	public void setRestartInterval8IsOK() {
		DriSegment segment = new DriSegment();

		segment.setRestartInterval(8);

		assertEquals(8, segment.getRestartInterval());
	}

	@Test
	public void setRestartInterval65535IsOK() {
		DriSegment segment = new DriSegment();

		segment.setRestartInterval(65535);

		assertEquals(65535, segment.getRestartInterval());
	}


	@Test
	public void setRestartIntervalNegOneIsNotOK() {
		DriSegment segment = new DriSegment();

		try {
			segment.setRestartInterval(-1);
			fail("should have thrown an exception");
		} catch (Exception e) {
			assertTrue (e instanceof IllegalArgumentException);
		}
	}


	@Test
	public void canReadGoodData() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("00 04 FF FF");

		DriSegment segment = new DriSegment(stream);


		assertEquals("Restart Interval", 0xFFFF, segment.getRestartInterval());
	}

	@Test
	public void throwsOnBadInput() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("00 06 FF FF");

		DriSegment segment;
		try {
			segment = new DriSegment(stream);
			fail("Reading should have failed");
		} catch (Exception e) {
			assertTrue(e instanceof InvalidJpegFormat);
		}
	}


	@Test
	public void writeDriSegment() throws IOException {
		DriSegment segment = new DriSegment();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		byte[] answer = utils.makeByteArrayFromString("00 04 00 26");

		segment.setRestartInterval(38);

		segment.write(stream);

		assertArrayEquals("written output", answer, stream.toByteArray());
	}


	@Test
	public void driSegmentsEqual() throws IOException {
		assertTrue(new DriSegment().equals(new DriSegment()));
	}


	@Test
	public void driSegmentsNotEqual() throws IOException {
		DriSegment other = new DriSegment();

		other.setRestartInterval(5);

		assertFalse(new DriSegment().equals(other));
	}

	@Test
	public void driSegmentNotEqualToOtherClass() throws IOException {
		assertFalse(new DriSegment().equals(new Object()));
	}

}