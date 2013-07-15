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

import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.ParseMode;
import bdw.formats.jpeg.TestUtils;
import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RstSegmentTest {

	private TestUtils utils;

	public RstSegmentTest() {
	}

	@Before
	public void setUp() {
		utils = new TestUtils();
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testThatHasTheRightMarkerByDefault() throws InvalidJpegFormat {
		assertEquals(RstSegment.FIRST_SUBTYPE, new RstSegment(RstSegment.FIRST_SUBTYPE).getMarker());
	}

	@Test
	public void testSoiSegmentReadsNoData() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("AA BB");

		RstSegment segment = new RstSegment(RstSegment.FIRST_SUBTYPE, stream, ParseMode.STRICT);
		
		assertEquals(0xAA, stream.read());
	}

	@Test
	public void rstSegmentsEqual() throws IOException, InvalidJpegFormat {
		assertTrue(new RstSegment(RstSegment.FIRST_SUBTYPE).equals(new RstSegment(RstSegment.FIRST_SUBTYPE)));
	}

	@Test
	public void rstSegmentNotEqualToOther() throws IOException, InvalidJpegFormat {
		assertFalse(new RstSegment(RstSegment.FIRST_SUBTYPE).equals(new Object()));
	}
}
