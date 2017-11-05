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

package com.davidjohnburrowes.formats.jpeg.marker;

import com.davidjohnburrowes.format.jpeg.marker.DhpSegment;
import com.davidjohnburrowes.format.jpeg.marker.SofSegment;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Test;

public class DhpSegmentTest {
	@Test
	public void getMarkerId_hasCorrectValue() {
		assertEquals(DhpSegment.MARKERID, new DhpSegment().getMarkerId());
	}

	@Test
	public void equal_twoNewSegmentsEqual() throws IOException {
		assertTrue(new DhpSegment().equals(new DhpSegment()));
	}

	@Test
	public void equal_dhpNotEqualToAnObject() throws IOException {
		assertFalse(new DhpSegment().equals(new Object()));
	}

	@Test
	public void equal_dhpNotEqualToAnSOFSegment() throws IOException {
		assertFalse(new DhpSegment().equals(new SofSegment(SofSegment.FIRST1_MARKERID)));
	}
}
