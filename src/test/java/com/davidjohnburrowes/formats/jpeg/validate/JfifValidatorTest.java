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
package com.davidjohnburrowes.formats.jpeg.validate;

import com.davidjohnburrowes.format.jpeg.JpegData;
import com.davidjohnburrowes.format.jpeg.component.FrameComponent;
import com.davidjohnburrowes.format.jpeg.component.ThumbnailJpeg;
import com.davidjohnburrowes.format.jpeg.data.DataItem;
import com.davidjohnburrowes.format.jpeg.data.EntropyData;
import com.davidjohnburrowes.format.jpeg.data.ExtraFf;
import com.davidjohnburrowes.format.jpeg.marker.AppNSegment;
import com.davidjohnburrowes.format.jpeg.marker.DhtSegment;
import com.davidjohnburrowes.format.jpeg.marker.EoiMarker;
import com.davidjohnburrowes.format.jpeg.marker.JfifSegment;
import com.davidjohnburrowes.format.jpeg.marker.JfxxSegment;
import com.davidjohnburrowes.format.jpeg.marker.SofSegment;
import com.davidjohnburrowes.format.jpeg.marker.SoiMarker;
import com.davidjohnburrowes.format.jpeg.marker.SosSegment;
import com.davidjohnburrowes.format.jpeg.support.FrameMode;
import com.davidjohnburrowes.format.jpeg.validate.JfifValidator;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class JfifValidatorTest {

	private TestUtils utils;
	private JfifValidator validator;
	private SofSegment sof;

	@Before
	public void setUp() {
		utils = new TestUtils();
		validator = new JfifValidator();
		sof = new SofSegment(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT.getValue());

		FrameComponent c = new FrameComponent();
		c.setComponentId(1);
		sof.addComponent(c);

		c = new FrameComponent();
		c.setComponentId(2);
		sof.addComponent(c);

		c = new FrameComponent();
		c.setComponentId(3);
		sof.addComponent(c);
	}

	@Test
	public void validate_withNoJFIF_hasProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(sof);
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(1, validator.validate(elements).size());
	}

	@Test
	public void validate_JFIF_InSecondPosition_hasProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new AppNSegment(AppNSegment.FIRST_MARKERID));
		elements.add(new JfifSegment());
		elements.add(sof);
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(1, validator.validate(elements).size());
	}

	@Test
	public void validate_JFIF_InFirstPosition_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new JfifSegment());
		elements.add(new AppNSegment(AppNSegment.FIRST_MARKERID));
		elements.add(sof);
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void validate_JFIF_InFirstPosition_followedByJfxx_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new JfifSegment());
		elements.add(new JfxxSegment());
		elements.add(new AppNSegment(AppNSegment.FIRST_MARKERID));
		elements.add(sof);
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void validate_JFIF_withInvalidSOF_hasProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		FrameComponent c = sof.getComponent(0);
		c.setComponentId(2);

		elements.add(new SoiMarker());
		elements.add(new JfifSegment());
		elements.add(sof);
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(1, validator.validate(elements).size());
	}

	@Test
	public void validate_JFIF_withInvalidSOFComponentCount_hasProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		sof.deleteComponent(1);

		elements.add(new SoiMarker());
		elements.add(new JfifSegment());
		elements.add(sof);
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(2, validator.validate(elements).size());
	}

	@Test
	public void validate_JFXXwithThumbnail_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		JfxxSegment jfxx = new JfxxSegment();
		ThumbnailJpeg thumb = new ThumbnailJpeg();
		JpegData jpeg = new JpegData();
		jpeg.addItem(new SoiMarker());
		jpeg.addItem(sof);
		jpeg.addItem(new SosSegment());
		jpeg.addItem(new EntropyData());
		jpeg.addItem(new EoiMarker());
		thumb.setJpegImage(jpeg);
		jfxx.setThumbnail(thumb);

		elements.add(new SoiMarker());
		elements.add(new JfifSegment());
		elements.add(jfxx);
		elements.add(sof);
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void validate_JFXXwithThumbnailWithJFIF_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		JfxxSegment jfxx = new JfxxSegment();
		ThumbnailJpeg thumb = new ThumbnailJpeg();
		JpegData jpeg = new JpegData();
		jpeg.addItem(new SoiMarker());
		jpeg.addItem(new JfifSegment());
		jpeg.addItem(sof);
		jpeg.addItem(new SosSegment());
		jpeg.addItem(new EntropyData());
		jpeg.addItem(new EoiMarker());
		thumb.setJpegImage(jpeg);
		jfxx.setThumbnail(thumb);

		elements.add(new SoiMarker());
		elements.add(new JfifSegment());
		elements.add(jfxx);
		elements.add(sof);
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(1, validator.validate(elements).size());
	}

	@Test
	public void validate_JFXXwithThumbnailWithBadSOF_hasProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();

		SofSegment sof2 = new SofSegment(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT.getValue());

		FrameComponent c = new FrameComponent();
		c.setComponentId(1);
		sof2.addComponent(c);

		c = new FrameComponent();
		c.setComponentId(2);
		sof2.addComponent(c);

		JfxxSegment jfxx = new JfxxSegment();
		ThumbnailJpeg thumb = new ThumbnailJpeg();
		JpegData jpeg = new JpegData();
		jpeg.addItem(new SoiMarker());
		jpeg.addItem(sof2);
		jpeg.addItem(new SosSegment());
		jpeg.addItem(new EntropyData());
		jpeg.addItem(new EoiMarker());
		thumb.setJpegImage(jpeg);
		jfxx.setThumbnail(thumb);

		elements.add(new SoiMarker());
		elements.add(new JfifSegment());
		elements.add(jfxx);
		elements.add(sof);
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(1, validator.validate(elements).size());
	}
}
