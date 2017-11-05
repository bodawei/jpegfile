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
package com.davidjohnburrowes.formats.jpeg.test;

import com.davidjohnburrowes.format.encode.Hex2Bin;
import com.davidjohnburrowes.format.jpeg.data.DataItem;
import com.davidjohnburrowes.format.jpeg.support.DataMode;
import com.davidjohnburrowes.format.jpeg.support.FrameMode;
import com.davidjohnburrowes.util.Size;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;

public class TestUtils {
	public static final int SKIP_CASE = Integer.MAX_VALUE;

	/**
	 * An enumeration of errors used by the property test routine, below.
	 */
	public enum PropResult {
		SUCCESS,
		INRANGE_ERROR1,
		INRANGE_ERROR2,
		INRANGE_ERROR3,
		INRANGE_ERROR4,
		INRANGE_ERROR5,
		CANT_INVOKE_SETTER_OR_GETTER,
		BELOWMIN_ERROR,
		ABOVEMAX_ERROR,
		ABOVELAXMAX_ERROR,
		SWITCH_FROM_MAX_TO_STRICT_ERROR,
		SWITCH_FROM_MIN_TO_STRICT_ERROR,
		GET_ERROR,
		ERROR
	}

	/**
	 * Given a string containing hexadecimal digits, produce a byte array
	 * containing the bytes specified in the string.
	 */
	public byte[] makeByteArray(String rawInput) throws IOException {
		StringReader inputReader = new StringReader(rawInput);
		byte[] rawInputBytes = new byte[rawInput.length()];
		int aChar = inputReader.read();
		int index = 0;
		while (aChar != -1) {
			rawInputBytes[index] = (byte) aChar;
			aChar = inputReader.read();
			index++;
		}

		Hex2Bin encoder = new Hex2Bin();
		InputStream inputStream = new ByteArrayInputStream(rawInputBytes);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		encoder.convert(inputStream, outputStream);

		return outputStream.toByteArray();
	}

	/**
	 * Given a string of hex digits, create an InputStream that accesses the
	 * binary data specified by those digits.
	 */
	public InputStream makeInputStream(String rawInput) throws IOException {
		return new ByteArrayInputStream(makeByteArray(rawInput));
	}

	/**
	 * Given a string of hex digits, create a file on disk and populate it with
	 * bytes specified by those hex digits.
	 */
	public RandomAccessFile makeRandomAccessFile(String data) throws IOException {
		File temp = File.createTempFile("prefix", "suffix");
		FileOutputStream output = new FileOutputStream(temp);

		StringReader inputReader = new StringReader(data);
		byte[] rawInputBytes = new byte[data.length()];
		int aChar = inputReader.read();
		int index = 0;
		while (aChar != -1) {
			rawInputBytes[index] = (byte) aChar;
			aChar = inputReader.read();
			index++;
		}

		Hex2Bin encoder = new Hex2Bin();
		InputStream inputStream = new ByteArrayInputStream(rawInputBytes);

		encoder.convert(inputStream, output);

		output.flush();
		output.close();

		return new RandomAccessFile(temp, "r");
	}

	/**
	 * Overloaded test routine.  Tests the item using a simple property accessor.
	 */
	public PropResult testProp(DataItem item, String propName,
			  Size dataSize,
			  int min, int max) {
		return testProp(item,propName, dataSize, min, max, min, max, min, max, min, max);
	}

	/**
	 * Overloaded routine. Tests the item with a simple property accessor.
	 */
	public PropResult testProp(DataItem item, String propName,
		  Size dataSize,
		  int baselineMin, int baselineMax,
		  int extendedMin, int extendedMax,
		  int progressiveMin, int progressiveMax,
		  int losslessMin, int losslessMax) {
		SimplePropertyAccessor simple = new SimplePropertyAccessor(propName, item, this);
		return this.testProp(simple, item, dataSize, baselineMin, baselineMax, extendedMin, extendedMax,
			  progressiveMin, progressiveMax,
			  losslessMin, losslessMax);
	}

	/**
	 * Tests one property of the specified DataItem in a wide variety of
	 * scenarios.  This runs the specified property through a bunch of tests for
	 * each of the possible categories of FrameMode values.  That is, it will
	 * test the property in a baseline mode, in an extended mode, etc.
	 *
	 * @param accessor An object which will provide read/write access to the
	 *						property to be tested
	 * @param item The data item that contains the property to access
	 * @param dataSize The size of the property's data on disk
	 * @param baselineMin The minimum value in a baseline FrameMode
	 * @param baselineMax The maximum value in a baseline FrameMode
	 * @param extendedMin The minimum value in an extended FrameMode
	 * @param extendedMax The maximum value in an extended FrameMode
	 * @param progressiveMin The minimum value in a progressive FrameMode
	 * @param progressiveMax The maximum value in a progressive FrameMode
	 * @param losslessMin The minimum value in a lossless FrameMode
	 * @param losslessMax The maximum value in a losaless FrameMode
	 * @return One of the PropResult values, above.
	 */
	public PropResult testProp(Accessor accessor, DataItem item,
			  Size dataSize,
			  int baselineMin, int baselineMax,
			  int extendedMin, int extendedMax,
			  int progressiveMin, int progressiveMax,
			  int losslessMin, int losslessMax) {
		PropResult result;

		result = testPropWithOneFrame(accessor, item, dataSize, null, 0, dataSize.getMax());
		if (result != PropResult.SUCCESS) {
			return result;
		}

		if (baselineMin != SKIP_CASE) {
			result = testPropWithOneFrame(accessor, item, dataSize,
					  FrameMode.HUFF_BASELINE_SEQUENTIAL_DCT, baselineMin, baselineMax);
			if (result != PropResult.SUCCESS) {
				return result;
			}
		}

		if (extendedMin != SKIP_CASE) {
			result = testPropWithOneFrame(accessor, item, dataSize,
					  FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT, extendedMin, extendedMax);

			if (result != PropResult.SUCCESS) {
				return result;
			}
		}

		if (progressiveMin != SKIP_CASE) {
			result = testPropWithOneFrame(accessor, item, dataSize,
					  FrameMode.HUFF_PROGRESSIVE_DCT, progressiveMin, progressiveMax);

			if (result != PropResult.SUCCESS) {
				return result;
			}
		}

		if (losslessMin != SKIP_CASE) {
			result = testPropWithOneFrame(accessor, item, dataSize,
					  FrameMode.HUFF_LOSSLESS, losslessMin, losslessMax);
			if (result != PropResult.SUCCESS) {
				return result;
			}
		}

		return result;
	}

	/**
	 * Test the data bounds of a particular property.  Each property is assumed
	 * to have a range of values it should adhere to.  So, this tests below that
	 * range, above the range, at the min and max of the range, and a value
	 * between the min and max.  The result of this is returned as a constant
	 * value.  This also tests that in LAX mode that the full range of the
	 * disk size is accepted.
	 *
	 * Note: When this fails, it is still relatively opaque and requires
	 * debugging to figure out what is going on.  I'm not proud of this design,
	 * but it has been sufficient for my own development.
	 *
	 * @param accessor An object that will access the property to be tested
	 * @param item The DataItem being tested
	 * @param dataSize The size of the property on disk
	 * @param mode The FrameMode to do this testing in
	 * @param min The minimum value allowed in this FrameMode
	 * @param max The maximum value allowed in this FrameMode
	 * @return An enum value approximately pinpointing the failure, if any.
	 */
	public PropResult testPropWithOneFrame(Accessor accessor, DataItem item,
			  Size dataSize, FrameMode mode, int min, int max) {
		int belowMin = min - 1;
		int aboveMax = max + 1;
		int middle = min + ((max - min) / 2);

		item.setDataMode(DataMode.LAX);
		item.setFrameMode(mode);
		Number num;
		try {
			accessor.setValue(min);
			item.setDataMode(DataMode.STRICT);
			accessor.setValue(min);
			num = (Number) accessor.getValue();
			if (num.intValue() != min) {
				return PropResult.GET_ERROR;
			}
		} catch (Throwable e) {
			return PropResult.INRANGE_ERROR1;
		}

		try {
			accessor.setValue(middle);
			num = (Number) accessor.getValue();
			if (num.intValue() != middle) {
				return PropResult.GET_ERROR;
			}
		} catch (Throwable e) {
			return PropResult.INRANGE_ERROR2;
		}

		try {
			accessor.setValue(max);
			num = (Number) accessor.getValue();
			if (num.intValue() != max) {
				return PropResult.GET_ERROR;
			}
		} catch (Throwable e) {
			return PropResult.INRANGE_ERROR3;
		}

		try {
			item.setDataMode(DataMode.LAX);
			accessor.setValue(0);
			num = (Number) accessor.getValue();
			if (num.intValue() != 0) {
				return PropResult.GET_ERROR;
			}
		} catch (Throwable e) {
			return PropResult.INRANGE_ERROR4;
		}

		try {
			accessor.setValue(dataSize.getMax());
			num = (Number) accessor.getValue();
			if (num.intValue() != dataSize.getMax()) {
				return PropResult.GET_ERROR;
			}

			accessor.setValue(min);
		} catch (Throwable e) {
			return PropResult.INRANGE_ERROR5;
		}

		try {
			item.setDataMode(DataMode.STRICT);
			accessor.setValue(belowMin);
			return PropResult.BELOWMIN_ERROR;
		} catch (Throwable e) {
		}

		try {
			item.setDataMode(DataMode.STRICT);
			accessor.setValue(aboveMax);
			return PropResult.ABOVEMAX_ERROR;
		} catch (Throwable e) {
		}


		try {
			item.setDataMode(DataMode.LAX);
			accessor.setValue(dataSize.getMax() + 1);
			return PropResult.ABOVELAXMAX_ERROR;
		} catch (Throwable e) {
		}

		if (max < dataSize.getMax()) {
			try {
				item.setDataMode(DataMode.LAX);
				accessor.setValue(dataSize.getMax());
				item.setDataMode(DataMode.STRICT);
				return PropResult.SWITCH_FROM_MAX_TO_STRICT_ERROR;
			} catch (Throwable e) {
			}
		}

		if (min > 0) {
			try {
				item.setDataMode(DataMode.LAX);
				accessor.setValue(0);
				item.setDataMode(DataMode.STRICT);
				return PropResult.SWITCH_FROM_MIN_TO_STRICT_ERROR;
			} catch (Throwable e) {
			}
		}

		return PropResult.SUCCESS;
	}
}
