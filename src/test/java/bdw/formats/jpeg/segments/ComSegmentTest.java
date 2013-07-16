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

import bdw.format.jpeg.segment.ComSegment;
import java.io.RandomAccessFile;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.ParseMode;
import bdw.formats.jpeg.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class ComSegmentTest {

    protected ComSegment segment;
    protected TestUtils utils;

    @Before
    public void setUp() {
        segment = new ComSegment();
        utils = new TestUtils();
    }
	
	public ComSegment createSegment(InputStream stream) throws IOException, InvalidJpegFormat {
		return new ComSegment(ComSegment.SUBTYPE, stream, ParseMode.STRICT);
	}

	public ComSegment createSegment(RandomAccessFile file) throws IOException, InvalidJpegFormat {
		return new ComSegment(ComSegment.SUBTYPE, file, ParseMode.STRICT);
	}

    @Test
    public void constructor_SetsCorrectMarker() {
        assertEquals(ComSegment.SUBTYPE, segment.getMarker());
    }

    @Test
    public void getComment_EmptyStringByDefault() throws IOException {
        assertEquals("", segment.getComment());
    }

    @Test
    public void setComment_WullValue_Exception() {
        try {
            segment.setComment(null);
            fail("Didn't get an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void setComment_OrdinaryString_IsSet() throws IOException {
        segment.setComment("This is a comment");
        assertEquals("This is a comment", segment.getComment());
    }

    @Test
    public void read_ZeroLength_OK() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("00 02");

        segment = createSegment(stream);

        assertEquals("", segment.getComment());
    }

    @Test
    public void read_ZeroLengthWithNull_OK()  throws IOException, InvalidJpegFormat {
	InputStream stream = utils.makeInputStreamFromString("00 03 00");

        segment = createSegment(stream);

        assertEquals("\000", segment.getComment());
    }

    @Test
    public void read_NullTerminatedString_OK()  throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("00 06 41 42 43 00");

        segment = createSegment(stream);

        assertEquals("ABC\000", segment.getComment());
    }

    @Test
    public void read_UnicodeString_OK()  throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("00 16 e9 80 99 e6 98 af e4 b8 80 e5 80 8b 00  63 6f 6d 6d 65 6e 74");

        segment = createSegment(stream);

        assertEquals("這是一個\000comment", segment.getComment());
    }

    @Test
    public void read_WindowsString_OK()  throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("00 07 93 20 85 20 94");	// “ … ”

        segment = createSegment(stream);

        assertEquals("“ … ”", segment.getComment());
    }

    @Test
    public void read_1025BytesOfComment_OK() throws IOException, InvalidJpegFormat {
		StringBuilder builder = new StringBuilder();

		builder.append("04 03");
		for (int index = 0; index < 1025; index++) {
			builder.append('4');
			builder.append((char)('0'+(index % 10)));
		}
        segment = createSegment(utils.makeRandomAccessFile(builder.toString()));

		assertEquals("byteCount", 1025, segment.getComment().length());
		assertEquals("lastByte", 'D', segment.getComment().charAt(1024));
	}


	@Test
    public void write_ZeroLength_OK()  throws IOException, InvalidJpegFormat {
	InputStream stream = utils.makeInputStreamFromString("00 02");
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        segment = createSegment(stream);

        segment.write(output);
        assertArrayEquals(utils.makeByteArrayFromString("00 02"), output.toByteArray());
    }

    @Test
    public void write_ZeroLengthWithNull_OK()  throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        segment.setComment("");

        segment.write(output);
        assertArrayEquals(utils.makeByteArrayFromString("00 02"), output.toByteArray());
    }

    @Test
    public void write_Comment_OK()  throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        segment.setComment("This is a comment");

        segment.write(output);
        assertArrayEquals(utils.makeByteArrayFromString("00 13 54 68 69 73 20 69 73 20 61 20 63 6f 6d 6d 65 6e 74"), output.toByteArray());
    }

    @Test
    public void write_UnicodeComment_OK()  throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        segment.setComment("這是一個comment");

        segment.write(output);
        assertArrayEquals(utils.makeByteArrayFromString("00 15 e9 80 99 e6 98 af e4 b8 80 e5 80 8b   63 6f 6d 6d 65 6e 74"), output.toByteArray());
    }

    @Test
    public void write_CommentWithEmbeddedNull_OK()  throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        segment.setComment("This is \000 a comment");

        segment.write(output);
        assertArrayEquals(utils.makeByteArrayFromString("00 15 54 68 69 73 20 69 73 20 00 20 61 20 63 6f 6d 6d 65 6e 74"), output.toByteArray());
    }

    @Test
    public void write_WindowsString_OK()  throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("00 05 93 85 94");	// “…”
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        segment = createSegment(stream);
        segment.write(output);

        assertArrayEquals(utils.makeByteArrayFromString("00 05 93 85 94"), output.toByteArray());
    }

    @Test
    public void setComment_WithUnicodeCharacters_ResetsWindowsFlag()  throws IOException, InvalidJpegFormat {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

		// set up with windows string
        segment = createSegment(utils.makeInputStreamFromString("00 07 93 20 85 20 94"));

		segment.setComment("這是一個\000comment");

        segment.write(output);
        assertArrayEquals(utils.makeByteArrayFromString("00 16 e9 80 99 e6 98 af e4 b8 80 e5 80 8b 00  63 6f 6d 6d 65 6e 74"), output.toByteArray());
    }


	@Test
    public void equals_WithEqualCommentWithoutTrailingNull_OK()  throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("00 13 54 68 69 73 20 69 73 20 61 20 63 6f 6d 6d 65 6e 74");

        segment = createSegment(stream);

        ComSegment segment2 = new ComSegment();

		segment2.setComment("This is a comment");

        assertTrue(segment.equals(segment2));
    }

	@Test
    public void equals_WithDifferentComments_False()  throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("00 13 54 68 69 73 20 69 73 20 61 20 63 6f 6d 6d 65 6e 74");

        segment = createSegment(stream);

        ComSegment segment2 = new ComSegment();

		segment2.setComment("Another Segment");

        assertFalse(segment.equals(segment2));
    }
}