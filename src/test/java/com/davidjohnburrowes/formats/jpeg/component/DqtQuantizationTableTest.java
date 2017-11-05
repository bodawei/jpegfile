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

package com.davidjohnburrowes.formats.jpeg.component;

import com.davidjohnburrowes.format.jpeg.component.DqtQuantizationTable;
import com.davidjohnburrowes.format.jpeg.support.FrameMode;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.formats.jpeg.test.ArrayElementAccessor;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import com.davidjohnburrowes.util.Size;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DqtQuantizationTableTest {

	private static String ONE_BYTE_GOOD = "02" +
				" 01 02 03 04 05 06 07 08 09" +
				" 0A 0B 0C 0D 0E 0f 10 11 12 13" +
				" 14 15 16 17 18 19 1A 1B 1C 1D" +
				" 1E 1F 20 21 22 23 24 25 26 27" +
				" 28 29 2A 2B 2C 2D 2E 2F 30 31" +
				" 32 33 34 35 36 37 38 39 3A 3B" +
				" 3C 3D 3E 3F 40";

	private static String ONE_BYTE_IFFY = "05" +
				" 00 01 02 03 04 05 06 07 08 09" +
				" 0A 0B 0C 0D 0E 0f 10 11 12 13" +
				" 14 15 16 17 18 19 1A 1B 1C 1D" +
				" 1E 1F 20 21 22 23 24 25 26 27" +
				" 28 29 2A 2B 2C 2D 2E 2F 30 31" +
				" 32 33 34 35 36 37 38 39 3A 3B" +
				" 3C 3D 3E 3F";

	private static String TWO_BYTE_GOOD = "12" +
				" 0A00 0A01 0A02 0A03 0A04 0A05 0A06 0A07 0A08 0A09" +
				" 0A0A 0A0B 0A0C 0A0D 0A0E 0A0f 0A10 0A11 0A12 0A13" +
				" 0A14 0A15 0A16 0A17 0A18 0A19 0A1A 0A1B 0A1C 0A1D" +
				" 0A1E 0A1F 0A20 0A21 0A22 0A23 0A24 0A25 0A26 0A27" +
				" 0A28 0A29 0A2A 0A2B 0A2C 0A2D 0A2E 0A2F 0A30 0A31" +
				" 0A32 0A33 0A34 0A35 0A36 0A37 0A38 0A39 0A3A 0A3B" +
				" 0A3C 0A3D 0A3E 0A3F";

	private static String TOO_FEW = "00 " +
				"00 01 02 03 04 05 06 07 08 09 " +
				"10 11 12 13 14 15 16 17 18 19 " +
				"20 21 22 23 24 25 26 27 28 29 " +
				"30 31 32 33 34 35 36 37 38 39 " +
				"40 41 42 43 44 45 46 47 48 49 " +
				"50 51 52 53 54 55 56 57 58 59 " +
				"60 61";


	private TestUtils utils;
	private DqtQuantizationTable table;

	@Before
    public void setUp() {
		utils = new TestUtils();
		table = new DqtQuantizationTable();
    }

	@Test
	public void setElementPrecision_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(table, "elementPrecision", Size.NIBBLE,
				  0, 0,
				  0, 1,
				  0, 1,
				  0, Size.NIBBLE.getMax()));
	}

	@Test
	public void setElementPrecision_withNullValue_Accepted() {
		table.setElementPrecision(null);
	}

	@Test
	public void setId_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(table, "tableId", Size.NIBBLE,
				  0, 3,
				  0, 3,
				  0, 3,
				  0, Size.NIBBLE.getMax()));
	}

	@Test
	public void setElementWithOneBytePrecision_honorsBounds() {
		table.setElementPrecision(0);
		ArrayElementAccessor ac = new ArrayElementAccessor(table, "element");
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(ac, table, Size.BYTE,
				  1, 255,
				  1, 255,
				  1, 255,
				  0, 255));
	}

	@Test
	public void setElementWithNullPrecision_honorsBounds() {
		ArrayElementAccessor ac = new ArrayElementAccessor(table, "element");
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(ac, table, Size.SHORT,
				  1, 255,
				  1, 65535,
				  1, 65535,
				  0, 65535));
	}

	@Test
	public void read_1ByteEntries_Works() throws IOException {
		InputStream stream = utils.makeInputStream(ONE_BYTE_IFFY);
		DataInputStream diStream = new DataInputStream(stream);
		DqtQuantizationTable answer = new DqtQuantizationTable();

		answer.setTableId(5);
		for (int index = 0; index < 64; index++) {

			answer.setElement(index, index);
		}
		table.readParameters(diStream);

		Assert.assertEquals(answer, table);
	}

	@Test(expected=InvalidJpegFormat.class)
	public void read_1ByteEntriesWithBadPrecision_Fails() throws IOException {
		table.setFrameMode(FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT);
		InputStream stream = utils.makeInputStream(ONE_BYTE_IFFY);
		DataInputStream diStream = new DataInputStream(stream);

		table.readParameters(diStream);
	}

	@Test
	public void read_1ByteEntriesMatchingExtended_ReadInSuccessfully() throws IOException {
		table.setFrameMode(FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT);
		InputStream stream = utils.makeInputStream(ONE_BYTE_GOOD);
		DataInputStream diStream = new DataInputStream(stream);
		DqtQuantizationTable answer = new DqtQuantizationTable();

		answer.setElementPrecision(0);
		answer.setTableId(2);
		for (int index = 0; index < 64; index++) {

			answer.setElement(index, index+1);
		}
		table.readParameters(diStream);

		Assert.assertEquals(answer, table);
	}

	@Test
	public void read_2ByteEntries_Works() throws IOException {
		table.setFrameMode(FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT);
		InputStream stream = utils.makeInputStream(TWO_BYTE_GOOD);
		DataInputStream diStream = new DataInputStream(stream);
		DqtQuantizationTable answer = new DqtQuantizationTable();

		answer.setTableId(2);
		for (int index = 0; index < 64; index++) {

			answer.setElement(index, 2560 + index);
		}
		table.readParameters(diStream);

		Assert.assertEquals(answer, table);
	}

	@Test(expected=EOFException.class)
	public void read_TooFewEntries_Exceptions() throws IOException {
		InputStream stream = utils.makeInputStream(TOO_FEW);
		DataInputStream diStream = new DataInputStream(stream);

		table.readParameters(diStream);
	}

	@Test
	public void write_InstancePopulatedWithOneByteValues_Works() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		table.setTableId(5);
		for (int index = 0; index < 64; index++) {
			table.setElement(index, index);
		}
		table.write(new DataOutputStream(output));

		Assert.assertArrayEquals(utils.makeByteArray(ONE_BYTE_IFFY), output.toByteArray());
	}

	@Test
	public void write_InstancePopulatedWithTwoByteValue_Works() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] answer = utils.makeByteArray("15 " +
				" 0000 0001 0002 0003 0004 0005 0006 0007 0008 0009" +
				" 000A 000B 000C 000D 000E 000f 0010 0011 0012 0013" +
				" 0014 0015 0016 0017 0018 0019 001A 001B 001C 001D" +
				" 001E 001F 0020 0021 0022 0023 0024 0025 0026 0027" +
				" 0028 0029 002A 002B 002C 002D 002E 002F 0030 0031" +
				" 0032 0033 0034 0035 0036 0037 0038 0039 003A 003B" +
				" 003C 003D 003E 0100");

		table.setTableId(5);
		for (int index = 0; index < 64; index++) {
			table.setElement(index, index);
		}
		table.setElement(63, 256);

		table.write(new DataOutputStream(output));

		Assert.assertArrayEquals(answer, output.toByteArray());
	}

	@Test
	public void write_InstanceReadIn_PreservesAllData() throws IOException {
		InputStream stream = utils.makeInputStream(TWO_BYTE_GOOD);
		DataInputStream diStream = new DataInputStream(stream);
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		table.readParameters(diStream);

		table.write(new DataOutputStream(output));

		Assert.assertArrayEquals(utils.makeByteArray(TWO_BYTE_GOOD), output.toByteArray());
	}

	@Test
	public void equals_SeparateButEqual_ReturnsTrue() {
		DqtQuantizationTable one = new DqtQuantizationTable();
		DqtQuantizationTable two = new DqtQuantizationTable();
		one.setTableId(5);
		two.setTableId(5);
		one.setElement(6, 87);
		two.setElement(6, 87);
		Assert.assertEquals(one, two);
	}

	@Test
	public void equals_SeparateButDifferent_ReturnsFalse() {
		DqtQuantizationTable one = new DqtQuantizationTable();
		DqtQuantizationTable two = new DqtQuantizationTable();
		one.setTableId(5);
		two.setTableId(5);
		one.setElement(6, 87);
		two.setElement(6, 88);
		Assert.assertFalse(one.equals(two));
	}

	@Test
	public void equals_NonTable_ReturnsFalse() {
		Assert.assertFalse(table.equals(new Object()));
	}
}