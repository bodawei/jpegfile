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
package com.davidjohnburrowes.formats.jpeg.component;

import com.davidjohnburrowes.format.jpeg.JpegData;
import com.davidjohnburrowes.format.jpeg.component.ThumbnailJpeg;
import com.davidjohnburrowes.format.jpeg.marker.SoiMarker;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ThumbnailJpegTest {
	private ThumbnailJpeg thumbnail;
	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
		thumbnail = new ThumbnailJpeg();
	}

	@Test
	public void getSizeOnDisk_byDefault_isZero() {
		assertEquals(0, thumbnail.getSizeOnDisk());
	}

	@Test(expected=IllegalArgumentException.class)
	public void setJpegImage_withNull_throwsAnException() {
		thumbnail.setJpegImage(null);
	}

	@Test
	public void setJpegImage_withJpegImage_throwsNoException() {
		JpegData image = new JpegData();
		thumbnail.setJpegImage(image);
	}

	@Test
	public void equals_withSelf_isTrue() {
		assertTrue(thumbnail.equals(thumbnail));
	}

	@Test
	public void equals_withObject_isFalse() {
		assertFalse(thumbnail.equals(new Object()));
	}

	@Test
	public void equals_withDifferentThumbnail_isFalse() {
		JpegData jpeg = new JpegData();
		jpeg.addItem(new SoiMarker());
		thumbnail.setJpegImage(jpeg);
		assertFalse(thumbnail.equals(new ThumbnailJpeg()));
	}
}
