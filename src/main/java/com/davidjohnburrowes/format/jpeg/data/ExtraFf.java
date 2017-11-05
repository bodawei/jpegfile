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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Simple DataItem that represents a series of 0xFF bytes.  Used by parsers to
 * represent redundant series of 0xFF bytes in a JPEG file.
 *
 * Note that this can not read anything, but only write.
 */
public class ExtraFf extends DataItem {
	/*
	 * The number of 0xFF bytes that preceede the first "real" 0xFF byte.
	 */
	private int ffCount;

	/**
	 * Constructor
	 */
	public ExtraFf() {
	}

	/*
	 * @param the number of 0xFF bytes this marker should track
	 */
	public void setFfCount(int count) {
		ffCount = count;
	}

	/*
	 * @return the number of 0xFF bytes that preceede this marker.
	 */
	public int getFfCount() {
		return ffCount;
	}

	/*
	 * @return the number of bytes of data. This does NOT include the 0xFF or markerId.
	 */
	@Override
	public int getSizeOnDisk() {
		return getFfCount();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Writes however many leading 0xFF, as well as the required 0xFF byte and
	 * marker ID.
	 *
	 * @param stream The stream to write to
	 * @throws IOException If an io problem happens.
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		super.write(stream);

		for (int index = 0; index < getFfCount(); index++) {
			stream.write((byte)0xFF);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if ((other != null) &&
			this.getClass().isInstance(other) &&
			((ExtraFf)other).getFfCount() == this.getFfCount()) {
			return true;
		}

		return false;
	}

	/*
	 * Removes any optional data, which in this case means removing all the
	 * 0xFF's.
	 */
	@Override
	public void clearPassthrough() {
		super.clearPassthrough();
		setFfCount(0);
	}
}
