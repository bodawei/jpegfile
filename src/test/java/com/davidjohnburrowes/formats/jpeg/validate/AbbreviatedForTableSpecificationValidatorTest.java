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

import com.davidjohnburrowes.format.jpeg.data.DataItem;
import com.davidjohnburrowes.format.jpeg.data.ExtraFf;
import com.davidjohnburrowes.format.jpeg.marker.AppNSegment;
import com.davidjohnburrowes.format.jpeg.marker.DhtSegment;
import com.davidjohnburrowes.format.jpeg.marker.EoiMarker;
import com.davidjohnburrowes.format.jpeg.marker.SofSegment;
import com.davidjohnburrowes.format.jpeg.marker.SoiMarker;
import com.davidjohnburrowes.format.jpeg.validate.AbbreviatedForTableSpecificationValidator;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class AbbreviatedForTableSpecificationValidatorTest {

	private TestUtils utils;
	private AbbreviatedForTableSpecificationValidator validator;

	@Before
	public void setUp() {
		utils = new TestUtils();
		validator = new AbbreviatedForTableSpecificationValidator();
	}

	@Test
	public void validate_withSOIAndEOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void validate_withSOITableAndEOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new AppNSegment(AppNSegment.FIRST_MARKERID));
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void validate_withSOIMultipleTablesAndEOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new AppNSegment(AppNSegment.FIRST_MARKERID));
		elements.add(new DhtSegment());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void validate_withIgnorableDataItem_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new AppNSegment(AppNSegment.FIRST_MARKERID));
		elements.add(new DhtSegment());
		elements.add(new ExtraFf());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void validate_withoutAnything_1Problem() {
		List<DataItem> elements = new ArrayList<DataItem>();

		assertEquals(1, validator.validate(elements).size());
	}

	@Test
	public void validate_withoutEOI_1Problem() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());

		assertEquals(1, validator.validate(elements).size());
	}

	@Test
	public void validate_withTrailingSegment_1Problems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new DhtSegment());
		elements.add(new EoiMarker());
		elements.add(new EoiMarker());

		assertEquals(1, validator.validate(elements).size());
	}

	@Test
	public void validate_withNonTablesInMiddle_1Problems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new DhtSegment());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new EoiMarker());

		assertEquals(1, validator.validate(elements).size());
	}
}
