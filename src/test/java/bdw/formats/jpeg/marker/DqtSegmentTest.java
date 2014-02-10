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

import bdw.format.jpeg.component.DqtQuantizationTable;
import bdw.format.jpeg.marker.DqtSegment;
import bdw.format.jpeg.support.DataMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.formats.jpeg.test.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DqtSegmentTest {

	private TestUtils utils;
	private DqtSegment segment;

	@Before
	public void setUp() {
		utils = new TestUtils();
		segment = new DqtSegment();
	}

	@Test
	public void getMarker_returnsRightDefault() {
		assertEquals(DqtSegment.MARKERID, new DqtSegment().getMarkerId());
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void insertTable_atNegPosition_returnsException() {
		segment.insertTable(-1, new DqtQuantizationTable());
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void insertTable_atOutOfRangePosition_returnsException() {
		segment.insertTable(2, new DqtQuantizationTable());
	}

	@Test
	public void insertTable_atLegitPosition_works() throws InvalidJpegFormat {
		DqtQuantizationTable table = new DqtQuantizationTable();

		segment.insertTable(0, table);

		assertEquals("Number of tables", 1, segment.getTableCount());
		assertEquals("Table is as expected", table, segment.getTable(0));
	}

	@Test
	public void read_OneTable_PopulatesAllAsExpected() throws IOException {
		segment.read(utils.makeInputStream("00 43" +
				"03" +
				" 01 02 03 04 05 06 07 08 09" +
				" 0A 0B 0C 0D 0E 0f 10 11 12 13" +
				" 14 15 16 17 18 19 1A 1B 1C 1D" +
				" 1E 1F 20 21 22 23 24 25 26 27" +
				" 28 29 2A 2B 2C 2D 2E 2F 30 31" +
				" 32 33 34 35 36 37 38 39 3A 3B" +
				" 3C 3D 3E 3F 40"));
		DqtSegment answer = new DqtSegment();
		DqtQuantizationTable table = new DqtQuantizationTable();
		table.setTableId(3);
		for (int index = 0; index < 64; index++) {
			table.setElement(index, index+1);
		}
		answer.insertTable(0, table);

		assertEquals("Segment read in ok", answer, segment);
	}

	@Test(expected=InvalidJpegFormat.class)
	public void read_BadLength_ThrowsException() throws IOException  {
		segment.read(utils.makeInputStream("00 33" +
				"03" +
				" 00 01 02 03 04 05 06 07 08 09" +
				" 0A 0B 0C 0D 0E 0f 10 11 12 13" +
				" 14 15 16 17 18 19 1A 1B 1C 1D" +
				" 1E 1F 20 21 22 23 24 25 26 27" +
				" 28 29 2A 2B 2C 2D 2E 2F 30 31" +
				" 32 33 34 35 36 37 38 39 3A 3B" +
				" 3C 3D 3E 3F"));
	}


	@Test
	public void readLax_WhenSizeWrong_readsOneTableAndBuffersRemainder() throws IOException  {
		segment.setDataMode(DataMode.LAX);
		segment.read(utils.makeInputStream("00 50" +
				"03" +
				" 00 01 02 03 04 05 06 07 08 09" +
				" 0A 0B 0C 0D 0E 0f 10 11 12 13" +
				" 14 15 16 17 18 19 1A 1B 1C 1D" +
				" 1E 1F 20 21 22 23 24 25 26 27" +
				" 28 29 2A 2B 2C 2D 2E 2F 30 31" +
				" 32 33 34 35 36 37 38 39 3A 3B" +
				" 3C 3D 3E 3F 00 41 42 43 44 45" +
				" 46 47 48 49 4A 4B 4C 4D 4E 4F"));

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		segment.write(output);

		assertArrayEquals("Written segment", utils.makeByteArray("FFDB 00 50" +
					"03" +
					" 00 01 02 03 04 05 06 07 08 09" +
					" 0A 0B 0C 0D 0E 0f 10 11 12 13" +
					" 14 15 16 17 18 19 1A 1B 1C 1D" +
					" 1E 1F 20 21 22 23 24 25 26 27" +
					" 28 29 2A 2B 2C 2D 2E 2F 30 31" +
					" 32 33 34 35 36 37 38 39 3A 3B" +
					" 3C 3D 3E 3F" +
				   " 00 41 42 43 44 45" +
					" 46 47 48 49 4A 4B 4C"),
					output.toByteArray());

		segment.clearPassthrough();
		output = new ByteArrayOutputStream();
		segment.write(output);

		assertArrayEquals("Written segment", utils.makeByteArray("FFDB 00 43" +
					"03" +
					" 00 01 02 03 04 05 06 07 08 09" +
					" 0A 0B 0C 0D 0E 0f 10 11 12 13" +
					" 14 15 16 17 18 19 1A 1B 1C 1D" +
					" 1E 1F 20 21 22 23 24 25 26 27" +
					" 28 29 2A 2B 2C 2D 2E 2F 30 31" +
					" 32 33 34 35 36 37 38 39 3A 3B" +
					" 3C 3D 3E 3F"),
					output.toByteArray());
	}

	@Test
	public void write_instance_generatesExpectedOutput() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		segment.setDataMode(DataMode.LAX);
		DqtQuantizationTable one = new DqtQuantizationTable();
		DqtQuantizationTable two = new DqtQuantizationTable();
		one.setDataMode(DataMode.LAX);
		two.setDataMode(DataMode.LAX);

		one.setTableId(5);
		two.setTableId(15);
		for (int index = 0; index < 64; index ++) {
			one.setElement(index, index);
			two.setElement(index, 2560 + index);
		}
		segment.insertTable(0, one);
		segment.insertTable(1, two);

		segment.write(output);

		assertArrayEquals("Written segment", utils.makeByteArray("FFDB 00 C4" +
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
		DqtQuantizationTable table = new DqtQuantizationTable();
		table.setTableId(2);
		table.setElement(5, 99);
		segment.insertTable(0, table);

		DqtSegment otherSegment = new DqtSegment();
		DqtQuantizationTable otherTable = new DqtQuantizationTable();
		otherTable.setTableId(2);
		otherTable.setElement(5, 99);

		otherSegment.insertTable(0, otherTable);

		assertTrue("Segments Equal", segment.equals(otherSegment));
	}

	@Test
	public void equals_OtherWithDifferentTable_ReturnsFalse() throws IOException {
		DqtSegment otherSegment = new DqtSegment();
		DqtQuantizationTable table = new DqtQuantizationTable();
		table.setTableId(2);
		otherSegment.insertTable(0, new DqtQuantizationTable());

		assertFalse(segment.equals(otherSegment));
	}

	@Test
	public void equals_OtherObject_ReturnsFalse() throws IOException {
		assertFalse(new DqtSegment().equals(new Object()));
	}
}
