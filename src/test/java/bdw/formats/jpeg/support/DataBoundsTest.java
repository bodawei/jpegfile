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
package bdw.formats.jpeg.support;

import bdw.format.jpeg.support.DataBounds;
import bdw.format.jpeg.support.DataMode;
import bdw.format.jpeg.support.FrameMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.formats.jpeg.test.TestUtils;
import bdw.util.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DataBoundsTest {

	private TestUtils utils;
	private List<Exception> list;
	private DataBounds db;

	@Before
	public void setUp() {
		utils = new TestUtils();
		list = new ArrayList<Exception>();
		db = new DataBounds("testProperty", Size.BYTE,
				  1, 2,
				  3, 4,
				  5, 6,
				  7, 8);
	}

	@Test
	public void constructor_withSimpleParams_generatesGoodnstance() {
		DataBounds db = new DataBounds("foo", Size.NIBBLE, 1, 2);

		db.accumulateOnViolation(0, FrameMode.AC_LOSSLESS, list);
		assertEquals(1, list.size());

		db.accumulateOnViolation(2, FrameMode.AC_LOSSLESS, list);
		assertEquals(1, list.size());

		db.accumulateOnViolation(3, FrameMode.AC_LOSSLESS, list);
		assertEquals(2, list.size());

		db.accumulateOnViolation(3, FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT, list);
		assertEquals(3, list.size());
	}

	@Test(expected=IllegalArgumentException.class)
	public void throwIfInvalid_withTooLargeInteger_throws() {
		db.throwIfInvalid(1000, FrameMode.AC_LOSSLESS, DataMode.LAX);
	}

	@Test(expected=IllegalArgumentException.class)
	public void throwIfInvalid_withInvalidNegativeInt_throws() {
		db.throwIfInvalid(-1, FrameMode.AC_LOSSLESS, DataMode.LAX);
	}

	@Test(expected=InvalidJpegFormat.class)
	public void throwIfInvalid_outOfBaselineRange_throws() {
		db.throwIfInvalid(3, FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT, DataMode.STRICT);
	}

	@Test
	public void throwIfInvalid_inBaselineRange_doesntThrow() {
		db.throwIfInvalid(2, FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT, DataMode.STRICT);
	}

	@Test
	public void throwIfInvalid_outOfBaselineRangeButLAX_doesntThrow() {
		db.throwIfInvalid(3, FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT, DataMode.LAX);
	}

	@Test(expected=InvalidJpegFormat.class)
	public void throwIfInvalid_outOfExtendedRange_throws() {
		db.throwIfInvalid(2, FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT, DataMode.STRICT);
	}

	@Test
	public void throwIfInvalid_inExtendedRange_doesntThrow() {
		db.throwIfInvalid(3, FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT, DataMode.STRICT);
	}

	@Test(expected=InvalidJpegFormat.class)
	public void throwIfInvalid_outOfProgressiveRange_throws() {
		db.throwIfInvalid(3, FrameMode.HUFF_PROGRESSIVE_DCT, DataMode.STRICT);
	}

	@Test
	public void throwIfInvalid_inProgressiveRange_doesntThrow() {
		db.throwIfInvalid(5, FrameMode.HUFF_PROGRESSIVE_DCT, DataMode.STRICT);
	}

	@Test(expected=InvalidJpegFormat.class)
	public void throwIfInvalid_outOfLosslessRange_throws() {
		db.throwIfInvalid(3, FrameMode.HUFF_LOSSLESS, DataMode.STRICT);
	}

	@Test
	public void throwIfInvalid_inLosselesRange_doesntThrow() {
		db.throwIfInvalid(7, FrameMode.HUFF_LOSSLESS, DataMode.STRICT);
	}
}