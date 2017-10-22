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
import bdw.format.jpeg.support.MarkerId;
import bdw.io.LimitingDataInput;
import bdw.util.Size;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Define Restart Interval
 *
 * Defined on page B-16 of the standard.
 */
@MarkerId(DriSegment.MARKERID)
public class DriSegment extends MarkerSegment {
	private static final  DataBounds riBounds =
			  new DataBounds("restartInterval", Size.SHORT, 0, 65535);
	private static final  DataBounds segSizeBounds =
			  new DataBounds("segmentSize", Size.SHORT, 4, 4);

	/**
	 * The only allowable marker for this segment type
	 */
	public static final int MARKERID = 0xDD;

	/**
	 * The restart interval this segment is managing
	 */
	private int restartInterval;

	/**
	 * Constructor
	 */
	public DriSegment() {
		super(MARKERID);
		restartInterval = 0;
	}

	/**
	 * @param interval The interval [0-65535]
	 */
	public void setRestartInterval(int interval) {
		riBounds.throwIfInvalid(interval, this.getFrameMode(), this.getDataMode());

		restartInterval = interval;
	}

	/**
	 * @return The interval value
	 */
	public int getRestartInterval() {
		return restartInterval;
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
		riBounds.accumulateOnViolation(getRestartInterval(), this.getFrameMode(), results);

		return results;
	}

	/**
	 * Two DriSegments are equal if they have the same restart interval
	 * @param other The other object to test
	 * @return If other and this are equal
	 */
	@Override
	public boolean equals(Object other) {
		return (super.equals(other) &&
				  (getRestartInterval() == ((DriSegment) other).getRestartInterval()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + this.restartInterval;
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readParameters(LimitingDataInput input) throws IOException {
		int remaining = input.getRemainingLimit();
		segSizeBounds.throwIfInvalid(remaining + 2, this.getFrameMode(), this.getDataMode());

		setRestartInterval(input.readUnsignedShort());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeParameters(DataOutputStream stream) throws IOException {
		super.writeParameters(stream);
		stream.writeShort(getRestartInterval());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void checkModeChange() {
		super.checkModeChange();
		setRestartInterval(getRestartInterval());
	}
}
