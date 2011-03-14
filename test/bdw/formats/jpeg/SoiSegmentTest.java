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
import java.io.IOException;
import java.io.InputStream;
import bdw.formats.jpeg.segments.SoiSegment;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class SoiSegmentTest {

	private TestUtils utils;

	public SoiSegmentTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
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
		assertEquals(SoiSegment.MARKER, new SoiSegment().getMarker());
	}

	@Test
	public void testNewInstanceIsValid() {
		assertEquals(true, new SoiSegment().isValid());
	}

	@Test
	public void testSoiSegmentReadsNoData() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("AA BB");

		SoiSegment segment = new SoiSegment();

		segment.readFromStream(stream);

		assertEquals(0xAA, stream.read());
	}

	@Test
	public void soiSegmentsEqual() throws IOException {
		assertTrue(new SoiSegment().equals(new SoiSegment()));
	}

	@Test
	public void soiSegmentNotEqualToOther() throws IOException {
		assertFalse(new SoiSegment().equals(new Object()));
	}
}
