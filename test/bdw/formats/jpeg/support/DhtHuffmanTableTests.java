/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdw.formats.jpeg.support;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import junit.framework.Assert;
import java.io.EOFException;
import bdw.formats.jpeg.InvalidJpegFormat;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.InputStream;
import bdw.formats.jpeg.segments.support.DhtRunLengthHeader;
import bdw.formats.jpeg.TestUtils;
import bdw.formats.jpeg.segments.support.DhtHuffmanTable;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dburrowes
 */
public class DhtHuffmanTableTests {

	protected TestUtils utils;
	protected DhtHuffmanTable table;

	@Before
	public void setUp() {
		utils = new TestUtils();
		table = new DhtHuffmanTable();
	}

	private final String SAMPLE_BYTES  = "13 "
				+ "01 01 00 04 00 00 00 00 00 00 00 00 00 00 00 00"
				+ "0A   5f   11 22 33 44";

	private DhtHuffmanTable buildSampleTable() {
		DhtHuffmanTable newTable = new DhtHuffmanTable();
		newTable.setAc(true);
		newTable.setId(3);
		newTable.addHeader(new DhtRunLengthHeader(0x00, 1, 0, 0x0A));
		newTable.addHeader(new DhtRunLengthHeader(0x02, 2, 5, 0x0F));
		newTable.addHeader(new DhtRunLengthHeader(0x0C, 4, 1, 0x01));
		newTable.addHeader(new DhtRunLengthHeader(0x0D, 4, 2, 0x02));
		newTable.addHeader(new DhtRunLengthHeader(0x0E, 4, 3, 0x03));
		newTable.addHeader(new DhtRunLengthHeader(0x0F, 4, 4, 0x04));

		return newTable;
	}


	@Test
	public void setId_With3_OK() {
		table.setId(3);
		assertEquals("Id", 3, table.getId());
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void setId_With4_ThrowsException() {
		table.setId(4);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void getHeader_WithNegativeValue_ThrowsException() {
		table.getHeader(-1);
	}

	@Test
	public void addHeader_IncreasesHeaderCount() {
		table.addHeader(new DhtRunLengthHeader(0x11, 5, 6, 7));
		assertEquals("header count", 1, table.getHeaderCount());
		assertEquals("header", new DhtRunLengthHeader(0x11, 5, 6, 7), table.getHeader(0));
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void addHeader_WithNonUniqueHuffmanCoding_Exception() {
		table.addHeader(new DhtRunLengthHeader(0x11, 5, 6, 7));
		table.addHeader(new DhtRunLengthHeader(0x02, 2, 6, 7)); // 0x02 overlaps 0x11
	}

	@Test
	public void addHeader_Sorts() {
		table.addHeader(new DhtRunLengthHeader(0x11, 5, 6, 7));
		table.addHeader(new DhtRunLengthHeader(0x01, 2, 6, 7));
		assertEquals("header", new DhtRunLengthHeader(0x01, 2, 6, 7), table.getHeader(0));
		assertEquals("header", new DhtRunLengthHeader(0x11, 5, 6, 7), table.getHeader(1));
	}

	@Test
	public void read_GoodData_ProducedExpectedInstance() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString(SAMPLE_BYTES);
		DataInputStream diStream = new DataInputStream(stream);

		table.read(diStream);

		DhtHuffmanTable expected = buildSampleTable();

		assertEquals("Dht table", expected, table);
	}

	@Test(expected = EOFException.class)
	public void read_ShortData_ThrowsException() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("13 "
				+ "01 01 00 04 00 00 00 00 00 00 00 00 00 00 00 00"
				+ "0A   5f   11 22 33");
		DataInputStream diStream = new DataInputStream(stream);

		table.read(diStream);
	}

	@Test
	public void write_ExampleInstance_GeneratesGoodDataStream() throws IOException {
		DhtHuffmanTable aTable = buildSampleTable();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		aTable.write(new DataOutputStream(output));

		byte[] expectedBytes = utils.makeByteArrayFromString(SAMPLE_BYTES);

		assertArrayEquals("streamed data", expectedBytes, output.toByteArray());
	}

}
