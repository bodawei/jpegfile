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
import bdw.format.jpeg.segment.JfifSegment;
import bdw.io.LimitingDataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Convenience class for capturing a thumbnail made up of a width, height and
 * raw 3 byte pixels
 */
public class ThreeBytesPerPixelThumbnail extends Thumbnail {
	
	/**
	 * The thumbnail width and height don't match the thumbnail bytes
	 */
	public static int ERROR_BYTES_SIZE_DONT_MATCH = 3;
	
	private byte[] pixelBytes;


	public ThreeBytesPerPixelThumbnail() {
		pixelBytes = new byte[0];
	}
	
	public void setPixelBytes(byte[] bytes) {
		if (bytes == null) {
			throw new IllegalArgumentException("thumbnailPixelBytes may not be nll");
		}

		this.pixelBytes = bytes;
		checkProblems();
	}

	public byte[] getPixelBytes() {
		return this.pixelBytes;
	}

	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof ThreeBytesPerPixelThumbnail)) {
			return false;
		}
		
		ThreeBytesPerPixelThumbnail jOther = (ThreeBytesPerPixelThumbnail) other;
		
		if ((getWidth() == jOther.getWidth()) &&
			(getHeight() == jOther.getHeight()) &&
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
		return hash;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		DataOutputStream dataStream = wrapAsDataOutputStream(stream);

		dataStream.writeByte(getWidth());
		dataStream.writeByte(getHeight());

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
	@Override
	public void readData(LimitingDataInput limited, ParseMode mode) throws IOException, InvalidJpegFormat {

		setWidth(limited.readByte());
		setHeight(limited.readByte());
		
		
		if (limited.getRemainingLimit() != sizeToBytes()) {
			throw new InvalidJpegFormat("JFIF segment expected " + sizeToBytes() + " bytes of data for thumbnail, but found " + limited.getRemainingLimit());
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
