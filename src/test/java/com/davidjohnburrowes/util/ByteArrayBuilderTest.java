/*
 *  Copyright 2014,2017 柏大衛
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
package com.davidjohnburrowes.util;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

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
		for (int index = 0; index < 1025; index++) {
			builder.append(index % 127);
		}
		assertEquals(1025, builder.getSize());
		assertEquals(8, builder.getByteAt(1024));
	}

	@Test(expected = IllegalArgumentException.class)
	public void appendingAnOutOfRangeValueThrowsException() {
		builder.append(1024);
	}

	@Test
	public void initialSizeIsZero() {
		assertEquals(0, builder.getSize());
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void getAByteOutOfRangeThrowsException() {
		builder.getByteAt(2);
	}
}