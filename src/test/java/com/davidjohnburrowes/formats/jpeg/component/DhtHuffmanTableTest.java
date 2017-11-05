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

import com.davidjohnburrowes.format.jpeg.component.DhtHuffmanTable;
import com.davidjohnburrowes.format.jpeg.support.DataMode;
import com.davidjohnburrowes.format.jpeg.support.FrameMode;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import com.davidjohnburrowes.util.Size;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DhtHuffmanTableTest {

	protected TestUtils utils;
	protected DhtHuffmanTable table;

	@Before
	public void setUp() {
		utils = new TestUtils();
		table = new DhtHuffmanTable();
		table.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
	}

	private final String SAMPLE_BYTES  = "11 "
				+ "01 01 00 04 00 00 00 00 00 00 00 00 00 00 00 00"
				+ "0A   5f   11 22 00 FF";

	private DhtHuffmanTable buildSampleTable() {
		DhtHuffmanTable newTable = new DhtHuffmanTable();
		newTable.setTableClass(1);
		newTable.setTableId(1);
		newTable.setElement(0, new short[] {0x0a});
		newTable.setElement(1, new short[] {0x5f});
		newTable.setElement(3, new short[] {0x11, 0x22, 0, 255});

		return newTable;
	}

	@Test
	public void setTableClass_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(table, "tableClass", Size.NIBBLE,
				  0, 1,
				  0, 1,
				  0, 1,
				  0, 0));
	}

	@Test
	public void setTableId_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(table, "tableId", Size.NIBBLE,
				  0, 1,
				  0, 3,
				  0, 3,
				  0, 3));
	}

	@Test(expected=IllegalArgumentException.class)
	public void setElement_withAnElementsMembersTooLarge_throwsException() throws IOException {
		short[] element = new short[1];
		element[0] = 256;
		table.setElement(16, element);
	}

	@Test(expected=IllegalArgumentException.class)
	public void setElement_withAnElementsMembersTooSmall_throwsException() throws IOException {
		short[] element = new short[1];
		element[0] = -1;
		table.setElement(16, element);
	}

	@Test(expected=IllegalArgumentException.class)
	public void setElement_withTooLArgeAnIndex_throwsException() throws IOException {
		table.setElement(16, new short[0]);
	}

	@Test(expected=IllegalArgumentException.class)
	public void setElement_withNullElement_throwsException() throws IOException {
		table.setElement(2, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void setElement_withLongElement_throwsException() throws IOException {
		table.setElement(2, new short[257]);
	}

	@Test
	public void read_goodData_producedExpectedInstance() throws IOException {
		InputStream stream = utils.makeInputStream(SAMPLE_BYTES);
		DataInputStream diStream = new DataInputStream(stream);

		table.readParameters(diStream);

		DhtHuffmanTable expected = buildSampleTable();

		assertTrue(expected.equals(table));
	}

	@Test(expected=InvalidJpegFormat.class)
	public void readStrict_strangeAc_throwsException() throws IOException {
		InputStream stream = utils.makeInputStream("33 "
				+ "01 01 00 04 00 00 00 00 00 00 00 00 00 00 00 00"
				+ "0A   5f   11 22 33 44");
		DataInputStream diStream = new DataInputStream(stream);
		table.setFrameMode(FrameMode.AC_LOSSLESS);

		table.readParameters(diStream);
	}

	@Test
	public void readLax_strangeAc_parsesOK() throws IOException {
		InputStream stream = utils.makeInputStream("33 "
				+ "01 01 00 04 00 00 00 00 00 00 00 00 00 00 00 00"
				+ "0A   5f   11 22 00 FF");
		DataInputStream diStream = new DataInputStream(stream);
		table.setFrameMode(FrameMode.AC_LOSSLESS);
		table.setDataMode(DataMode.LAX);
		table.readParameters(diStream);

		DhtHuffmanTable expected = buildSampleTable();
		expected.setDataMode(DataMode.LAX);
		expected.setTableClass(3);
		expected.setTableId(3);

		assertTrue(expected.equals(table));
	}

	@Test(expected=EOFException.class)
	public void read_shortData_throwsException() throws IOException {
		InputStream stream = utils.makeInputStream("11 "
				+ "01 01 00 04 00 00 00 00 00 00 00 00 00 00 00 00"
				+ "0A   5f   11 22 33");
		DataInputStream diStream = new DataInputStream(stream);

		table.readParameters(diStream);
	}

	@Test
	public void write_exampleInstance_generatesGoodDataStream() throws IOException {
		DhtHuffmanTable aTable = buildSampleTable();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		aTable.write(new DataOutputStream(output));

		byte[] expectedBytes = utils.makeByteArray(SAMPLE_BYTES);

		assertArrayEquals("streamed data", expectedBytes, output.toByteArray());
	}

	@Test
	public void getSizeOnDisk_exampleInstance_hasExpectedSize() throws IOException {
		DhtHuffmanTable aTable = buildSampleTable();

		assertEquals("size on disk", 23, aTable.getSizeOnDisk());
	}

	@Test
	public void getSizeOnDisk_emptyIntance_hasExpectedSize() throws IOException {
		DhtHuffmanTable aTable = new DhtHuffmanTable();

		assertEquals("size on disk", 17, aTable.getSizeOnDisk());
	}

	@Test(expected=InvalidJpegFormat.class)
	public void setFrameMode_withIncompatibleData_throwsException() throws IOException {
		DhtHuffmanTable aTable = new DhtHuffmanTable();
		aTable.setDataMode(DataMode.LAX);
		aTable.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
		aTable.setTableId(15);
		aTable.setDataMode(DataMode.STRICT);
	}
}
