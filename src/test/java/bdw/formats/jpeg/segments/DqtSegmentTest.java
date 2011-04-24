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

import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.TestUtils;
import java.io.ByteArrayOutputStream;
import bdw.formats.jpeg.segments.support.DqtQuantizationTable;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DqtSegmentTest {

	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
	}

	@Test
	public void getMarker_returnsRightDefault() {
		assertEquals(DqtSegment.MARKER, new DqtSegment().getMarker());
	}

	@Test
	public void insertTable_atNegPosition_returnsException() {
		try {
			DqtSegment segment = new DqtSegment();
			segment.insertTable(-1, new DqtQuantizationTable());
		} catch (Exception e) {
			assertTrue(e instanceof IndexOutOfBoundsException);
		}
	}

	@Test
	public void insertTable_atOutOfRangePosition_returnsException() {
		try {
			DqtSegment segment = new DqtSegment();
			segment.insertTable(2, new DqtQuantizationTable());
		} catch (Exception e) {
			assertTrue(e instanceof IndexOutOfBoundsException);
		}
	}

	@Test
	public void insertTable_atLegitPosition_works() {
		DqtSegment segment = new DqtSegment();
		DqtQuantizationTable table = new DqtQuantizationTable();

		segment.insertTable(0, table);

		assertEquals("Number of tables", 1, segment.getTableCount());
		assertEquals("Table is as expected", table, segment.getTable(0));
	}

	@Test
	public void read_OneTable_PopulatesAllAsExpected() throws IOException, InvalidJpegFormat {
		DqtSegment segment = new DqtSegment(utils.makeInputStreamFromString("00 43" +
				"05" +
				" 00 01 02 03 04 05 06 07 08 09" +
				" 0A 0B 0C 0D 0E 0f 10 11 12 13" +
				" 14 15 16 17 18 19 1A 1B 1C 1D" +
				" 1E 1F 20 21 22 23 24 25 26 27" +
				" 28 29 2A 2B 2C 2D 2E 2F 30 31" +
				" 32 33 34 35 36 37 38 39 3A 3B" +
				" 3C 3D 3E 3F"));
		DqtSegment answer = new DqtSegment();
		DqtQuantizationTable table = new DqtQuantizationTable();
		table.setId(5);
		for (int index = 0; index < 64; index++) {
			table.setEntry(index, index);
		}
		answer.insertTable(0, table);

		assertEquals("Segment read in ok", answer, segment);
	}

	@Test
	public void read_BadLength_ThrowsException() throws IOException, InvalidJpegFormat  {
		try {
			DqtSegment segment = new DqtSegment(utils.makeInputStreamFromString("00 33" +
					"05" +
					" 00 01 02 03 04 05 06 07 08 09" +
					" 0A 0B 0C 0D 0E 0f 10 11 12 13" +
					" 14 15 16 17 18 19 1A 1B 1C 1D" +
					" 1E 1F 20 21 22 23 24 25 26 27" +
					" 28 29 2A 2B 2C 2D 2E 2F 30 31" +
					" 32 33 34 35 36 37 38 39 3A 3B" +
					" 3C 3D 3E 3F"));
			fail("Should have thrown an exception");
		} catch (Exception e) {
			assertTrue(e instanceof InvalidJpegFormat);
		}
	}

	@Test
	public void write_Instance_GeneratesExpectedOutput() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		DqtSegment segment = new DqtSegment();
		DqtQuantizationTable one = new DqtQuantizationTable();
		DqtQuantizationTable two = new DqtQuantizationTable();

		one.setId(5);
		two.setId(15);
		for (int index = 0; index < 64; index ++) {
			one.setEntry(index, index);
			two.setEntry(index, 2560 + index);
		}
		segment.insertTable(0, one);
		segment.insertTable(1, two);

		segment.write(output);

		assertArrayEquals("Written segment", utils.makeByteArrayFromString("00 C4" +
					"05" +
					" 00 01 02 03 04 05 06 07 08 09" +
					" 0A 0B 0C 0D 0E 0f 10 11 12 13" +
					" 14 15 16 17 18 19 1A 1B 1C 1D" +
					" 1E 1F 20 21 22 23 24 25 26 27" +
					" 28 29 2A 2B 2C 2D 2E 2F 30 31" +
					" 32 33 34 35 36 37 38 39 3A 3B" +
					" 3C 3D 3E 3F" +
					"1F" +
					" 0A00 0A01 0A02 0A03 0A04 0A05 0A06 0A07 0A08 0A09" +
					" 0A0A 0A0B 0A0C 0A0D 0A0E 0A0f 0A10 0A11 0A12 0A13" +
					" 0A14 0A15 0A16 0A17 0A18 0A19 0A1A 0A1B 0A1C 0A1D" +
					" 0A1E 0A1F 0A20 0A21 0A22 0A23 0A24 0A25 0A26 0A27" +
					" 0A28 0A29 0A2A 0A2B 0A2C 0A2D 0A2E 0A2F 0A30 0A31" +
					" 0A32 0A33 0A34 0A35 0A36 0A37 0A38 0A39 0A3A 0A3B" +
					" 0A3C 0A3D 0A3E 0A3F"),
					output.toByteArray());

	}

	@Test
	public void equals_OtherEqualObject_ReturnsTrue() throws IOException {
		DqtSegment segment = new DqtSegment();
		DqtQuantizationTable table = new DqtQuantizationTable();
		table.setId(2);
		table.setEntry(5, 99);
		segment.insertTable(0, table);

		DqtSegment otherSegment = new DqtSegment();
		DqtQuantizationTable otherTable = new DqtQuantizationTable();
		otherTable.setId(2);
		otherTable.setEntry(5, 99);

		otherSegment.insertTable(0, otherTable);

		assertTrue("Segments Equal", segment.equals(otherSegment));
	}

	@Test
	public void equals_OtherWithDifferentTable_ReturnsFalse() throws IOException {
		DqtSegment segment = new DqtSegment();
		DqtSegment otherSegment = new DqtSegment();
		DqtQuantizationTable table = new DqtQuantizationTable();
		table.setId(2);
		otherSegment.insertTable(0, new DqtQuantizationTable());

		assertFalse(segment.equals(otherSegment));
	}

	@Test
	public void equals_OtherObject_ReturnsFalse() throws IOException {
		assertFalse(new DqtSegment().equals(new Object()));
	}
}
