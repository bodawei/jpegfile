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

import bdw.format.jpeg.segment.JfxxSegment;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.ParseMode;
import bdw.formats.jpeg.TestUtils;
import bdw.format.jpeg.support.Problem;
import bdw.format.jpeg.segment.support.ThreeBytesPerPixelThumbnail;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class JfxxSegmentTest {

	private TestUtils utils;
	private JfxxSegment segment;
	private static final String basicJfxxBytes = "000A 4A46585800 13 00 00";
	private static final String threeByteThumbnailJfxxBytes = "0013 4A46585800 13 01 03 000000 FFFFFF 000000";

	@Before
    public void setUp() throws InvalidJpegFormat {
		segment = new JfxxSegment();
		utils = new TestUtils();
    }
	
	private JfxxSegment makeBasicSegment() throws InvalidJpegFormat {
		JfxxSegment seg = new JfxxSegment();
		
		return seg;
	}
	
	private JfxxSegment makeBasicSegmentWithThumbnail() throws InvalidJpegFormat {
		JfxxSegment seg = new JfxxSegment();
		
		return seg;
	}
	
	private JfxxSegment createSegment(RandomAccessFile file) throws InvalidJpegFormat, IOException {
		return new JfxxSegment(JfxxSegment.SUBTYPE, file, ParseMode.STRICT);
	}

	private JfxxSegment createSegment(InputStream stream) throws InvalidJpegFormat, IOException {
		return new JfxxSegment(JfxxSegment.SUBTYPE, stream, ParseMode.STRICT);
	}

    @Test
    public void creationSetsMarker() {
		assertEquals(JfxxSegment.SUBTYPE, segment.getMarker());
	}

    @Test(expected=InvalidJpegFormat.class)
    public void otherMarkersNotAcceptedForFiles() throws IOException, InvalidJpegFormat {
		RandomAccessFile file = utils.makeRandomAccessFile(basicJfxxBytes);
		
		JfxxSegment ignored = new JfxxSegment(0x23, file, ParseMode.STRICT);
	}

    @Test(expected=InvalidJpegFormat.class)
    public void otherMarkersNotAcceptedForStreams() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString(basicJfxxBytes);
		
		JfxxSegment ignored = new JfxxSegment(0x23, stream, ParseMode.STRICT);
	}

    @Test(expected=InvalidJpegFormat.class)
    public void segmentMustHaveMinimumLength() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("0007 4A46585800 13");
		
		JfxxSegment ignored = new JfxxSegment(JfxxSegment.SUBTYPE, stream, ParseMode.STRICT);
	}

    @Test(expected=InvalidJpegFormat.class)
    public void segmentMustHaveJFXXMarker() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("0008 4A465858FF 13");
		
		JfxxSegment ignored = new JfxxSegment(JfxxSegment.SUBTYPE, stream, ParseMode.STRICT);
	}

	@Test
    public void writeABasicSegment() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals(utils.makeByteArrayFromString(basicJfxxBytes),
				output.toByteArray());
	}

    @Test(expected=InvalidJpegFormat.class)
    public void invalidThumbnailTypeRejected() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("0008 4A46585800 64");
		
		JfxxSegment ignored = new JfxxSegment(JfxxSegment.SUBTYPE, stream, ParseMode.STRICT);
	}

	@Test
    public void badThumbnailTypeGeneratesError() throws IOException {
		segment.setThumbnailType(64);
		
		ArrayList<Problem> list = new ArrayList<Problem>();
		list.add(new Problem(Problem.ProblemType.ERROR, JfxxSegment.ERROR_UNKNOWN_THUMBNAIL_TYPE));
		assertEquals(list, segment.getProblems());
	}

	@Test
    public void writeABizarreSegment() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.setThumbnailType(64);
		segment.write(output);

		assertArrayEquals(utils.makeByteArrayFromString("000A 4A46585800 40 00 00"),
				output.toByteArray());
	}

    @Test()
    public void readSegmentWithThreeByteThumbnail() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString(threeByteThumbnailJfxxBytes);
		
		segment = new JfxxSegment(JfxxSegment.SUBTYPE, stream, ParseMode.STRICT);
		
		JfxxSegment expected = new JfxxSegment();
		expected.getThumbnail().setWidth(1);
		expected.getThumbnail().setHeight(3);
		((ThreeBytesPerPixelThumbnail) expected.getThumbnail()).setPixelBytes(new byte[] {0x00, 0x00, 0x00, -1, -1, -1, 0x00, 0x00, 0x00});
		
		assertEquals(expected, segment);
	}

}
