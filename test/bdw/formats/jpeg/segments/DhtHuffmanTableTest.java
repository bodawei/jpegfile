/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.segments.support.DhtHuffmanTable;
import bdw.formats.jpeg.TestUtils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dburrowes
 */
public class DhtHuffmanTableTest {
	protected TestUtils utils;
	protected DhtHuffmanTable table;

    @Before
    public void setUp() {
		utils = new TestUtils();
		table = new DhtHuffmanTable();
    }

	@Test
	public void setId_Passed2_Succeeds() {
		table.setId(2);
		assertEquals("Id", 2, table.getId());
	}

	@Test
	public void setId_Passed17_Exception() {
		try {
			table.setId(17);
			fail("Expected exception");
		} catch (Exception e) {
			assertTrue(e instanceof InvalidJpegFormat);
		}
	}

	@Test
	public void setEntry_Passed5ByteArray_Works() {
		table.setEntry(2, new byte[5]);
		assertArrayEquals("Array", new byte[5], table.getEntry(2));
	}

	@Test
	public void setEntry_Passed30ByteArray_Exception() {
		try {
			table.setEntry(2, new byte[30]);
			fail("Expected exception");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void equals_AnEqualObject_ReturnsTrue() {
		DhtHuffmanTable two = new DhtHuffmanTable();

		table.setAc(true);
		two.setAc(true);

		table.setId(4);
		two.setId(4);

		table.setEntry(4, new byte[4]);
		two.setEntry(4, new byte[4]);

		table.setEntry(8, new byte[8]);
		two.setEntry(8, new byte[8]);
		assertTrue(table.equals(two));
	}

	@Test
	public void equals_AnUnequalObject_ReturnsFalse() {
		DhtHuffmanTable two = new DhtHuffmanTable();

		table.setAc(true);
		two.setAc(true);

		table.setId(4);
		two.setId(4);

		table.setEntry(4, new byte[4]);
		two.setEntry(4, new byte[4]);

		table.setEntry(8, new byte[8]);
		two.setEntry(8, new byte[2]);
		assertFalse(table.equals(two));
	}

	@Test
	public void equals_AnUnrelatedObject_ReturnsFalse() {
		assertFalse(table.equals(new Object()));
	}

	@Test
	public void read_GoodData_GivesExpectedObject() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("03" +
				"01 00 01 00  01 00 01 00  00 01 00 01  01 00 00 01" +
				"AA BB CC DD EE FF 11 22");
		DataInput input = new DataInputStream(stream);

		table.read(input);

		DhtHuffmanTable answer = new DhtHuffmanTable();
		answer.setAc(false);
		answer.setId(3);
		answer.setEntry(0, new byte[] {(byte)0xAA});
		answer.setEntry(2, new byte[] {(byte)0xBB});
		answer.setEntry(4, new byte[] {(byte)0xCC});
		answer.setEntry(6, new byte[] {(byte)0xDD});
		answer.setEntry(9, new byte[] {(byte)0xEE});
		answer.setEntry(11, new byte[] {(byte)0xFF});
		answer.setEntry(12, new byte[] {(byte)0x11});
		answer.setEntry(15, new byte[] {(byte)0x22});

		assertEquals("read Object", answer, table);
	}

	@Test
	public void read_BadFlags_GivesError() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("F3" +
				"01 00 01 00  01 00 01 00  00 01 00 01  01 00 00 01" +
				"AA BB CC DD EE FF 11 22");
		DataInput input = new DataInputStream(stream);
		try {
			table.read(input);
			fail("Exception expected");
		} catch (Exception e) {
			assertTrue(e instanceof InvalidJpegFormat);
		}
	}

	@Test
	public void read_ShortLength_Exception() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("03" +
				"01 00 01 00  01 00 01 00  00 01 00 01  01 00 00 01" +
				"AA BB CC DD EE");
		DataInput input = new DataInputStream(stream);
		try {
			table.read(input);
			fail("Exception expected");
		} catch (Exception e) {
			assertTrue(e instanceof EOFException);
		}
	}

	@Test
	public void write_WithAc_IsExpectedOutput() throws IOException {
		DhtHuffmanTable source = new DhtHuffmanTable();
		source.setAc(true);
		source.setId(3);
		source.setEntry(0, new byte[] {(byte)0xAA});
		source.setEntry(2, new byte[] {(byte)0xBB});
		source.setEntry(4, new byte[] {(byte)0xCC});
		source.setEntry(6, new byte[] {(byte)0xDD});
		source.setEntry(9, new byte[] {(byte)0xEE});
		source.setEntry(11, new byte[] {(byte)0xFF});
		source.setEntry(12, new byte[] {(byte)0x11});
		source.setEntry(15, new byte[] {(byte)0x22});

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		source.write(stream);

		assertArrayEquals(utils.makeByteArrayFromString("13" +
				"01 00 01 00  01 00 01 00  00 01 00 01  01 00 00 01" +
				"AA BB CC DD EE FF 11 22"), bytes.toByteArray());
	}

	@Test
	public void write_WithoutAc_IsExpectedOutput() throws IOException {
		DhtHuffmanTable source = new DhtHuffmanTable();
		source.setAc(false);
		source.setId(3);
		source.setEntry(0, new byte[] {(byte)0xAA});
		source.setEntry(2, new byte[] {(byte)0xBB});
		source.setEntry(4, new byte[] {(byte)0xCC});
		source.setEntry(6, new byte[] {(byte)0xDD});
		source.setEntry(9, new byte[] {(byte)0xEE});
		source.setEntry(11, new byte[] {(byte)0xFF});
		source.setEntry(12, new byte[] {(byte)0x11});
		source.setEntry(15, new byte[] {(byte)0x22});

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		source.write(stream);

		assertArrayEquals(utils.makeByteArrayFromString("03" +
				"01 00 01 00  01 00 01 00  00 01 00 01  01 00 00 01" +
				"AA BB CC DD EE FF 11 22"), bytes.toByteArray());
	}
}