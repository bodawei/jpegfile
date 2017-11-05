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

import com.davidjohnburrowes.format.jpeg.component.Thumbnail3BytesPerPixel;
import com.davidjohnburrowes.format.jpeg.data.MarkerSegment;
import com.davidjohnburrowes.format.jpeg.support.DataBounds;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import com.davidjohnburrowes.format.jpeg.support.MarkerId;
import com.davidjohnburrowes.io.LimitingDataInput;
import com.davidjohnburrowes.util.Size;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Defines an JFIF APP0 "JFIF" segment.
 *
 * @see <a href="http://www.w3.org/Graphics/JPEG/jfif3.pdf">http://www.w3.org/Graphics/JPEG/jfif3.pdf</a>
 */
@MarkerId(JfifSegment.MARKERID)
public class JfifSegment extends MarkerSegment {
	private static final DataBounds versionBounds =
			  new DataBounds("version", Size.SHORT, 0x0100, 0x0102);

	private static final DataBounds unitsBounds =
			  new DataBounds("units", Size.BYTE, 0, 2);

	private static final DataBounds xDensityBounds =
			  new DataBounds("xDensity", Size.SHORT, 1, Size.SHORT.getMax());

	private static final DataBounds yDensityBounds =
			  new DataBounds("yDensity", Size.SHORT, 1, Size.SHORT.getMax());
	private static final String IDENTIFIER = "JFIF\0";

	/**
	 * Standard marker for this type
	 */
	public static final int MARKERID = 0xE0;

	/**
	 * Probably the first version of the Jfif standard. Honestly, the Jfif
	 * standard doesn't mention the version history or details of older versions.
	 * Until I started scanning actual files, I thought there was only version
	 * 1.02, but I found various fairly recent (2013) files that are 1.01.
	 */
	public static final int FIRST_VERSION = 0x0100;

	/**
	 * Standard version
	 */
	public static final int NEWEST_VERSION = 0x0102;

	/**
	 * Standard units value for "no units"
	 */
	public static final int NO_UNITS = 0;

	/**
	 * Standard units value for "dots per inch"
	 */
	public static final int DOTS_PER_INCH = 1;

	/**
	 * Standard units value for "dots per cm"
	 */
	public static final int DOTS_PER_CENTIMETER = 2;

	private int version;
	private int units;
	private int xDensity;
	private int yDensity;
	private Thumbnail3BytesPerPixel thumbnail;

	/**
	 * Constructor
	 */
	public JfifSegment() {
		super(MARKERID);
		version = NEWEST_VERSION;
		units = 0;
		xDensity = 1;
		yDensity = 1;
		thumbnail = new Thumbnail3BytesPerPixel();
	}

	/**
	 * @return Identifier for this segment (JFIF\0).  Can never be different
	 */
	public String getIdentifier() {
		return IDENTIFIER;
	}

	/**
	 * @param version The version to set on this segment.
	 */
	public void setVersion(int version) {
		versionBounds.throwIfInvalid(version, getFrameMode(), getDataMode());

		this.version = version;
	}

	public int getVersion() {
		return this.version;
	}

	/**
	 * @param units The units to measure the image size
	 */
	public void setUnits(int units) {
		unitsBounds.throwIfInvalid(units, getFrameMode(), getDataMode());

		this.units = units;
	}

	public int getUnits() {
		return this.units;
	}

	/**
	 * @param xDensity The horizontal measure of the image. Meaning determined by units
	 */
	public void setXDensity(int xDensity) {
		xDensityBounds.throwIfInvalid(xDensity, getFrameMode(), getDataMode());

		this.xDensity = xDensity;
	}

	public int getXDensity() {
		return this.xDensity;
	}

	/**
	 * @param yDensity The vertical measure of the image. Meaning determined by units
	 */
	public void setYDensity(int yDensity) {
		yDensityBounds.throwIfInvalid(yDensity, getFrameMode(), getDataMode());
		this.yDensity = yDensity;
	}

	public int getYDensity() {
		return this.yDensity;
	}

	/**
	 * @param thumbnail The thumbnail for this image.
	 */
	public void setThumbnail(Thumbnail3BytesPerPixel thumbnail) {
		if (thumbnail == null) {
			throw new IllegalArgumentException("thumbnail may not be null");
		}

		thumbnail.setFrameMode(getFrameMode());
		thumbnail.setDataMode(getDataMode());
		thumbnail.setHierarchicalMode(getHierarchicalMode());

		this.thumbnail = thumbnail;
	}

	public Thumbnail3BytesPerPixel getThumbnail() {
		return thumbnail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + 12 + thumbnail.getSizeOnDisk();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();

		versionBounds.accumulateOnViolation(getVersion(), this.getFrameMode(), results);
		unitsBounds.accumulateOnViolation(getUnits(), this.getFrameMode(), results);
		xDensityBounds.accumulateOnViolation(getXDensity(), this.getFrameMode(), results);
		yDensityBounds.accumulateOnViolation(getYDensity(), this.getFrameMode(), results);
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

		JfifSegment jOther = (JfifSegment) other;

		if ((getVersion() == jOther.getVersion()) &&
			(getUnits() == jOther.getUnits()) &&
			(getXDensity() == jOther.getXDensity()) &&
			(getYDensity() == jOther.getYDensity()) &&
			(getThumbnail().equals(jOther.getThumbnail()))) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + this.version;
		hash = 97 * hash + this.units;
		hash = 97 * hash + this.xDensity;
		hash = 97 * hash + this.yDensity;
		hash = 97 * hash + thumbnail.hashCode();
		return hash;
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
		setVersion(getVersion());
		setUnits(getUnits());
		setXDensity(getXDensity());
		setYDensity(getYDensity());
		setThumbnail(getThumbnail());
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
			throw new InvalidJpegFormat("JFIF segment did not have an identifier of 'JFIF\\0'. Instead it had " + identBuilder.toString());
		}

		setVersion(input.readUnsignedShort());
		setUnits(input.readByte());
		setXDensity(input.readUnsignedShort());
		setYDensity(input.readUnsignedShort());

		thumbnail = new Thumbnail3BytesPerPixel();

		thumbnail.readParameters(input);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeParameters(DataOutputStream stream) throws IOException {
		super.writeParameters(stream);

		stream.writeByte('J');
		stream.writeByte('F');
		stream.writeByte('I');
		stream.writeByte('F');
		stream.writeByte('\0');
		stream.writeShort(getVersion());
		stream.writeByte(getUnits());
		stream.writeShort(getXDensity());
		stream.writeShort(getYDensity());

		thumbnail.write(stream);
	}
}
