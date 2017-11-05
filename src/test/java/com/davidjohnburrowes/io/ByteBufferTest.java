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
package com.davidjohnburrowes.io;

import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ByteBufferTest {

	private TestUtils utils;
	private ByteBuffer bb;

	@Before
	public void setUp() {
		utils = new TestUtils();
		bb = new ByteBuffer();
	}

	private void assertState_NoMark() {
		assertFalse(bb.canAdd());
		assertFalse(bb.mustRead());
	}

	private void assertState_MarkWithNoBuffer() {
		assertTrue(bb.canAdd());
		assertFalse(bb.mustRead());
	}

	private void assertState_MarkWithBuffer(int expectedValue) throws IOException {
		assertTrue(bb.canAdd());
		assertFalse(bb.mustRead());

		bb.reset();
		assertEquals(expectedValue, bb.readByte());
	}

	private void assertState_MarkWithFullBuffer() {
		assertTrue(bb.canAdd());
		assertFalse(bb.mustRead());

		bb.addByte((byte)99);

		assertState_NoMark();
	}

	private void assertState_MarkWithReadBuffer(int expectedValue) {
		assertFalse(bb.canAdd());
		assertTrue(bb.mustRead());

		assertEquals(expectedValue, bb.readByte());
	}

	// State: No mark

	@Test
	public void initially_inNoMark() {
		assertState_NoMark();
	}

	@Test
	public void mark_inNoMark_toMarkWithNoBuffer() {
		bb.mark(3);

		assertState_MarkWithNoBuffer();
	}

	@Test(expected=IOException.class)
	public void reset_inNoMark_throwsException() throws IOException {
		bb.reset();
	}

	@Test
	public void canAdd_inNoMark_false() {
		assertFalse(bb.canAdd());
	}

	@Test
	public void mustRead_inNoMark_false() {
		assertFalse(bb.mustRead());
	}

	@Test(expected=IllegalStateException.class)
	public void addByte_inNoMark_throwsException() {
		bb.addByte((byte)2);
	}

	@Test(expected=IllegalStateException.class)
	public void readByte_inNoMark_throwsException() {
		bb.readByte();
	}

	// State: Mark with no buffer

	@Test
	public void mark_inMarkWithNoBuffer_toInMarkWithNoBuffer() {
		bb.mark(3);

		bb.mark(3);

		assertState_MarkWithNoBuffer();
	}

	@Test
	public void reset_inMarkWithNoBuffer_toInMarkWithNoBuffer() throws IOException {
		bb.mark(3);

		bb.reset();

		assertState_MarkWithNoBuffer();
	}

	@Test
	public void canAdd_inMarkWithNoBuffer_true() {
		bb.mark(3);
		assertTrue(bb.canAdd());
		assertState_MarkWithNoBuffer();
	}

	@Test
	public void mustRead_inMarkWithNoBuffer_false() {
		bb.mark(2);
		assertFalse(bb.mustRead());
	}

	@Test
	public void addByte_inMarkWithNoBuffer_toInMarkWithBuffer() throws IOException {
		bb.mark(3);

		bb.addByte((byte)1);

		assertState_MarkWithBuffer(1);
	}

	@Test(expected=IllegalStateException.class)
	public void readByte_inMarkWithNoBuffer_throwsException() {
		bb.mark(2);

		bb.readByte();
	}

	// State: Mark with buffer

	@Test
	public void mark_inMarkWithBuffer_toInMarkNoBuffer() {
		bb.mark(3);
		bb.addByte((byte)1);

		bb.mark(3);

		assertState_MarkWithNoBuffer();
	}

	@Test
	public void reset_inMarkWithBuffer_toMarkWithReadBuffer() throws IOException {
		bb.mark(3);
		bb.addByte((byte)1);

		bb.reset();

		assertState_MarkWithReadBuffer(1);
	}

	@Test
	public void canAdd_inMarkWithBuffer_true() throws IOException {
		bb.mark(3);
		bb.addByte((byte)1);

		assertTrue(bb.canAdd());

		assertState_MarkWithBuffer(1);
	}

	@Test
	public void mustRead_inMarkWithBuffer_false() throws IOException {
		bb.mark(3);
		bb.addByte((byte)1);

		assertFalse(bb.mustRead());

		assertState_MarkWithBuffer(1);
	}

	@Test
	public void addByte_inMarkWithBuffer_toInMarkWithBuffer() throws IOException {
		bb.mark(3);
		bb.addByte((byte)1);

		bb.addByte((byte)2);

		assertState_MarkWithBuffer(1);
	}

	@Test
	public void addByte_inMarkWithBuffer_toInMarkWithFullBuffer() {
		bb.mark(2);
		bb.addByte((byte)1);

		bb.addByte((byte)2);

		assertState_MarkWithFullBuffer();
	}

	@Test(expected=IllegalStateException.class)
	public void readByte_inMarkWithBuffer_throwsException() {
		bb.mark(2);
		bb.addByte((byte)1);

		bb.readByte();
	}

	// State: Mark with full buffer

	@Test
	public void mark_inMarkWithFullBuffer_toInMarkNoBuffer() {
		bb.mark(1);
		bb.addByte((byte)1);

		bb.mark(3);

		assertState_MarkWithNoBuffer();
	}

	@Test
	public void reset_inMarkWithFullBuffer_toMarkWithReadBuffer() throws IOException {
		bb.mark(1);
		bb.addByte((byte)1);

		bb.reset();

		assertState_MarkWithReadBuffer(1);
	}

	@Test
	public void canAdd_inMarkWithFullBuffer_true() throws IOException {
		bb.mark(1);
		bb.addByte((byte)1);

		assertTrue(bb.canAdd());

		assertState_MarkWithBuffer(1);
	}

	@Test
	public void mustRead_inMarkWithFullBuffer_false() throws IOException {
		bb.mark(1);
		bb.addByte((byte)1);

		assertFalse(bb.mustRead());

		assertState_MarkWithBuffer(1);
	}

	@Test
	public void addByte_inMarkWithFullBuffer_toNoMark() {
		bb.mark(1);
		bb.addByte((byte)1);

		bb.addByte((byte)2);

		assertState_NoMark();
	}

	@Test(expected=IllegalStateException.class)
	public void readByte_inMarkWithFullBuffer_throwsException() {
		bb.mark(1);
		bb.addByte((byte)1);

		bb.readByte();
	}

	// State: Mark with read buffer

	@Test
	public void mark_inMarkWithReadBuffer_toInMarkWithReadBuffer() throws IOException {
		bb.mark(2);
		bb.addByte((byte)1);
		bb.reset();

		bb.mark(3);

		assertState_MarkWithReadBuffer(1);
	}

	@Test
	public void reset_inMarkWithReadBuffer_toMarkWithReadBuffer() throws IOException {
		bb.mark(2);
		bb.addByte((byte)1);
		bb.reset();

		bb.reset();

		assertState_MarkWithReadBuffer(1);
	}

	@Test
	public void canAdd_inMarkWithReadBuffer_false() throws IOException {
		bb.mark(2);
		bb.addByte((byte)1);
		bb.reset();

		assertFalse(bb.canAdd());

		assertState_MarkWithReadBuffer(1);
	}

	@Test
	public void mustRead_inMarkWithReadBuffer_true() throws IOException {
		bb.mark(2);
		bb.addByte((byte)1);
		bb.reset();

		assertTrue(bb.mustRead());

		assertState_MarkWithReadBuffer(1);
	}

	@Test(expected=IllegalStateException.class)
	public void addByte_inMarkWithReadBuffer_throwsException() throws IOException {
		bb.mark(2);
		bb.addByte((byte)1);
		bb.reset();

		bb.addByte((byte)2);
	}

	@Test
	public void readByte_inMarkWithReadBuffer_toMarkWithReadBuffer() throws IOException {
		bb.mark(3);
		bb.addByte((byte)1);
		bb.addByte((byte)2);
		bb.reset();

		assertEquals(1, bb.readByte());

		assertState_MarkWithReadBuffer(2);
	}

	@Test
	public void readByte_inMarkWithReadBuffer_toNoMark() throws IOException {
		bb.mark(1);
		bb.addByte((byte)1);
		bb.reset();

		assertEquals(1, bb.readByte());

		assertState_NoMark();
	}
}