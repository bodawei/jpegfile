package bdw.formats.utf8;

import bdw.formats.utf8.InvalidUcsCharException;
import bdw.formats.utf8.MalformedIntException;
import bdw.formats.utf8.UcsFromUtf8Parser;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;


public class UcsFromUtf8ReaderTest {
	
	@Test
	public void testAsciiOk() throws IOException, MalformedIntException, InvalidUcsCharException {
		byte[] data = {65};
		UcsFromUtf8Parser reader = new UcsFromUtf8Parser(new ByteArrayInputStream(data));
		
		assertEquals('A', reader.readChar());
	}

	@Test
	public void testD800IsOkWhenStrictOff() throws IOException, MalformedIntException, InvalidUcsCharException {
		byte[] data = {(byte)0xED, (byte)0xA0, (byte)0x80};
		UcsFromUtf8Parser reader = new UcsFromUtf8Parser(new ByteArrayInputStream(data));
		reader.setStrict(false);
		assertEquals((char)0xD800, reader.readChar());
	}
	
	@Test
	public void testD800IsNotOk() throws IOException, MalformedIntException {
		byte[] data = {(byte)0xED, (byte)0xA0, (byte)0x80};
		UcsFromUtf8Parser reader = new UcsFromUtf8Parser(new ByteArrayInputStream(data));
		try {
			char result = reader.readChar();
			fail("didn't get an error when reading " + result);
		} catch (InvalidUcsCharException e) {
			assertEquals(0xD800, e.getBadChar());
		}		
	}
}
