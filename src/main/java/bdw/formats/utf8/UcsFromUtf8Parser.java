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

package bdw.formats.utf8;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads Universal Character Set characters from a utf8 encoding in a the provided input stream.
 */
public class UcsFromUtf8Parser {

	protected VarIntParser intReader;
	protected boolean strict;
	
	public UcsFromUtf8Parser(InputStream stream) {
		intReader = new VarIntParser(stream);
		strict = true;
	}

	/**
	 * @param value the value for the strict mode
	 */
	public void setStrict(boolean value) {
		intReader.setStrict(value);
		strict = value;
	}

	/**
	 * @return the strict mode
	 */
	public boolean getStrict() {
		return strict;
	}

	/**
	 * @return True if this is at the end of the input stream
	 * @throws IOException If something goes wrong when touching the input stream
	 */
	public boolean atEos() throws IOException {
		return intReader.atEos();
	}

	/**
	 * Reads a character from the input stream
	 * @return The character read
	 * @throws IOException General IO problem
	 * @throws EOFException At the end of stream
	 * @throws MalformedInt The integer representation of the character in the input stream was invalid
	 * @throws InvalidUcsChar The integer read from the stream doesn't correspond to a unicode character
	 */
	public char readChar() throws IOException, EOFException, InvalidUcsCharException, MalformedIntException {
		int intRep = intReader.readInt();
		if (strict) {
			if (((intRep >= 0xD800) && (intRep <= 0xDFFF)) || (intRep > 0x10FFFF)) {
				throw new InvalidUcsCharException(intRep);
			}
		}
		return (char) intRep;
	}
}
