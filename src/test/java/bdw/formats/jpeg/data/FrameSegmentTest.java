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

package bdw.formats.jpeg.data;

import bdw.format.jpeg.data.FrameSegment;
import bdw.format.jpeg.marker.SofSegment;
import bdw.format.jpeg.component.FrameComponent;
import bdw.format.jpeg.support.FrameMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.DataMode;
import bdw.util.Size;
import bdw.formats.jpeg.test.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class FrameSegmentTest {

	private TestUtils utils;
	InputStream twoComponentStream;
	FrameSegment segment;

	@Before
	public void setUp() throws IOException {
		utils = new TestUtils();
		twoComponentStream = utils.makeInputStream("00 0E 08 0033 1045 02 01 11 11 02 22 22");
		segment = new FrameSegment(SofSegment.FIRST1_MARKERID);
	}

	@Test
	public void setSamplePrecision_for8BitSamples_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "samplePrecision", Size.BYTE,
				  8, 8,
				  8, 8,
				  8, 8,
				  2, 16));
	}

	@Test
	public void setSamplePrecision_for12BitSamples_honorsBounds() {
		// Actually, we can put either 8 or 12 only in some modes, so check those here.
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "samplePrecision", Size.BYTE,
				  8, 8,
				  12, 12,
				  12, 12,
				  2, 16));
	}

	@Test
	public void setImageHeight_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "imageHeight", Size.SHORT,
				  0, 65535));
	}

	@Test
	public void setImageWidth_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(segment, "imageWidth", Size.SHORT,
				  1, 65535));
	}

	@Test
	public void componentCount_isZeroByDefault() {
		assertEquals(0, segment.getComponentCount());
	}

	@Test
	public void deleteCompnent_inStrictMode_allowsDeletionOfLast() {
		FrameComponent comp = new FrameComponent();
		segment.addComponent(comp);

		segment.deleteComponent(0);

		assertEquals(0, segment.getComponentCount());
	}

	@Test
	public void deleteCompoent_inLaxMode_allowsDeletionOfLast() {
		FrameComponent comp = new FrameComponent();
		segment.addComponent(comp);

		segment.deleteComponent(0);

		assertEquals(0, segment.getComponentCount());
	}

	@Test
	public void insertComponent_inserts() {
		FrameComponent comp = new FrameComponent();

		segment.insertComponent(0, comp);

		assertEquals(1, segment.getComponentCount());
		assertEquals(comp, segment.getComponent(0));
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void insertCompoent_withOutOfRangeIndex_throwsAnException() {
		segment.insertComponent(7, new FrameComponent());
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void deleteComponent_withOutOfRangeIndex_throwsException() {
		segment.deleteComponent(2);
	}

	@Test(expected=IllegalArgumentException.class)
	public void canNotInsertMoreThan256Components() {
		for (int index = 0; index < 256; index++) {
			segment.insertComponent(index, new FrameComponent());
		}

		segment.insertComponent(0, new FrameComponent());
	}

	@Test
	public void read_basicFrame_isOK() throws IOException {
		segment.read(utils.makeInputStream("00 0B 08 0033 1045 01 02 22 02"));
		FrameSegment answerFormat = new FrameSegment(SofSegment.FIRST1_MARKERID);
		FrameComponent entry = new FrameComponent();

		answerFormat.setSamplePrecision(0x08);
		answerFormat.setImageHeight(0x33);
		answerFormat.setImageWidth(0x1045);
		entry.setComponentId(2);
		entry.setHorizontalScaling(0x2);
		entry.setVerticalScaling(0x2);
		entry.setQuantizationSelector(0x2);
		answerFormat.addComponent(entry);

		assertEquals(answerFormat, segment);
	}

	@Test
	public void laxReadWithComponentIdOutOfRange() throws IOException {
		segment.setDataMode(DataMode.LAX);
		segment.read(utils.makeInputStream("00 0B 44 0033 1045 01 06 11 11 "));
		FrameSegment answerFormat = new FrameSegment(SofSegment.FIRST1_MARKERID);
		FrameComponent entry = new FrameComponent();
		answerFormat.setDataMode(DataMode.LAX);

		answerFormat.setSamplePrecision(0x44);
		answerFormat.setImageHeight(0x33);
		answerFormat.setImageWidth(0x1045);
		entry.setComponentId(0x06);
		entry.setHorizontalScaling(1);
		entry.setVerticalScaling(1);
		entry.setQuantizationSelector(0x11);
		answerFormat.addComponent(entry);

		assertEquals(answerFormat, segment);
	}

	@Test
	public void laxReadWithTwoComponentsOK() throws IOException {
		segment.read(twoComponentStream);
		FrameSegment answerFormat = new FrameSegment(SofSegment.FIRST1_MARKERID);
		FrameComponent entry = new FrameComponent();

		answerFormat.setSamplePrecision(0x08);
		answerFormat.setImageHeight(0x33);
		answerFormat.setImageWidth(0x1045);
		entry.setComponentId(0x01);
		entry.setHorizontalScaling(1);
		entry.setVerticalScaling(1);
		entry.setQuantizationSelector(0x11);
		answerFormat.addComponent(entry);
		entry = new FrameComponent();
		entry.setComponentId(0x02);
		entry.setHorizontalScaling(2);
		entry.setVerticalScaling(2);
		entry.setQuantizationSelector(0x22);
		answerFormat.addComponent(entry);

		assertEquals(answerFormat, segment);
	}

	@Test
	public void equals_unequalSegments_notEqual() throws IOException {
		segment.read(utils.makeInputStream("00 0B 08 0033 1045 01 02 EB 12"));
		FrameSegment answerFormat = new FrameSegment(SofSegment.FIRST1_MARKERID);
		FrameComponent entry = new FrameComponent();

		answerFormat.setSamplePrecision(0x08);
		answerFormat.setImageHeight(0x33);
		answerFormat.setImageWidth(0x50); // difference
		entry.setComponentId(2);
		entry.setHorizontalScaling(0xE);
		entry.setVerticalScaling(0xB);
		entry.setQuantizationSelector(0x12);
		answerFormat.insertComponent(0, entry);

		assertFalse(answerFormat.equals(segment));
	}

	@Test(expected=InvalidJpegFormat.class)
	public void read_withInvalidLength_throwsError() throws IOException {
		segment.read(utils.makeInputStream("00 03 08 0033 1045 01 02 EB 12"));
	}

	@Test(expected=InvalidJpegFormat.class)
	public void read_withInvalidLengthCount_throwsError() throws IOException {
		segment.read(utils.makeInputStream("00 0B 08 0033 1045 02 02 EB 12 03 EB 12"));
	}

	@Test(expected=EOFException.class)
	public void read_withEOFInMidStream_throwsError() throws IOException {
		segment.read(utils.makeInputStream("00 0E 08 0033 1045 02 02 EB"));
	}


	@Test(expected=InvalidJpegFormat.class)
	public void read_withStrictInvalidStream_throwsException() throws IOException {
		segment.read(utils.makeInputStream("00 0E 08 0033 1045 01 06 11 11 "));
	}

	@Test(expected=IllegalStateException.class)
	public void write_withNoComponentSegment_throwsError() throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		segment.setSamplePrecision(0x08);
		segment.setImageHeight(0x33);
		segment.setImageWidth(0x1045);

		segment.write(stream);
	}

	@Test
	public void write_withSimpleSegment_works() throws IOException {
		byte[] rawData = utils.makeByteArray("FFC0 00 0B 08 0033 1045 01 00 11 00");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		segment.setSamplePrecision(0x08);
		segment.setImageHeight(0x33);
		segment.setImageWidth(0x1045);
		segment.addComponent(new FrameComponent());

		segment.write(stream);

		assertArrayEquals(rawData, stream.toByteArray());
	}

	@Test
	public void write_withThreeComponents_works() throws IOException {
		byte[] rawData = utils.makeByteArray("FFC0 00 11 08 0033 1045 03 01 11 01 02 22 02 03 33 03");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		FrameComponent entry;

		segment.setSamplePrecision(0x08);
		segment.setImageHeight(0x33);
		segment.setImageWidth(0x1045);
		entry  = new FrameComponent();
		entry.setComponentId(1);
		entry.setHorizontalScaling(1);
		entry.setVerticalScaling(1);
		entry.setQuantizationSelector(1);
		segment.addComponent(entry);

		entry  = new FrameComponent();
		entry.setComponentId(2);
		entry.setHorizontalScaling(2);
		entry.setVerticalScaling(2);
		entry.setQuantizationSelector(2);
		segment.addComponent(entry);

		entry  = new FrameComponent();
		entry.setComponentId(3);
		entry.setHorizontalScaling(3);
		entry.setVerticalScaling(3);
		entry.setQuantizationSelector(3);
		segment.addComponent(entry);

		segment.write(stream);

		assertArrayEquals(rawData, stream.toByteArray());
	}

	@Test
	public void getSegmentSizeOnDisk_withNewSegment_works() throws IOException {
		assertEquals(8, segment.getParameterSizeOnDisk());
	}

	@Test
	public void getSegmentSizeOnDisk_withTwoComponentSegment_works() throws IOException {
		segment.addComponent(new FrameComponent());
		segment.addComponent(new FrameComponent());
		assertEquals(14, segment.getParameterSizeOnDisk());
	}

	@Test
	public void getSegmentSizeOnDisk_withThreeComponents_works() throws IOException {
		FrameComponent entry;

		segment.setSamplePrecision(0x08);
		segment.setImageHeight(0x33);
		segment.setImageWidth(0x1045);
		entry  = new FrameComponent();
		entry.setComponentId(1);
		entry.setHorizontalScaling(1);
		entry.setVerticalScaling(1);
		entry.setQuantizationSelector(1);
		segment.addComponent(entry);

		entry  = new FrameComponent();
		entry.setComponentId(2);
		entry.setHorizontalScaling(2);
		entry.setVerticalScaling(2);
		entry.setQuantizationSelector(2);
		segment.addComponent(entry);

		entry  = new FrameComponent();
		entry.setComponentId(3);
		entry.setHorizontalScaling(3);
		entry.setVerticalScaling(3);
		entry.setQuantizationSelector(3);
		segment.addComponent(entry);

		assertEquals(17, segment.getParameterSizeOnDisk());
	}

	@Test
	public void validate_withInvalidValues_reportsRightCountOfValidationErrors() throws IOException {
		segment.setDataMode(DataMode.LAX);
		segment.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
		FrameComponent entry = new FrameComponent();
		entry.setDataMode(DataMode.LAX);

		segment.setSamplePrecision(8);
		segment.setImageHeight(33);
		segment.setImageWidth(0); // error 1
		entry.setComponentId(2);
		entry.setHorizontalScaling(14); // not goodeither
		entry.setVerticalScaling(11); // nor this
		entry.setQuantizationSelector(3);
		segment.addComponent(entry);

		assertEquals(3, segment.validate().size());
	}

	@Test
	public void validate_withFreshlyInstantiatedStrictInstance_reportsThatComponentCountIsWrong() throws IOException {
		segment.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
		assertEquals(1, segment.validate().size());
	}
}