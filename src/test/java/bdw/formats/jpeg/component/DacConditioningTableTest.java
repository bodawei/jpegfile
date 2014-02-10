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
package bdw.formats.jpeg.component;

import bdw.format.jpeg.component.DacConditioningTable;
import bdw.format.jpeg.support.FrameMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.formats.jpeg.test.TestUtils;
import bdw.util.Size;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DacConditioningTableTest {
	private DacConditioningTable table;
	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
		table = new DacConditioningTable();
		table.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
	}

	@Test
	public void setTableClass_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(table, "tableClass", Size.NIBBLE,
				  0, 15,
				  0, 1,
				  0, 1,
				  0, 0));
	}

	@Test
	public void setTableId_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(table, "tableId", Size.NIBBLE,
				  0, 15,
				  0, 3,
				  0, 3,
				  0, 3));
	}

	@Test
	public void setTableValue_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(table, "tableValue", Size.BYTE,
				  0, 255,
				  0, 255,
				  0, 255,
				  0, 255));
	}

	@Test(expected=IllegalArgumentException.class)
	public void setTableClass_tableClass1andTableValue64Extended_throwsException() {
		table.setFrameMode(FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT);
		table.setTableClass(0);
		table.setTableValue(64);
		table.setTableClass(1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void setTableClass_tableClass1andTableValue0Extended_throwsException() {
		table.setFrameMode(FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT);
		table.setTableClass(0);
		table.setTableValue(0);
		table.setTableClass(1);
	}

	@Test
	public void setTableClass_tableClass1andTableValue63Extended_throwsException() {
		table.setFrameMode(FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT);
		table.setTableClass(0);
		table.setTableValue(63);
		table.setTableClass(1);

		assertEquals(1, table.getTableClass());
	}

	@Test(expected=IllegalArgumentException.class)
	public void setTableClass_tableClass1andTableValue64PRogressive_throwsException() {
		table.setFrameMode(FrameMode.HUFF_PROGRESSIVE_DCT);
		table.setTableClass(0);
		table.setTableValue(64);
		table.setTableClass(1);
	}

	@Test
	public void setTableClass_tableClass1andTableValue63Progressive_throwsException() {
		table.setFrameMode(FrameMode.HUFF_PROGRESSIVE_DCT);
		table.setTableClass(0);
		table.setTableValue(63);
		table.setTableClass(1);

		assertEquals(1, table.getTableClass());
	}

	@Test(expected=IllegalArgumentException.class)
	public void setTableValue_tableClass1andTableValue64Extended_throwsException() {
		table.setFrameMode(FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT);
		table.setTableClass(1);
		table.setTableValue(64);
	}

	@Test
	public void setTableValue_tableClass1andTableValue63Extended_throwsNoException() {
		table.setFrameMode(FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT);
		table.setTableClass(1);
		table.setTableValue(63);

		assertEquals(63, table.getTableValue());
	}

	@Test(expected=IllegalArgumentException.class)
	public void setTableValue_tableClass1andTableValue64PRogressive_throwsException() {
		table.setFrameMode(FrameMode.HUFF_PROGRESSIVE_DCT);
		table.setTableClass(1);
		table.setTableValue(64);
	}

	@Test
	public void setTableValue_tableClass1andTableValue63Progressive_throwsNoException() {
		table.setFrameMode(FrameMode.HUFF_PROGRESSIVE_DCT);
		table.setTableClass(1);
		table.setTableValue(63);

		assertEquals(63, table.getTableValue());
	}

	@Test
	public void read_goodInput_readSuccessfully() throws IOException {
		InputStream stream = utils.makeInputStream("00 FF");

		table.readParameters(new DataInputStream(stream));

		DacConditioningTable expected = new DacConditioningTable();
		expected.setTableValue(255);

		assertTrue(expected.equals(table));
	}

	@Test(expected=InvalidJpegFormat.class)
	public void read_badInput_throwsException() throws IOException {
		InputStream stream = utils.makeInputStream("FF FF");
		table.setFrameMode(FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT);

		table.readParameters(new DataInputStream(stream));
	}

	@Test
	public void write_exampleInstance_generatesGoodDataStream() throws IOException {
		DacConditioningTable aTable = new DacConditioningTable();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		aTable.setTableClass(1);
		aTable.setTableId(2);
		aTable.setTableValue(3);

		aTable.write(new DataOutputStream(output));

		byte[] expectedBytes = utils.makeByteArray("12 03");

		assertArrayEquals(expectedBytes, output.toByteArray());
	}

	@Test
	public void getSizeOnDisk_isTwo() {
		assertEquals(2, table.getSizeOnDisk());
	}

	@Test
	public void equals_withSelf_isTrue() {
		assertTrue(table.equals(table));
	}

	@Test
	public void equals_withObject_isFalse() {
		assertFalse(table.equals(new Object()));
	}

	@Test
	public void equals_withDifferentTable_isFalse() {
		table.setTableValue(20);
		assertFalse(table.equals(new DacConditioningTable()));
	}

	@Test(expected=IllegalArgumentException.class)
	public void setFrameMode_toInvalidState_throwsException() throws IOException {
		table.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
		table.setTableClass(1);
		table.setTableValue(255);

		table.setFrameMode(FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT);
	}

	@Test
	public void setFrameMode_toValidState_setsCorrectly() throws IOException {
		table.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
		table.setTableClass(1);
		table.setTableValue(60);

		table.setFrameMode(FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT);

		assertEquals(FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT, table.getFrameMode());
	}
}
