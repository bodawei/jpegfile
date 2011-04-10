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

import bdw.formats.jpeg.InvalidJpegFormat;
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
public class ComSegmentTests {

    protected ComSegment segment;
    protected TestUtils utils;

    @Before
    public void setUp() {
        segment = new ComSegment();
        utils = new TestUtils();
    }

    @Test
    public void constructor_SetsCorrectMarker() {
        assertEquals(ComSegment.MARKER, segment.getMarker());
    }

    @Test
    public void getComment_EmptyStringByDefault() {
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
    public void setComment_OrdinaryString_IsSet() {
        segment.setComment("This is a comment");
        assertEquals("This is a comment", segment.getComment());
    }

    @Test
    public void read_ZeroLength_OK() throws IOException, InvalidJpegFormat {
	InputStream stream = utils.makeInputStreamFromString("00 02");

        segment.readFromStream(stream);

        assertEquals("", segment.getComment());
    }

    @Test
    public void read_ZeroLengthWithNull_OK()  throws IOException, InvalidJpegFormat {
	InputStream stream = utils.makeInputStreamFromString("00 03 00");

        segment.readFromStream(stream);

        assertEquals("", segment.getComment());
    }

    @Test
    public void read_NullTerminatedString_OK()  throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("00 06 41 42 43 00");

        segment.readFromStream(stream);

        assertEquals("ABC", segment.getComment());
    }

    @Test
    public void write_ZeroLength_OK()  throws IOException, InvalidJpegFormat {
	InputStream stream = utils.makeInputStreamFromString("00 02");
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        segment.readFromStream(stream);

        segment.write(output);
        assertArrayEquals(utils.makeByteArrayFromString("00 02"), output.toByteArray());
    }

    @Test
    public void write_ZeroLengthWithNull_OK()  throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        segment.setComment("");

        segment.write(output);
        assertArrayEquals(utils.makeByteArrayFromString("00 03 00"), output.toByteArray());
    }

    @Test
    public void write_Comment_OK()  throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        segment.setComment("This is a comment");

        segment.write(output);
        assertArrayEquals(utils.makeByteArrayFromString("00 14 54 68 69 73 20 69 73 20 61 20 63 6f 6d 6d 65 6e 74 00"), output.toByteArray());
    }

    @Test
    public void equals_WithEqualCommentWithoutTrailingNull_OK()  throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("00 13 54 68 69 73 20 69 73 20 61 20 63 6f 6d 6d 65 6e 74");

        segment.readFromStream(stream);

        ComSegment segment2 = new ComSegment();

		segment2.setComment("This is a comment");

        assertTrue(segment.equals(segment2));
    }

	@Test
    public void equals_WithDifferentComments_False()  throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("00 13 54 68 69 73 20 69 73 20 61 20 63 6f 6d 6d 65 6e 74");

        segment.readFromStream(stream);

        ComSegment segment2 = new ComSegment();

		segment2.setComment("Another Segment");

        assertFalse(segment.equals(segment2));
    }
}