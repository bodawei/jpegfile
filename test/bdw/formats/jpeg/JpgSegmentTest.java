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

import bdw.formats.jpeg.segments.JpgSegment;
import bdw.formats.jpeg.segments.support.InvalidJpegFormat;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class JpgSegmentTest {

	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
	}

	@Test
	public void testThatHasTheRightMarkerByDefault() {
		assertEquals(JpgSegment.MARKER, new JpgSegment().getMarker());
	}

	@Test
	public void testJpgSegmentReadsNoData() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("AA BB");

		JpgSegment segment = new JpgSegment();

		segment.readFromStream(stream);

		assertEquals(0xAA, stream.read());
	}

	@Test
	public void jpgSegmentsEqual() throws IOException {
		assertTrue(new JpgSegment().equals(new JpgSegment()));
	}

	@Test
	public void jpgSegmentNotEqualToOther() throws IOException {
		assertFalse(new JpgSegment().equals(new Object()));
	}
}
