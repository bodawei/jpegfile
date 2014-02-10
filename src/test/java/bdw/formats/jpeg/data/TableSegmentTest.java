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
package bdw.formats.jpeg.data;

import bdw.format.jpeg.support.FrameMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.formats.jpeg.test.TestUtils;
import bdw.formats.jpeg.mocks.TrivialComponent;
import bdw.formats.jpeg.mocks.TrivialTableSegment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TableSegmentTest {

	protected TestUtils utils;
	protected TrivialTableSegment segment;

	@Before
	public void setUp() throws InvalidJpegFormat {
		segment = new TrivialTableSegment();
		utils = new TestUtils();
	}

	@Test
	public void getMarkerId_construction_setCorrectly() {
		assertEquals(41, segment.getMarkerId());
	}

	@Test
	public void getEntryCount_byDefault_isZero() {
		assertEquals(0, segment.getTableCount());
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void getEntry_outOfRange_throwsException() {
		segment.getTable(23);
	}

	@Test
	public void getEntry_returns_expectedEntry() {
		segment.addTable(new TrivialComponent());
		TrivialComponent two = new TrivialComponent();
		segment.addTable(two);

		assertEquals(two, segment.getTable(1));
	}

	@Test
	public void addEntry_addsAnEntry() {
		segment.addTable(new TrivialComponent());
		assertEquals(1, segment.getTableCount());
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void insertEntry_outOfRange_throwsException() {
		segment.insertTable(3, new TrivialComponent());
	}

	@Test
	public void insertEntry_addsAnEntry() {
		segment.insertTable(0, new TrivialComponent());
		assertEquals(1, segment.getTableCount());
	}

//	@Test(expected=IllegalArgumentException.class)
//	public void insertEntry_whenAlreadyTooMany_throwsException() {
//		for (int index = 0; index < 70000; index++) {
//			segment.insertEntry(0, new TrivialComponent());
//		}
//	}

	@Test
	public void getSegmentSizeOnDisk_byDefault_is2() {
		assertEquals(2, segment.getParameterSizeOnDisk());
	}

	@Test
	public void getSegmentSizeOnDisk_withOneEntry_isCorrect() {
		segment.addTable(new TrivialComponent());

		assertEquals(3, segment.getParameterSizeOnDisk());
	}

	@Test
	public void iterator_returnsAnIterator() {
		segment.addTable(new TrivialComponent());
		Iterator iterator = segment.iterator();

		assertEquals(true, iterator.hasNext());
	}

	@Test
	public void validate_returnsNothing() {
		assertEquals(0, segment.validate().size());
	}

	@Test
	public void setFrameMode_changesChildren() {
		TrivialComponent child = new TrivialComponent();
		segment.addTable(child);

		segment.setFrameMode(FrameMode.AC_LOSSLESS);

		assertEquals(FrameMode.AC_LOSSLESS, child.getFrameMode());
	}

	@Test
	public void read_readsAllChildren() throws IOException {
		segment.read(utils.makeInputStream("00 0C 20 19 18 17 16 15 14 13 12 11"));

		assertEquals(10, segment.getTableCount());
	}

	@Test
	public void write_willWriteAll() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		segment.addTable(new TrivialComponent());

		segment.write(output);

		assertArrayEquals(utils.makeByteArray("FF29 00 03 00"),
				  output.toByteArray());
	}
}
