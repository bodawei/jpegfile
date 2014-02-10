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

import bdw.format.jpeg.component.DhtHuffmanTable;
import bdw.format.jpeg.marker.DhtSegment;
import bdw.format.jpeg.support.DataMode;
import bdw.format.jpeg.support.FrameMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.formats.jpeg.test.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DhtSegmentTest {

	protected TestUtils utils;
	protected DhtSegment segment;
	protected DhtHuffmanTable sampleTable1;
	protected DhtHuffmanTable sampleTable2;
	protected String sampleTable1String;
	protected String sampleTable2String;

	@Before
	public void setUp() {
		utils = new TestUtils();
		segment = new DhtSegment();
		sampleTable1 = new DhtHuffmanTable();
		sampleTable1.setElement(4, new short[] {0x12});
		sampleTable1String = "00" +
				"00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00" +
				"12";

		sampleTable2 = new DhtHuffmanTable();
		sampleTable2.setTableId(1);
		sampleTable2.setTableClass(1);
		sampleTable2.setElement(3, new short[] {0x34});
		sampleTable2String = "11" +
				"00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00" +
				"34";
	}

	@Test
	public void getMarkerId_byDefault_isCorrect() {
		assertEquals("marker", DhtSegment.MARKERID, segment.getMarkerId());
	}

	@Test
	public void getSegmentSizeOnDisk_initially_isCorrect() {
		assertEquals(2, segment.getParameterSizeOnDisk());
	}

	@Test
	public void getSegmentSizeOnDisk_withAddedData_returnsExpectedValue() throws IOException {
		segment.read(utils.makeInputStream("00 26" +
				sampleTable1String +
				sampleTable2String));

		assertEquals(38, segment.getParameterSizeOnDisk());
	}

	@Test
	public void getTableCount_initially_isZero() throws IOException {
		assertEquals(0, segment.getTableCount());
	}

	@Test
	public void getTableCount_withTwoSegments_isTwo() throws IOException {
		segment.read(utils.makeInputStream("00 26" +
				sampleTable1String +
				sampleTable2String));

		assertEquals(2, segment.getTableCount());
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void insertTable_withOutOfBoundsIndex_throwsException() throws IOException {
		segment.insertTable(10, sampleTable1);
	}

	@Test
	public void insertTable_withOneEntry_insertsCorrectly() throws IOException {
		segment.insertTable(0, sampleTable1);

		assertEquals(sampleTable1, segment.getTable(0));
	}

	@Test(expected=IllegalArgumentException.class)
	public void addTable_withTooManyTables_throwsException() throws IOException {
		for (int index = 0; index < 4000; index++) {
			segment.addTable(sampleTable1);
		}
	}

	@Test
	public void addTable_withOneEntry_insertsCorrectly() throws IOException {
		segment.addTable(sampleTable1);

		assertEquals(sampleTable1, segment.getTable(0));
	}

	@Test
	public void iterator_returnsTables() throws IOException {
		segment.addTable(sampleTable1);

		Iterator iterator = segment.iterator();

		assertEquals(sampleTable1, iterator.next());
	}

	@Test
	public void validate_initially_returnsNoExceptions() throws IOException {
		assertEquals(0, segment.validate().size());
	}

	@Test
	public void validate_withProblematicTable_returnsExceptions() throws IOException {
		DhtHuffmanTable table = new DhtHuffmanTable();
		table.setDataMode(DataMode.LAX);
		table.setTableId(15); // invalid

		segment.setDataMode(DataMode.LAX);
		segment.setFrameMode(FrameMode.AC_LOSSLESS);
		segment.addTable(table);

		assertEquals(1, segment.validate().size());
	}

	@Test
	public void equals_twoSegments_areEqual() throws IOException {
		assertTrue(segment.equals(new DhtSegment()));
	}

	@Test
	public void equals_twoDifferentSegments_areNotEqual() throws IOException {
		segment.addTable(sampleTable1);
		assertFalse(segment.equals(new DhtSegment()));
	}

	@Test
	public void equals_segmentAndNonSegment_areNotEqual() throws IOException {
		assertFalse(segment.equals(new Object()));
	}

	@Test
	public void read_twoSegmentTable_readsSuccessfully() throws IOException {
		segment.read(utils.makeInputStream("00 26" +
				sampleTable1String +
				sampleTable2String));

		DhtSegment expected = new DhtSegment();
		expected.insertTable(0, this.sampleTable1);
		expected.insertTable(1, this.sampleTable2);

		assertEquals("read segment", expected, segment);
	}

	@Test(expected=EOFException.class)
	public void read_incompleteData_givesExeption() throws IOException {
		segment.read(utils.makeInputStream("00 26" +
				sampleTable1String +
				"00 01"));
	}

	@Test(expected=InvalidJpegFormat.class)
	public void read_withTrailingBytes_rejectedInStrictMode() throws IOException {
		segment.read(utils.makeInputStream("00 28" +
				sampleTable1String +
				sampleTable2String + "AB CD"));
	}

	@Test
	public void read_withTrailingBytes_acceptedInLaxMode() throws IOException {
		DhtSegment newSegment = new DhtSegment();
		newSegment.setDataMode(DataMode.LAX);
		newSegment.read(utils.makeInputStream("00 28" +
				sampleTable1String +
				sampleTable2String + "AB CD"));
		assertEquals((byte)0xAB, newSegment.getTrailingBytes()[0]);
	}


	@Test
	public void write_emptySegment_producesExpectedOutput() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals("written form", utils.makeByteArray("FFC4 00 02"),
				output.toByteArray());
	}

	@Test
	public void write_twoSegmentTables_producesExpectedOutput() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.insertTable(0, this.sampleTable1);
		segment.insertTable(0, this.sampleTable2);

		segment.write(output);

		assertArrayEquals("written form",
				utils.makeByteArray("FFC4 00 26" +
					sampleTable2String +
					sampleTable1String
				),
				output.toByteArray());
	}

	@Test
	public void setValidationMode_toStrictWithStrictInvalidTables_rejectedLeavingModesIntact() throws IOException {
		segment = new DhtSegment();
		segment.setDataMode(DataMode.LAX);
		segment.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
		DhtHuffmanTable table = new DhtHuffmanTable();
		table.setDataMode(DataMode.LAX);
		table.setTableId(15);
		segment.addTable(table);

		try {
			segment.setDataMode(DataMode.STRICT);
			fail("did not throw an exception");
		} catch (InvalidJpegFormat e) {
			assertEquals(DataMode.LAX, segment.getDataMode());
			assertEquals(DataMode.LAX, table.getDataMode());
		}
	}

}