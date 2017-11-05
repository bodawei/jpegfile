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
import java.io.InputStream;
import java.io.StringReader;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Hex2BinTest {

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
		Hex2Bin encoder = new Hex2Bin();
		byte[] answer = {0x41, 0x42, 0x43, 0x44};
		InputStream inputStream = buildInputStreamFromString("41 42\n43     44");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		encoder.convert(inputStream, outputStream);

		byte[] output = outputStream.toByteArray();
		assertArrayEquals(answer, output);
	}

    @Test
    public void testTrailingCharacterIgnored() throws IOException {
		Hex2Bin encoder = new Hex2Bin();
		byte[] answer = {0x41, 0x42, 0x43, 0x44};
		InputStream inputStream = buildInputStreamFromString("41 42\n43     44 4");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		encoder.convert(inputStream, outputStream);

		byte[] output = outputStream.toByteArray();
		assertArrayEquals(answer, output);
	}


    @Test
    public void testGibberishIsIgnored() throws IOException {
		Hex2Bin encoder = new Hex2Bin();
		byte[] answer = {0x41, 0x42, 0x43, 0x44};
		InputStream inputStream = buildInputStreamFromString("41 42 Wush! \n43     44");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		encoder.convert(inputStream, outputStream);

		byte[] output = outputStream.toByteArray();
		assertArrayEquals(answer, output);
	}

    @Test
    public void testCommentIsHonored() throws IOException {
		Hex2Bin encoder = new Hex2Bin();
		byte[] answer = {0x41, 0x42, 0x43, 0x44};
		InputStream inputStream = buildInputStreamFromString("41 4#2 Wush! \n243     44");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		encoder.convert(inputStream, outputStream);

		byte[] output = outputStream.toByteArray();
		assertArrayEquals(answer, output);
	}

    @Test
    public void testGetConvertsWithoutCorruption() throws IOException {
		Hex2Bin encoder = new Hex2Bin();
		byte[] answer = {0x23, 0x21, 0x09, 0x2f, 0x62, 0x69, 0x6e, 0x2f, 0x73, 0x68, 0x0a, 0x0a, 0x23, 0x20, 0x52, 0x69, 0x67, 0x68, 0x74, 0x20, 0x6e, 0x6f, 0x77, 0x2c, 0x20, 0x74, 0x68, 0x69, 0x73, 0x20, 0x69, 0x73, 0x20, 0x61, 0x20, 0x6c, 0x61, 0x6d, 0x65, 0x20, 0x77, 0x72, 0x61, 0x70, 0x70, 0x65, 0x72, 0x2e, 0x20, 0x20, 0x55, 0x73, 0x61, 0x67, 0x65, 0x20, 0x69, 0x73, 0x3a, 0x0a, 0x23, 0x20, 0x3e, 0x20, 0x63, 0x61, 0x74, 0x20, 0x6d, 0x79, 0x46, 0x69, 0x6c, 0x65, 0x20, 0x7c, 0x20, 0x62, 0x69, 0x6e, 0x32, 0x68, 0x65, 0x78, 0x2e, 0x73, 0x68, 0x20, 0x3e, 0x20, 0x68, 0x65, 0x78, 0x46, 0x69, 0x6c, 0x65, 0x2e, 0x74, 0x78, 0x74, 0x0a};
		InputStream inputStream = buildInputStreamFromString("23 21 09 2f 62 69 6e 2f 73 68 0a 0a 23 20 52 69 67 68 74 20 6e 6f 77 2c 20 74 68 69 73 20 69 73 20 61 20 6c 61 6d 65 20 77 72 61 70 70 65 72 2e 20 20 55 73 61 67 65 20 69 73 3a 0a 23 20 3e 20 63 61 74 20 6d 79 46 69 6c 65 20 7c 20 62 69 6e 32 68 65 78 2e 73 68 20 3e 20 68 65 78 46 69 6c 65 2e 74 78 74 0a");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		encoder.convert(inputStream, outputStream);

		byte[] output = outputStream.toByteArray();
		assertArrayEquals(answer, output);
	}

    @Test
    public void testAllBytesConvert() throws IOException {
		Hex2Bin encoder = new Hex2Bin();
		byte[] answer = {(byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08, (byte)0x09, (byte)0x0a, (byte)0x0b, (byte)0x0c, (byte)0x0d, (byte)0x0e, (byte)0x0f, (byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13, (byte)0x14, (byte)0x15, (byte)0x16, (byte)0x17, (byte)0x18, (byte)0x19, (byte)0x1a, (byte)0x1b, (byte)0x1c, (byte)0x1d, (byte)0x1e, (byte)0x1f, (byte)0x20, (byte)0x21, (byte)0x22, (byte)0x23, (byte)0x24, (byte)0x25, (byte)0x26, (byte)0x27, (byte)0x28, (byte)0x29, (byte)0x2a, (byte)0x2b, (byte)0x2c, (byte)0x2d, (byte)0x2e, (byte)0x2f, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37, (byte)0x38, (byte)0x39, (byte)0x3a, (byte)0x3b, (byte)0x3c, (byte)0x3d, (byte)0x3e, (byte)0x3f, (byte)0x40, (byte)0x41, (byte)0x42, (byte)0x43, (byte)0x44, (byte)0x45, (byte)0x46, (byte)0x47, (byte)0x48, (byte)0x49, (byte)0x4a, (byte)0x4b, (byte)0x4c, (byte)0x4d, (byte)0x4e, (byte)0x4f, (byte)0x50, (byte)0x51, (byte)0x52, (byte)0x53, (byte)0x54, (byte)0x55, (byte)0x56, (byte)0x57, (byte)0x58, (byte)0x59, (byte)0x5a, (byte)0x5b, (byte)0x5c, (byte)0x5d, (byte)0x5e, (byte)0x5f, (byte)0x60, (byte)0x61, (byte)0x62, (byte)0x63, (byte)0x64, (byte)0x65, (byte)0x66, (byte)0x67, (byte)0x68, (byte)0x69, (byte)0x6a, (byte)0x6b, (byte)0x6c, (byte)0x6d, (byte)0x6e, (byte)0x6f, (byte)0x70, (byte)0x71, (byte)0x72, (byte)0x73, (byte)0x74, (byte)0x75, (byte)0x76, (byte)0x77, (byte)0x78, (byte)0x79, (byte)0x7a, (byte)0x7b, (byte)0x7c, (byte)0x7d, (byte)0x7e, (byte)0x7f, (byte)0x80, (byte)0x81, (byte)0x82, (byte)0x83, (byte)0x84, (byte)0x85, (byte)0x86, (byte)0x87, (byte)0x88, (byte)0x89, (byte)0x8a, (byte)0x8b, (byte)0x8c, (byte)0x8d, (byte)0x8e, (byte)0x8f, (byte)0x90, (byte)0x91, (byte)0x92, (byte)0x93, (byte)0x94, (byte)0x95, (byte)0x96, (byte)0x97, (byte)0x98, (byte)0x99, (byte)0x9a, (byte)0x9b, (byte)0x9c, (byte)0x9d, (byte)0x9e, (byte)0x9f, (byte)0xa0, (byte)0xa1, (byte)0xa2, (byte)0xa3, (byte)0xa4, (byte)0xa5, (byte)0xa6, (byte)0xa7, (byte)0xa8, (byte)0xa9, (byte)0xaa, (byte)0xab, (byte)0xac, (byte)0xad, (byte)0xae, (byte)0xaf, (byte)0xb0, (byte)0xb1, (byte)0xb2, (byte)0xb3, (byte)0xb4, (byte)0xb5, (byte)0xb6, (byte)0xb7, (byte)0xb8, (byte)0xb9, (byte)0xba, (byte)0xbb, (byte)0xbc, (byte)0xbd, (byte)0xbe, (byte)0xbf, (byte)0xc0, (byte)0xc1, (byte)0xc2, (byte)0xc3, (byte)0xc4, (byte)0xc5, (byte)0xc6, (byte)0xc7, (byte)0xc8, (byte)0xc9, (byte)0xca, (byte)0xcb, (byte)0xcc, (byte)0xcd, (byte)0xce, (byte)0xcf, (byte)0xd0, (byte)0xd1, (byte)0xd2, (byte)0xd3, (byte)0xd4, (byte)0xd5, (byte)0xd6, (byte)0xd7, (byte)0xd8, (byte)0xd9, (byte)0xda, (byte)0xdb, (byte)0xdc, (byte)0xdd, (byte)0xde, (byte)0xdf, (byte)0xe0, (byte)0xe1, (byte)0xe2, (byte)0xe3, (byte)0xe4, (byte)0xe5, (byte)0xe6, (byte)0xe7, (byte)0xe8, (byte)0xe9, (byte)0xea, (byte)0xeb, (byte)0xec, (byte)0xed, (byte)0xee, (byte)0xef, (byte)0xf0, (byte)0xf1, (byte)0xf2, (byte)0xf3, (byte)0xf4, (byte)0xf5, (byte)0xf6, (byte)0xf7, (byte)0xf8, (byte)0xf9, (byte)0xfa, (byte)0xfb, (byte)0xfc, (byte)0xfd, (byte)0xfe, (byte)0xff, (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08, (byte)0x09, (byte)0x0A, (byte)0x0B, (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F, (byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13, (byte)0x14, (byte)0x15, (byte)0x16, (byte)0x17, (byte)0x18, (byte)0x19, (byte)0x1A, (byte)0x1B, (byte)0x1C, (byte)0x1D, (byte)0x1E, (byte)0x1F, (byte)0x20, (byte)0x21, (byte)0x22, (byte)0x23, (byte)0x24, (byte)0x25, (byte)0x26, (byte)0x27, (byte)0x28, (byte)0x29, (byte)0x2A, (byte)0x2B, (byte)0x2C, (byte)0x2D, (byte)0x2E, (byte)0x2F, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37, (byte)0x38, (byte)0x39, (byte)0x3A, (byte)0x3B, (byte)0x3C, (byte)0x3D, (byte)0x3E, (byte)0x3F, (byte)0x40, (byte)0x41, (byte)0x42, (byte)0x43, (byte)0x44, (byte)0x45, (byte)0x46, (byte)0x47, (byte)0x48, (byte)0x49, (byte)0x4A, (byte)0x4B, (byte)0x4C, (byte)0x4D, (byte)0x4E, (byte)0x4F, (byte)0x50, (byte)0x51, (byte)0x52, (byte)0x53, (byte)0x54, (byte)0x55, (byte)0x56, (byte)0x57, (byte)0x58, (byte)0x59, (byte)0x5A, (byte)0x5B, (byte)0x5C, (byte)0x5D, (byte)0x5E, (byte)0x5F, (byte)0x60, (byte)0x61, (byte)0x62, (byte)0x63, (byte)0x64, (byte)0x65, (byte)0x66, (byte)0x67, (byte)0x68, (byte)0x69, (byte)0x6A, (byte)0x6B, (byte)0x6C, (byte)0x6D, (byte)0x6E, (byte)0x6F, (byte)0x70, (byte)0x71, (byte)0x72, (byte)0x73, (byte)0x74, (byte)0x75, (byte)0x76, (byte)0x77, (byte)0x78, (byte)0x79, (byte)0x7A, (byte)0x7B, (byte)0x7C, (byte)0x7D, (byte)0x7E, (byte)0x7F, (byte)0x80, (byte)0x81, (byte)0x82, (byte)0x83, (byte)0x84, (byte)0x85, (byte)0x86, (byte)0x87, (byte)0x88, (byte)0x89, (byte)0x8A, (byte)0x8B, (byte)0x8C, (byte)0x8D, (byte)0x8E, (byte)0x8F, (byte)0x90, (byte)0x91, (byte)0x92, (byte)0x93, (byte)0x94, (byte)0x95, (byte)0x96, (byte)0x97, (byte)0x98, (byte)0x99, (byte)0x9A, (byte)0x9B, (byte)0x9C, (byte)0x9D, (byte)0x9E, (byte)0x9F, (byte)0xA0, (byte)0xA1, (byte)0xA2, (byte)0xA3, (byte)0xA4, (byte)0xA5, (byte)0xA6, (byte)0xA7, (byte)0xA8, (byte)0xA9, (byte)0xAA, (byte)0xAB, (byte)0xAC, (byte)0xAD, (byte)0xAE, (byte)0xAF, (byte)0xB0, (byte)0xB1, (byte)0xB2, (byte)0xB3, (byte)0xB4, (byte)0xB5, (byte)0xB6, (byte)0xB7, (byte)0xB8, (byte)0xB9, (byte)0xBA, (byte)0xBB, (byte)0xBC, (byte)0xBD, (byte)0xBE, (byte)0xBF, (byte)0xC0, (byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC7, (byte)0xC8, (byte)0xC9, (byte)0xCA, (byte)0xCB, (byte)0xCC, (byte)0xCD, (byte)0xCE, (byte)0xCF, (byte)0xD0, (byte)0xD1, (byte)0xD2, (byte)0xD3, (byte)0xD4, (byte)0xD5, (byte)0xD6, (byte)0xD7, (byte)0xD8, (byte)0xD9, (byte)0xDA, (byte)0xDB, (byte)0xDC, (byte)0xDD, (byte)0xDE, (byte)0xDF, (byte)0xE0, (byte)0xE1, (byte)0xE2, (byte)0xE3, (byte)0xE4, (byte)0xE5, (byte)0xE6, (byte)0xE7, (byte)0xE8, (byte)0xE9, (byte)0xEA, (byte)0xEB, (byte)0xEC, (byte)0xED, (byte)0xEE, (byte)0xEF, (byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3, (byte)0xF4, (byte)0xF5, (byte)0xF6, (byte)0xF7, (byte)0xF8, (byte)0xF9, (byte)0xFA, (byte)0xFB, (byte)0xFC, (byte)0xFD, (byte)0xFE, (byte)0xFF};
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		InputStream inputStream = buildInputStreamFromString("00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f 20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f 30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f 40 41 42 43 44 45 46 47 48 49 4a 4b 4c 4d 4e 4f 50 51 52 53 54 55 56 57 58 59 5a 5b 5c 5d 5e 5f 60 61 62 63 64 65 66 67 68 69 6a 6b 6c 6d 6e 6f 70 71 72 73 74 75 76 77 78 79 7a 7b 7c 7d 7e 7f 80 81 82 83 84 85 86 87 88 89 8a 8b 8c 8d 8e 8f 90 91 92 93 94 95 96 97 98 99 9a 9b 9c 9d 9e 9f a0 a1 a2 a3 a4 a5 a6 a7 a8 a9 aa ab ac ad ae af b0 b1 b2 b3 b4 b5 b6 b7 b8 b9 ba bb bc bd be bf c0 c1 c2 c3 c4 c5 c6 c7 c8 c9 ca cb cc cd ce cf d0 d1 d2 d3 d4 d5 d6 d7 d8 d9 da db dc dd de df e0 e1 e2 e3 e4 e5 e6 e7 e8 e9 ea eb ec ed ee ef f0 f1 f2 f3 f4 f5 f6 f7 f8 f9 fa fb fc fd fe ff 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F 20 21 22 23 24 25 26 27 28 29 2A 2B 2C 2D 2E 2F 30 31 32 33 34 35 36 37 38 39 3A 3B 3C 3D 3E 3F 40 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50 51 52 53 54 55 56 57 58 59 5A 5B 5C 5D 5E 5F 60 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F 70 71 72 73 74 75 76 77 78 79 7A 7B 7C 7D 7E 7F 80 81 82 83 84 85 86 87 88 89 8A 8B 8C 8D 8E 8F 90 91 92 93 94 95 96 97 98 99 9A 9B 9C 9D 9E 9F A0 A1 A2 A3 A4 A5 A6 A7 A8 A9 AA AB AC AD AE AF B0 B1 B2 B3 B4 B5 B6 B7 B8 B9 BA BB BC BD BE BF C0 C1 C2 C3 C4 C5 C6 C7 C8 C9 CA CB CC CD CE CF D0 D1 D2 D3 D4 D5 D6 D7 D8 D9 DA DB DC DD DE DF E0 E1 E2 E3 E4 E5 E6 E7 E8 E9 EA EB EC ED EE EF F0 F1 F2 F3 F4 F5 F6 F7 F8 F9 FA FB FC FD FE FF");

		encoder.convert(inputStream, outputStream);

		byte[] output = outputStream.toByteArray();
		assertArrayEquals(answer, output);
	}

    @Test
    public void testCanUseComment() throws IOException {
		Hex2Bin encoder = new Hex2Bin();
		byte[] answer = {(byte)0x00, (byte)0x01};
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		InputStream inputStream = buildInputStreamFromString("00 /* 05 */ 01");

		encoder.convert(inputStream, outputStream);

		byte[] output = outputStream.toByteArray();
		assertArrayEquals(answer, output);
	}


    @Test
    public void testCanEmbedComments() throws IOException {
		Hex2Bin encoder = new Hex2Bin();
		byte[] answer = {(byte)0x00, (byte)0x01};
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		InputStream inputStream = buildInputStreamFromString("00 /* 02 /* 05 */ 03 */ 01");

		encoder.convert(inputStream, outputStream);

		byte[] output = outputStream.toByteArray();
		assertArrayEquals(answer, output);
	}

    @Test
    public void testFunkyComment() throws IOException {
		Hex2Bin encoder = new Hex2Bin();
		byte[] answer = {(byte)0x00, (byte)0x05, (byte)0x03, (byte)0x01};
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		InputStream inputStream = buildInputStreamFromString("00 /* 02 */* 05 */ 03 */ 01");

		encoder.convert(inputStream, outputStream);

		byte[] output = outputStream.toByteArray();
		assertArrayEquals(answer, output);
	}


	protected InputStream buildInputStreamFromString(String inputString) throws IOException {
		StringReader inputReader = new StringReader(inputString);
		byte[] rawInputBytes = new byte[inputString.length()];
		int aChar = inputReader.read();
		int index = 0;
		while (aChar != -1) {
			rawInputBytes[index] = (byte)aChar;
			aChar = inputReader.read();
			index++;
		}

		return new ByteArrayInputStream(rawInputBytes);
	}


}