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

import com.davidjohnburrowes.format.jpeg.marker.TemMarker;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TemMarkerTest {

	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
	}

	@Test
	public void testThatHasTheRightMarkerIdByDefault() {
		assertEquals(TemMarker.MARKERID, new TemMarker().getMarkerId());
	}

	@Test
	public void testTemMarkerReadsNoData() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStream("AA BB");

		TemMarker marker = new TemMarker();
		marker.read(stream);

		assertEquals(0xAA, stream.read());
	}

	@Test
	public void temMarkersEqual() throws IOException {
		assertTrue(new TemMarker().equals(new TemMarker()));
	}

	@Test
	public void temMarkerNotEqualToOther() throws IOException {
		assertFalse(new TemMarker().equals(new Object()));
	}
}
