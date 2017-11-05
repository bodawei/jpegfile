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
package com.davidjohnburrowes.formats.jpeg.data;

import com.davidjohnburrowes.format.jpeg.data.ExtraFf;
import com.davidjohnburrowes.format.jpeg.data.MarkerSegment;
import com.davidjohnburrowes.format.jpeg.support.DataMode;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ExtraFfTest {

	protected TestUtils utils;
	protected ExtraFf extraFf;

	@Before
	public void setUp() throws InvalidJpegFormat {
		extraFf = new ExtraFf();
		utils = new TestUtils();
	}

	@Test
	public void getFfCount_byDefault_isZero() {
		assertEquals(0, extraFf.getFfCount());
	}

	@Test
	public void getFfCount_afterFfCountSet_matches() {
		extraFf.setFfCount(9);
		assertEquals(9, extraFf.getFfCount());
	}

	@Test
	public void getSizeOnDisk_afterFfCountSet_matches() {
		extraFf.setFfCount(9);
		assertEquals(9, extraFf.getSizeOnDisk());
	}

	@Test
	public void clearPassthrough_afterFfCountSet_clearsCount() {
		extraFf.setFfCount(9);
		extraFf.clearPassthrough();
		assertEquals(0, extraFf.getSizeOnDisk());
	}

	@Test
	public void equals_withNull_notEqual() {
		assertFalse(extraFf.equals(null));
	}

	@Test
	public void equals_withAnotherExtraFf_equal() {
		assertEquals(extraFf, new ExtraFf());
	}

	@Test
	public void equals_withDifferentCounts_notEqual() {
		extraFf.setFfCount(4);
		assertFalse(extraFf.equals(new ExtraFf()));
	}

	@Test
	public void equals_withAnotherObject_notEqual() {
		assertFalse(extraFf.equals(new MarkerSegment(3)));
	}

	@Test
	public void write_byDefault_writesNothing() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		extraFf.write(output);

		assertArrayEquals(new byte[0], output.toByteArray());
	}

	@Test
	public void write_withTrailingBytes_writesSizeAndBytes() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		extraFf.setFfCount(4);

		extraFf.write(output);

		assertArrayEquals(utils.makeByteArray("FF FF FF FF"),
				  output.toByteArray());
	}
}
