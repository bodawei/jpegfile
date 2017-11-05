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

import com.davidjohnburrowes.format.jpeg.component.Thumbnail3BytesPerPixel;
import com.davidjohnburrowes.format.jpeg.marker.JfifSegment;
import com.davidjohnburrowes.format.jpeg.support.DataMode;
import com.davidjohnburrowes.format.jpeg.support.FrameMode;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import com.davidjohnburrowes.io.LimitExceeded;
import com.davidjohnburrowes.util.Size;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class JfifSegmentTest {

	private TestUtils utils;
	private JfifSegment segment;
	private String basicSegmentBytes = "00 10" +
			"4A46494600 0102 00 0001 0001 00 00";
	private String basicSegmentWithThumbnailBytes = "00 22" +
			"4A46494600 0102 01 0003 0002 03 02" +
			"FFFFFF 000000 FFFFFF" +
			"000000 FFFFFF 000000";

	private JfifSegment makeBasicSegment() throws InvalidJpegFormat {
		JfifSegment seg = new JfifSegment();
		seg.setVersion(0x0102);
		seg.setUnits(JfifSegment.NO_UNITS);
		seg.setXDensity(1);
		seg.setYDensity(1);
		seg.getThumbnail().setWidth(0);
		seg.getThumbnail().setHeight(0);

		return seg;
	}

	private JfifSegment makeBasicSegmentWithThumbnail() throws InvalidJpegFormat {
		JfifSegment seg = new JfifSegment();
		seg.setVersion(JfifSegment.NEWEST_VERSION);
		seg.setUnits(JfifSegment.DOTS_PER_INCH);
		seg.setXDensity(3);
		seg.setYDensity(2);
		seg.getThumbnail().setWidth(3);
		seg.getThumbnail().setHeight(2);
		byte[] buffer = new byte[] {-1, -1, -1, 0, 0, 0, -1, -1, -1, 0, 0, 0, -1, -1, -1, 0, 0, 0};
		seg.getThumbnail().setPixelBytes(buffer);

		return seg;
	}

	@Before
   public void setUp() throws InvalidJpegFormat {
		segment = new JfifSegment();
		utils = new TestUtils();
    }

	@Test
	public void getMarkerId_isCorrect() {
		assertEquals(JfifSegment.MARKERID, segment.getMarkerId());
	}

	@Test
	public void setVersion_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "version", Size.SHORT,
				  JfifSegment.FIRST_VERSION, JfifSegment.NEWEST_VERSION));
	}

	@Test
	public void setUnits_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "units", Size.BYTE, 0, 2));
	}

	@Test
	public void setXDensity_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "xDensity", Size.SHORT, 1, 65535));
	}

	@Test
	public void setYDensity_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "yDensity", Size.SHORT, 1, 65535));
	}

	@Test(expected=IllegalArgumentException.class)
	public void setThumbnail_withNull_throwsException() {
		segment.setThumbnail(null);
	}

	@Test
	public void setThumbnail_withThumbnail_setsTheThumbnail() {
		Thumbnail3BytesPerPixel thumbnail = new Thumbnail3BytesPerPixel();
		segment.setThumbnail(thumbnail);

		assertEquals(thumbnail, segment.getThumbnail());
	}

	@Test
	public void validate_withInvalidVersion_reportsError() {
		segment.setVersion(4);
		segment.setFrameMode(FrameMode.AC_LOSSLESS);

		assertEquals(1, segment.validate().size());
	}

	@Test
	public void equals_withSelf_isTrue() {
		assertTrue(segment.equals(segment));
	}

	@Test
	public void equals_withObject_isFalse() {
		assertFalse(segment.equals(new Object()));
	}

	@Test
	public void equals_withDifferentThumbnail_isFalse() {
		segment.setVersion(4);
		assertFalse(segment.equals(new JfifSegment()));
	}

	@Test(expected=InvalidJpegFormat.class)
	public void setValidateMode_goingFromInvalidData_throwsException() {
		segment.setVersion(4);
		segment.setFrameMode(FrameMode.AC_LOSSLESS);
		segment.setDataMode(DataMode.STRICT);
	}

    @Test
    public void readFromStreamWorks() throws IOException, InvalidJpegFormat {
		segment.read(utils.makeInputStream(basicSegmentBytes));

		assertTrue(segment.equals(makeBasicSegment()));
	}

    @Test(expected=InvalidJpegFormat.class)
    public void readFromStreamWithBadLength() throws IOException, InvalidJpegFormat {
		segment.read(utils.makeInputStream("00 12" +
			"4A46494600 0102 00 0000 0000 00 00"));
	}

    @Test(expected=LimitExceeded.class)
    public void readFromStreamWithInvalidWidthAndHeight() throws IOException, InvalidJpegFormat {
		segment.read(utils.makeInputStream("00 10" +
			"4A46494600 0102 00 0001 0001 05 05"));
	}

    @Test
    public void readFromStreamWithThumbnail() throws IOException, InvalidJpegFormat {
		segment.read(utils.makeInputStream(basicSegmentWithThumbnailBytes));
		assertTrue(segment.equals(makeBasicSegmentWithThumbnail()));
	}

    @Test
    public void readFromSmallFileWorks() throws IOException, InvalidJpegFormat {
		segment.read(utils.makeRandomAccessFile(basicSegmentBytes));

		assertTrue(segment.equals(makeBasicSegment()));
	}

    @Test(expected=InvalidJpegFormat.class)
    public void readFromFileWithBadLength() throws IOException, InvalidJpegFormat {
		segment.read(utils.makeRandomAccessFile("FFE0 00 12" +
			"4A46494600 0102 00 0000 0000 00 00"));
	}

	@Test
    public void writeABasicSegment() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals(utils.makeByteArray("FFE0" + basicSegmentBytes),
				output.toByteArray());
	}

	@Test
    public void writeABizarreSegment() throws IOException {
		segment.setVersion(129);
		segment.setUnits(255);
		segment.setXDensity(32767);
		segment.setYDensity(1);
		segment.getThumbnail().setHeight(250);

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals(utils.makeByteArray("FFE0 00 10" +
				"4A46494600 0081 FF 7FFF 0001 00 FA"),
				output.toByteArray());
	}
}