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
package com.davidjohnburrowes.formats.jpeg.data;

import com.davidjohnburrowes.format.jpeg.marker.AppNSegment;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class GenericSegmentTest {

	protected TestUtils utils;
	protected AppNSegment segment;

	@Before
	public void setUp() throws InvalidJpegFormat {
		segment = new AppNSegment(AppNSegment.FIRST_MARKERID);
		utils = new TestUtils();
	}

	@Test
	public void creationSetsMarker() {
		assertEquals("marker", AppNSegment.FIRST_MARKERID, segment.getMarkerId());
	}

	@Test
	public void readFromStreamWorks() throws IOException, InvalidJpegFormat {
		segment.read(utils.makeInputStream("00 0C"
				  + "01 02 03 04 05 06 07 08 09 0a"));

		assertArrayEquals("bytes", utils.makeByteArray("01 02 03 04 05 06 07 08 09 0a"),
				  segment.getBytes());
	}

	@Test(expected = EOFException.class)
	public void readFromStreamWithBadLength() throws IOException, InvalidJpegFormat {
		segment.read(utils.makeInputStream("00 10"
				  + "01 02 03 04 05 06 07 08 09 0a"));
	}

	@Test
	public void readFromSmallFileWorks() throws IOException, InvalidJpegFormat {
		segment.read(utils.makeRandomAccessFile("00 0C"
				  + "01 02 03 04 05 06 07 08 09 0a"));

		assertArrayEquals("bytes", utils.makeByteArray("01 02 03 04 05 06 07 08 09 0a"),
				  segment.getBytes());
	}

	@Test(expected = EOFException.class)
	public void readFromFileWithBadLength() throws IOException, InvalidJpegFormat {
		segment.read(utils.makeRandomAccessFile("00 10"
				  + "01 02 03 04 05 06 07 08 09 0a"));
	}

	@Test
	public void readFromBigFileWorks() throws IOException, InvalidJpegFormat {
		StringBuilder builder = new StringBuilder();

		builder.append("1F E2");

		for (int index = 0; index < 0x1FE0; index++) {
			builder.append("00");
		}
		segment.read(utils.makeRandomAccessFile(builder.toString()));

		assertEquals("length", 0x1FE0, segment.getBytes().length);
	}

	@Test
	public void readFromBigFileSkipsBytes() throws IOException, InvalidJpegFormat {
		StringBuilder builder = new StringBuilder();
		RandomAccessFile file;

		builder.append("1F E2");

		for (int index = 0; index < 0x1FE0; index++) {
			builder.append("00");
		}
		builder.append("77");
		file = utils.makeRandomAccessFile(builder.toString());

		segment.read(file);

		assertEquals("Expected byte after reading", 0x77, file.readByte());
	}

	@Test
	public void twoEqualSegmentsEqual() throws IOException, InvalidJpegFormat {
		segment.read(utils.makeInputStream("00 0C"
				  + "01 02 03 04 05 06 07 08 09 0a"));

		AppNSegment other = new AppNSegment(AppNSegment.FIRST_MARKERID);
		other.setBytes(utils.makeByteArray("01 02 03 04 05 06 07 08 09 0a"));
		assertEquals("segment", other, segment);
	}

	@Test
	public void unequalSegmentsAreNotEqual() throws IOException, InvalidJpegFormat {
		segment.read(utils.makeInputStream("00 0C"
				  + "01 02 03 04 05 06 07 08 09 0a"));

		AppNSegment other = new AppNSegment(AppNSegment.FIRST_MARKERID);
		other.setBytes(utils.makeByteArray("FF FE FD 04 05 06 07 08 09 0a"));

		assertFalse("segment", other.equals(segment));
	}

	@Test
	public void writeATrivialSegment() throws IOException, InvalidJpegFormat {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals("output", utils.makeByteArray("FFE0 00 02"),
				  output.toByteArray());
	}

	@Test
	public void writeAShortSegment() throws IOException, InvalidJpegFormat {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.setBytes(utils.makeByteArray("FF FE FD 04 05 06 07 08 09 0a"));

		segment.write(output);

		assertArrayEquals("output", utils.makeByteArray("FFE0 00 0C"
				  + "FF FE FD 04 05 06 07 08 09 0a"),
				  output.toByteArray());
	}

	@Test(expected = IllegalArgumentException.class)
	public void byteArrayIsLimitedTo64K() throws IOException {
		byte[] data = new byte[65536];
		segment.setBytes(data);
	}
}