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

import java.io.RandomAccessFile;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.TestUtils;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AppNSegmentTest {

	protected TestUtils utils;
	protected AppNSegment segment;

	@Before
    public void setUp() {
		segment = new AppNSegment();
		utils = new TestUtils();
    }

    @Test
    public void creationSetsMarker() {
		assertEquals("marker", AppNSegment.START_MARKER, segment.getMarker());
	}


    @Test
    public void readFromStreamWorks() throws IOException, InvalidJpegFormat {
		segment = new AppNSegment(utils.makeInputStreamFromString("00 0C" +
			"01 02 03 04 05 06 07 08 09 0a"));

		assertArrayEquals("bytes", utils.makeByteArrayFromString("01 02 03 04 05 06 07 08 09 0a"),
				segment.getBytes());
	}

    @Test(expected=EOFException.class)
    public void readFromStreamWithBadLength() throws IOException, InvalidJpegFormat {
		segment = new AppNSegment(utils.makeInputStreamFromString("00 10" +
			"01 02 03 04 05 06 07 08 09 0a"));
	}

    @Test
    public void readFromSmallFileWorks() throws IOException, InvalidJpegFormat {
		segment = new AppNSegment(utils.makeRandomAccessFile("00 0C" +
			"01 02 03 04 05 06 07 08 09 0a"));

		assertArrayEquals("bytes", utils.makeByteArrayFromString("01 02 03 04 05 06 07 08 09 0a"),
				segment.getBytes());
	}

    @Test(expected=EOFException.class)
    public void readFromFileWithBadLength() throws IOException, InvalidJpegFormat {
		segment = new AppNSegment(utils.makeRandomAccessFile("00 10" +
			"01 02 03 04 05 06 07 08 09 0a"));
	}

	@Test
    public void readFromBigFileWorks() throws IOException, InvalidJpegFormat {
		StringBuilder builder = new StringBuilder();

		builder.append("1F E2");

		for (int index = 0; index < 0x1FE0; index++) {
			builder.append("00");
		}
		segment = new AppNSegment(utils.makeRandomAccessFile(builder.toString()));

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

		segment = new AppNSegment(file);

		assertEquals("Expected byte after reading", 0x77, file.readByte());
	}


	@Test
    public void twoEqualSegmentsEqual() throws IOException, InvalidJpegFormat {
		segment = new AppNSegment(utils.makeInputStreamFromString("00 0C" +
			"01 02 03 04 05 06 07 08 09 0a"));

		AppNSegment other = new AppNSegment();
		other.setBytes(utils.makeByteArrayFromString("01 02 03 04 05 06 07 08 09 0a"));
		assertEquals("segment", other, segment);
	}

    @Test
    public void unequalSegmentsAreNotEqual() throws IOException, InvalidJpegFormat {
		segment = new AppNSegment(utils.makeInputStreamFromString("00 0C" +
			"01 02 03 04 05 06 07 08 09 0a"));

		AppNSegment other = new AppNSegment();
		other.setBytes(utils.makeByteArrayFromString("FF FE FD 04 05 06 07 08 09 0a"));

		assertFalse("segment", other.equals(segment));
	}


	@Test
    public void writeATrivialSegment() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals("output", utils.makeByteArrayFromString("00 02"),
				output.toByteArray());
	}

	@Test
    public void writeAShortSegment() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.setBytes(utils.makeByteArrayFromString("FF FE FD 04 05 06 07 08 09 0a"));

		segment.write(output);

		assertArrayEquals("output", utils.makeByteArrayFromString("00 0C" +
				"FF FE FD 04 05 06 07 08 09 0a"),
				output.toByteArray());
	}


	@Test(expected=IllegalArgumentException.class)
    public void byteArrayIsLimitedTo64K() throws IOException {
		byte[] data = new byte[65536];
		segment.setBytes(data);
	}

}