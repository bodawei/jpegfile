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

import com.davidjohnburrowes.format.jpeg.component.SosComponentSpec;
import com.davidjohnburrowes.format.jpeg.marker.SosSegment;
import com.davidjohnburrowes.format.jpeg.support.FrameMode;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import com.davidjohnburrowes.util.Size;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Assert;
import static org.junit.Assert.*;
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
	private SosSegment segment;

	@Before
	public void setUp() {
		utils = new TestUtils();
		segment = new SosSegment();
	}

	@Test
	public void testMarkerId_isCorrect() throws IOException {
		assertEquals(SosSegment.MARKERID, segment.getMarkerId());
	}

	@Test
	public void setSpectralSelectionStart_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "spectralSelectionStart", Size.BYTE,
				  0, 0,
				  0, 0,
				  TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
				  TestUtils.SKIP_CASE, TestUtils.SKIP_CASE));
	}

	@Test(expected=IllegalArgumentException.class)
	public void setSpectralSelectionStart_cantBeSetTo0IfACLossless() {
		segment.setFrameMode(FrameMode.AC_LOSSLESS);
		segment.setSpectralSelectionStart(0);
	}

	@Test(expected=IllegalArgumentException.class)
	public void setSpectralSelectionStart_cantBeSetTo0IsHuffLossless() {
		segment.setFrameMode(FrameMode.HUFF_LOSSLESS);
		segment.setSpectralSelectionStart(0);
	}

	@Test
	public void setSpectralSelectionStart_canBeSetTo0IfDiffAcSpatial() {
		segment.setFrameMode(FrameMode.DIFF_AC_SPATIAL);
		segment.setSpectralSelectionStart(0);
		assertEquals(0, segment.getSpectralSelectionStart());
	}

	@Test
	public void setSpectralSelectionStart_canBeSetTo0IfDiffHuffSpatial() {
		segment.setFrameMode(FrameMode.DIFF_HUFF_SPATIAL);
		segment.setSpectralSelectionStart(0);
		assertEquals(0, segment.getSpectralSelectionStart());
	}

	@Test
	public void setSpectralSelectionEnd_honorsBounds() {
		segment.setSpectralSelectionStart(0);
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "spectralSelectionEnd", Size.BYTE,
				  63, 63,
				  63, 63,
				  TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
				  TestUtils.SKIP_CASE, TestUtils.SKIP_CASE));

		segment.setSpectralSelectionStart(1);
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "spectralSelectionEnd", Size.BYTE,
				  TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
				  TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
				  TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
				  0, 0));
	}

	@Test
	public void setSpectralSelectionStartAndEnd_Progressive_0And0() {
		segment.setFrameMode(FrameMode.HUFF_PROGRESSIVE_DCT);
		segment.setSpectralSelectionStart(0);
		segment.setSpectralSelectionEnd(0);
		assertEquals(0, segment.getSpectralSelectionStart());
		assertEquals(0, segment.getSpectralSelectionEnd());
	}

	@Test(expected=IllegalArgumentException.class)
	public void setSpectralSelectionStartAndEnd_Progressive_0And1_Bad() {
		segment.setFrameMode(FrameMode.HUFF_PROGRESSIVE_DCT);
		segment.setSpectralSelectionStart(0);
		segment.setSpectralSelectionEnd(1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void setSpectralSelectionStartAndEnd_Progressive_1And0_Bad() {
		segment.setFrameMode(FrameMode.HUFF_PROGRESSIVE_DCT);
		segment.setSpectralSelectionStart(1);
		segment.setSpectralSelectionEnd(0);
	}

	@Test
	public void setSpectralSelectionStartAndEnd_Progressive_63And63() {
		segment.setFrameMode(FrameMode.HUFF_PROGRESSIVE_DCT);
		segment.setSpectralSelectionStart(63);
		segment.setSpectralSelectionEnd(63);
	}

	@Test
	public void setSuccessiveApproximationHigh_honorsBounds() {
		segment.setSpectralSelectionStart(0);
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "successiveApproximationHigh", Size.NIBBLE,
				  0, 0,
				  0, 0,
				  TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
				  TestUtils.SKIP_CASE, TestUtils.SKIP_CASE));

			segment.setSpectralSelectionStart(1);
			segment.setSpectralSelectionEnd(2);
			assertEquals(TestUtils.PropResult.SUCCESS,
				utils.testProp(segment, "successiveApproximationHigh", Size.NIBBLE,
					TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
					TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
					0, 13,
					TestUtils.SKIP_CASE, TestUtils.SKIP_CASE));

			segment.setSpectralSelectionStart(1);
			segment.setSpectralSelectionEnd(0);
			assertEquals(TestUtils.PropResult.SUCCESS,
				utils.testProp(segment, "successiveApproximationHigh", Size.NIBBLE,
					TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
					TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
					TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
					0, 0));
	}

	@Test
	public void setSuccessiveApproximationLow_honorsBounds() {
		segment.setSpectralSelectionStart(0);
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "successiveApproximationLow", Size.NIBBLE,
				  0, 0,
				  0, 0,
				  TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
				  TestUtils.SKIP_CASE, TestUtils.SKIP_CASE));

		segment.setSpectralSelectionStart(1);
		segment.setSpectralSelectionEnd(2);
		assertEquals(TestUtils.PropResult.SUCCESS,
				utils.testProp(segment, "successiveApproximationLow", Size.NIBBLE,
					TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
					TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
					0, 13,
					TestUtils.SKIP_CASE, TestUtils.SKIP_CASE));

		segment.setSpectralSelectionStart(1);
		segment.setSpectralSelectionEnd(0);
		assertEquals(TestUtils.PropResult.SUCCESS,
				utils.testProp(segment, "successiveApproximationLow", Size.NIBBLE,
					TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
					TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
					TestUtils.SKIP_CASE, TestUtils.SKIP_CASE,
					0, 15));
	}

	@Test
	public void testEquals_twoIdenticalSegments_equal() throws IOException {
		SosSegment segmentTwo = new SosSegment();
		segment.read(utils.makeInputStream(SOSSEGMENT_1));
		segmentTwo.read(utils.makeInputStream(SOSSEGMENT_1));

		assertEquals(segment, segmentTwo);
	}

	@Test
	public void testEquals_differentSegments_notEqual() throws IOException {
		SosSegment segmentTwo = new SosSegment();
		segment.read(utils.makeInputStream(SOSSEGMENT_1));
		segmentTwo.read(utils.makeInputStream(SOSSEGMENT_2));

		assertFalse(segment.equals(segmentTwo));
	}

	@Test
	public void testValidate_withDefaultInStrictWithFrameMode_reportsError() throws IOException {
		segment.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);

		assertEquals(1, segment.validate().size());
	}

	@Test(expected=InvalidJpegFormat.class)
	public void testAddComponentSpec_withMoreThan4_throwsException() throws IOException {
		segment.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
		segment.addComponentSpec(new SosComponentSpec());
		segment.addComponentSpec(new SosComponentSpec());
		segment.addComponentSpec(new SosComponentSpec());
		segment.addComponentSpec(new SosComponentSpec());

		segment.addComponentSpec(new SosComponentSpec());
	}

	@Test
	public void testInsertComponentSpec_inMiddleLocation_insertsIntoThatLocation() throws IOException {
		segment.addComponentSpec(new SosComponentSpec());
		segment.addComponentSpec(new SosComponentSpec());

		SosComponentSpec expected = new SosComponentSpec();
		segment.insertComponentSpec(1, expected);

		assertEquals(expected, segment.getComponentSpec(1));
	}

	@Test
	public void testDeleteComponentSpec_deletesComponent() throws IOException {
		segment.addComponentSpec(new SosComponentSpec());

		segment.deleteComponentSpec(0);

		assertEquals(0, segment.getComponentSpecCount());
	}

	@Test
	public void testReadSegmentFromStream() throws IOException {
		segment.read(utils.makeInputStream(SOSSEGMENT_1));

		Assert.assertEquals("Spectral Selection Start", 1, segment.getSpectralSelectionStart());
		Assert.assertEquals("Spectral Selection End", 2, segment.getSpectralSelectionEnd());
		Assert.assertEquals("Successive Approximation", 3, segment.getSuccessiveApproximationLow());

		Assert.assertEquals("ScanDescriptor count", 3, segment.getComponentSpecCount());
	}

	@Test(expected=InvalidJpegFormat.class)
	public void testReadInvalidComponentCountErrors() throws IOException {
		segment.read(utils.makeInputStream(SOSSEGMENT_BAD_COMPONENT_COUNT));
	}

	@Test
	public void testWrite() throws IOException {
		segment.read(utils.makeInputStream(SOSSEGMENT_1));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		segment.write(out);

		Assert.assertArrayEquals(utils.makeByteArray("FF DA" + SOSSEGMENT_1), out.toByteArray());
	}
}