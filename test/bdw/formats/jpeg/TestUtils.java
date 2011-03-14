/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdw.formats.jpeg;

import bdw.formats.encode.Hex2Bin;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 *
 * @author dburrowes
 */
public class TestUtils {

	public InputStream makeInputStreamFromString(String rawInput) throws IOException {
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

		return new ByteArrayInputStream(outputStream.toByteArray());
	}
}
