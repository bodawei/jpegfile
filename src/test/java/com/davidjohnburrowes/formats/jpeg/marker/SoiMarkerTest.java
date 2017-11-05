/*
 *  Copyright 2014,2017 柏大衛
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

package com.davidjohnburrowes.formats.jpeg.marker;

import com.davidjohnburrowes.format.jpeg.marker.SoiMarker;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class SoiMarkerTest {

	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
	}

	@Test
	public void testThatHasTheRightMarkerIdByDefault() {
		assertEquals(SoiMarker.MARKERID, new SoiMarker().getMarkerId());
	}

	@Test
	public void testSoiMarkerReadsNoData() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStream("AA BB");

		SoiMarker marker = new SoiMarker();
		marker.read(stream);

		assertEquals(0xAA, stream.read());
	}

	@Test
	public void soiSegmEqual() throws IOException {
		assertTrue(new SoiMarker().equals(new SoiMarker()));
	}

	@Test
	public void soiMarkerNotEqualToOther() throws IOException {
		assertFalse(new SoiMarker().equals(new Object()));
	}
}
