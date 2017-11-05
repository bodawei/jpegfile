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
package com.davidjohnburrowes.format.encode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Bin2HexTest {

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testBasicEncoding() throws IOException {
		Bin2Hex encoder = new Bin2Hex();
		byte[] inputBuffer = new byte[256];
		byte[] answer = new byte[(256*3) - 1];
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ByteArrayInputStream inputStream;

		for (int index = 0; index < 256; index++) {
			inputBuffer[index] = (byte) index;
		}

		for (int ansHigh = 0; ansHigh < 16; ansHigh++) {
			for (int ansLow = 0; ansLow < 16; ansLow++) {
				if (ansHigh < 10) {
					answer[((ansHigh*16) + ansLow) * 3] = (byte)(ansHigh + '0');
				} else {
					answer[((ansHigh*16) + ansLow) * 3] = (byte)((ansHigh-10) + 'a');
				}

				if (ansLow < 10) {
					answer[(((ansHigh*16) + ansLow) * 3) + 1] = (byte)(ansLow + '0');
				} else {
					answer[(((ansHigh*16) + ansLow) * 3) + 1] = (byte)((ansLow-10) + 'a');
				}

				if ((ansHigh != 15) || (ansLow != 15)) {
					answer[(((ansHigh*16) + ansLow) * 3) + 2] = ' ';
				}
			}
		}

		inputStream = new ByteArrayInputStream(inputBuffer);

		encoder.convert(inputStream, outputStream);

		byte[] output = outputStream.toByteArray();
		assertArrayEquals(answer, output);
	}

    @Test
    public void testLineBreakingWorks() throws IOException {
		Bin2Hex encoder = new Bin2Hex();
		byte[] inputBuffer = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a};
		byte[] answer = {
			'0', '0'+1, ' ', '0', '0'+2, ' ', '0', '0'+3, 0x0a,
			'0', '0'+4, ' ', '0', '0'+5, ' ', '0', '0'+6, 0x0a,
			'0', '0'+7, ' ', '0', '0'+8, ' ', '0', '0'+9, 0x0a,
			'0', 'a' };
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ByteArrayInputStream inputStream;

		encoder.setBytesPerLine(3);

		inputStream = new ByteArrayInputStream(inputBuffer);

		encoder.convert(inputStream, outputStream);

		byte[] output = outputStream.toByteArray();
		assertArrayEquals(answer, output);
	}

}