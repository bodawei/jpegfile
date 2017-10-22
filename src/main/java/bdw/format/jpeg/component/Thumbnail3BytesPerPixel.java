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

import bdw.format.jpeg.support.DataBounds;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.util.Size;
import bdw.util.Util;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Convenience class for capturing a thumbnail made up of a width, height and
 * raw 3 byte pixels
 */
public class Thumbnail3BytesPerPixel extends Thumbnail {
	private static final DataBounds widthBounds =
			  new DataBounds("width", Size.BYTE, 0, 255);
	private static final DataBounds heightBounds =
			  new DataBounds("height", Size.BYTE, 0, 255);

	private int width;
	private int height;
	private byte[] pixelBytes;

	/**
	 * Constructor. Creates a 0 by 0 thumbnail
	 */
	public Thumbnail3BytesPerPixel() {
		width = 0;
		height = 0;
		pixelBytes = new byte[0];
	}

	/**
	 * Sets the width of the image. Note that setting this does not alter the
	 * pixel bytes.  You must set those independently.
	 * @param width the width, up to 255 pixels wide
	 */
	public void setWidth(int width) {
		widthBounds.throwIfInvalid(width, getFrameMode(), getDataMode());

		this.width = width;
	}

	public int getWidth() {
		return this.width;
	}

	/**
	 * Sets the height of the image. Note that setting this does not alter the
	 * pixel bytes.  You must set those independently.
	 * @param height the high, up to 255 pixels high
	 */
	public void setHeight(int height) {
		heightBounds.throwIfInvalid(height, getFrameMode(), getDataMode());

		this.height = height;
	}

	public int getHeight() {
		return this.height;
	}

	/**
	 * Sets the image bytes.  Note that this does not change the width and height
	 * properties.  you must set those yourself.
	 * @param bytes the bytes that make up the pixels. Three bytes make one pixel
	 */
	public void setPixelBytes(byte[] bytes) {
		if (bytes == null) {
			throw new IllegalArgumentException("thumbnailPixelBytes may not be nll");
		}

		if (bytes.length > (255 * 255 * 3)) {
			throw new IllegalArgumentException("thumbnailPixelBytes must be no more than " + (255 * 255 * 3) + " bytes long.");
		}

		if ((bytes.length / 3.0) != (bytes.length / 3)) {
			throw new IllegalArgumentException("thumbnailPixelBytes must be a multiple of 3 bytes.");
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
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + 2 + pixelBytes.length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> invalids = super.validate();
		if ((getWidth() + getHeight() * 3) != getPixelBytes().length) {
			invalids.add(new InvalidJpegFormat("Height and width don't match the pixel byte count."));
		}

		return invalids;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readParameters(DataInput dataSource) throws IOException {
		super.readParameters(dataSource);

		setWidth(dataSource.readUnsignedByte());
		setHeight(dataSource.readUnsignedByte());

		byte[] rawBytes = new byte[getWidth() * getHeight() * 3];

		dataSource.readFully(rawBytes);

		setPixelBytes(rawBytes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeParameters(DataOutputStream output) throws IOException {
		super.writeParameters(output);

		output.writeByte(getWidth());
		output.writeByte(getHeight());

		output.write(getPixelBytes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof Thumbnail3BytesPerPixel)) {
			return false;
		}

		Thumbnail3BytesPerPixel jOther = (Thumbnail3BytesPerPixel) other;

		if ((getWidth() == jOther.getWidth()) &&
			(getHeight() == jOther.getHeight()) &&
			Arrays.equals(getPixelBytes(), jOther.getPixelBytes())) {
			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 13 * hash + this.getWidth();
		hash = 13 * hash + this.getHeight();
		hash = 13 * hash + Arrays.hashCode(this.pixelBytes);
		return hash;
	}
}
