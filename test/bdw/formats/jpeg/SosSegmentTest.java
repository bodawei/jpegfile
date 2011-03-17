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
import bdw.formats.jpeg.segments.support.InvalidJpegFormat;
import bdw.formats.jpeg.segments.support.SosDescriptor;
import bdw.formats.jpeg.segments.SosSegment;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import org.junit.Assert;
import org.junit.Before;
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

	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
	}

	@Test
	public void testMarkerIsRight() throws IOException {
		SosSegment segment = new SosSegment();
		Assert.assertEquals("Marker ID", SosSegment.MARKER, segment.getMarker());
	}

	@Test
	public void testSetSpectralSelectionStartZeroIsOK() {
		SosSegment segment = new SosSegment();

		try {
			segment.setSpectralSelectionStart(0);
		} catch (Exception e) {
			Assert.fail("Got an exception when we shouldn't have");
		}

		Assert.assertEquals(0, segment.getSpectralSelectionStart());
	}

	@Test
	public void testSetSpectralSelectionStartNegOneIsNotOK() {
		SosSegment segment = new SosSegment();

		try {
			segment.setSpectralSelectionStart(-1);
			Assert.fail("Should have failed");
		} catch (Exception e) {
			Assert.assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testSetSpectralSelectionStart256IsNotOK() {
		SosSegment segment = new SosSegment();

		try {
			segment.setSpectralSelectionStart(256);
			Assert.fail("Should have failed");
		} catch (Exception e) {
			Assert.assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testSetSpectralSelectionEndZeroIsOK() {
		SosSegment segment = new SosSegment();

		try {
			segment.setSpectralSelectionEnd(0);
		} catch (Exception e) {
			Assert.fail("Got an exception when we shouldn't have");
		}

		Assert.assertEquals(0, segment.getSpectralSelectionStart());
	}

	@Test
	public void testSetSpectralSelectionEndNegOneIsNotOK() {
		SosSegment segment = new SosSegment();

		try {
			segment.setSpectralSelectionEnd(-1);
			Assert.fail("Should have failed");
		} catch (Exception e) {
			Assert.assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testSetSpectralSelectionEnd256IsNotOK() {
		SosSegment segment = new SosSegment();

		try {
			segment.setSpectralSelectionEnd(256);
			Assert.fail("Should have failed");
		} catch (Exception e) {
			Assert.assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testSetSuccessiveApproximationZeroIsOK() {
		SosSegment segment = new SosSegment();

		try {
			segment.setSuccessiveApproximation(0);
		} catch (Exception e) {
			Assert.fail("Got an exception when we shouldn't have");
		}

		Assert.assertEquals(0, segment.getSpectralSelectionStart());
	}

	@Test
	public void testSetSuccessiveApproximationNegOneIsNotOK() {
		SosSegment segment = new SosSegment();

		try {
			segment.setSuccessiveApproximation(-1);
			Assert.fail("Should have failed");
		} catch (Exception e) {
			Assert.assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testSetSuccessiveApproximationEnd256IsNotOK() {
		SosSegment segment = new SosSegment();

		try {
			segment.setSuccessiveApproximation(256);
			Assert.fail("Should have failed");
		} catch (Exception e) {
			Assert.assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testReadSegmentFromStream() throws IOException, InvalidJpegFormat {
		SosSegment segment = new SosSegment();
		segment.readFromStream(utils.makeInputStreamFromString(SOSSEGMENT_1));

		Assert.assertEquals("Spectral Selection Start", 1, segment.getSpectralSelectionStart());
		Assert.assertEquals("Spectral Selection End", 2, segment.getSpectralSelectionEnd());
		Assert.assertEquals("Successive Approximation", 3, segment.getSuccessiveApproximation());

		Assert.assertEquals("ScanDescriptor count", 3, segment.getDescriptorCount());
	}

	@Test
	public void testReadInvalidComponentCountErrors() throws IOException {
		int count = 0;
		SosSegment segment = new SosSegment();
		try {
			segment.readFromStream(utils.makeInputStreamFromString(SOSSEGMENT_BAD_COMPONENT_COUNT));
			Assert.fail("Should have failed to read the segment");
		} catch (Exception e) {

		}
	}

	@Test
	public void testEquals() throws IOException, InvalidJpegFormat {
		SosSegment one = new SosSegment();
		SosSegment two = new SosSegment();
		one.readFromStream(utils.makeInputStreamFromString(SOSSEGMENT_1));
		two.readFromStream(utils.makeInputStreamFromString(SOSSEGMENT_1));

		Assert.assertTrue("one equals two", one.equals(two));
		Assert.assertTrue("two equals one", two.equals(one));
	}

	@Test
	public void testEqualsFailsForUnequalSegments() throws IOException, InvalidJpegFormat {
		SosSegment one = new SosSegment();
		SosSegment two = new SosSegment();
		one.readFromStream(utils.makeInputStreamFromString(SOSSEGMENT_1));
		two.readFromStream(utils.makeInputStreamFromString(SOSSEGMENT_2));

		Assert.assertFalse("one doesn't equal two", one.equals(two));
		Assert.assertFalse("two doesn't equal one", two.equals(one));
	}

	@Test
	public void testWrite() throws IOException, InvalidJpegFormat {
		SosSegment one = new SosSegment();
		one.readFromStream(utils.makeInputStreamFromString(SOSSEGMENT_1));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		one.write(out);

		Assert.assertArrayEquals(utils.makeByteArrayFromString(SOSSEGMENT_1), out.toByteArray());
	}


}