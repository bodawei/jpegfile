/*
 *  Copyright 2014,2017 柏大衛
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.davidjohnburrowes.formats.jpeg.marker;

import com.davidjohnburrowes.format.jpeg.marker.ComSegment;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ComSegmentTest {

	protected ComSegment segment;
	protected TestUtils utils;

	@Before
	public void setUp() {
		segment = new ComSegment();
		utils = new TestUtils();
	}

	@Test
	public void constructor_SetsCorrectMarker() {
		assertEquals(ComSegment.MARKERID, segment.getMarkerId());
	}

	@Test
	public void getComment_EmptyStringByDefault() throws IOException {
		assertEquals("", segment.getStringComment());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setComment_WullValue_Exception() {
		segment.setStringComment(null);
	}

	@Test
	public void setComment_OrdinaryString_IsSet() throws IOException {
		segment.setStringComment("This is a comment");
		assertEquals("This is a comment", segment.getStringComment());
	}

	@Test
	public void read_ZeroLength_OK() throws IOException {
		InputStream stream = utils.makeInputStream("00 02");

		segment.read(stream);

		assertEquals("", segment.getStringComment());
	}

	@Test
	public void read_ZeroLengthWithNull_OK() throws IOException {
		InputStream stream = utils.makeInputStream("00 03 00");

		segment.read(stream);

		assertEquals("\000", segment.getStringComment());
	}

	@Test
	public void read_NullTerminatedString_OK() throws IOException {
		InputStream stream = utils.makeInputStream("00 06 41 42 43 00");

		segment.read(stream);

		assertEquals("ABC\000", segment.getStringComment());
	}

	@Test
	public void read_UnicodeString_OK() throws IOException {
		InputStream stream = utils.makeInputStream("00 16 e9 80 99 e6 98 af e4 b8 80 e5 80 8b 00  63 6f 6d 6d 65 6e 74");

		segment.read(stream);

		assertEquals("這是一個\000comment", segment.getStringComment());
	}

	@Test
	public void read_ISOLatin1String_OK() throws IOException {
		InputStream stream = utils.makeInputStream("00 04 C2 A2");	// ¢

		segment.read(stream);

		assertEquals("¢", segment.getStringComment());
	}

	@Test
	public void read_1025BytesOfComment_OK() throws IOException {
		StringBuilder builder = new StringBuilder();

		builder.append("04 03");
		for (int index = 0; index < 1025; index++) {
			builder.append('4');
			builder.append((char) ('0' + (index % 10)));
		}
		segment.read(utils.makeRandomAccessFile(builder.toString()));

		assertEquals("byteCount", 1025, segment.getStringComment().length());
		assertEquals("lastByte", 'D', segment.getStringComment().charAt(1024));
	}

	@Test
	public void write_ZeroLength_OK() throws IOException {
		InputStream stream = utils.makeInputStream("00 02");
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.read(stream);

		segment.write(output);
		assertArrayEquals(utils.makeByteArray("FFFE 00 02"), output.toByteArray());
	}

	@Test
	public void write_Comment_OK() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.setStringComment("This is a comment");

		segment.write(output);
		assertArrayEquals(utils.makeByteArray("FFFE 00 13 54 68 69 73 20 69 73 20 61 20 63 6f 6d 6d 65 6e 74"), output.toByteArray());
	}

	@Test
	public void write_UnicodeComment_OK() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.setStringComment("這是一個comment");

		segment.write(output);
		assertArrayEquals(utils.makeByteArray("FFFE 00 15 e9 80 99 e6 98 af e4 b8 80 e5 80 8b   63 6f 6d 6d 65 6e 74"), output.toByteArray());
	}

	@Test
	public void write_CommentWithEmbeddedNull_OK() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.setStringComment("This is \000 a comment");

		segment.write(output);
		assertArrayEquals(utils.makeByteArray("FFFE 00 15 54 68 69 73 20 69 73 20 00 20 61 20 63 6f 6d 6d 65 6e 74"), output.toByteArray());
	}

	@Test
	public void write_WindowsString_OK() throws IOException {
		InputStream stream = utils.makeInputStream("00 05 93 85 94");	// “…”
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.read(stream);
		segment.write(output);

		assertArrayEquals(utils.makeByteArray("FFFE  00 05 93 85 94"), output.toByteArray());
	}

	@Test
	public void equals_WithEqualComment_OK() throws IOException {
		InputStream stream = utils.makeInputStream("00 13 54 68 69 73 20 69 73 20 61 20 63 6f 6d 6d 65 6e 74");

		segment.read(stream);

		ComSegment segment2 = new ComSegment();

		segment2.setStringComment("This is a comment");

		assertTrue(segment.equals(segment2));
	}

	@Test
	public void equals_WithDifferentComments_False() throws IOException {
		InputStream stream = utils.makeInputStream("00 13 54 68 69 73 20 69 73 20 61 20 63 6f 6d 6d 65 6e 74");

		segment.read(stream);

		ComSegment segment2 = new ComSegment();

		segment2.setStringComment("Another Segment");

		assertFalse(segment.equals(segment2));
	}

	@Test
	public void readFromStream_WithBinaryData_ReadsInUnchanged() throws IOException {
		InputStream stream = utils.makeInputStream("00 06 A1 B1 C1 D1");

		segment.read(stream);

		ComSegment segment2 = new ComSegment();

		segment2.setComment(new byte[]{(byte) 0xA1, (byte) 0xB1, (byte) 0xC1, (byte) 0xD1});

		assertEquals(segment, segment2);
	}

	@Test
	public void write_BinaryComment_OK() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.setComment(new byte[]{(byte) 0xBB, (byte) 0xAA, (byte) 0xCC});
		segment.write(output);

		assertArrayEquals(utils.makeByteArray("FFFE 00 05 BB AA CC"), output.toByteArray());
	}

	@Test
	public void readFromStream_WithBinaryData_ParsedAsExpected() throws IOException {
		InputStream stream = utils.makeInputStream("00 06 A1 B1 C1 D1");

		segment.read(stream);

		assertEquals(segment.getStringComment(), "¡±ÁÑ");
	}

	@Test(expected = EOFException.class)
	public void readFromStream_WithInsufficientData_ThrowsAnException() throws IOException {
		InputStream stream = utils.makeInputStream("00 10 00");

		segment.read(stream);
	}
}