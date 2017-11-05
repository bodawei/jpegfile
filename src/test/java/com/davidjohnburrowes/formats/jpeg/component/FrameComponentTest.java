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

import com.davidjohnburrowes.format.jpeg.component.FrameComponent;
import com.davidjohnburrowes.format.jpeg.support.DataMode;
import com.davidjohnburrowes.format.jpeg.support.FrameMode;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.formats.jpeg.test.TestUtils;
import com.davidjohnburrowes.util.Size;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class FrameComponentTest {
	private FrameComponent component;
	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
		component = new FrameComponent();
		component.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
	}

	@Test
	public void setComponentId_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(component, "componentId", Size.BYTE, 0, 255));
	}

	@Test
	public void setHorizontalScaling_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(component, "horizontalScaling", Size.NIBBLE, 1, 4));
	}

	@Test
	public void setVerticalScaling_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(component, "verticalScaling", Size.NIBBLE, 1, 4));
	}

	@Test
	public void setQuantizationSelector_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(component, "quantizationSelector", Size.BYTE,
				  0, 3,
				  0, 3,
				  0, 3,
				  0, 0));
	}

	@Test
	public void setQuantizationSelector_forHierarchicalMode_honorsBounds() {
		component = new FrameComponent();
		component.setHierarchicalMode(true);
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(component, "quantizationSelector", Size.BYTE,
				  0, 0,
				  0, 0,
				  0, 0,
				  0, 0));
	}

	@Test
	public void equals_twoNewlyCreated_areEqual() {
		assertTrue(component.equals(new FrameComponent()));
	}

	@Test
	public void equals_hierarchicalAndNonHierarchical_areNotEqual() {
		component.setHierarchicalMode(true);
		assertFalse(component.equals(new FrameComponent()));
	}

	@Test
	public void equals_otherObject_areNotEqual() {
		assertFalse(component.equals(new Object()));
	}

	@Test
	public void sizeOnDisk_isThree() {
		assertEquals(3, component.getSizeOnDisk());
	}

	@Test
	public void validate_initiallyValid() {
		assertEquals(0, component.validate().size());
	}

	@Test
	public void validate_withInvalidValue_reportsAnError() {
		component.setDataMode(DataMode.LAX);
		component.setHorizontalScaling(15);
		assertEquals(1, component.validate().size());
	}

	@Test
	public void read_withValidInput_readsCorrectly() throws IOException {
		component.readParameters(new DataInputStream(
				  utils.makeInputStream("55 31 02")));
		FrameComponent expected = new FrameComponent();
		expected.setComponentId(0x55);
		expected.setHorizontalScaling(3);
		expected.setVerticalScaling(1);
		expected.setQuantizationSelector(2);
		assertEquals(expected, component);
	}

	@Test(expected=InvalidJpegFormat.class)
	public void read_withInvalidInput_throwsException() throws IOException {
		component.readParameters(new DataInputStream(
				  utils.makeInputStream("55 AB 02")));
	}

	@Test
	public void read_withInvalidInputInLaxMode_readsInCorrectly() throws IOException {
		component.setDataMode(DataMode.LAX);
		component.readParameters(new DataInputStream(
				  utils.makeInputStream("55 AB 02")));

		FrameComponent expected = new FrameComponent();
		expected.setDataMode(DataMode.LAX);
		expected.setComponentId(0x55);
		expected.setHorizontalScaling(10);
		expected.setVerticalScaling(11);
		expected.setQuantizationSelector(2);

		assertEquals(expected, component);
	}

	@Test
	public void write_goodComponent_writesTheCorrectData() throws IOException {
		component.setComponentId(0xAB);
		component.setHorizontalScaling(3);
		component.setVerticalScaling(2);
		component.setQuantizationSelector(2);
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		component.writeParameters(new DataOutputStream(output));

		assertArrayEquals(utils.makeByteArray("AB 32 02"), output.toByteArray());
	}

	@Test
	public void write_withInvalidComponent_writesTheCorrectInvalidData() throws IOException {
		component.setDataMode(DataMode.LAX);
		component.readParameters(new DataInputStream(
				  utils.makeInputStream("55 AB 02")));
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		component.writeParameters(new DataOutputStream(output));

		assertArrayEquals(utils.makeByteArray("55 AB 02"), output.toByteArray());
	}
}
