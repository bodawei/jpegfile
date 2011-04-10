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

package bdw.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import bdw.formats.utf8.InvalidUcsCharException;
import bdw.formats.utf8.MalformedIntException;
import bdw.formats.utf8.UcsFromUtf8Parser;

/**
 * Routine which scans a file to see if it is a valid utf8 file. If not, it reports
 * a message about where the malformed input is.  If it is, it reports a message to that effect.
 */
public class ValidateUtf8File {
	public static void main(String[] args) {
		try {
			InterceptingInputStream iis;
			
			if (args.length == 0) {
				iis = new InterceptingInputStream(System.in); 
			} else {
				iis = new InterceptingInputStream(new FileInputStream(new File(args[0])));
			}
			UcsFromUtf8Parser reader = new UcsFromUtf8Parser(iis);
			boolean allOK = true;
			int byteCount = 0;
			int lineCount = 0;
			int colCount = 0;
			char lastChar = '\000';
			
			while ( ! reader.atEos() ) {
				try {
					char nextChar = reader.readChar();
					if ((nextChar == '\n') || (nextChar == '\r')) {
						if ((lastChar == '\r') && (nextChar == '\n')) {
							// do nothing
						} else {
							lineCount ++;
							colCount = 0;
						}
					} else {
						colCount ++;
					}
					lastChar = nextChar;
					byteCount +=  iis.getInterceptedLength();
					iis.flushInterception();
				} catch (InvalidUcsCharException e) {
					byteCount +=  iis.getInterceptedLength();
					iis.flushInterception();
					System.out.println("Line " + lineCount + " Col " + colCount + " :: Invalid char " + e.getBadChar() + " @ byte " + byteCount + " - " + e.getMessage());
					allOK = false;
				} catch (MalformedIntException e) {
					byteCount +=  iis.getInterceptedLength();
					iis.flushInterception();
					System.out.printf("Line %d col %d : Invalid integer (%d  0x%x) : @byte %d - %s\n", lineCount, colCount, e.getBadValue(), e.getBadValue(), byteCount, e.getMessage());
					allOK = false;
				}
			}
			if (allOK) {
				System.out.println("It is a valid UTF8 file");
				System.exit(0);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
		
	}
}
