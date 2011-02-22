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

import java.net.URI;
import java.io.File;
import bdw.formats.jpeg.segments.EoiSegment;
import bdw.formats.jpeg.segments.SoiSegment;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bodawei
 */
public class JpegFileTest {

    public JpegFileTest() {
    }

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testReadingTrivialFile() throws Exception {
		JpegFile file = new JpegFile();
		file.addStandardSegments();

		URI uri = this.getClass().getResource("trivial.jpg").toURI();

		file.readFromFile(new File(uri));

		assertEquals(2, file.getSegments().size());
		assertEquals("Start of Image", SoiSegment.MARKER, file.getSegments().get(0).getMarker());
		assertEquals("End of Image", EoiSegment.MARKER, file.getSegments().get(1).getMarker());
	}

    @Test
    public void testReadingSampleFile() throws Exception {
		JpegFile file = new JpegFile();
		file.addStandardSegments();

		URI uri = this.getClass().getResource("knuth.jpg").toURI();

		file.readFromFile(new File(uri));

		assertEquals(8, file.getSegments().size());
		assertEquals("Start of Image", SoiSegment.MARKER, file.getSegments().get(0).getMarker());
		assertEquals("End of Image", EoiSegment.MARKER, file.getSegments().get(7).getMarker());
	}

}