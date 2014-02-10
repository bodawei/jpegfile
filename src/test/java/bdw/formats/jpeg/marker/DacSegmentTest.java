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

import bdw.format.jpeg.component.DacConditioningTable;
import bdw.format.jpeg.marker.DacSegment;
import bdw.format.jpeg.support.DataMode;
import bdw.formats.jpeg.test.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DacSegmentTest {

	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
	}

	@Test
	public void getMarker_returnsRightDefault() {
		assertEquals(DacSegment.MARKERID, new DacSegment().getMarkerId());
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void insertTable_atNegPosition_returnsException() {
		DacSegment segment = new DacSegment();
		segment.insertTable(-1, new DacConditioningTable());
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void insertTable_atOutOfRangePosition_returnsException() {
		DacSegment segment = new DacSegment();
		segment.insertTable(2, new DacConditioningTable());
	}

	@Test
	public void insertTable_atLegitPosition_works() {
		DacSegment segment = new DacSegment();
		DacConditioningTable table = new DacConditioningTable();

		segment.insertTable(0, table);

		assertEquals("Number of tables", 1, segment.getTableCount());
		assertEquals("Table is as expected", table, segment.getTable(0));
	}

	@Test
	public void read_oneTable_populatesAllAsExpected() throws IOException {
		DacSegment segment = new DacSegment();
		segment.read(utils.makeInputStream("00 04 02 FF"));

		DacSegment answer = new DacSegment();
		DacConditioningTable table = new DacConditioningTable();
		table.setTableId(2);
		table.setTableValue(255);
		answer.insertTable(0, table);

		assertEquals(answer, segment);
	}

	@Test(expected=EOFException.class)
	public void read_badLength_ThrowsException() throws IOException  {
		DacSegment segment = new DacSegment();
		segment.read(utils.makeInputStream("00 05 02 FF"));
	}


	@Test
	public void readLax_whenSizeWrong_readsOneTableAndBuffersRemainder() throws IOException  {
		DacSegment segment = new DacSegment();
		segment.setDataMode(DataMode.LAX);
		segment.read(utils.makeInputStream("00 05 02 FF A0"));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		segment.write(output);

		assertArrayEquals(utils.makeByteArray("FFCC 00 05 02 FF A0"),
					output.toByteArray());

		segment.clearPassthrough();
		output = new ByteArrayOutputStream();
		segment.write(output);

		assertArrayEquals("Written segment", utils.makeByteArray("FFCC 00 04 02 FF"),
					output.toByteArray());
	}

	@Test
	public void write_instance_generatesExpectedOutput() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		DacSegment segment = new DacSegment();
		segment.setDataMode(DataMode.LAX);
		DacConditioningTable one = new DacConditioningTable();
		DacConditioningTable two = new DacConditioningTable();
		one.setDataMode(DataMode.LAX);
		two.setDataMode(DataMode.LAX);

		one.setTableId(5);
		two.setTableId(15);
		segment.insertTable(0, one);
		segment.insertTable(1, two);

		segment.write(output);

		assertArrayEquals(utils.makeByteArray("FFCC 00 06    05 01  0F 01"),
					output.toByteArray());
	}

	@Test
	public void equals_OtherEqualObject_ReturnsTrue() throws IOException {
		DacSegment segment = new DacSegment();
		DacConditioningTable table = new DacConditioningTable();
		table.setTableId(2);
		segment.insertTable(0, table);

		DacSegment otherSegment = new DacSegment();
		DacConditioningTable otherTable = new DacConditioningTable();
		otherTable.setTableId(2);
		otherSegment.insertTable(0, otherTable);

		assertTrue(segment.equals(otherSegment));
	}

	@Test
	public void equals_OtherWithDifferentTable_ReturnsFalse() throws IOException {
		DacSegment segment = new DacSegment();
		DacSegment otherSegment = new DacSegment();
		DacConditioningTable table = new DacConditioningTable();
		table.setTableId(2);
		otherSegment.insertTable(0, new DacConditioningTable());

		assertFalse(segment.equals(otherSegment));
	}

	@Test
	public void equals_OtherObject_ReturnsFalse() throws IOException {
		assertFalse(new DacSegment().equals(new Object()));
	}

	@Test
	public void getSegmentSizeOnDisk_returnsCorrectValue() throws IOException {
		DacSegment segment = new DacSegment();
		segment.addTable(new DacConditioningTable());

		assertEquals(4, segment.getParameterSizeOnDisk());
	}
}
