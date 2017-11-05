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
package com.davidjohnburrowes.formats.jpeg.component;

import com.davidjohnburrowes.format.jpeg.component.Thumbnail;
import com.davidjohnburrowes.format.jpeg.component.Thumbnail3BytesPerPixel;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import com.davidjohnburrowes.io.LimitingDataInput;
import com.davidjohnburrowes.util.Size;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class Thumbnail3BytesPerPixelTest {
	private Thumbnail3BytesPerPixel thumbnail;
	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
		thumbnail = new Thumbnail3BytesPerPixel();
	}

	@Test
	public void setWidth_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(thumbnail, "width", Size.BYTE,
				  0, 255));
	}

	@Test
	public void setHeight_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(thumbnail, "height", Size.BYTE,
				  0, 255));
	}

	@Test
	public void getSizeOnDisk_byDefault_isTwo() {
		assertEquals(2, thumbnail.getSizeOnDisk());
	}

	@Test
	public void getSizeOnDisk_withData_isCorrect() {
		byte[] bytes = new byte[6];
		thumbnail.setPixelBytes(bytes);
		assertEquals(8, thumbnail.getSizeOnDisk());
	}

	@Test
	public void getSizeOnDisk_withNoPixelData_isNotAffectedByWidthAndHeight() {
		thumbnail.setWidth(10);
		thumbnail.setHeight(41);
		assertEquals(2, thumbnail.getSizeOnDisk());
	}

	@Test
	public void getSizeOnDisk_isZero() {
		assertEquals(2, thumbnail.getSizeOnDisk());
	}

	@Test(expected=IllegalArgumentException.class)
	public void setPixelBytes_withNull_throwsException() throws IOException {
		thumbnail.setPixelBytes(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void setPixelBytes_withTooManyBytes_throwsException() throws IOException {
		thumbnail.setPixelBytes(new byte[(256* 256 * 3) + 3]);
	}

	@Test(expected=IllegalArgumentException.class)
	public void setPixelBytes_withNonMultipleOfThree_throwsException() throws IOException {
		thumbnail.setPixelBytes(new byte[4]);
	}

	@Test
	public void equals_withSelf_isTrue() {
		assertTrue(thumbnail.equals(thumbnail));
	}

	@Test
	public void equals_withObject_isFalse() {
		assertFalse(thumbnail.equals(new Object()));
	}

	@Test
	public void equals_withDifferentThumbnail_isFalse() {
		thumbnail.setWidth(20);
		assertFalse(thumbnail.equals(new Thumbnail()));
	}

	@Test
	public void validate_withInvalidThumbnail() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		thumbnail.setWidth(1);
		thumbnail.setHeight(4);


		assertEquals(1, thumbnail.validate().size());
	}

	@Test
	public void read_goodInput_readSuccessfully() throws IOException {
		InputStream stream = utils.makeInputStream("00 FF");

		thumbnail.readParameters(new LimitingDataInput(new DataInputStream(stream), 2));
		Thumbnail3BytesPerPixel expected = new Thumbnail3BytesPerPixel();
		expected.setWidth(0);
		expected.setHeight(255);

		assertEquals(expected, thumbnail);
	}

	@Test
	public void read_goodInputWithThumbnail_readSuccessfully() throws IOException {
		InputStream stream = utils.makeInputStream("01 02 01 02 03 11 12 13");

		thumbnail.readParameters(new LimitingDataInput(new DataInputStream(stream), 8));
		Thumbnail3BytesPerPixel expected = new Thumbnail3BytesPerPixel();
		expected.setWidth(1);
		expected.setHeight(2);
		byte[] bytes = new byte[] {1, 2, 3, 17, 18, 19};
		expected.setPixelBytes(bytes);

		assertEquals(expected, thumbnail);
	}

	@Test
	public void write_exampleInstance_generatesGoodDataStream2() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		thumbnail.setWidth(1);
		thumbnail.setHeight(4);
		byte[] bytes = new byte[] {1, 2, 3, 17, 18, 19, 33, 34, 35, 49, 50, 51};
		thumbnail.setPixelBytes(bytes);
		thumbnail.write(output);

		byte[] expectedBytes = utils.makeByteArray("01 04 01 02 03 11 12 13 21 22 23 31 32 33");

		assertArrayEquals(expectedBytes, output.toByteArray());
	}
}
