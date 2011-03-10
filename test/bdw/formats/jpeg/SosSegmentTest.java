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

import bdw.formats.encode.Hex2Bin;
import bdw.formats.jpeg.segments.ScanDescriptorEntry;
import bdw.formats.jpeg.segments.SosSegment;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SosSegmentTest {

	private String SOSSEGMENT_1 = "000c # size\n"
			+ "03					# component count\n"
			+ "00 01 02 03 04 05	# components\n"
			+ "01 02 03				# spectral start, end etc\n";
	private String SOSSEGMENT_2 = "000c # size\n"
			+ "03					# component count\n"
			+ "FF 01 02 03 04 05	# components\n"
			+ "01 02 03				# spectral start, end etc\n";
	private String SOSSEGMENT_BAD_COMPONENT_COUNT = "000e # size\n"
			+ "03					# component count\n"
			+ "FF 01 02 03 04 05	# components\n"
			+ "01 02 03				# spectral start, end etc\n";


	public SosSegmentTest() {
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
	public void testMarkerIsRight() throws IOException {
		SosSegment segment = new SosSegment();
		Assert.assertEquals("Marker ID", SosSegment.MARKER, segment.getMarker());
	}


	@Test
	public void testReadSegmentFromStream() throws IOException {
		int count = 0;
		SosSegment segment = new SosSegment();
		segment.readFromStream(prepareInputStream(SOSSEGMENT_1));

		Assert.assertEquals("Spectral Selection Start", 1, segment.getSpectralSelectionStart());
		Assert.assertEquals("Spectral Selection End", 2, segment.getSpectralSelectionEnd());
		Assert.assertEquals("Successive Approximation", 3, segment.getSuccessiveApproximation());

		Iterator<ScanDescriptorEntry> i = segment.iterator();
		while (i.hasNext()) {
			count ++;
			i.next();
		}
		Assert.assertEquals("ScanDescriptor count", 3, count);
	}

	@Test
	public void testReadInvalidComponentCountErrors() throws IOException {
		int count = 0;
		SosSegment segment = new SosSegment();
		try {
			segment.readFromStream(prepareInputStream(SOSSEGMENT_BAD_COMPONENT_COUNT));
			Assert.fail("Should have failed to read the segment");
		} catch (Exception e) {

		}
	}

	@Test
	public void testEquals() throws IOException {
		SosSegment one = new SosSegment();
		SosSegment two = new SosSegment();
		one.readFromStream(prepareInputStream(SOSSEGMENT_1));
		two.readFromStream(prepareInputStream(SOSSEGMENT_1));

		Assert.assertTrue("one equals two", one.equals(two));
		Assert.assertTrue("two equals one", two.equals(one));
	}

	@Test
	public void testEqualsFailsForUnequalSegments() throws IOException {
		SosSegment one = new SosSegment();
		SosSegment two = new SosSegment();
		one.readFromStream(prepareInputStream(SOSSEGMENT_1));
		two.readFromStream(prepareInputStream(SOSSEGMENT_2));

		Assert.assertFalse("one doesn't equal two", one.equals(two));
		Assert.assertFalse("two doesn't equal one", two.equals(one));
	}

	@Test
	public void testWrite() throws IOException {
		SosSegment one = new SosSegment();
		one.readFromStream(prepareInputStream(SOSSEGMENT_1));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		one.write(out);

		Assert.assertArrayEquals(prepareInputBytes(SOSSEGMENT_1), out.toByteArray());
	}


	protected byte[] prepareInputBytes(String rawInput) throws IOException {
		StringReader inputReader = new StringReader(rawInput);
		byte[] rawInputBytes = new byte[rawInput.length()];
		int aChar = inputReader.read();
		int index = 0;
		while (aChar != -1) {
			rawInputBytes[index] = (byte) aChar;
			aChar = inputReader.read();
			index++;
		}

		Hex2Bin encoder = new Hex2Bin();
		InputStream inputStream = new ByteArrayInputStream(rawInputBytes);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		encoder.convert(inputStream, outputStream);

		return outputStream.toByteArray();
	}

	protected InputStream prepareInputStream(String rawInput) throws IOException {

		return new ByteArrayInputStream(prepareInputBytes(rawInput));
	}
}