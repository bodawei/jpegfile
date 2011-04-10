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
import bdw.formats.jpeg.segments.DhpSegment;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DhpSegmentTest {

	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
	}

	@Test
	public void testThatHasTheRightMarkerByDefault() {
		assertEquals(DhpSegment.MARKER, new DhpSegment().getMarker());
	}

	@Test
	public void testDhpSegmentReadsNoData() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("AA BB");

		DhpSegment segment = new DhpSegment();

		segment.readFromStream(stream);

		assertEquals(0xAA, stream.read());
	}

	@Test
	public void DhpSegmentsEqual() throws IOException {
		assertTrue(new DhpSegment().equals(new DhpSegment()));
	}

	@Test
	public void DhpSegmentNotEqualToOther() throws IOException {
		assertFalse(new DhpSegment().equals(new Object()));
	}
}
