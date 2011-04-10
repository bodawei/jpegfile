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
package bdw.formats.jpeg;

import bdw.formats.encode.Hex2Bin;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;

public class TestUtils {

	public byte[] makeByteArrayFromString(String rawInput) throws IOException {
		StringReader inputReader = new StringReader(rawInput);
		byte[] rawInputBytes = new byte[rawInput.length()];
		int aChar = inputReader.read();
		int index = 0;
		while (aChar != -1) {
			rawInputBytes[index] = (byte) aChar;
			aChar = inputReader.read();
			index++;
		}

		Hex2Bin encoder = new Hex2Bin();
		InputStream inputStream = new ByteArrayInputStream(rawInputBytes);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		encoder.convert(inputStream, outputStream);

		return outputStream.toByteArray();
	}

	public InputStream makeInputStreamFromString(String rawInput) throws IOException {
		return new ByteArrayInputStream(makeByteArrayFromString(rawInput));
	}

	public RandomAccessFile makeRandomAccessFile(String data) throws IOException {
		File temp = File.createTempFile("prefix", "suffix");
		FileOutputStream output = new FileOutputStream(temp);

		StringReader inputReader = new StringReader(data);
		byte[] rawInputBytes = new byte[data.length()];
		int aChar = inputReader.read();
		int index = 0;
		while (aChar != -1) {
			rawInputBytes[index] = (byte) aChar;
			aChar = inputReader.read();
			index++;
		}

		Hex2Bin encoder = new Hex2Bin();
		InputStream inputStream = new ByteArrayInputStream(rawInputBytes);

		encoder.convert(inputStream, output);

		output.flush();
		output.close();
		
		return new RandomAccessFile(temp, "r");
	}
}
