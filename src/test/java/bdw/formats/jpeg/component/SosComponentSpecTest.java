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
package bdw.formats.jpeg.component;

import bdw.format.jpeg.component.SosComponentSpec;
import bdw.format.jpeg.support.DataMode;
import bdw.format.jpeg.support.FrameMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.formats.jpeg.test.TestUtils;
import bdw.util.Size;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class SosComponentSpecTest {
	private SosComponentSpec spec;
	private TestUtils utils;

	@Before
	public void setUp() {
		utils = new TestUtils();
		spec = new SosComponentSpec();
		spec.setFrameMode(FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT);
	}

	@Test
	public void setComponentSelector_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(spec, "componentSelector", Size.BYTE,
				  0, 255));
	}

	@Test
	public void setDcTableSelector_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(spec, "dcTableSelector", Size.NIBBLE,
				  0, 1,
				  0, 3,
				  0, 3,
				  0, 3));
	}

	@Test
	public void setAcTableSelector_honorsBounds() {
		assertEquals(TestUtils.PropResult.SUCCESS,
			utils.testProp(spec, "acTableSelector", Size.NIBBLE,
				  0, 1,
				  0, 3,
				  0, 3,
				  0, 0));
	}

	@Test
	public void equals_withSelf_isTrue() {
		assertTrue(spec.equals(spec));
	}

	@Test
	public void equals_withObject_isFalse() {
		assertFalse(spec.equals(new Object()));
	}

	@Test
	public void equals_withDifferentTable_isFalse() {
		spec.setDcTableSelector(1);
		assertFalse(spec.equals(new SosComponentSpec()));
	}

	@Test
	public void read_goodInput_readSuccessfully() throws IOException {
		InputStream stream = utils.makeInputStream("00 11");

		spec.readParameters(new DataInputStream(stream));

		SosComponentSpec expected = new SosComponentSpec();
		expected.setDcTableSelector(1);
		expected.setAcTableSelector(1);

		assertTrue(expected.equals(spec));
	}

	@Test(expected=InvalidJpegFormat.class)
	public void read_badInput_throwsException() throws IOException {
		InputStream stream = utils.makeInputStream("FF FF");

		spec.readParameters(new DataInputStream(stream));
	}

	@Test
	public void write_exampleInstance_generatesGoodDataStream() throws IOException {
		SosComponentSpec aTable = new SosComponentSpec();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		aTable.setComponentSelector(1);
		aTable.setDcTableSelector(2);
		aTable.setAcTableSelector(3);

		aTable.writeParameters(new DataOutputStream(output));

		byte[] expectedBytes = utils.makeByteArray("01 23");

		assertArrayEquals(expectedBytes, output.toByteArray());
	}

	@Test
	public void getSizeOnDisk_isTwo() {
		assertEquals(2, spec.getSizeOnDisk());
	}

	@Test(expected=InvalidJpegFormat.class)
	public void setValidateMode_toStrictState_throwsException() throws IOException {
		spec.setDataMode(DataMode.LAX);
		spec.setDcTableSelector(3);
		spec.setDataMode(DataMode.STRICT);
	}
}
