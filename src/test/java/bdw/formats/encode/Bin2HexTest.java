package bdw.formats.encode;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bodawei
 */
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