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

import java.io.RandomAccessFile;
import bdw.format.jpeg.support.ParseMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.formats.jpeg.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import bdw.formats.jpeg.segments.support.SofComponent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SofSegmentTest {

	private TestUtils utils;
	InputStream twoComponentStream;

	@Before
	public void setUp() throws IOException {
		utils = new TestUtils();
		twoComponentStream = utils.makeInputStreamFromString("00 0E 44 0033 1045 02 01 11 11 02 22 22");
	}

	@After
	public void tearDown() {
	}

	public SofSegment createSegment(InputStream stream) throws IOException, InvalidJpegFormat {
		return new SofSegment(SofSegment.FIRST1_SUBTYPE, stream, ParseMode.STRICT);
	}

	public SofSegment createSegment(RandomAccessFile file) throws IOException, InvalidJpegFormat {
		return new SofSegment(SofSegment.FIRST1_SUBTYPE, file, ParseMode.STRICT);
	}

	@Test
	public void newInstanceHasZeroMarkerByDefault() throws InvalidJpegFormat {
		assertEquals(SofSegment.FIRST1_SUBTYPE, new SofSegment(SofSegment.FIRST1_SUBTYPE).getMarker());
	}

	@Test
	public void canAssignPrecisionValue() throws InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);

		segment.setSamplePrecision(10);

		assertEquals(10, segment.getSamplePrecision());
	}

	@Test
	public void canNotAssignNegativePrecisionValue() throws InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);

		try {
			segment.setSamplePrecision(-10);
			fail("Did not get exception on setting");
		} catch (Exception e) {
			assertTrue((e instanceof IllegalArgumentException));
		}
	}

	@Test
	public void canNotAssignOutOfRangePrecisionValue() throws InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);

		try {
			segment.setSamplePrecision(256);
			fail("Did not get exception on setting");
		} catch (Exception e) {
			assertTrue((e instanceof IllegalArgumentException));
		}
	}


	@Test
	public void canAssignWidthValue() throws InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);

		segment.setImageWidth(10);

		assertEquals(10, segment.getImageWidth());
	}

	@Test
	public void canNotAssignNegativeWidth() throws InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);

		try {
			segment.setSamplePrecision(-10);
			fail("Did not get exception on setting");
		} catch (Exception e) {
			assertTrue((e instanceof IllegalArgumentException));
		}
	}

	@Test
	public void canNotAssignOutOfRangeWidthValue() throws InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);

		try {
			segment.setImageWidth(65536);
			fail("Did not get exception on setting");
		} catch (Exception e) {
			assertTrue((e instanceof IllegalArgumentException));
		}
	}

	@Test
	public void newInstanceHasZeroComponents() throws InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);

		assertEquals(0, segment.getComponentCount());
	}

	@Test
	public void canInsertAComponent() throws InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);
		SofComponent comp = new SofComponent();

		segment.addComponent(0, comp);

		assertEquals("Component count", 1, segment.getComponentCount());
		assertEquals("Inserted component", comp, segment.getComponent(0));
	}

	@Test
	public void insertAComponentOutOfRangeErrors() throws InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);
		SofComponent comp = new SofComponent();

		segment.addComponent(0, new SofComponent());
		try {
			segment.addComponent(7, comp);
			fail("Should have failed to insert at position 7");
		} catch (Exception e) {
			assertTrue("Exception type", e instanceof IndexOutOfBoundsException);
		}
	}

	@Test
	public void insertAComponentAtBeginningMovesOthersDown() throws InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);
		SofComponent comp = new SofComponent();

		segment.addComponent(0, new SofComponent());
		segment.addComponent(0, comp);

		assertEquals("Component count", 2, segment.getComponentCount());
		assertEquals("First component", comp, segment.getComponent(0));
	}

	@Test
	public void caNotDeleteComponentThatDoesntExist() throws InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);
		SofComponent comp1 = new SofComponent();

		segment.addComponent(0, comp1);
		try {
			segment.deleteComponent(2);
			fail("Should have gotten an exception");
		} catch (Exception e) {
			assertTrue(e instanceof IndexOutOfBoundsException);
		}

		assertEquals("Component count", 1, segment.getComponentCount());
		assertEquals("First component", comp1, segment.getComponent(0));
	}

	@Test
	public void canDeleteComponent() throws InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);
		SofComponent comp1 = new SofComponent();

		segment.addComponent(0, comp1);
		segment.addComponent(1, comp1);
		segment.deleteComponent(0);

		assertEquals("Component count", 1, segment.getComponentCount());
	}

	@Test
	public void canNotInsertMoreThan256Components() throws InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);

		for (int index = 0; index < 256; index++) {
			segment.addComponent(index, new SofComponent());
		}

		try {
			segment.addComponent(0, new SofComponent());
			fail("Should have thrown an exception");
		} catch (Exception exception) {
			assertTrue(exception instanceof IllegalArgumentException);
		}
		assertEquals(256, segment.getComponentCount());
	}

	@Test
	public void basicSofReadOK() throws IOException, InvalidJpegFormat {
		SofSegment segment = createSegment(utils.makeInputStreamFromString("00 0B 44 0033 1045 01 02 EB 12"));
		SofSegment answerFormat = new SofSegment(SofSegment.FIRST1_SUBTYPE);
		SofComponent entry = new SofComponent();

		answerFormat.setSamplePrecision(0x44);
		answerFormat.setImageHeight(0x33);
		answerFormat.setImageWidth(0x1045);
		entry.setId(2);
		entry.setSamplingX(0xE);
		entry.setSamplingY(0xB);
		entry.setQuantizationId(0x12);
		answerFormat.addComponent(0, entry);

		assertEquals("SofSegment", answerFormat, segment);
	}

	@Test(expected=InvalidJpegFormat.class)
	public void strictReadWithTwoComponentsThrowsException() throws IOException, InvalidJpegFormat {
		SofSegment segment = createSegment(twoComponentStream);
	}

	@Test(expected=InvalidJpegFormat.class)
	public void strictReadWithComponentIdOutOfRange() throws IOException, InvalidJpegFormat {
		SofSegment segment = createSegment(utils.makeInputStreamFromString("00 0E 44 0033 1045 01 06 11 11 "));
	}

	@Test
	public void laxReadWithComponentIdOutOfRange() throws IOException, InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE, 
				utils.makeInputStreamFromString("00 0B 44 0033 1045 01 06 11 11 "), ParseMode.LAX);
		SofSegment answerFormat = new SofSegment(SofSegment.FIRST1_SUBTYPE);
		SofComponent entry = new SofComponent();

		answerFormat.setSamplePrecision(0x44);
		answerFormat.setImageHeight(0x33);
		answerFormat.setImageWidth(0x1045);
		entry.setId(0x06);
		entry.setSamplingX(1);
		entry.setSamplingY(1);
		entry.setQuantizationId(0x11);
		answerFormat.addComponent(0, entry);

		assertEquals(answerFormat, segment);
		assertFalse(segment.isValid());
	}

	@Test
	public void laxReadWithTwoComponentsOK() throws IOException, InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE, 
				twoComponentStream, ParseMode.LAX);
		SofSegment answerFormat = new SofSegment(SofSegment.FIRST1_SUBTYPE);
		SofComponent entry = new SofComponent();

		answerFormat.setSamplePrecision(0x44);
		answerFormat.setImageHeight(0x33);
		answerFormat.setImageWidth(0x1045);
		entry.setId(0x01);
		entry.setSamplingX(1);
		entry.setSamplingY(1);
		entry.setQuantizationId(0x11);
		answerFormat.addComponent(0, entry);
		entry = new SofComponent();
		entry.setId(0x02);
		entry.setSamplingX(2);
		entry.setSamplingY(2);
		entry.setQuantizationId(0x22);
		answerFormat.addComponent(1, entry);

		assertEquals(answerFormat, segment);
		assertFalse(segment.isValid());
	}


	@Test
	public void unequalSegmentsAreNotEqual() throws IOException, InvalidJpegFormat {
		SofSegment segment = createSegment(utils.makeInputStreamFromString("00 0B 44 0033 1045 01 02 EB 12"));
		SofSegment answerFormat = new SofSegment(SofSegment.FIRST1_SUBTYPE);
		SofComponent entry = new SofComponent();

		answerFormat.setSamplePrecision(0x44);
		answerFormat.setImageHeight(0x33);
		answerFormat.setImageWidth(0x50); // difference
		entry.setId(2);
		entry.setSamplingX(0xE);
		entry.setSamplingY(0xB);
		entry.setQuantizationId(0x12);
		answerFormat.addComponent(0, entry);

		assertFalse(answerFormat.equals(segment));
	}

	@Test(expected=InvalidJpegFormat.class)
	public void invalidLengthThrowsException() throws IOException, InvalidJpegFormat {
		SofSegment segment = createSegment(utils.makeInputStreamFromString("00 03 44 0033 1045 01 02 EB 12"));
	}

	@Test(expected=InvalidJpegFormat.class)
	public void lengthAndComponentCountDontAgree() throws IOException, InvalidJpegFormat {
		SofSegment segment = createSegment(utils.makeInputStreamFromString("00 0B 44 0033 1045 02 02 EB 12 03 EB 12"));
	}

	@Test(expected=InvalidJpegFormat.class)
	public void eofInMiddleThrowsException() throws IOException, InvalidJpegFormat {
		SofSegment segment = createSegment(utils.makeInputStreamFromString("00 0E 44 0033 1045 02 02 EB"));
	}

	@Test(expected=IOException.class)
	public void writeSegmentWithNoComponentsShouldFail() throws IOException, InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);
		byte[] rawData = utils.makeByteArrayFromString("00 08 44 0033 1045 00");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		segment.setSamplePrecision(0x44);
		segment.setImageHeight(0x33);
		segment.setImageWidth(0x1045);

		segment.write(stream);

		assertArrayEquals("written bytes", rawData, stream.toByteArray());
	}

	@Test
	public void writeSegmentWithThreeComponents() throws IOException, InvalidJpegFormat {
		SofSegment segment = new SofSegment(SofSegment.FIRST1_SUBTYPE);
		byte[] rawData = utils.makeByteArrayFromString("00 11 44 0033 1045 03 01 12 03 04 75 06 05 E8 09");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		SofComponent entry;

		segment.setSamplePrecision(0x44);
		segment.setImageHeight(0x33);
		segment.setImageWidth(0x1045);
		entry  = new SofComponent();
		entry.setId(1);
		entry.setSamplingX(1);
		entry.setSamplingY(0x2);
		entry.setQuantizationId(3);
		segment.addComponent(0, entry);

		entry  = new SofComponent();
		entry.setId(4);
		entry.setSamplingX(7);
		entry.setSamplingY(5);
		entry.setQuantizationId(6);
		segment.addComponent(1, entry);

		entry  = new SofComponent();
		entry.setId(5);
		entry.setSamplingX(0xE);
		entry.setSamplingY(8);
		entry.setQuantizationId(9);
		segment.addComponent(2, entry);

		segment.write(stream);

		assertArrayEquals("written bytes", rawData, stream.toByteArray());
	}

}