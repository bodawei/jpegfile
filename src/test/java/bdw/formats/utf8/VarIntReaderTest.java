package bdw.formats.utf8;

import bdw.formats.utf8.VarIntParser;
import bdw.formats.utf8.MalformedIntException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;
import org.junit.Test;


public class VarIntReaderTest {

	@Test
	public void testEofOnEmptyArray() throws IOException {
		byte[] data = new byte[0];
		VarIntParser reader = new VarIntParser(new ByteArrayInputStream(data));
		assertTrue(reader.atEos());
	}


	@Test
	public void test7BitIntOk() throws IOException, MalformedIntException {
		byte[] data = {65};
		VarIntParser reader = new VarIntParser(new ByteArrayInputStream(data));
		
		assertFalse(reader.atEos());
		assertEquals(65, reader.readInt());
		assertTrue(reader.atEos());
	}

	@Test
	public void testTwoByteInt() throws IOException, MalformedIntException {
		byte[] data = {(byte)0xE2, (byte)0x89, (byte)0xA2};
		VarIntParser reader = new VarIntParser(new ByteArrayInputStream(data));
		
		assertEquals(0x2262, reader.readInt());
	}

	@Test
	public void testFourByteInt() throws IOException, MalformedIntException {
		byte[] data = {(byte)0xF0, (byte)0xA3, (byte)0x8E, (byte)0xB4};
		VarIntParser reader = new VarIntParser(new ByteArrayInputStream(data));
		
		assertEquals(0x233B4, reader.readInt());
	}

	@Test
	public void testNonMinimalRepWithStrictOff() throws IOException, MalformedIntException {
		byte[] data = {(byte)0xFC, (byte)0x80, (byte)0x80, (byte)0x80, (byte)0x80, (byte)0xA0};
		VarIntParser reader = new VarIntParser(new ByteArrayInputStream(data));
		reader.setStrict(false);

		assertEquals(0x20, reader.readInt());
	}

	@Test
	public void testNonMinimalRep() throws IOException, MalformedIntException {
		byte[] data = {(byte)0xFC, (byte)0x80, (byte)0x80, (byte)0x80, (byte)0x80, (byte)0xA0};
		VarIntParser reader = new VarIntParser(new ByteArrayInputStream(data));
		
		try {
			int result = reader.readInt();
			fail("Did not get an exception, but got " + result);
		} catch (MalformedIntException e) {
			assertEquals("Not a minimal representation", e.getMessage());
			assertEquals(0x20, e.getBadValue());
		}
	}

	@Test
	public void testNonMinimalNull() throws IOException, MalformedIntException {
		byte[] data = {(byte)0xC0, (byte)0x80};
		VarIntParser reader = new VarIntParser(new ByteArrayInputStream(data));
		
		try {
			int result = reader.readInt();
			fail("Did not get an exception, but got " + result);
		} catch (MalformedIntException e) {
			assertEquals("Not a minimal representation", e.getMessage());
			assertEquals(0x00, e.getBadValue());
		}
	}


	@Test
	public void testIncompleteFourByteInt() throws IOException {
		byte[] data = {(byte)0xF0, (byte)0xA3, (byte)0x8E};
		VarIntParser reader = new VarIntParser(new ByteArrayInputStream(data));

		try {
			int result = reader.readInt();
			fail("Did not get an exception, but got " + result);
		} catch (MalformedIntException e) {
			assertEquals("Integer terminated early (3 bytes read, 4 expected)", e.getMessage());
		}
	}

	@Test
	public void testIntEndingEarlyWithAnotherStarting() throws IOException {
		byte[] data = {(byte)0xF0, (byte)0xA3, (byte)0x8E, (byte)0xED};
		VarIntParser reader = new VarIntParser(new ByteArrayInputStream(data));

		try {
			int result = reader.readInt();
			fail("Did not get an exception, but got " + result);
		} catch (MalformedIntException e) {
			assertEquals("Integer terminated early. Found byte without continuation bits.", e.getMessage());
			assertEquals(0xED, e.getBadValue());
		}
	}

}
