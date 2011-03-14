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

import bdw.formats.jpeg.segments.support.InvalidJpegFormat;
import java.io.IOException;
import java.io.InputStream;
import bdw.formats.jpeg.segments.support.SofComponentEntry;
import bdw.formats.jpeg.segments.SofSegment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SofSegmentTest {

	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
	}

	@After
	public void tearDown() {
	}

	@Test
	public void newInstanceHasZeroMarkerByDefault() {
		assertEquals(0, new SofSegment().getMarker());
	}

	@Test
	public void newInstanceIsNotValidByDefault() {
		assertEquals(false, new SofSegment().isValid());
	}

	@Test
	public void canAssignPrecisionValue() {
		SofSegment segment = new SofSegment();

		segment.setSamplePrecision(10);

		assertEquals(10, segment.getSamplePrecision());
	}

	@Test
	public void canNotAssignNegativePrecisionValue() {
		SofSegment segment = new SofSegment();

		try {
			segment.setSamplePrecision(-10);
			fail("Did not get exception on setting");
		} catch (Exception e) {
			assertTrue((e instanceof IllegalArgumentException));
		}
	}

	@Test
	public void canNotAssignOutOfRangePrecisionValue() {
		SofSegment segment = new SofSegment();

		try {
			segment.setSamplePrecision(256);
			fail("Did not get exception on setting");
		} catch (Exception e) {
			assertTrue((e instanceof IllegalArgumentException));
		}
	}


	@Test
	public void canAssignWidthValue() {
		SofSegment segment = new SofSegment();

		segment.setImageWidth(10);

		assertEquals(10, segment.getImageWidth());
	}

	@Test
	public void canNotAssignNegativeWidth() {
		SofSegment segment = new SofSegment();

		try {
			segment.setSamplePrecision(-10);
			fail("Did not get exception on setting");
		} catch (Exception e) {
			assertTrue((e instanceof IllegalArgumentException));
		}
	}

	@Test
	public void canNotAssignOutOfRangeWidthValue() {
		SofSegment segment = new SofSegment();

		try {
			segment.setImageWidth(65536);
			fail("Did not get exception on setting");
		} catch (Exception e) {
			assertTrue((e instanceof IllegalArgumentException));
		}
	}

	@Test
	public void newInstanceHasZeroComponents() {
		SofSegment segment = new SofSegment();

		assertEquals(0, segment.getComponentCount());
	}

	@Test
	public void canInsertAComponent() {
		SofSegment segment = new SofSegment();
		SofComponentEntry comp = new SofComponentEntry();

		segment.addComponent(0, comp);
		
		assertEquals("Component count", 1, segment.getComponentCount());
		assertEquals("Inserted component", comp, segment.getComponent(0));
	}

	@Test
	public void insertAComponentOutOfRangeErrors() {
		SofSegment segment = new SofSegment();
		SofComponentEntry comp = new SofComponentEntry();

		segment.addComponent(0, new SofComponentEntry());
		try {
			segment.addComponent(7, comp);
			fail("Should have failed to insert at position 7");
		} catch (Exception e) {
			assertTrue("Exception type", e instanceof IndexOutOfBoundsException);
		}
	}

	@Test
	public void insertAComponentAtBeginningMovesOthersDown() {
		SofSegment segment = new SofSegment();
		SofComponentEntry comp = new SofComponentEntry();

		segment.addComponent(0, new SofComponentEntry());
		segment.addComponent(0, comp);

		assertEquals("Component count", 2, segment.getComponentCount());
		assertEquals("First component", comp, segment.getComponent(0));
	}

	@Test
	public void caNotDeleteComponentThatDoesntExist() {
		SofSegment segment = new SofSegment();
		SofComponentEntry comp1 = new SofComponentEntry();

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
	public void canDeleteComponent() {
		SofSegment segment = new SofSegment();
		SofComponentEntry comp1 = new SofComponentEntry();

		segment.addComponent(0, comp1);
		segment.addComponent(1, comp1);
		segment.deleteComponent(0);

		assertEquals("Component count", 1, segment.getComponentCount());
	}

	@Test
	public void canNotInsertMoreThan256Components() {
		SofSegment segment = new SofSegment();

		for (int index = 0; index < 256; index++) {
			segment.addComponent(index, new SofComponentEntry());
		}

		try {
			segment.addComponent(0, new SofComponentEntry());
			fail("Should have thrown an exception");
		} catch (Exception exception) {
			assertTrue(exception instanceof IllegalArgumentException);
		}
		assertEquals(256, segment.getComponentCount());
	}

	@Test
	public void basicSofReadOK() throws IOException, InvalidJpegFormat {
		SofSegment segment = new SofSegment();
		InputStream stream = utils.makeInputStreamFromString("00 0B 44 0033 1045 01 02 EB 12");
		SofSegment answerFormat = new SofSegment();
		SofComponentEntry entry = new SofComponentEntry();
		
		answerFormat.setSamplePrecision(0x44);
		answerFormat.setImageHeight(0x33);
		answerFormat.setImageWidth(0x1045);
		entry.setId(2);
		entry.setSamplingX(0xE);
		entry.setSamplingY(0xB);
		entry.setQuantizationId(0x12);
		answerFormat.addComponent(0, entry);

		segment.readFromStream(stream);
		
		assertEquals("SofSegment", answerFormat, segment);
	}

	@Test
	public void unequalSegmentsAreNotEqual() throws IOException, InvalidJpegFormat {
		SofSegment segment = new SofSegment();
		InputStream stream = utils.makeInputStreamFromString("00 0B 44 0033 1045 01 02 EB 12");
		SofSegment answerFormat = new SofSegment();
		SofComponentEntry entry = new SofComponentEntry();

		answerFormat.setSamplePrecision(0x44);
		answerFormat.setImageHeight(0x33);
		answerFormat.setImageWidth(0x50); // difference
		entry.setId(2);
		entry.setSamplingX(0xE);
		entry.setSamplingY(0xB);
		entry.setQuantizationId(0x12);
		answerFormat.addComponent(0, entry);

		segment.readFromStream(stream);

		assertFalse(answerFormat.equals(segment));
	}

	@Test
	public void invalidLengthThrowsException() throws IOException, InvalidJpegFormat {
		SofSegment segment = new SofSegment();
		InputStream stream = utils.makeInputStreamFromString("00 03 44 0033 1045 01 02 EB 12");

		try {
			segment.readFromStream(stream);
			fail("Didn't throw an exception when it should have");
		} catch (Exception e) {
			assertTrue(e instanceof InvalidJpegFormat);
		}
	}

	@Test
	public void lengthAndComponentCountDontAgree() throws IOException, InvalidJpegFormat {
		SofSegment segment = new SofSegment();
		InputStream stream = utils.makeInputStreamFromString("00 0B 44 0033 1045 02 02 EB 12 03 EB 12");

		try {
			segment.readFromStream(stream);
			fail("Didn't throw an exception when it should have");
		} catch (Exception e) {
			assertTrue(e instanceof InvalidJpegFormat);
		}
	}

	@Test
	public void eofInMiddleThrowsException() throws IOException, InvalidJpegFormat {
		SofSegment segment = new SofSegment();
		InputStream stream = utils.makeInputStreamFromString("00 0E 44 0033 1045 02 02 EB");

		try {
			segment.readFromStream(stream);
			fail("Didn't throw an exception when it should have");
		} catch (Exception e) {
			assertTrue(e instanceof InvalidJpegFormat);
		}
	}
}