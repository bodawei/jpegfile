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
package com.davidjohnburrowes.format.jpeg.support;

import com.davidjohnburrowes.util.Size;
import java.util.List;

/**
 * This is used to help evaluate the bounds for a data value. In many segments,
 * a particular field may have different ranges of values that are allowed based
 * on the current frame mode. For example, in the SOS segment, the successive
 * approximation bit position low or point transform value can be 0, or 0-13 or
 * 0-15 depending on which frame mode it is interpreted within. A DataBounds
 * instance can be set up with these ranges for each of the major categories of
 * frame modes, and thereafter it can be provided a value and it will return
 * an indication of whether that value is valid for a particular mode.
 *
 * Put another way, rather than having a myriad of tedious checks in each and
 * every setter in all the segments, the segment classes instead set up a
 * DataBounds instance for each property, and then rely on it to do most checks
 * (in a handful of cases, there are checks that can't be managed by this class,
 * but this covers the vast majority of the cases.
 *
 * All values are unsigned integers.
 *
 * Note that when the frame mode is unknown (null), any value that can be
 * represented in the disk storage space is allowed.
 */
public class DataBounds {

	private String name;
	private Size size;
	private int baseLower;
	private int baseUpper;
	private int extLower;
	private int extUpper;
	private int proLower;
	private int proUpper;
	private int lossLower;
	private int lossUpper;

	/**
	 * Initialize an instance. Values passed to the constructor are all inclusive.
	 *
	 * @param name the name of the property
	 * @param size the data size of the property on disk
	 * @param baseLower the lower bound of acceptable values in 'baseline" frame mode
	 * @param baseUpper the upper bound of values in baseline mode
	 * @param extLower the lower bound of values in extended mode
	 * @param extUpper the upper bound of values in extended mode
	 * @param proLower the lower bound of values in progressive mode
	 * @param proUpper the upper bound of values in progressive mode
	 * @param lossLower the lower bound of values in lossless mode
	 * @param lossUpper the upper bound of values in lossless mode
	 */
	public DataBounds(String name, Size size,
			  int baseLower, int baseUpper,
			  int extLower, int extUpper,
			  int proLower, int proUpper,
			  int lossLower, int lossUpper) {
		this.name = name;
		this.size = size;
		this.baseLower = baseLower;
		this.baseUpper = baseUpper;
		this.extLower = extLower;
		this.extUpper = extUpper;
		this.proLower = proLower;
		this.proUpper = proUpper;
		this.lossLower = lossLower;
		this.lossUpper = lossUpper;
	}

	/**
	 * Initializes an instance where the range of values is the same across all
	 * frame modes. Values passed to the constructor are all inclusive.
	 *
	 * @param name the name of the property
	 * @param size the size of the property on disk
	 * @param lower the lower bound of values across all frame modes
	 * @param upper the upper bound of values across all frame modes
	 */
	public DataBounds(String name, Size size,
			  int lower, int upper) {
		this(name, size, lower, upper, lower, upper, lower, upper, lower, upper);
	}

	/**
	 * If the value isn't valid within the provided modes, throw an exception.
	 *
	 * @param value the value to evaluated
	 * @param frameMode the frame mode to evaluate the value within
	 * @param mode the data mode to evaluate this within
	 */
	public void throwIfInvalid(int value, FrameMode frameMode, DataMode mode) {
		RuntimeException e = checkState(value, frameMode);

		if (e != null &&
				(e instanceof IllegalArgumentException ||
					mode == DataMode.STRICT)) {
			throw e;
		}
	}

	/**
	 * If the provided value isn't valid in the provided frame mode, add an
	 * exception to the provided list
	 *
	 * @param value the value to evaluated
	 * @param frameMode the frame mode to evaluate the value within
	 * @param list the list to add an exception to if needed.
	 */
	public void accumulateOnViolation(int value, FrameMode frameMode,
			  List<Exception> list) {
		RuntimeException e = checkState(value, frameMode);

		if (e != null) {
			list.add(e);
		}
	}

	/**
	 * @param frameMode the framemode to do this evaluation within
	 * @return the upper bound based on the frame mode
	 */
	private int upperFor(FrameMode frameMode) {
		if (frameMode == null) {
			return size.getMax();
		}

		if (frameMode.isSequentialBaseline()) {
			return baseUpper;
		} else if (frameMode.isSequentialExtended()) {
			return extUpper;
		} else if (frameMode.isProgressive()) {
			return proUpper;
		} else if (frameMode.isLossless()) {
			return lossUpper;
		}

		return size.getMax();
	}

	/**
	 * @param frameMode the frame mode to do this evaluation within
	 * @return the lower bound based on the frame mode
	 */
	private int lowerFor(FrameMode frameMode) {
		if (frameMode == null) {
			return 0;
		}

		if (frameMode.isSequentialBaseline()) {
			return baseLower;
		} else if (frameMode.isSequentialExtended()) {
			return extLower;
		} else if (frameMode.isProgressive()) {
			return proLower;
		} else if (frameMode.isLossless()) {
			return lossLower;
		}

		return 0;
	}

	/**
	 * Given a value, frame mode and data mode, return an IllegalArgumentException
	 * if the value is outside of the storage space available for this property
	 * on disk, or an InvalidJpegFormat if it is within that range, but outside
	 * of the range allowed by the frame mode
	 *
	 * @param value the value to consider
	 * @param frameMode the frame mode to consider this within
	 * @param mode the data mode to consider this within
	 * @return an exception if the value is not valid, or null
	 */
	private RuntimeException checkState(int value, FrameMode frameMode) {
		if (value < 0 || value > size.getMax()) {
			return new IllegalArgumentException(name
					  + " must be between 0 and " + size.getMax() + ". However, found: " + value);
		}
		int lower = lowerFor(frameMode);
		int upper = upperFor(frameMode);

		if (value < lower || value > upper) {
			return new InvalidJpegFormat(name + " should be between " + lowerFor(frameMode)
					  + " and " + upperFor(frameMode) + ". However, found: " + value);
		}

		return null;
	}
}
