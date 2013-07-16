/*
 *  Copyright 2013 柏大衛
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
package bdw.format.jpeg.segment.support;

import bdw.format.jpeg.support.Problem;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.ParseMode;
import bdw.io.LimitingDataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class OneBytPerPixelThumbnail extends Thumbnail  {
	
	public class Color {
		public int red;
		public int green;
		public int blue;
	};
	
	/**
	 * The thumbnail width and height don't match the thumbnail bytes
	 */
	public static int ERROR_BYTES_SIZE_DONT_MATCH = 3;

	public static int COLOR_TABLE_SIZE = 256;
	public static int COLOR_TABLE_BYTE_SIZE = 768;
	
	private Color[] colorTable;
	private byte[] pixelBytes;

	public OneBytPerPixelThumbnail() {
		pixelBytes = new byte[0];
		colorTable = new Color[COLOR_TABLE_SIZE];
	}
	
	public void setColorTable(Color[] colors) {
		if (colors == null) {
			throw new IllegalArgumentException("colorTable may not be null");
		}
		
		if (colors.length != COLOR_TABLE_SIZE) {
			throw new IllegalArgumentException("colorTable must be " + COLOR_TABLE_SIZE + " entries long.");
		}

		this.colorTable = colors;
		checkProblems();
	}

	public Color[] getColorTable() {
		return this.colorTable;
	}

	public void setPixelBytes(byte[] bytes) {
		if (bytes == null) {
			throw new IllegalArgumentException("thumbnailPixelBytes may not be null");
		}

		this.pixelBytes = bytes;
		checkProblems();
	}

	public byte[] getPixelBytes() {
		return this.pixelBytes;
	}

	public List<Problem> getProblems() {
		return problems;
	}

	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof OneBytPerPixelThumbnail)) {
			return false;
		}
		
		OneBytPerPixelThumbnail jOther = (OneBytPerPixelThumbnail) other;
		
		if ((getWidth() == jOther.getWidth()) &&
			(getHeight() == jOther.getHeight()) &&
			Arrays.equals(getColorTable(), jOther.getColorTable()) &&
			Arrays.equals(getPixelBytes(), jOther.getPixelBytes())) {
			return true;
		}
				
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 13 * hash + this.getWidth();
		hash = 13 * hash + this.getHeight();
		hash = 13 * hash + Arrays.hashCode(this.pixelBytes);
		hash = 13 * hash + Arrays.hashCode(this.colorTable);
		return hash;
	}

	/**
	 * @inheritdoc
	 */
	public void write(OutputStream stream) throws IOException {
		DataOutputStream dataStream = wrapAsDataOutputStream(stream);

		dataStream.writeByte(getWidth());
		dataStream.writeByte(getHeight());

		Color[] colors = getColorTable();
		
		for (int index = 0; index < colors.length; index++) {
			Color c = colors[index];
			dataStream.writeByte(c.red);
			dataStream.writeByte(c.green);
			dataStream.writeByte(c.blue);
		}
		
		byte[] pixels = getPixelBytes();
		
		for (int index = 0; index < sizeToBytes(); index++) {
			if (index < pixels.length) {
				dataStream.writeByte(pixels[index]);
			} else {
				dataStream.writeByte(0);
			}
		}
	}

	/**
	 * @inheritdoc
	 */
	public void readData(LimitingDataInput limited, ParseMode mode) throws IOException, InvalidJpegFormat {

		setWidth(limited.readByte());
		setHeight(limited.readByte());
		
		
		if (limited.getRemainingLimit() != (COLOR_TABLE_BYTE_SIZE + sizeToBytes())) {
			throw new InvalidJpegFormat("JFIF segment expected " + sizeToBytes() + " bytes of data for thumbnail, but found " + limited.getRemainingLimit());
		}
		
		for (int cIndex = 0; cIndex < COLOR_TABLE_SIZE; cIndex++) {
			Color c = new Color();
			c.red = limited.readByte();
			c.green = limited.readByte();
			c.blue = limited.readByte();
			colorTable[cIndex] = c;
		}
		
		byte[] rawBytes = new byte[sizeToBytes()];
		
		for (int ctr = 0; ctr < sizeToBytes(); ctr++) {
			rawBytes[ctr] = limited.readByte();
		}
		
		setPixelBytes(rawBytes);
	}
	
	private int sizeToBytes() {
		return (getWidth() * getHeight()) * 3;
	}

	protected void checkProblems() {
		super.checkProblems();
		
		if (sizeToBytes() != pixelBytes.length) {
			problems.add(new Problem(Problem.ProblemType.ERROR, ERROR_BYTES_SIZE_DONT_MATCH));
		}
	}

	/**
	 * Convenience routine so subclasses can get an output stream
	 * as a DataOutputStream easily
	 *
	 * @param stream The stream to wrap as a DataOutputStream
	 *
	 * @return A DataOutputStream (either wrapping the input, or the input if it already was one)
	 */
	protected DataOutputStream wrapAsDataOutputStream(OutputStream stream) {
		if (stream instanceof DataOutputStream) {
			return (DataOutputStream) stream;
		} else {
			return new DataOutputStream(stream);
		}
	}
}
