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
package com.davidjohnburrowes.format.jpeg.marker;

import com.davidjohnburrowes.format.jpeg.component.Thumbnail;
import com.davidjohnburrowes.format.jpeg.component.Thumbnail1BytePerPixel;
import com.davidjohnburrowes.format.jpeg.component.Thumbnail3BytesPerPixel;
import com.davidjohnburrowes.format.jpeg.component.ThumbnailJpeg;
import com.davidjohnburrowes.format.jpeg.component.ThumbnailUnknown;
import com.davidjohnburrowes.format.jpeg.data.MarkerSegment;
import com.davidjohnburrowes.format.jpeg.support.DataBounds;
import com.davidjohnburrowes.format.jpeg.support.DataMode;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.format.jpeg.support.MarkerId;
import com.davidjohnburrowes.io.LimitingDataInput;
import com.davidjohnburrowes.util.Size;
import com.davidjohnburrowes.util.Util;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Defines an JFIF APP0 "JFXX" segment.
 *
 * @see <a href="http://www.w3.org/Graphics/JPEG/jfif3.pdf">http://www.w3.org/Graphics/JPEG/jfif3.pdf</a>
 */
@MarkerId(JfxxSegment.MARKERID)
public class JfxxSegment extends MarkerSegment {
	private static final DataBounds extensionCodeBounds =
			  new DataBounds("extensionCode", Size.BYTE, 0x10, 0x13);
	private static final String IDENTIFIER = "JFXX\0";

	/**
	 * Standard marker for this type
	 */
	public static final int MARKERID = 0xE0;

	/**
	 * Extension code for a JPEG thumbnail
	 */
	public static final int JPEG = 0x10;

	/**
	 * Extension code for a one byte per pixel thumbnail
	 */
	public static final int ONE_BYTE_PER_PIXEL = 0x11;

	/**
	 * Extension code for a three byte per pixel thumbnail
	 */
	public static final int THREE_BYTES_PER_PIXEL = 0x13;

	private int extensionCode;
	private Thumbnail thumbnail;

	/**
	 * Constructor
	 */
	public JfxxSegment() {
		super(MARKERID);
		this.extensionCode = THREE_BYTES_PER_PIXEL;
		this.thumbnail = new Thumbnail3BytesPerPixel();
	}

	/**
	 * @return The identifier for this segment. Always "JFXX\0"
	 */
	public String getIdentifier() {
		return IDENTIFIER;
	}

	/**
	 * @param value Identifier of what kind of thumbnail this has.  Note that
	 * setting this will not change the thumbnail, so this may leave this segment
	 * in an invalid state
	 */
	public void setExtensionCode(int value) {
		extensionCodeBounds.throwIfInvalid(value, getFrameMode(), getDataMode());
		if (value == 0x12 && getDataMode() == DataMode.STRICT) {
			throw new IllegalArgumentException("Can not specify an extension code of 12");
		}

		this.extensionCode = value;
	}

	public int getExtensionCode() {
		return extensionCode;
	}

	/**
	 * @param thumbnail The thumbnail for this to carry. This will always set
	 * the extensionCode property to match the thumbnail type
	 */
	public void setThumbnail(Thumbnail thumbnail) {
		if (thumbnail == null) {
			throw new IllegalArgumentException("thumbnail may not be null");
		}

		thumbnail.setFrameMode(getFrameMode());
		thumbnail.setDataMode(getDataMode());
		thumbnail.setHierarchicalMode(getHierarchicalMode());

		if (thumbnail instanceof ThumbnailJpeg) {
			setExtensionCode(JPEG);
		} else if (thumbnail instanceof Thumbnail3BytesPerPixel) {
			setExtensionCode(THREE_BYTES_PER_PIXEL);
		} else if (thumbnail instanceof Thumbnail1BytePerPixel) {
			setExtensionCode(ONE_BYTE_PER_PIXEL);
		} else if (thumbnail instanceof ThumbnailUnknown) {
			setExtensionCode(0);
		}

		this.thumbnail = thumbnail;
	}

	public Thumbnail getThumbnail() {
		return thumbnail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + 6 + thumbnail.getSizeOnDisk();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();
		extensionCodeBounds.accumulateOnViolation(getExtensionCode(), getFrameMode(), results);
		if (getExtensionCode() == 0x12) {
			results.add(new InvalidJpegFormat("Can not specify an extension code of 12"));
		}

		switch (getExtensionCode()) {
			case JPEG:
				if (! (thumbnail instanceof ThumbnailJpeg)) {
					results.add(new InvalidJpegFormat("Thumbnail should be ThumbnailJpeg, but is " + thumbnail.getClass().getSimpleName()));
				}
				break;
			case THREE_BYTES_PER_PIXEL:
				if (! (thumbnail instanceof Thumbnail3BytesPerPixel)) {
					results.add(new InvalidJpegFormat("Thumbnail should be Thumbnail3BytesPerPixel, but is " + thumbnail.getClass().getSimpleName()));
				}
				break;
			case ONE_BYTE_PER_PIXEL:
				if (! (thumbnail instanceof Thumbnail1BytePerPixel)) {
					results.add(new InvalidJpegFormat("Thumbnail should be Thumbnail1BytePerPixel, but is " + thumbnail.getClass().getSimpleName()));
				}
				break;
			default:
				if (!(thumbnail instanceof ThumbnailUnknown)) {
					results.add(new InvalidJpegFormat("Thumbnail should be ThumbnailUnknown, but is " + thumbnail.getClass().getSimpleName()));
				}
				break;
		}

		List<Exception> subResults = thumbnail.validate();
		if (!subResults.isEmpty()) {
			results.addAll(subResults);
		}

		return results;
	}

	@Override
	public boolean equals(Object other) {
		if (!super.equals(other)) {
			return false;
		}

		JfxxSegment jOther = (JfxxSegment) other;

		if ((getExtensionCode() == jOther.getExtensionCode()) &&
			 getThumbnail().equals(jOther.getThumbnail())) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + this.extensionCode;
		hash = 97 * hash + this.thumbnail.hashCode();
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readParameters(LimitingDataInput input) throws IOException {
		super.readParameters(input);

		StringBuilder identBuilder = new StringBuilder();
		for (int identCount = 0; identCount < 5; identCount++) {
			identBuilder.append((char)input.readByte());
		}

		if ( ! identBuilder.toString().equals(IDENTIFIER)) {
			throw new InvalidJpegFormat("JFXX segment did not have an identifier of 'JFXX\\0'. Instead it had " + identBuilder.toString());
		}

		setExtensionCode(input.readUnsignedByte());

		switch (getExtensionCode()) {
			case JPEG:
				thumbnail = new ThumbnailJpeg();
				thumbnail.setDataMode(getDataMode());
				thumbnail.read(input);
				break;
			case THREE_BYTES_PER_PIXEL:
				thumbnail = new Thumbnail3BytesPerPixel();
				thumbnail.setDataMode(getDataMode());
				thumbnail.readParameters(input);
				break;
			case ONE_BYTE_PER_PIXEL:
				thumbnail = new Thumbnail1BytePerPixel();
				thumbnail.setDataMode(getDataMode());
				thumbnail.readParameters(input);
				break;
			default:
				thumbnail = new ThumbnailUnknown();
				thumbnail.setDataMode(getDataMode());
				thumbnail.readParameters(input);
				break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeParameters(DataOutputStream stream) throws IOException {
		stream.writeByte('J');
		stream.writeByte('F');
		stream.writeByte('X');
		stream.writeByte('X');
		stream.writeByte('\0');
		stream.writeByte(getExtensionCode());
		getThumbnail().write(stream);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void	changeChildrenModes() {
		super.changeChildrenModes();

		getThumbnail().setDataMode(getDataMode());
		getThumbnail().setFrameMode(getFrameMode());
		getThumbnail().setHierarchicalMode(getHierarchicalMode());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void checkModeChange() {
		super.checkModeChange();
		setExtensionCode(getExtensionCode());
	}
}
