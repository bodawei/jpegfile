/*
 *  Copyright 2014 柏大衛
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
package bdw.formats.jpeg.data;

import bdw.format.jpeg.data.MarkerSegment;
import bdw.format.jpeg.support.DataMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.formats.jpeg.test.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class MarkerSegmentTest {

	protected TestUtils utils;
	protected MarkerSegment segment;

	@Before
	public void setUp() throws InvalidJpegFormat {
		segment = new MarkerSegment(35);
		utils = new TestUtils();
	}

	@Test
	public void construction_setsMarkerIdCorrectly() {
		assertEquals(35, segment.getMarkerId());
	}

	@Test
	public void getSegmentSizeOnDisk_byDefault_is2() {
		assertEquals(2, segment.getParameterSizeOnDisk());
	}

	@Test
	public void getSegmentSizeOnDisk_with5TrailingBytes_is7() {
		segment.setTrailingBytes(new byte[5]);
		assertEquals(7, segment.getParameterSizeOnDisk());
	}

	@Test
	public void clearPassthrough_withTrailingBytes_removesThem() {
		segment.setTrailingBytes(new byte[5]);
		segment.clearPassthrough();
		assertEquals(2, segment.getParameterSizeOnDisk());
	}

	@Test
	public void read_withSizeAndNoTrailingBytes_setsCorrectly() throws IOException {
		segment.read(utils.makeInputStream("00 02"));
		assertEquals(2, segment.getParameterSizeOnDisk());
	}

	@Test(expected=InvalidJpegFormat.class)
	public void read_withSizeAndTrailingBytes_throwsException() throws IOException {
		segment.read(utils.makeInputStream("00 04 FF FE"));
	}

	@Test
	public void read_withSizeAndTrailingBytesInLaxMode_setsCorrectly() throws IOException {
		segment.setDataMode(DataMode.LAX);
		segment.read(utils.makeInputStream("00 04 FF FE"));
		assertEquals(4, segment.getParameterSizeOnDisk());
	}

	@Test
	public void write_withNoTrailingBytes_writesJustSize() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		segment.write(output);

		assertArrayEquals(utils.makeByteArray("FF23 00 02"),
				  output.toByteArray());
	}

	@Test
	public void write_withTrailingBytes_writesSizeAndBytes() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] bytes = new byte[] {1, 2, 3, 4, 5};
		segment.setTrailingBytes(bytes);

		segment.write(output);

		assertArrayEquals(utils.makeByteArray("FF23 00 07 01 02 03 04 05"),
				  output.toByteArray());
	}
}
