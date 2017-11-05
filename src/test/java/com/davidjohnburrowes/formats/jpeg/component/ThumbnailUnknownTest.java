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

import com.davidjohnburrowes.format.jpeg.component.ThumbnailUnknown;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import com.davidjohnburrowes.io.LimitingDataInput;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ThumbnailUnknownTest {
	private ThumbnailUnknown thumbnail;
	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
		thumbnail = new ThumbnailUnknown();
	}

	@Test
	public void getSizeOnDisk_byDefault_isZero() {
		assertEquals(0, thumbnail.getSizeOnDisk());
	}

	@Test
	public void getSizeOnDisk_withPixelData_reflectsPizelDataSize() {
		thumbnail.setPixelBytes(new byte[10]);
		assertEquals(10, thumbnail.getSizeOnDisk());
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
		thumbnail.setPixelBytes(new byte[2]);
		assertFalse(thumbnail.equals(new ThumbnailUnknown()));
	}

	@Test
	public void readParams_withLimitingInput_readsAllBytes() throws IOException {
		InputStream stream = utils.makeInputStream("01 02 03 04 05 06 07 08");
		LimitingDataInput input = new LimitingDataInput(new DataInputStream(stream), 7);
		thumbnail.readParameters(input);
		assertEquals(7, thumbnail.getPixelBytes().length);
	}

	@Test
	public void write_withPixelData_writesExpectedData() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] bytes = new byte[] {1, 2, 3, 4, 5, 6, 7};
		thumbnail.setPixelBytes(bytes);

		thumbnail.write(output);

		byte[] expectedBytes = utils.makeByteArray("01 02 03 04 05 06 07");

		assertArrayEquals(expectedBytes, output.toByteArray());
	}
}
