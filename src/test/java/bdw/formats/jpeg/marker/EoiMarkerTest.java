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

import bdw.format.jpeg.marker.EoiMarker;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.formats.jpeg.test.TestUtils;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class EoiMarkerTest {

	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
	}

	@Test
	public void testThatHasTheRightMarkerIdByDefault() {
		assertEquals(EoiMarker.MARKERID, new EoiMarker().getMarkerId());
	}

	@Test
	public void testEoiMarkerReadsNoData() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStream("AA BB");
		EoiMarker marker = new EoiMarker();
		marker.read(stream);

		assertEquals(0xAA, stream.read());
	}

	@Test
	public void eoiMarkersEqual() throws IOException {
		assertTrue(new EoiMarker().equals(new EoiMarker()));
	}

	@Test
	public void eoiMarkerNotEqualToOther() throws IOException {
		assertFalse(new EoiMarker().equals(new Object()));
	}
}
