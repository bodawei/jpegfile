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

import bdw.format.jpeg.data.EntropyData;
import bdw.format.jpeg.support.DataMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.formats.jpeg.test.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class EntropyDataTest {

	private TestUtils utils;
	private EntropyData data;

	@Before
	public void setUp() {
		utils = new TestUtils();
		data = new EntropyData();
	}

	@Test
	public void testGetData_returnsEmptyByDefault() {
		assertArrayEquals(new byte[0], data.getData());
	}

	@Test
	public void testSetData_setsData() {
		data.setData(new byte[] { 1, 2, 3});
		assertArrayEquals(new byte[] {1, 2, 3}, data.getData());
	}

	@Test
	public void testReadFromStream_ordinaryInput_readsSuccessfully() throws IOException {
		InputStream stream = utils.makeInputStream("00 01 02 03 04 05 06 07 08 09 0a");
		data.read(stream);

		assertArrayEquals(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, data.getData());
	}

	@Test
	public void testReadFromFile_ordinaryInput_readsSuccessfully() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("00 01 02 03 04 05 06 07 08 09 0a");
		data.read(file);

		assertArrayEquals(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, data.getData());
	}

	@Test
	public void testReadFromStream_stopsAtMarker() throws IOException {
		InputStream stream = utils.makeInputStream("00 01 FF 23");
		data.read(stream);

		assertArrayEquals(new byte[] {0, 1}, data.getData());
	}

	@Test
	public void testReadFromFile_stopsAtMarker() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("00 03 FF 45");
		data.read(file);

		assertArrayEquals(new byte[] {0, 3}, data.getData());
	}

	@Test
	public void testReadFromStream_acceptsEscapedFF() throws IOException {
		InputStream stream = utils.makeInputStream("00 01 FF 00 23");
		data.read(stream);

		assertArrayEquals(new byte[] {0, 1, -1, 35}, data.getData());
	}

	@Test
	public void testReadFromFile_acceptsEscapedFF() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("00 03 FF 00 10");
		data.read(file);

		assertArrayEquals(new byte[] {0, 3, -1, 16}, data.getData());
	}

	@Test
	public void testReadFromStream_acceptsTrailingFF_InLaxMode() throws IOException {
		InputStream stream = utils.makeInputStream("00 01 FF");
		data.setDataMode(DataMode.LAX);
		data.read(stream);

		assertArrayEquals(new byte[] {0, 1, -1}, data.getData());
	}

	@Test
	public void testReadFromFile_acceptsTrailingFF_InLaxMode() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("00 03 FF");
		data.setDataMode(DataMode.LAX);
		data.read(file);

		assertArrayEquals(new byte[] {0, 3, -1}, data.getData());
	}

	@Test(expected=InvalidJpegFormat.class)
	public void testReadFromStream_rejectsTrailingFF_InStrictMode() throws IOException {
		InputStream stream = utils.makeInputStream("00 01 FF");
		data.read(stream);
	}

	@Test(expected=InvalidJpegFormat.class)
	public void testReadFromFile_rejectsTrailingFF_InStrictMode() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("00 03 FF");
		data.read(file);
	}

	@Test
	public void testWrite_writesEscapedFF() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		data.setDataMode(DataMode.LAX);
		data.setData(new byte[] {1, 2, 3, -1, 0, -1, 5});

		data.write(output);
		assertArrayEquals(utils.makeByteArray("01 02 03 FF 00 00 FF 00 05"), output.toByteArray());
	}

	@Test
	public void testWrite_writesTrailingFF() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		InputStream stream = utils.makeInputStream("00 01 FF");
		data.setDataMode(DataMode.LAX);
		data.read(stream);

		data.write(output);
		assertArrayEquals(utils.makeByteArray("00 01 FF"), output.toByteArray());
	}

	@Test
	public void testValidate_withTrailingFF_reportsAProblem() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		InputStream stream = utils.makeInputStream("00 01 FF");
		data.setDataMode(DataMode.LAX);
		data.read(stream);

		List<Exception> list = data.validate();
		assertEquals(1, list.size());
	}

	@Test(expected=InvalidJpegFormat.class)
	public void testValidateMode_withTrailingFF_throwsErrorIfTryingToSwitchStrict() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		InputStream stream = utils.makeInputStream("00 01 FF");
		data.setDataMode(DataMode.LAX);
		data.read(stream);

		data.setDataMode(DataMode.STRICT);
	}

	@Test
	public void equals_twoEmptyEntropyData_equal() throws IOException {
		assertEquals(data, new EntropyData());
	}

	@Test
	public void equals_twoDifferentEntropyData_notEqual() throws IOException {
		data.setData(utils.makeByteArray("00 01"));
		assertFalse(data.equals(new EntropyData()));
	}
}
