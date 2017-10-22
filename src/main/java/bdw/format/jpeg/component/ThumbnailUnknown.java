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
package bdw.format.jpeg.component;

import bdw.io.LimitingDataInput;
import bdw.util.ByteArrayBuilder;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Used by JfxxSegment to represent a thumbnail of a type we don't know about.
 */
public class ThumbnailUnknown extends Thumbnail  {

	private byte[] pixelBytes;

	/**
	 * Creates a thumbnail with 0 pixel bytes
	 */
	public ThumbnailUnknown() {
		pixelBytes = new byte[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + pixelBytes.length;
	}

	/**
	 * @param bytes The raw bytes that make up the thumbnail
	 */
	public void setPixelBytes(byte[] bytes) {
		if (bytes == null) {
			throw new IllegalArgumentException("thumbnailPixelBytes may not be null");
		}

		this.pixelBytes = bytes;
	}

	public byte[] getPixelBytes() {
		return this.pixelBytes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof ThumbnailUnknown)) {
			return false;
		}

		return Arrays.equals(getPixelBytes(),
			((ThumbnailUnknown) other).getPixelBytes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 13 * hash + Arrays.hashCode(this.pixelBytes);
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readParameters(DataInput input) throws IOException {
		super.readParameters(input);
		if (input instanceof LimitingDataInput) {
			LimitingDataInput limited = (LimitingDataInput) input;
			byte[] rawBytes = new byte[limited.getRemainingLimit()];
			limited.readFully(rawBytes);
			setPixelBytes(rawBytes);
		} else {
			ByteArrayBuilder builder = new ByteArrayBuilder();
			int aByte;
			try {
				aByte = input.readByte();
				while (aByte != -1) {
					builder.append(aByte);
					aByte = input.readByte();
				}
			} catch (Exception e) {
				// do nothing. we're done.
			}
			setPixelBytes(builder.toArray());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeParameters(DataOutputStream output) throws IOException {
		super.writeParameters(output);
		output.write(pixelBytes);
	}
}
