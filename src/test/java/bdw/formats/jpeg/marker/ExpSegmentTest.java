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

import bdw.format.jpeg.marker.ExpSegment;
import bdw.format.jpeg.support.DataMode;
import bdw.format.jpeg.support.FrameMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.formats.jpeg.test.TestUtils;
import bdw.util.Size;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ExpSegmentTest {

	private TestUtils utils;
	private ExpSegment segment;

	@Before
	public void setUp() {
		utils = new TestUtils();
		segment = new ExpSegment();
		segment.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
	}

	@Test
	public void instanceHasProperMarker() {
		assertEquals(ExpSegment.MARKERID, new ExpSegment().getMarkerId());
	}

	@Test
	public void setExpandHorizontally_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "expandHorizontally", Size.NIBBLE, 0, 1));
	}

	@Test
	public void setExpandVertically_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "expandVertically", Size.NIBBLE, 0, 1));
	}

	@Test(expected=InvalidJpegFormat.class)
	public void readFromStream_throwsWhenSizeNotRight() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStream("00 04 AB");
		ExpSegment firstSegment = new ExpSegment();
		firstSegment.read(stream);
	}

	@Test
	public void readFromStream_inLaxMode_gathersExtraBytesWhenSizeInvalid() throws IOException {
		InputStream stream = utils.makeInputStream("00 04 AB FF");
		segment.setDataMode(DataMode.LAX);
		segment.read(stream);

		assertEquals(4, segment.getParameterSizeOnDisk());
		assertEquals((byte) 0xFF, segment.getTrailingBytes()[0]);
		assertEquals(1, segment.getTrailingBytes().length);
	}

	@Test
	public void readFromStream_storesValidValues() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStream("00 03 11");
		segment.read(stream);

		assertEquals(1, segment.getExpandHorizontally());
		assertEquals(1, segment.getExpandVertically());
	}

	@Test
	public void readFromStream_storesInvalidValuesWithProblems() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStream("00 03 F5");
		segment.setDataMode(DataMode.LAX);
		segment.read(stream);

		assertEquals(15, segment.getExpandHorizontally());
		assertEquals(5, segment.getExpandVertically());
	}

	@Test
	public void write_writesValidValues() throws IOException, InvalidJpegFormat {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		segment.setExpandHorizontally(1);
		segment.setExpandVertically(1);

		segment.write(output);
		assertArrayEquals(utils.makeByteArray("FFDF 00 03 11"), output.toByteArray());

	}

	@Test
	public void write_writesInvalidValues() throws IOException, InvalidJpegFormat {
		segment.setDataMode(DataMode.LAX);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		segment.setExpandHorizontally(15);
		segment.setExpandVertically(3);

		segment.write(output);
		assertArrayEquals(utils.makeByteArray("FFDF 00 03 F3"), output.toByteArray());
	}

	@Test
	public void clearPassthrough_inLaxMode_resetsPassthrough() throws IOException {
		InputStream stream = utils.makeInputStream("00 04 AB 01");
		segment.setDataMode(DataMode.LAX);
		segment.read(stream);
		segment.clearPassthrough();

		assertEquals(3, segment.getParameterSizeOnDisk());
		assertEquals(0, segment.getTrailingBytes().length);
	}

	@Test
	public void validate_inLaxMode_returnsAllExceptions() throws IOException {
		InputStream stream = utils.makeInputStream("00 04 AB 01");
		segment.setDataMode(DataMode.LAX);
		segment.read(stream);
		List<Exception> problems = segment.validate();

		assertEquals(3, problems.size());
	}

	@Test
	public void equals_readFromStreamEqualsHandBuilt() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStream("00 03 AB");
		ExpSegment firstSegment = new ExpSegment();
		ExpSegment secondSegment = new ExpSegment();
		firstSegment.setDataMode(DataMode.LAX);
		secondSegment.setDataMode(DataMode.LAX);
		firstSegment.read(stream);
		secondSegment.setExpandHorizontally(10);
		secondSegment.setExpandVertically(11);

		assertTrue(new ExpSegment().equals(new ExpSegment()));
	}

	@Test
	public void equals_equalToExpSegment() throws IOException {
		assertTrue(new ExpSegment().equals(new ExpSegment()));
	}

	@Test
	public void equals_notEqualToOther() throws IOException {
		assertFalse(new ExpSegment().equals(new Object()));
	}
}