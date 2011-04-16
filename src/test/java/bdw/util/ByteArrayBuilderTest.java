/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.util;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dburrowes
 */
public class ByteArrayBuilderTest {

	protected ByteArrayBuilder builder;

    @Before
    public void setUp() {
		builder = new ByteArrayBuilder();
    }

    @Test
    public void appendAddsAByteAtTheEnd() {
		builder.append(5);
		assertEquals(1, builder.getSize());
		assertEquals(5, builder.getByteAt(0));
	}

    @Test
    public void deAppendFromNoBytesDoesNothing() {
		builder.deAppend();
		assertEquals(0, builder.getSize());
	}

    @Test
    public void deAppendRemovesAByteFromTheLength() {
		builder.append(5);
		builder.append(8);
		builder.deAppend();

		assertEquals(1, builder.getSize());
		assertEquals(5, builder.getByteAt(0));
	}

	@Test
    public void setExtendsTheLength() {
		builder.setByteAt(23, 5);
		assertEquals(24, builder.getSize());
		assertEquals(0, builder.getByteAt(0));
		assertEquals(5, builder.getByteAt(23));
	}

    @Test
    public void appendUntilAReallocDone() {
		for (int index= 0; index < 1025; index++) {
			builder.append(index % 127);
		}
		assertEquals(1025, builder.getSize());
		assertEquals(8, builder.getByteAt(1024));
	}

    @Test(expected=IllegalArgumentException.class)
    public void appendingAnOutOfRangeValueThrowsException() {
		builder.append(1024);
	}

    @Test
    public void initialSizeIsZero() {
		assertEquals(0, builder.getSize());
	}

    @Test(expected=IndexOutOfBoundsException.class)
    public void getAByteOutOfRangeThrowsException() {
		builder.getByteAt(2);
	}
}