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
package com.davidjohnburrowes.format.jpeg.component;

import com.davidjohnburrowes.format.jpeg.support.DataBounds;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.util.Size;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a thumbnail which has a color table, and then each pixel in the
 * thumbnail is represented by a byte which is an index into the color table.
 */
public class Thumbnail1BytePerPixel extends Thumbnail  {
	private static int COLOR_TABLE_SIZE = 256;
	private static int COLOR_TABLE_BYTE_SIZE = 768;
	private static final DataBounds widthBounds =
			  new DataBounds("width", Size.BYTE, 0, 255);
	private static final DataBounds heightBounds =
			  new DataBounds("height", Size.BYTE, 0, 255);

	public static class Color {
		public int red;
		public int green;
		public int blue;

		@Override
		public boolean equals(Object other) {
			return ((other instanceof Color) &&
					  ((Color)other).red == red &&
					  ((Color)other).green == green &&
					  ((Color)other).blue == blue);
		}
	};

	private int width;
	private int height;
	private Color[] colorTable;
	private byte[] pixelBytes;

	/**
	 * Creates a new 0 by 0 thumbnail, with a color table full of black.
	 */
	public Thumbnail1BytePerPixel() {
		width = 0;
		height = 0;
		pixelBytes = new byte[0];
		colorTable = new Color[COLOR_TABLE_SIZE];
		for (int index = 0; index < COLOR_TABLE_SIZE; index++) {
			colorTable[index] = new Color();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + 2 + 768 + pixelBytes.length;
	}

	/**
	 * Sets the width of the image. Note that setting this does not alter the
	 * pixel bytes.  You must set those independently.
	 * @param width new width of the image. up to 255 pixels wide
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
	 * @param height new height of the image. up to 255 pixels high
	 */
	public void setHeight(int height) {
		heightBounds.throwIfInvalid(height, getFrameMode(), getDataMode());

		this.height = height;
	}

	public int getHeight() {
		return this.height;
	}

	/**
	 * @param colors The new color table to be used. It must have 256 entries
	 */
	public void setColorTable(Color[] colors) {
		if (colors == null) {
			throw new IllegalArgumentException("colorTable may not be null");
		}

		if (colors.length != COLOR_TABLE_SIZE) {
			throw new IllegalArgumentException("colorTable must be " + COLOR_TABLE_SIZE + " entries long.");
		}

		this.colorTable = colors;
	}

	public Color[] getColorTable() {
		return this.colorTable;
	}

	/**
	 * Sets the bytes for the thumbnail. Note that after setting this, it is up
	 * to the caller to set the width and the height properties to match the data.
	 *
	 * @param bytes The bytes of image data
	 */
	public void setPixelBytes(byte[] bytes) {
		if (bytes == null) {
			throw new IllegalArgumentException("thumbnailPixelBytes may not be null");
		}

		if (bytes.length > 256 * 256) {
			throw new IllegalArgumentException("thumbnailPixelBytes may not be more than " + 256 * 256 + " bytes.");
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
	public void readParameters(DataInput input) throws IOException {
		super.readParameters(input);
		setWidth(input.readByte());
		setHeight(input.readByte());

		for (int cIndex = 0; cIndex < COLOR_TABLE_SIZE; cIndex++) {
			Color c = new Color();
			c.red = input.readUnsignedByte();
			c.green = input.readUnsignedByte();
			c.blue = input.readUnsignedByte();
			colorTable[cIndex] = c;
		}

		byte[] rawBytes = new byte[getWidth() * getHeight()];
		input.readFully(rawBytes);

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

		Color[] colors = getColorTable();

		for (int index = 0; index < colors.length; index++) {
			Color c = colors[index];
			output.writeByte(c.red);
			output.writeByte(c.green);
			output.writeByte(c.blue);
		}

		byte[] pixels = getPixelBytes();
		output.write(pixels);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> invalids = super.validate();
		if ((getWidth() + getHeight()) != getPixelBytes().length) {
			invalids.add(new InvalidJpegFormat("Height and width don't match the pixel byte count."));
		}

		return invalids;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof Thumbnail1BytePerPixel)) {
			return false;
		}

		Thumbnail1BytePerPixel jOther = (Thumbnail1BytePerPixel) other;

		return ((getWidth() == jOther.getWidth()) &&
			(getHeight() == jOther.getHeight()) &&
			Arrays.equals(getColorTable(), jOther.getColorTable()) &&
			Arrays.equals(getPixelBytes(), jOther.getPixelBytes()));
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
		hash = 13 * hash + Arrays.hashCode(this.colorTable);
		return hash;
	}
}
