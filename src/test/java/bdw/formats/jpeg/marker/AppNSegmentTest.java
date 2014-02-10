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

import bdw.format.jpeg.data.GenericSegment;
import bdw.format.jpeg.marker.AppNSegment;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.formats.jpeg.test.TestUtils;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class AppNSegmentTest {

	protected TestUtils utils;
	protected AppNSegment segment;

	@Before
	public void setUp() throws InvalidJpegFormat {
		segment = new AppNSegment(AppNSegment.FIRST_MARKERID);
		utils = new TestUtils();
	}

	@Test
	public void creationSetsMarker() {
		assertEquals("marker", AppNSegment.FIRST_MARKERID, segment.getMarkerId());
	}

	@Test
	public void getBytes_returnsTheBytes() throws IOException {
		segment.read(utils.makeInputStream("00 0C"
				  + "01 02 03 04 05 06 07 08 09 0a"));

		assertArrayEquals("bytes", utils.makeByteArray("01 02 03 04 05 06 07 08 09 0a"),
				  segment.getBytes());
	}

	@Test
	public void setBytes_changesTheButes() throws IOException {
		segment.read(utils.makeInputStream("00 0C"
				  + "01 02 03 04 05 06 07 08 09 0a"));

		segment.setBytes(new byte[] { 10, 11, 12 });

		assertArrayEquals("bytes", utils.makeByteArray("0a 0b 0c"),
				  segment.getBytes());
	}

	@Test
	public void twoEqualSegmentsEqual() throws IOException {
		segment.read(utils.makeInputStream("00 0C"
				  + "01 02 03 04 05 06 07 08 09 0a"));

		AppNSegment other = new AppNSegment(AppNSegment.FIRST_MARKERID);
		other.setBytes(utils.makeByteArray("01 02 03 04 05 06 07 08 09 0a"));
		assertEquals("segment", other, segment);
	}

	@Test
	public void unequalSegmentsAreNotEqual() throws IOException {
		segment.read(utils.makeInputStream("00 0C"
				  + "01 02 03 04 05 06 07 08 09 0a"));

		AppNSegment other = new AppNSegment(AppNSegment.FIRST_MARKERID);
		other.setBytes(utils.makeByteArray("FF FE FD 04 05 06 07 08 09 0a"));

		assertFalse("segment", other.equals(segment));
	}

	@Test
	public void equals_withGenericSegment_notEqual() throws IOException {

		GenericSegment other = new GenericSegment(AppNSegment.FIRST_MARKERID);
		assertFalse(segment.equals(other));
	}
}