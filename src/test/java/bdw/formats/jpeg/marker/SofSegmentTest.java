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

package bdw.formats.jpeg.marker;

import bdw.format.jpeg.marker.SofSegment;
import bdw.format.jpeg.support.FrameMode;
import static org.junit.Assert.*;
import org.junit.Test;

public class SofSegmentTest {
	@Test
	public void getSegmentId_hasSpecifiedMarker() {
		assertEquals(SofSegment.FIRST1_MARKERID, new SofSegment(SofSegment.FIRST1_MARKERID).getMarkerId());
	}

	@Test(expected=IllegalArgumentException.class)
	public void construction_withInvalidMarkerId_throwsException() {
		new SofSegment(0);
	}

	@Test
	public void construction_setsFrameMode() {
		assertEquals(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT, new SofSegment(SofSegment.FIRST1_MARKERID).getFrameMode());
		assertEquals(FrameMode.DIFF_AC_EXTENDED_SEQUENTIAL_DCT, new SofSegment(SofSegment.FIRST4_MARKERID).getFrameMode());
	}
}