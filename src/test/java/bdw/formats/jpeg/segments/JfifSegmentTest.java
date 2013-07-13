/*
 *  Copyright 2013 柏大衛
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

import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.ParseMode;
import bdw.formats.jpeg.TestUtils;
import bdw.formats.jpeg.segments.support.Problem;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class JfifSegmentTest {

	private TestUtils utils;
	private JfifSegment segment;
	private String basicSegmentBytes = "00 10" +
			"4A46494600 0102 00 0000 0000 00 00";
	private String basicSegmentWithThumbnailBytes = "00 22" +
			"4A46494600 0102 01 0003 0002 03 02" +
			"FFFFFF 000000 FFFFFF" +
			"000000 FFFFFF 000000";

	@Before
    public void setUp() throws InvalidJpegFormat {
		segment = new JfifSegment();
		utils = new TestUtils();
    }
	
	private JfifSegment makeBasicSegment() throws InvalidJpegFormat {
		JfifSegment seg = new JfifSegment();
		seg.setMajorVersion(1);
		seg.setMinorVersion(2);
		seg.setUnits(JfifSegment.UnitsEnum.NO_UNITS.getValue());
		seg.setImageXDensity(0);
		seg.setImageYDensity(0);
		seg.getThumbnail().setWidth(0);
		seg.getThumbnail().setHeight(0);
		
		return seg;
	}
	
	private JfifSegment makeBasicSegmentWithThumbnail() throws InvalidJpegFormat {
		JfifSegment seg = new JfifSegment();
		seg.setMajorVersion(1);
		seg.setMinorVersion(2);
		seg.setUnits(JfifSegment.UnitsEnum.DOTS_PER_INCH.getValue());
		seg.setImageXDensity(3);
		seg.setImageYDensity(2);
		seg.getThumbnail().setWidth(3);
		seg.getThumbnail().setHeight(2);
		byte[] buffer = new byte[] {-1, -1, -1, 0, 0, 0, -1, -1, -1, 0, 0, 0, -1, -1, -1, 0, 0, 0};
		seg.getThumbnail().setPixelBytes(buffer);
		
		return seg;
	}
	
	private JfifSegment createSegment(RandomAccessFile file) throws InvalidJpegFormat, IOException {
		return new JfifSegment(JfifSegment.SUBTYPE, file, ParseMode.STRICT);
	}

	private JfifSegment createSegment(InputStream stream) throws InvalidJpegFormat, IOException {
		return new JfifSegment(JfifSegment.SUBTYPE, stream, ParseMode.STRICT);
	}
	
    @Test
    public void creationSetsMarker() {
		assertEquals("marker", JfifSegment.SUBTYPE, segment.getMarker());
	}

    @Test
    public void readFromStreamWorks() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeInputStreamFromString(basicSegmentBytes));

		assertTrue(segment.equals(makeBasicSegment()));
	}

    @Test(expected=InvalidJpegFormat.class)
    public void readFromStreamWithBadLength() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeInputStreamFromString("00 12" +
			"4A46494600 0102 00 0000 0000 00 00"));
	}

    @Test(expected=InvalidJpegFormat.class)
    public void readFromStreamWithInvalidWidthAndHeight() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeInputStreamFromString("00 10" +
			"4A46494600 0102 00 0000 0000 05 05"));
	}

    @Test
    public void readFromStreamWithThumbnail() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeInputStreamFromString(basicSegmentWithThumbnailBytes));
		assertTrue(segment.equals(makeBasicSegmentWithThumbnail()));
	}

    @Test
    public void readFromSmallFileWorks() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeRandomAccessFile(basicSegmentBytes));

		assertTrue(segment.equals(makeBasicSegment()));
	}

    @Test(expected=EOFException.class)
    public void readFromFileWithBadLength() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeRandomAccessFile("00 12" +
			"4A46494600 0102 00 0000 0000 00 00"));
	}

	@Test
    public void writeABasicSegment() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals(utils.makeByteArrayFromString(basicSegmentBytes),
				output.toByteArray());
	}

	@Test
    public void writeABizarreSegment() throws IOException {
		segment.setMajorVersion(129);
		segment.setMinorVersion(100);
		segment.setUnits(255);
		segment.setImageXDensity(32767);
		segment.setImageYDensity(1);
		segment.getThumbnail().setHeight(250);
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals(utils.makeByteArrayFromString("00 10" +
				"4A46494600 8164 FF 7FFF 0001 00 FA"),
				output.toByteArray());
	}

    @Test(expected=IllegalArgumentException.class)
    public void invalidUnitsValueRejected() throws IllegalArgumentException {
		segment.setUnits(32000);
	}
	
	@Test
    public void badVersionYeildsWarning() throws IOException {
		segment.setMajorVersion(129);

		ArrayList<Problem> list = new ArrayList<Problem>();
		list.add(new Problem(Problem.ProblemType.WARNING, JfifSegment.WARNING_UNKNOWN_VERSION));
		assertEquals(list, segment.getProblems());
	}

	@Test
    public void badUnitsYeildsWarning() throws IOException {
		segment.setUnits(129);

		ArrayList<Problem> list = new ArrayList<Problem>();
		list.add(new Problem(Problem.ProblemType.WARNING, JfifSegment.WARNING_UNKNOWN_UNITS));
		assertEquals(list, segment.getProblems());
	}

	@Test
    public void badThumbnailYeildsWarning() throws IOException {
		segment.getThumbnail().setHeight(129);
		segment.getThumbnail().setWidth(129);

		ArrayList<Problem> list = new ArrayList<Problem>();
		list.add(new Problem(Problem.ProblemType.ERROR, JfifSegment.ERROR_BYTES_SIZE_DONT_MATCH));
		assertEquals(list, segment.getProblems());
	}

    public void readFromFileWithStrangeUnitsAndVersion() throws IOException, InvalidJpegFormat {
		segment = createSegment(utils.makeRandomAccessFile("00 12" +
			"4A46494600 0103 05 0000 0000 00 00"));

		ArrayList<Problem> list = new ArrayList<Problem>();
		list.add(new Problem(Problem.ProblemType.WARNING, JfifSegment.WARNING_UNKNOWN_VERSION));
		list.add(new Problem(Problem.ProblemType.WARNING, JfifSegment.WARNING_UNKNOWN_UNITS));
		assertEquals(list, segment.getProblems());
	}

	@Test
    public void fixingBadUnitsMakesProblemGoAway() throws IOException {
		segment.setUnits(129);

		segment.setUnits(1);

		ArrayList<Problem> list = new ArrayList<Problem>();
		assertEquals(list, segment.getProblems());
	}

}