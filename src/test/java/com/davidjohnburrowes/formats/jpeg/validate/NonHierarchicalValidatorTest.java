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
import com.davidjohnburrowes.format.jpeg.data.EntropyData;
import com.davidjohnburrowes.format.jpeg.data.ExtraFf;
import com.davidjohnburrowes.format.jpeg.marker.AppNSegment;
import com.davidjohnburrowes.format.jpeg.marker.ComSegment;
import com.davidjohnburrowes.format.jpeg.marker.DhtSegment;
import com.davidjohnburrowes.format.jpeg.marker.DnlSegment;
import com.davidjohnburrowes.format.jpeg.marker.DqtSegment;
import com.davidjohnburrowes.format.jpeg.marker.EoiMarker;
import com.davidjohnburrowes.format.jpeg.marker.RstMMarker;
import com.davidjohnburrowes.format.jpeg.marker.SofSegment;
import com.davidjohnburrowes.format.jpeg.marker.SoiMarker;
import com.davidjohnburrowes.format.jpeg.marker.SosSegment;
import com.davidjohnburrowes.format.jpeg.validate.NonHierarchicalValidator;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class NonHierarchicalValidatorTest {

	private TestUtils utils;
	private NonHierarchicalValidator validator;

	@Before
	public void setUp() {
		utils = new TestUtils();
		validator = new NonHierarchicalValidator();
	}

	@Test
	public void SOI_SOF_SOS_Entropy_EOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void SOI_Table_SOF_SOS_Entropy_EOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new DqtSegment());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void SOI_Tables_SOF_SOS_Entropy_EOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new DqtSegment());
		elements.add(new ComSegment());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void SOI_Tables_SOF_Table_SOS_Entropy_EOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new DqtSegment());
		elements.add(new ComSegment());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new DhtSegment());
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void SOI_Tables_SOF_Tables_SOS_Entropy_EOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new DqtSegment());
		elements.add(new ComSegment());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new DhtSegment());
		elements.add(new AppNSegment((AppNSegment.FIRST_MARKERID)));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void SOI_T_SOF_T_SOS_E_SOS_E_EOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new DqtSegment());
		elements.add(new ComSegment());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new DhtSegment());
		elements.add(new AppNSegment((AppNSegment.FIRST_MARKERID)));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void SOI_SOF_T_SOS_E_T_SOS_E_EOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new DhtSegment());
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new DhtSegment());
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new DhtSegment());
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void SOI_T_SOF_T_SOS_E_T_SOS_E_EOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new DqtSegment());
		elements.add(new ComSegment());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new DhtSegment());
		elements.add(new AppNSegment((AppNSegment.FIRST_MARKERID)));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new ComSegment());
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void SOI_Tables_SOF_Tables_SOS_Entropy_RST_Entropy_EOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new DqtSegment());
		elements.add(new ComSegment());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new DhtSegment());
		elements.add(new AppNSegment((AppNSegment.FIRST_MARKERID)));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new RstMMarker(RstMMarker.FIRST_MARKERID));
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void SOI_Tables_SOF_Tables_SOS_Entropy_RST_Entropy_RST_Entropy_EOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new DqtSegment());
		elements.add(new ComSegment());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new DhtSegment());
		elements.add(new AppNSegment((AppNSegment.FIRST_MARKERID)));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new RstMMarker(RstMMarker.FIRST_MARKERID));
		elements.add(new EntropyData());
		elements.add(new RstMMarker(RstMMarker.FIRST_MARKERID + 1));
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void SOI_Tables_SOF_Tables_SOS_Entropy_RST_Entropy_DNL_SOS_Entropy_EOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new DqtSegment());
		elements.add(new ComSegment());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new DhtSegment());
		elements.add(new AppNSegment((AppNSegment.FIRST_MARKERID)));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new RstMMarker(RstMMarker.FIRST_MARKERID));
		elements.add(new EntropyData());
		elements.add(new DnlSegment());
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void SOI_T_SOF_T_SOS_E_RST_E_DNL_T_SOS_E_EOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new DqtSegment());
		elements.add(new ComSegment());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new DhtSegment());
		elements.add(new AppNSegment((AppNSegment.FIRST_MARKERID)));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new RstMMarker(RstMMarker.FIRST_MARKERID));
		elements.add(new EntropyData());
		elements.add(new DnlSegment());
		elements.add(new DqtSegment());
		elements.add(new ComSegment());
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void SOI_T_SOF_T_SOS_E_RST_E_DNL_T_SOS_E_RST_E_EOI_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new DqtSegment());
		elements.add(new ComSegment());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new DhtSegment());
		elements.add(new AppNSegment((AppNSegment.FIRST_MARKERID)));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new RstMMarker(RstMMarker.FIRST_MARKERID));
		elements.add(new EntropyData());
		elements.add(new DnlSegment());
		elements.add(new DqtSegment());
		elements.add(new ComSegment());
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new RstMMarker(RstMMarker.FIRST_MARKERID));
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void segmentsWithExtraFf_noProblems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new ExtraFf());
		elements.add(new SoiMarker());
		elements.add(new DqtSegment());
		elements.add(new ComSegment());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new ExtraFf());
		elements.add(new DhtSegment());
		elements.add(new AppNSegment((AppNSegment.FIRST_MARKERID)));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new ExtraFf());
		elements.add(new RstMMarker(RstMMarker.FIRST_MARKERID));
		elements.add(new EntropyData());
		elements.add(new ExtraFf());
		elements.add(new DnlSegment());
		elements.add(new ExtraFf());
		elements.add(new DqtSegment());
		elements.add(new ExtraFf());
		elements.add(new ComSegment());
		elements.add(new SosSegment());
		elements.add(new ExtraFf());
		elements.add(new EntropyData());
		elements.add(new RstMMarker(RstMMarker.FIRST_MARKERID));
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(0, validator.validate(elements).size());
	}

	@Test
	public void Empty_Problems() {
		List<DataItem> elements = new ArrayList<DataItem>();

		assertEquals(1, validator.validate(elements).size());
	}

	@Test
	public void SOI_Problems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());

		assertEquals(1, validator.validate(elements).size());
	}

	@Test
	public void SOI_EOI_Problems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new EoiMarker());

		assertEquals(2, validator.validate(elements).size());
	}

	@Test
	public void SOI_SOF_EOI_Problems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new EoiMarker());

		assertEquals(2, validator.validate(elements).size());
	}

	@Test
	public void SOI_SOF_SOS_EOI_Problems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new SosSegment());
		elements.add(new EoiMarker());

		assertEquals(2, validator.validate(elements).size());
	}

	@Test
	public void SOI_SOF_SOS_E_DNL_EOI_Problems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new DnlSegment());
		elements.add(new EoiMarker());

		assertEquals(2, validator.validate(elements).size());
	}

	@Test
	public void SOI_SOF_SOS_E_SOS_DNL_E_EOI_Problems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new DnlSegment());
		elements.add(new EoiMarker());

		assertEquals(2, validator.validate(elements).size());
	}

	@Test
	public void SOI_SOF_SOS_RST_E_EOI_Problems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new SosSegment());
		elements.add(new RstMMarker(RstMMarker.FIRST_MARKERID));
		elements.add(new EntropyData());
		elements.add(new EoiMarker());

		assertEquals(1, validator.validate(elements).size());
	}

	@Test
	public void SOI_SOF_SOS_E_RST_EOI_Problems() {
		List<DataItem> elements = new ArrayList<DataItem>();
		elements.add(new SoiMarker());
		elements.add(new SofSegment(SofSegment.FIRST1_MARKERID));
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new SosSegment());
		elements.add(new EntropyData());
		elements.add(new RstMMarker(RstMMarker.FIRST_MARKERID));
		elements.add(new EoiMarker());

		assertEquals(2, validator.validate(elements).size());
	}
}
