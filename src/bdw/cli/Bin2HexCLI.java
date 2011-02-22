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

import bdw.formats.encode.Bin2Hex;
import java.io.IOException;

/**
 * CLI interface to the Bin2Hex encoder
 */
public class Bin2HexCLI {

    /**
	 * Right now this is a lame thing. It ignores its inputs and just reads from
	 * standard in and writes to standard out.
	 *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Bin2Hex encoder = new Bin2Hex();

		if (args[0].toLowerCase().equals("--bytesperline")) {
			int limit = Integer.valueOf(args[1]);
			encoder.setBytesPerLine(limit);
		}

		try {
			encoder.convert(System.in, System.out);
			System.exit(0);
		} catch (IOException exception) {
			System.err.print(exception);
			exception.printStackTrace(System.err);
			System.exit(1);
		}
    }

}
