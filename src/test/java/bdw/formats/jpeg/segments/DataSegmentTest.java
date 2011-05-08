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
import java.io.InputStream;
import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.ParseMode;
import bdw.formats.jpeg.TestUtils;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DataSegmentTest {

	protected TestUtils utils;
	protected DataSegment segment;

	@Before
    public void setUp() {
		segment = new DataSegment();
		utils = new TestUtils();
    }
	
	public DataSegment createSegment(InputStream stream) throws InvalidJpegFormat, IOException {
		return new DataSegment(DataSegment.SUBTYPE, stream, ParseMode.STRICT);
	}

	public DataSegment createSegment(RandomAccessFile file) throws InvalidJpegFormat, IOException {
		return new DataSegment(DataSegment.SUBTYPE, file, ParseMode.STRICT);
	}

    @Test
    public void setByte1KIn() throws IOException {
		segment.setDataAt(1024, (byte)0xFF);

		assertEquals("length", 1025, segment.getDataLength());
		assertEquals("Last Byte", (byte)0xFF, segment.getDataAt(1024));
	}

    @Test
    public void streamRead() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeInputStreamFromString("01 02 03 04 05 06 07 08 09 0a"));

		assertEquals("bytes", 10, segment.getDataLength());
		assertEquals("Last Byte", (byte)0x0A, segment.getDataAt(9));
	}

    @Test
    public void streamWithFF00ReadAsFF() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeInputStreamFromString("01 FF 00 04 05 06 07 08 09 0a"));

		assertEquals("bytes", 9, segment.getDataLength());
	}

    @Test
    public void streamWithNoDataCreatesZeroDataBytes() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeInputStreamFromString(""));

		assertEquals("bytes", 0, segment.getDataLength());
	}

    @Test
    public void streamWithTrailingFFWorks() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeInputStreamFromString("02 FF"));

		assertEquals("bytes", 2, segment.getDataLength());
		assertEquals("Last Byte", (byte)0xFF, segment.getDataAt(1));
	}

	@Test
    public void sreamStopsAtOtherSegmentStart() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("01 FF 00 04 05 FF D8 08 09 0a");
		segment = createSegment(stream);

		assertEquals("bytes", 4, segment.getDataLength());
		assertEquals("nextByte", 0xFF, stream.read());
	}

    @Test
    public void streamStopsAtOtherSegmentStartEvenIfFirstByte() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("FF D8 08 09 0a");
		segment = createSegment(stream);

		assertEquals("bytes", 0, segment.getDataLength());
		assertEquals("nextByte", 0xFF, stream.read());
	}

    @Test
    public void fileWithMoreThan1KOfData() throws IOException, InvalidJpegFormat {
		StringBuilder builder = new StringBuilder();

		for (int index = 0; index < 0x1FE0; index++) {
			builder.append("00");
		}
		segment = createSegment(utils.makeRandomAccessFile(builder.toString()));

		assertEquals("byteCount", 0x1FE0, segment.getDataLength());
	}

    @Test
    public void fileWithFF00ReadAsFF() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeRandomAccessFile("01 FF 00 04 05 06 07 08 09 0a"));

		assertEquals("byteCount", 9, segment.getDataLength());
	}

    @Test
    public void fileWithNoDataCreatesZeroDataBytes() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeRandomAccessFile(""));

		assertEquals("byteCount", 0, segment.getDataLength());
	}

    @Test
    public void fileWithTrailingFFWorks() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeRandomAccessFile("02 FF"));

		assertEquals("byteCount", 2, segment.getDataLength());
		assertEquals("Last Byte", (byte)0xFF, segment.getDataAt(1));
	}


	@Test
    public void fileStopsAtOtherSegmentStart() throws IOException, InvalidJpegFormat {
		RandomAccessFile file = utils.makeRandomAccessFile("01 FF 00 04 05 FF D8 08 09 0a");
		segment = createSegment(file);

		assertEquals("byteCount", 4, segment.getDataLength());
		assertEquals("nextByte", 0xFF, file.read());
	}

    @Test
    public void fileStopsAtOtherSegmentStartEvenIfFirstByte() throws IOException, InvalidJpegFormat {
		RandomAccessFile file = utils.makeRandomAccessFile("FF D8 08 09 0a");
		segment = createSegment(file);

		assertEquals("byteCount", 0, segment.getDataLength());
		assertEquals("nextByte", 0xFF, file.read());
	}

	@Test
    public void writeZeroLengthSegment() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals("bytes", new byte[0], output.toByteArray());
	}

	@Test
    public void writeSegmentWorks() throws IOException {
		segment.setDataAt(0, (byte)0x0a);
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals("bytes", utils.makeByteArrayFromString("0a"), output.toByteArray());
	}

	@Test
    public void writeSegmentWithFFWorks() throws IOException {
		segment.setDataAt(0, (byte)0x0a);
		segment.setDataAt(1, (byte)0xFF);
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals("bytes", utils.makeByteArrayFromString("0a ff 00"), output.toByteArray());
	}

	@Test
    public void equalSegmentsEqual() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeInputStreamFromString("00 01 02 03 04 05 06 07 08 09"));

		DataSegment other = new DataSegment();
		for (int index = 0; index < 10; index++) {
			other.setDataAt(index, (byte)index);
		}

		assertEquals("segment", other, segment);
	}

	@Test
    public void unequalSegmentsNotEqual() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeInputStreamFromString("01 03 03 04 05 06 07 08 09 0a"));

		DataSegment other = new DataSegment();
		for (int index = 0; index < 10; index++) {
			other.setDataAt(index, (byte)index);
		}

		assertFalse("segment", other.equals(segment));
	}
}