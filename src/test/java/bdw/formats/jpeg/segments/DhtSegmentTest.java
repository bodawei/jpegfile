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

import java.io.EOFException;
import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.segments.support.DhtRunLengthHeader;
import bdw.formats.jpeg.segments.support.DhtHuffmanTable;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import bdw.formats.jpeg.TestUtils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

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
		sampleTable1.addHeader(new DhtRunLengthHeader(0x00, 5, 1, 2));
		sampleTable1String = "00" +
				"00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00" +
				"12";

		sampleTable2 = new DhtHuffmanTable();
		sampleTable2.setId(1);
		sampleTable2.setAc(true);
		sampleTable2.addHeader(new DhtRunLengthHeader(0x00, 4, 3, 4));
		sampleTable2String = "11" +
				"00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00" +
				"34";
	}

	@Test
	public void segmentHasExpectedMarker() {
		assertEquals("marker", DhtSegment.MARKER, segment.getMarker());
	}

	@Test
	public void canWriteEmpySegment() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals("written form", utils.makeByteArrayFromString("00 02"),
				output.toByteArray());
	}

	@Test
	public void canWriteSegmentWithTwoTables() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.insertTable(0, this.sampleTable1);
		segment.insertTable(0, this.sampleTable2);

		segment.write(output);

		assertArrayEquals("written form",
				utils.makeByteArrayFromString("00 26" +
				sampleTable2String +
				sampleTable1String
				),
				output.toByteArray());
	}


	@Test
	public void canReadSegmentWithTwoTables() throws IOException, InvalidJpegFormat {
		segment.readFromStream(utils.makeInputStreamFromString("00 26" +
				sampleTable1String +
				sampleTable2String));

		DhtSegment expected = new DhtSegment();
		expected.insertTable(0, this.sampleTable1);
		expected.insertTable(1, this.sampleTable2);

		assertEquals("read segment", expected, segment);
	}

	@Test(expected=EOFException.class)
	public void incompleteDataGivesExeption() throws IOException, InvalidJpegFormat {
		segment.readFromStream(utils.makeInputStreamFromString("00 26" +
				sampleTable1String +
				"00 01"));

	}
}