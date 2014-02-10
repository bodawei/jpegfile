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

import bdw.format.jpeg.JpegData;
import bdw.format.jpeg.component.Thumbnail1BytePerPixel;
import bdw.format.jpeg.component.Thumbnail3BytesPerPixel;
import bdw.format.jpeg.component.ThumbnailJpeg;
import bdw.format.jpeg.marker.EoiMarker;
import bdw.format.jpeg.marker.JfxxSegment;
import bdw.format.jpeg.marker.SoiMarker;
import bdw.format.jpeg.support.DataMode;
import bdw.format.jpeg.support.FrameMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.formats.jpeg.test.TestUtils;
import bdw.io.LimitExceeded;
import bdw.util.Size;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class JfxxSegmentTest {

	private TestUtils utils;
	private JfxxSegment segment;
	private static final String basicJfxxBytes = "000A 4A46585800 13 00 00";
	private static final String threeByteThumbnailJfxxBytes = "0013 4A46585800 13 01 03 000000 FFFFFF 000000";
	private static final String jpegThumbnailJfxxBytes = "000C 4A46585800 10 FF D8 FF D9";

	@Before
	public void setUp() throws InvalidJpegFormat {
		segment = new JfxxSegment();
		segment.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
		utils = new TestUtils();
	}

	@Test
	public void getMarkerId_hasCorrectValue() {
		assertEquals(JfxxSegment.MARKERID, segment.getMarkerId());
	}

	@Test
	public void getSegmentSizeOnDisk_byDefault_hasRightSize() {
		assertEquals(10, segment.getParameterSizeOnDisk());
	}

	@Test
	public void getIdentifier_isExpected() {
		assertEquals("JFXX\0", segment.getIdentifier());
	}

	@Test
	public void getExtensionCode_defaultsTo3Bytes() {
		assertEquals(0x13, segment.getExtensionCode());
	}

	@Test
	public void getExtensionCode_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
				  utils.testProp(segment, "extensionCode", Size.BYTE, 0x10, 0x13));
	}

	@Test(expected = IllegalArgumentException.class)
	public void setExtensionCode_with12_throwsError() {
		segment.setExtensionCode(0x12);
	}

	@Test
	public void setThumbnail_updatesExtensionCode() {
		segment.setThumbnail(new Thumbnail1BytePerPixel());
		assertEquals(0x11, segment.getExtensionCode());
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
		segment.setExtensionCode(0x13);
		assertEquals(segment, new JfxxSegment());
	}

	@Test
	public void validate_byDefault_isEmpty() {
		assertEquals(0, segment.validate().size());
	}

	@Test
	public void validate_withInvalidThumbnail_showsThatProblem() {
		Thumbnail3BytesPerPixel thumbnail = (Thumbnail3BytesPerPixel) segment.getThumbnail();
		thumbnail.setHeight(99);
		thumbnail.setWidth(3);
		assertEquals(1, segment.validate().size());
	}

	@Test
	public void validate_withInvalidExtensionCode_showsThatProblem() {
		segment.setDataMode(DataMode.LAX);
		segment.setExtensionCode(4);
		assertEquals(2, segment.validate().size());
	}

	@Test
	public void validate_withInvalidExtensionCodeOf12_showsThatProblem() {
		segment.setDataMode(DataMode.LAX);
		segment.setExtensionCode(0x12);
		assertEquals(2, segment.validate().size());
	}

	@Test
	public void validate_withExtensionCodeNotMatchingThumbnail_showsProblem() {
		segment.setExtensionCode(0x10);
		assertEquals(1, segment.validate().size());
	}

	@Test(expected = LimitExceeded.class)
	public void readFromStream_withShortData_throwsException() throws IOException {
		InputStream stream = utils.makeInputStream("0007 4A46585800 13");
		segment = new JfxxSegment();

		segment.read(stream);
	}

	@Test(expected = InvalidJpegFormat.class)
	public void readFromStream_withoutJFXXMarker_throwsException() throws IOException {
		InputStream stream = utils.makeInputStream("0008 4A465858FF 13");
		segment = new JfxxSegment();

		segment.read(stream);
	}

	@Test(expected = InvalidJpegFormat.class)
	public void readFromStream_WithInvalidThumbnailType_throwsException() throws IOException {
		InputStream stream = utils.makeInputStream("0008 4A46585800 64");
		segment = new JfxxSegment();
		segment.setFrameMode(FrameMode.AC_LOSSLESS);

		segment.read(stream);
	}

	@Test
	public void write_defaultSegment_writesSuccessfully() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals(utils.makeByteArray("FFE0" + basicJfxxBytes),
				  output.toByteArray());
	}


	@Test
	public void write_invalidSegmentInLaxMode_writesCorrectly() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.setDataMode(DataMode.LAX);
		segment.setExtensionCode(64);
		segment.write(output);

		assertArrayEquals(utils.makeByteArray("FFE0 000A 4A46585800 40 00 00"),
				  output.toByteArray());
	}

	@Test()
	public void readFromStream_withThreeByteThumbnail_readsSuccessfully() throws IOException {
		InputStream stream = utils.makeInputStream(threeByteThumbnailJfxxBytes);

		segment = new JfxxSegment();

		segment.read(stream);

		JfxxSegment expected = new JfxxSegment();
		Thumbnail3BytesPerPixel thumb = (Thumbnail3BytesPerPixel) expected.getThumbnail();
		thumb.setWidth(1);
		thumb.setHeight(3);
		thumb.setPixelBytes(new byte[]{0x00, 0x00, 0x00, -1, -1, -1, 0x00, 0x00, 0x00});

		assertEquals(expected, segment);
	}

	@Test()
	public void readFromStream_withJpegThumbnail_readsSuccessfully() throws IOException {
		InputStream stream = utils.makeInputStream(jpegThumbnailJfxxBytes);

		segment = new JfxxSegment();
		segment.setDataMode(DataMode.LAX);

		segment.read(stream);

		JpegData jpegImage = new JpegData();
		jpegImage.addItem(new SoiMarker());
		jpegImage.addItem(new EoiMarker());
		ThumbnailJpeg jThumb = new ThumbnailJpeg();
		jThumb.setJpegImage(jpegImage);
		JfxxSegment expected = new JfxxSegment();
		expected.setThumbnail(jThumb);

		assertEquals(expected, segment);
	}
}
