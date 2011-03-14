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

package bdw.formats.jpeg;

import bdw.formats.jpeg.segments.support.InvalidJpegFormat;
import bdw.formats.jpeg.segments.EoiSegment;
import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EoiSegmentTest {

	private TestUtils utils;

	public EoiSegmentTest() {
	}

	@Before
	public void setUp() {
		utils = new TestUtils();
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testThatHasTheRightMarkerByDefault() {
		assertEquals(EoiSegment.MARKER, new EoiSegment().getMarker());
	}

	@Test
	public void testNewInstanceIsValid() {
		assertEquals(true, new EoiSegment().isValid());
	}

	@Test
	public void testSoiSegmentReadsNoData() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("AA BB");

		EoiSegment segment = new EoiSegment();

		segment.readFromStream(stream);

		assertEquals(0xAA, stream.read());
	}

	@Test
	public void eoiSegmentsEqual() throws IOException {
		assertTrue(new EoiSegment().equals(new EoiSegment()));
	}

	@Test
	public void eoiSegmentNotEqualToOther() throws IOException {
		assertFalse(new EoiSegment().equals(new Object()));
	}
}
