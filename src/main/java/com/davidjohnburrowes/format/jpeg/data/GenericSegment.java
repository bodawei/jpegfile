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
package com.davidjohnburrowes.format.jpeg.data;

import com.davidjohnburrowes.io.LimitingDataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Generic base class, used for segments which are just undifferentiated series
 * of bytes. Generally these are "reserved" segments.
 */
public class GenericSegment extends MarkerSegment {

	private static final int MAX_SEGMENT_DATA_SIZE = 65534;
	/**
	 * The data we have read from the file or stream
	 */
	protected byte[] data;

	/**
	 * construct the instance
    * @param markerId Id of the marker.
	 */
	public GenericSegment(int markerId) {
		super(markerId);
		data = new byte[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + data.length;
	}

	/**
	 * @param bytes The bytes this should hold. This array is used directly, not
	 * copied. This may not be more than 65K bytes
	 */
	protected void setByteArray(byte[] bytes) {
		if (bytes.length >= MAX_SEGMENT_DATA_SIZE) {
			throw new IllegalArgumentException();
		}
		data = bytes;
	}

	/**
	 * @return The bytes this represents
	 */
	protected byte[] getByteArray() {
		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		return (super.equals(other) &&
			Arrays.equals(this.getByteArray(), ((GenericSegment)other).getByteArray()));
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 29 * hash + Arrays.hashCode(this.data);
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readParameters(LimitingDataInput input) throws IOException {
		super.readParameters(input);
		int remaining = input.getRemainingLimit();
		byte[] buffer = new byte[remaining];

		input.readFully(buffer);

		data = buffer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeParameters(DataOutputStream output) throws IOException {
		super.writeParameters(output);
		byte[] bytes = getByteArray();
		output.write(bytes);
	}
}
