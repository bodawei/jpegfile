/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

import java.io.RandomAccessFile;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.TestUtils;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bodawei
 */
public class DataSegmentTest {

	protected TestUtils utils;
	protected DataSegment segment;

	@Before
    public void setUp() {
		segment = new DataSegment();
		utils = new TestUtils();
    }

    @Test
    public void setByte1KIn() throws IOException {
		segment.setDataAt(1024, (byte)0xFF);

		assertEquals("length", 1025, segment.getDataLength());
		assertEquals("Last Byte", (byte)0xFF, segment.getDataAt(1024));
	}

    @Test
    public void readFromAnOrdinaryStreamWorks() throws IOException, InvalidJpegFormat {
		segment.readFromStream(utils.makeInputStreamFromString("01 02 03 04 05 06 07 08 09 0a"));

		assertEquals("bytes", 10, segment.getDataLength());
		assertEquals("Last Byte", (byte)0x0A, segment.getDataAt(9));
	}

    @Test
    public void streamWithFF00ReadAsFF() throws IOException, InvalidJpegFormat {
		segment.readFromStream(utils.makeInputStreamFromString("01 FF 00 04 05 06 07 08 09 0a"));

		assertEquals("bytes", 9, segment.getDataLength());
	}

    @Test
    public void streamWithNoDataCreatesZeroDataBytes() throws IOException, InvalidJpegFormat {
		segment.readFromStream(utils.makeInputStreamFromString(""));

		assertEquals("bytes", 0, segment.getDataLength());
	}

    @Test
    public void streamWithTrailingFFWorks() throws IOException, InvalidJpegFormat {
		segment.readFromStream(utils.makeInputStreamFromString("02 FF"));

		assertEquals("bytes", 2, segment.getDataLength());
		assertEquals("Last Byte", (byte)0xFF, segment.getDataAt(1));
	}


	@Test
    public void readStopsAtOtherSegmentStart() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("01 FF 00 04 05 FF D8 08 09 0a");
		segment.readFromStream(stream);

		assertEquals("bytes", 4, segment.getDataLength());
		assertEquals("nextByte", 0xFF, stream.read());
	}

    @Test
    public void readStopsAtOtherSegmentStartEvenIfFirstByte() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("FF D8 08 09 0a");
		segment.readFromStream(stream);

		assertEquals("bytes", 0, segment.getDataLength());
		assertEquals("nextByte", 0xFF, stream.read());
	}

    @Test
    public void readFromFileMoreThan1KOfData() throws IOException, InvalidJpegFormat {
		StringBuilder builder = new StringBuilder();

		for (int index = 0; index < 0x1FE0; index++) {
			builder.append("00");
		}
		segment.readFromFile(utils.makeRandomAccessFile(builder.toString()));

		assertEquals("length", 0x1FE0, segment.getDataLength());
	}

    @Test
    public void fileWithFF00ReadAsFF() throws IOException, InvalidJpegFormat {
		segment.readFromFile(utils.makeRandomAccessFile("01 FF 00 04 05 06 07 08 09 0a"));

		assertEquals("bytes", 9, segment.getDataLength());
	}

    @Test
    public void fileWithNoDataCreatesZeroDataBytes() throws IOException, InvalidJpegFormat {
		segment.readFromFile(utils.makeRandomAccessFile(""));

		assertEquals("bytes", 0, segment.getDataLength());
	}

    @Test
    public void fileReadWithTrailingFFWorks() throws IOException, InvalidJpegFormat {
		segment.readFromFile(utils.makeRandomAccessFile("02 FF"));

		assertEquals("bytes", 2, segment.getDataLength());
		assertEquals("Last Byte", (byte)0xFF, segment.getDataAt(1));
	}


	@Test
    public void fileReadStopsAtOtherSegmentStart() throws IOException, InvalidJpegFormat {
		RandomAccessFile file = utils.makeRandomAccessFile("01 FF 00 04 05 FF D8 08 09 0a");
		segment.readFromFile(file);

		assertEquals("bytes", 4, segment.getDataLength());
		assertEquals("nextByte", 0xFF, file.read());
	}

    @Test
    public void fileReadStopsAtOtherSegmentStartEvenIfFirstByte() throws IOException, InvalidJpegFormat {
		RandomAccessFile file = utils.makeRandomAccessFile("FF D8 08 09 0a");
		segment.readFromFile(file);

		assertEquals("bytes", 0, segment.getDataLength());
		assertEquals("nextByte", 0xFF, file.read());
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
		segment.readFromStream(utils.makeInputStreamFromString("00 01 02 03 04 05 06 07 08 09"));

		DataSegment other = new DataSegment();
		for (int index = 0; index < 10; index++) {
			other.setDataAt(index, (byte)index);
		}

		assertEquals("segment", other, segment);
	}

	@Test
    public void unequalSegmentsNotEqual() throws IOException, InvalidJpegFormat {
		segment.readFromStream(utils.makeInputStreamFromString("01 03 03 04 05 06 07 08 09 0a"));

		DataSegment other = new DataSegment();
		for (int index = 0; index < 10; index++) {
			other.setDataAt(index, (byte)index);
		}

		assertFalse("segment", other.equals(segment));
	}
}