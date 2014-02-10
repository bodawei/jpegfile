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
package bdw.format.jpeg.data;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Base class for all JPEG markers.
 * A marker is started with a 0xFF byte, followed by an additional byte which
 * signifies the kind of marker.
 *
 * Each non-abstract subclass of this class must be annotated with an annotation
 * to indicate what what marker id's it can hold.  This is used by parsers
 * to pick which marker should be fed the next chunk of data. The most common
 * annotation would be @Marker(0xXX), but there are others that may be used
 * as well.

 */
public abstract class Marker extends DataItem {

	/**
	 * The marker id for this marker
	 */
	protected int markerId;

	/**
	 * Constructor
	 * @param markerId The ID to assign
	 */
	public Marker(int markerId) {
		this.markerId = markerId;
	}

	/**
	 * @return the code that represents this marker.
	 */
	public int getMarkerId() {
		return markerId;
	}

	/*
	 * @return the number of bytes of data. This does NOT include the 0xFF or markerId.
	 */
	@Override
	public int getSizeOnDisk() {
		return 2;
	}

	/*
	 * @return the size of the marker data (data after the 0xFF and marker id)
	 */
	public int getParameterSizeOnDisk() {
		return getSizeOnDisk() - 2;
	}

	/**
	 * @inheritdoc
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

		stream.write((byte)0xFF);
		stream.write((byte)getMarkerId());
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public boolean equals(Object other) {
		if ((other != null) &&
			this.getClass().isInstance(other) &&
			((Marker)other).getMarkerId() == this.getMarkerId()) {
			return true;
		}

		return false;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + this.markerId;
		return hash;
	}
}
