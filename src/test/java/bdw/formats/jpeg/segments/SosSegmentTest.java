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

import bdw.format.jpeg.segment.SosSegment;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.ParseMode;
import bdw.formats.jpeg.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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

	public SosSegment createSegment(InputStream stream) throws IOException, InvalidJpegFormat {
		return new SosSegment(SosSegment.SUBTYPE, stream, ParseMode.STRICT);
	}

	public SosSegment createSegment(RandomAccessFile file) throws IOException, InvalidJpegFormat {
		return new SosSegment(SosSegment.SUBTYPE, file, ParseMode.STRICT);
	}

	@Test
	public void testMarkerIsRight() throws IOException {
		SosSegment segment = new SosSegment();
		Assert.assertEquals("Marker ID", SosSegment.SUBTYPE, segment.getMarker());
	}

	@Test
	public void testSetSpectralSelectionStartZeroIsOK() {
		SosSegment segment = new SosSegment();

		segment.setSpectralSelectionStart(0);

		Assert.assertEquals(0, segment.getSpectralSelectionStart());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetSpectralSelectionStartNegOneIsNotOK() {
		SosSegment segment = new SosSegment();

		segment.setSpectralSelectionStart(-1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetSpectralSelectionStart256IsNotOK() {
		SosSegment segment = new SosSegment();

		segment.setSpectralSelectionStart(256);
	}

	@Test
	public void testSetSpectralSelectionEndZeroIsOK() {
		SosSegment segment = new SosSegment();

		segment.setSpectralSelectionEnd(0);

		Assert.assertEquals(0, segment.getSpectralSelectionStart());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetSpectralSelectionEndNegOneIsNotOK() {
		SosSegment segment = new SosSegment();

		segment.setSpectralSelectionEnd(-1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetSpectralSelectionEnd256IsNotOK() {
		SosSegment segment = new SosSegment();

		segment.setSpectralSelectionEnd(256);
	}

	@Test
	public void testSetSuccessiveApproximationZeroIsOK() {
		SosSegment segment = new SosSegment();

		segment.setSuccessiveApproximation(0);

		Assert.assertEquals(0, segment.getSpectralSelectionStart());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetSuccessiveApproximationNegOneIsNotOK() {
		SosSegment segment = new SosSegment();

		segment.setSuccessiveApproximation(-1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetSuccessiveApproximationEnd256IsNotOK() {
		SosSegment segment = new SosSegment();

		segment.setSuccessiveApproximation(256);
	}

	@Test
	public void testReadSegmentFromStream() throws IOException, InvalidJpegFormat {
		SosSegment segment = createSegment(utils.makeInputStreamFromString(SOSSEGMENT_1));

		Assert.assertEquals("Spectral Selection Start", 1, segment.getSpectralSelectionStart());
		Assert.assertEquals("Spectral Selection End", 2, segment.getSpectralSelectionEnd());
		Assert.assertEquals("Successive Approximation", 3, segment.getSuccessiveApproximation());

		Assert.assertEquals("ScanDescriptor count", 3, segment.getDescriptorCount());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testReadInvalidComponentCountErrors() throws IOException, InvalidJpegFormat {
		SosSegment segment = createSegment(utils.makeInputStreamFromString(SOSSEGMENT_BAD_COMPONENT_COUNT));
	}

	@Test
	public void testEquals() throws IOException, InvalidJpegFormat {
		SosSegment one = createSegment(utils.makeInputStreamFromString(SOSSEGMENT_1));
		SosSegment two = createSegment(utils.makeInputStreamFromString(SOSSEGMENT_1));

		Assert.assertTrue("one equals two", one.equals(two));
		Assert.assertTrue("two equals one", two.equals(one));
	}

	@Test
	public void testEqualsFailsForUnequalSegments() throws IOException, InvalidJpegFormat {
		SosSegment one = createSegment(utils.makeInputStreamFromString(SOSSEGMENT_1));
		SosSegment two = createSegment(utils.makeInputStreamFromString(SOSSEGMENT_2));

		Assert.assertFalse("one doesn't equal two", one.equals(two));
		Assert.assertFalse("two doesn't equal one", two.equals(one));
	}

	@Test
	public void testWrite() throws IOException, InvalidJpegFormat {
		SosSegment one = createSegment(utils.makeInputStreamFromString(SOSSEGMENT_1));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		one.write(out);

		Assert.assertArrayEquals(utils.makeByteArrayFromString(SOSSEGMENT_1), out.toByteArray());
	}


}