/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdw.formats.jpeg;

import java.io.IOException;
import java.io.InputStream;
import bdw.formats.jpeg.segments.ExpSegment;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bodawei
 */
public class ExpSegmentTest {

	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
	}


	@Test
	public void instanceHasProperMarker() {
		assertEquals(ExpSegment.MARKER, new ExpSegment().getMarker());
	}

	@Test
	public void expSegmentReadsNoData() throws IOException, InvalidJpegFormat {
		InputStream stream = utils.makeInputStreamFromString("AA BB");

		ExpSegment segment = new ExpSegment();

		segment.readFromStream(stream);

		assertEquals(0xAA, stream.read());
	}

	@Test
	public void expSegmentEqualToExpSegment() throws IOException {
		assertTrue(new ExpSegment().equals(new ExpSegment()));
	}

	@Test
	public void expSegmentNotEqualToOther() throws IOException {
		assertFalse(new ExpSegment().equals(new Object()));
	}


}