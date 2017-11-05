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
package com.davidjohnburrowes.format.jpeg.marker;

import com.davidjohnburrowes.format.jpeg.data.MarkerSegment;
import com.davidjohnburrowes.format.jpeg.support.DataBounds;
import com.davidjohnburrowes.format.jpeg.support.MarkerId;
import com.davidjohnburrowes.io.LimitingDataInput;
import com.davidjohnburrowes.util.Size;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Define number of lines
 *
 * Defined on page B-18 of the standard.
 */
@MarkerId(DnlSegment.MARKERID)
public class DnlSegment extends MarkerSegment {
	private static final  DataBounds nlBounds =
			  new DataBounds("numberOfLines", Size.SHORT, 1, 65535);
	private static final  DataBounds segSizeBounds =
			  new DataBounds("segmentSize", Size.SHORT, 4, 4);
	/**
	 * Marker for this segment type
	 */
	public static final int MARKERID = 0xDC;

	private int numberOfLines;

	/**
	 * Construct an instance with a number of lines of 1.
	 */
	public DnlSegment() {
		super(MARKERID);
		numberOfLines = 1;
	}

	/**
	 * @param lineCount The number of lines [1-65535]
	 */
	public void setNumberOfLines(int lineCount) {
		nlBounds.throwIfInvalid(lineCount, this.getFrameMode(), this.getDataMode());

		numberOfLines = lineCount;
	}

	/**
	 * @return The number of lines
	 */
	public int getNumberOfLines() {
		return numberOfLines;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + 2;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();

		segSizeBounds.accumulateOnViolation(getParameterSizeOnDisk(), this.getFrameMode(), results);
		nlBounds.accumulateOnViolation(getNumberOfLines(), this.getFrameMode(), results);

		return results;
	}

	/**
	 * Two DriSegments are equal if they have the same number of lines
	 * @param other The other object to test
	 * @return If other and this are equal
	 */
	@Override
	public boolean equals(Object other) {
		return (super.equals(other) &&
				  getNumberOfLines() == ((DnlSegment) other).getNumberOfLines());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + this.numberOfLines;
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readParameters(LimitingDataInput input) throws IOException {
		int remaining = input.getRemainingLimit();
		segSizeBounds.throwIfInvalid(remaining + 2, this.getFrameMode(), this.getDataMode());

		setNumberOfLines(input.readUnsignedShort());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeParameters(DataOutputStream stream) throws IOException {
		super.writeParameters(stream);

		stream.writeShort(getNumberOfLines());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void checkModeChange() {
		super.checkModeChange();
		setNumberOfLines(getNumberOfLines());
	}
}
