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
package bdw.io;

import bdw.formats.jpeg.test.TestUtils;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class LimitingDataInputTest {
	protected DataInputStream lengthyInput;
	protected DataInputStream zeroByteInput;
	protected DataInputStream byteExtremes;
	protected DataInputStream shortExtremes;

    @Before
    public void setUp() throws IOException {
		TestUtils utils = new TestUtils();
		byte[] bytes = utils.makeByteArray("FF 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10 11 12 13 14 15 16 17 18 19 1a 1b1 c1 d 1e 1f");
		lengthyInput = new DataInputStream(new ByteArrayInputStream(bytes));
		bytes = new byte[0];
		zeroByteInput = new DataInputStream(new ByteArrayInputStream(bytes));

		bytes = new byte[] { (byte) 0x00, (byte) 0xFF };
		byteExtremes = new DataInputStream(new ByteArrayInputStream(bytes));

		bytes = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF };
		shortExtremes = new DataInputStream(new ByteArrayInputStream(bytes));
}

	@Test
	public void readBoolean_LimitOne_FirstWorksSecondFails() throws IOException {
		LimitingDataInput input = new LimitingDataInput(lengthyInput, 1);

		assertEquals("First ok", true, input.readBoolean());
		try {
			input.readBoolean();
			fail("Excpected a limit exception");
		} catch (LimitExceeded e) {
			assertEquals("amount exceeded", 1, e.getExceededAmount());
		}
	}

	@Test
	public void readByte_LimitOne_FirstWorksSecondFails() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(lengthyInput, 1);

		assertEquals("First ok", -1, input.readByte());
		try {
			input.readByte();
			fail("Excpected a limit exception");
		} catch (LimitExceeded e) {
			assertEquals("amount exceeded", 1, e.getExceededAmount());
		}
	}

	@Test
	public void readUnsignedByte_LimitOne_FirstWorksSecondFails() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(lengthyInput, 1);

		assertEquals("First ok", 255, input.readUnsignedByte());
		try {
			input.readUnsignedByte();
			fail("Excpected a limit exception");
		} catch (LimitExceeded e) {
			assertEquals("amount exceeded", 1, e.getExceededAmount());
		}
	}

	@Test
	public void readShort_LimitTwo_FirstWorksSecondFails() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(lengthyInput, 2);

		assertEquals("First ok", -255, input.readShort());
		try {
			input.readShort();
			fail("Excpected a limit exception");
		} catch (LimitExceeded e) {
			assertEquals("amount exceeded", 2, e.getExceededAmount());
		}
	}

	@Test
	public void readShort_LimitOne_FailsAndConsumesOneByte() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(lengthyInput, 1);

		try {
			input.readShort();
			fail("Excpected a limit exception");
		} catch (LimitExceeded e) {
			assertEquals("amount exceeded", 1, e.getExceededAmount());
			assertEquals("Next input byte", 1, lengthyInput.read());
		}
	}

	@Test
	public void readUnsignedShort_LimitTwo_FirstWorksSecondFails() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(lengthyInput, 2);

		assertEquals("First ok", 0xFF01, input.readUnsignedShort());
		try {
			input.readUnsignedShort();
			fail("Excpected a limit exception");
		} catch (LimitExceeded e) {
			assertEquals("amount exceeded", 2, e.getExceededAmount());
		}
	}

	@Test
	public void readUnsignedShort_LimitOne_FailsAndConsumesOneByte() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(lengthyInput, 1);

		try {
			input.readUnsignedShort();
			fail("Excpected a limit exception");
		} catch (LimitExceeded e) {
			assertEquals("amount exceeded", 1, e.getExceededAmount());
			assertEquals("Next input byte", 1, lengthyInput.read());
		}
	}

	@Test
	public void readByte_WithZero_returnsZero() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(byteExtremes, 2);

		assertEquals("Zero byte", (int)0x00, input.readByte());
	}

	@Test
	public void readByte_WithFF_returnsNegOne() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(byteExtremes, 2);

		input.readByte();
		assertEquals("FF byte", -1, input.readByte());
	}

	@Test
	public void readUnsignedByte_WithZero_returnsZero() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(byteExtremes, 2);

		assertEquals("Zero byte", (int)0x00, input.readUnsignedByte());
	}

	@Test
	public void readUnsignedByte_WithFF_returns255() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(byteExtremes, 2);

		input.readByte();
		assertEquals("FF byte", 255, input.readUnsignedByte());
	}


	@Test
	public void readShort_WithZero_returnsZero() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(shortExtremes, 4);

		assertEquals("Zero short", 0, input.readShort());
	}

	@Test
	public void readShort_WithFFFF_returnsNegOne() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(shortExtremes, 4);

		input.readShort();
		assertEquals("FFFF short", -1, input.readShort());
	}

	@Test
	public void readUnsignedShort_WithZero_returnsZero() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(shortExtremes, 4);

		assertEquals("Zero short", (int)0x00, input.readUnsignedShort());
	}

	@Test
	public void readUnsignedShort_WithFFFF_returns65535() throws IOException {
		LimitingDataInput input =
				new LimitingDataInput(shortExtremes, 4);

		input.readUnsignedShort();
		assertEquals("FFFF short", 65535, input.readUnsignedShort());
	}
}