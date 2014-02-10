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
package bdw.format.encode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Given an input stream of textual data, interpret as a stream of hexadecimal digits.
 * and write out a binary version of the same.
 * Thus, an input of "40 41 42" would be written out as "ABC".
 *
 * Notes:
 * <ul>
 * <li>0-9a-fA-F are considered hexadecimal digits</li>
 * <li>White space (space, newlines, etc) is ignored</li>
 * <li>The # character is interpreted as line comment. Everything after it to a newline is ignored.</li>
 * <li>/* ... * / can be used to surround things that should be ignore</li>
 * <li>Other characters are simply ignored.</li>
 * <li>If a single trailing nibble is at the end of the file, it will simply be ignored</li>
 * </ul>
 *
 * Instances of this class have a variety of configuration properties that control
 * how the output is formatted, exactly.
 */
public class Hex2Bin {

	/**
	 * Using any configuration properties set on the instance, convert the contents of
	 * the input stream to the output stream.
	 *
	 * @param input The stream to read characters from (not null)
	 * @param outputStream The stream to write the binary representation to (not null)
	 * @throws IOException If something goes wonky on io.
	 * @throws IllegalArgumentException If either argument is null
	 */
	public void convert(InputStream inputStream, OutputStream output) throws IOException {
		if (inputStream == null) {
			throw new IllegalArgumentException("Input may not be null");
		}

		if (output == null) {
			throw new IllegalArgumentException("Output may not be null");
		}

		BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
		int aChar = input.read();
		boolean ignoreChars = false;
		boolean doHighNibble = true;
		int aByte = 0;

		while (aChar != -1) {
			switch (aChar) {
				case '#':
					ignoreChars = true;
					break;
				case '\n':
					ignoreChars = false;
					break;
				case '\r':
					ignoreChars = false;
					break;
				case '/':
					ignoreChars = false;
					input.mark(1);
					aChar = input.read();
					if (aChar != '*') {
						input.reset();
					} else {
						skipComment(input);
					}
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case 'a':
				case 'A':
				case 'b':
				case 'B':
				case 'c':
				case 'C':
				case 'd':
				case 'D':
				case 'e':
				case 'E':
				case 'f':
				case 'F':
					if (ignoreChars == false) {
						int nibble;
						if ((aChar >= '0') && (aChar <= '9')) {
							nibble = aChar - '0';
						} else if ((aChar >= 'A') && (aChar <= 'F')) {
							nibble = (aChar - 'A') + 10;
						} else {
							nibble = (aChar - 'a') + 10;
						}

						if (doHighNibble) {
							aByte = nibble << 4;
							doHighNibble = false;
						} else {
							aByte = aByte | nibble;
							doHighNibble = true;
							output.write(aByte);
							aByte = 0;
						}
					}
					break;
				default:
					break;
			}

			aChar = input.read();
		}
		output.flush();
	}

	protected void skipComment(BufferedReader stream) throws IOException {
		boolean sawStar = false;

		while (true) {
			int aChar = stream.read();
			switch (aChar) {
				case '*':
					sawStar = true;
					break;
				case '/':
					if (sawStar == true) {
						return;
					} else {
						sawStar = false;
						stream.mark(1);
						aChar = stream.read();
						if (aChar != '*') {
							stream.reset();
						} else {
							skipComment(stream);
						}
					}
					break;
				default:
					sawStar = false;
					break;
			}
		}
	}
}
