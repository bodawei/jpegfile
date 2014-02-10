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

import bdw.format.jpeg.marker.RstMMarker;
import bdw.formats.jpeg.test.TestUtils;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class RstMMarkerTest {

	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
	}

	@Test
	public void testThatHasTheRightMarkerByDefault() {
		assertEquals(RstMMarker.FIRST_MARKERID, new RstMMarker(RstMMarker.FIRST_MARKERID).getMarkerId());
	}

	@Test
	public void testRstMMarkerReadsNoData() throws IOException {
		InputStream stream = utils.makeInputStream("AA BB");

		RstMMarker marker = new RstMMarker(RstMMarker.FIRST_MARKERID);
		marker.read(stream);

		assertEquals(0xAA, stream.read());
	}

	@Test
	public void rstMMarkersWithSameMarkersIdsEqual() throws IOException {
		assertTrue(new RstMMarker(RstMMarker.FIRST_MARKERID).equals(new RstMMarker(RstMMarker.FIRST_MARKERID)));
	}

	@Test
	public void rstMMarkersWithDifferentMarkerIdsNotEqual() throws IOException {
		assertFalse(new RstMMarker(RstMMarker.FIRST_MARKERID + 2).equals(new RstMMarker(RstMMarker.FIRST_MARKERID)));
	}
	@Test
	public void rstMMarkerNotEqualToOther() throws IOException {
		assertFalse(new RstMMarker(RstMMarker.FIRST_MARKERID).equals(new Object()));
	}

    @Test(expected=IllegalArgumentException.class)
    public void markerWillOnlyAcceptLegalMarkerIds() throws IOException {
		RstMMarker marker = new RstMMarker(0x01);
	}
}
