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

package bdw.formats.encode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Given an input stream of binary data, write out a textual version of the same
 * data in a hexadecimal format.  Thus, an input of "ABC" might be written out
 * as 40 41 42.
 * 
 * Instances of this class have a variety of configuration properties that control
 * how the output is formatted, exactly.
 */
public class Bin2Hex {

	/**
	 * Using any configuration properties set on the instance, convert the contents of
	 * the input stream to the output stream.
	 * 
	 * @param input The stream to read bytes from (not null)
	 * @param outputStream The stream to write the textual representation to (not null)
	 * @throws IOException If something goes wonky on io.
	 * @throws IllegalArgumentException If either argument is null
	 */
	public void convert(InputStream input, OutputStream outputStream) throws IOException {
		if (input == null) {
			throw new IllegalArgumentException("Input may not be null");
		}
		
		if (outputStream == null) {
			throw new IllegalArgumentException("Output may not be null");
		}
		
		PrintWriter output = new PrintWriter(outputStream);
		int aByte = input.read();
		int count = 0;
		
		while (aByte != -1) {
			if (count > 0) {
				output.printf(" ");
			}
			output.printf("%02x", aByte);
			
			count++;
			
			aByte = input.read();
		}
		output.flush();
	}
}
