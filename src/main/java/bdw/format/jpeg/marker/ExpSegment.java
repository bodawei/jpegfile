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
package bdw.format.jpeg.marker;

import bdw.format.jpeg.data.MarkerSegment;
import bdw.format.jpeg.support.DataBounds;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.MarkerId;
import bdw.io.LimitingDataInput;
import bdw.util.Nibble;
import bdw.util.Size;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Expand reference component
 *
 * Defined on page B-20 of the standard.
 */
@MarkerId(ExpSegment.MARKERID)
public class ExpSegment extends MarkerSegment {
	private static final DataBounds ehBounds =
			  new DataBounds("expandHorizontally", Size.NIBBLE, 0, 1);
	private static final  DataBounds evBounds =
			  new DataBounds("expandVertically", Size.NIBBLE, 0, 1);
	private static final  DataBounds segSizeBounds =
			  new DataBounds("segmentSize", Size.SHORT, 3, 3);
	public static final int MARKERID = 0xDF;

	private byte expandHorizontally;
	private byte expandVertically;

	public ExpSegment() {
		super(MARKERID);
	}

	/*
	 * Defined to only be 0 or 1.  Four bits in size.
	 */
	public void setExpandHorizontally(int value) {
		ehBounds.throwIfInvalid(value, this.getFrameMode(), this.getDataMode());

		expandHorizontally = (byte)value;
	}

	public byte getExpandHorizontally() {
		return expandHorizontally;
	}

	/*
	 * Defined to only be 0 or 1.  Four bits in size.
	 */
	public void setExpandVertically(int value) {
		evBounds.throwIfInvalid(value, this.getFrameMode(), this.getDataMode());

		expandVertically = (byte)value;
	}

	public byte getExpandVertically() {
		return expandVertically;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();

		segSizeBounds.accumulateOnViolation(getSizeOnDisk(), this.getFrameMode(), results);
		ehBounds.accumulateOnViolation(getExpandHorizontally(), this.getFrameMode(), results);
		evBounds.accumulateOnViolation(getExpandVertically(), this.getFrameMode(), results);

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if (!super.equals(other)) {
			return false;
		}

		ExpSegment castOther = (ExpSegment) other;

		if (castOther.getExpandVertically() != getExpandVertically() ||
			castOther.getExpandHorizontally() != getExpandHorizontally()) {
			return false;
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 13 * hash + getExpandHorizontally();
		hash = 13 * hash + getExpandVertically();
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readParameters(LimitingDataInput input) throws IOException {
		int remaining = input.getRemainingLimit();
		segSizeBounds.throwIfInvalid(remaining + 2, this.getFrameMode(), this.getDataMode());

		int data = input.readUnsignedByte();

		setExpandHorizontally(Nibble.getUpper(data));
		setExpandVertically(Nibble.getLower(data));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeParameters(DataOutputStream stream) throws IOException {
		super.writeParameters(stream);

		stream.writeByte(Nibble.makeByte(getExpandHorizontally(),
				  getExpandVertically()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void checkModeChange() {
		super.checkModeChange();
		setExpandHorizontally(getExpandHorizontally());
		setExpandVertically(getExpandVertically());
	}

}
