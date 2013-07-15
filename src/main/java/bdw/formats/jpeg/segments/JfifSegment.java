/*
 *  Copyright 2011 柏大衛
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
package bdw.formats.jpeg.segments;

import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.ParseMode;
import bdw.formats.jpeg.segments.support.ParamCheck;
import bdw.format.jpeg.support.Problem;
import bdw.formats.jpeg.segments.support.ThreeBytesPerPixelThumbnail;
import bdw.io.LimitingDataInput;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Defines an JFIF APP0 "JFIF" segment.
 * 
 * @see http://www.w3.org/Graphics/JPEG/jfif3.pdf
 */
public class JfifSegment extends AppNSegment {

	/**
	 * Standard marker for this type
	 */
	public static final int SUBTYPE = 0xE0;

	/**
	 * The version is out of range for what this class is defined to support.
	 */
	public static int WARNING_UNKNOWN_VERSION = 1;

	/**
	 * The units are out of range. Spec only allows for 0-2
	 */
	public static int WARNING_UNKNOWN_UNITS = 2;
	
	/**
	 * The thumbnail width and height don't match the thumbnail bytes
	 */
	public static int ERROR_BYTES_SIZE_DONT_MATCH = 3;
	

	public static enum UnitsEnum {
		NO_UNITS(0),
		DOTS_PER_INCH(1),
		DOTS_PER_CENTIMETER(2);
		
		private int value;
		
		UnitsEnum(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}
	
	private static final String IDENTIFIER = "JFIF\0";

	private int majorVersion;
	private int minorVersion;
	private int units;
	private int xDensity;
	private int yDensity;
	private int width;
	private int height;
	private byte[] pixelBytes;
	private ThreeBytesPerPixelThumbnail thumbnail;
	
	/**
	 * Constructor
	 */
	public JfifSegment() throws InvalidJpegFormat {
		super(SUBTYPE);
		setMarker(JfifSegment.SUBTYPE);
		majorVersion = 1;
		minorVersion = 2;
		units = 0;
		xDensity = 0;
		yDensity = 0;
		width = 0;
		height = 0;
		pixelBytes = new byte[0];
		thumbnail = new ThreeBytesPerPixelThumbnail();
	}

	/**
	 * Construct an instance from a stream.
	 *
	 * @param stream The stream to read from
	 * @param mode The mode to parse this in.
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public JfifSegment(int subType, InputStream stream, ParseMode mode) throws IOException, InvalidJpegFormat {
		this();
		if (subType != SUBTYPE) {
			throw new InvalidJpegFormat("JfifSegment can not parse a marker of type: " + subType);
		}
		super.readFromStream(stream, mode);
    }

	/**
	 * Construct an instance from a stream.
	 *
	 * @param file The file to read from
	 * @param mode The mode to parse this in. 
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public JfifSegment(int subType, RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
		this();
		if (subType != SUBTYPE) {
			throw new InvalidJpegFormat("JfifSegment can not parse a marker of type: " + subType);
		}
		super.readFromFile(file, mode);
    }

	/**
	 * Checks whether instances of this class should be constructed
	 * with the specified marker.
	 *
	 * @param marker The marker to check.
	 * @return true if this conventionally can be associated with that marker.
	 */
	public static boolean canHandleMarker(int marker) {
		return (marker == JfifSegment.SUBTYPE);
	}
	
	public String getIdentifier() {
		return IDENTIFIER;
	}

	public void setMajorVersion(int majorVersion) {
		ParamCheck.checkByte(majorVersion);

		this.majorVersion = majorVersion;
		checkProblems();
	}

	public int getMajorVersion() {
		return this.majorVersion;
	}

	public void setMinorVersion(int minorVersion) {
		ParamCheck.checkByte(minorVersion);

		this.minorVersion = minorVersion;
		checkProblems();
	}

	public int getMinorVersion() {
		return this.minorVersion;
	}

	public void setUnits(int units) {
		ParamCheck.checkByte(units);
		
		this.units = units;
		checkProblems();
	}

	public int getUnits() {
		return this.units;
	}

	public void setImageXDensity(int xDensity) {
		ParamCheck.checkShort(xDensity);

		this.xDensity = xDensity;
		checkProblems();
	}

	public int getImageXDensity() {
		return this.xDensity;
	}

	public void setImageYDensity(int yDensity) {
		ParamCheck.checkShort(yDensity);
		this.yDensity = yDensity;
		checkProblems();
	}

	public int getImageYDensity() {
		return this.yDensity;
	}
	
	public void setThumbnail(ThreeBytesPerPixelThumbnail thumbnail) {
		if (thumbnail == null) {
			throw new IllegalArgumentException("thumbnail may not be null");
		}

		this.thumbnail = thumbnail;   
	}
	
	public ThreeBytesPerPixelThumbnail getThumbnail() {
		return thumbnail;
	}

	@Override
	public List<Problem> getProblems() {
		checkProblems();
		return problems;
	}
	
	/**
	 * @inheritdoc
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		DataOutputStream dataStream = super.wrapAsDataOutputStream(stream);

		dataStream.writeShort(16 + sizeToBytes());
		
		dataStream.writeByte('J');
		dataStream.writeByte('F');
		dataStream.writeByte('I');
		dataStream.writeByte('F');
		dataStream.writeByte('\0');
		dataStream.writeByte(getMajorVersion());
		dataStream.writeByte(getMinorVersion());
		dataStream.writeByte(getUnits());
		dataStream.writeShort(getImageXDensity());
		dataStream.writeShort(getImageYDensity());
		
		thumbnail.write(stream);
	}

	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof JfifSegment)) {
			return false;
		}
		
		JfifSegment jOther = (JfifSegment) other;
		
		if ((getMajorVersion() == jOther.getMajorVersion()) &&
			(getMinorVersion() == jOther.getMinorVersion()) &&
			(getUnits() == jOther.getUnits()) &&
			(getImageXDensity() == jOther.getImageXDensity()) &&
			(getImageYDensity() == jOther.getImageYDensity()) &&
			(getThumbnail().equals(jOther.getThumbnail()))) {
			return true;
		}
				
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + this.majorVersion;
		hash = 97 * hash + this.minorVersion;
		hash = 97 * hash + this.units;
		hash = 97 * hash + this.xDensity;
		hash = 97 * hash + this.yDensity;
		hash = 97 * hash + thumbnail.hashCode();
		return hash;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void readData(DataInput input, ParseMode mode) throws IOException, InvalidJpegFormat {
		int dataLength = input.readUnsignedShort();
		LimitingDataInput limited = new LimitingDataInput(input, dataLength -2);
		int index = 0;

		if (dataLength < 14) {
			throw new InvalidJpegFormat("JFIF segment doesn't have the minimum length");
		}
		
		StringBuilder identBuilder = new StringBuilder();
		for (int identCount = 0; identCount < 5; identCount++) {
			identBuilder.append((char)limited.readByte());
		}

		if ( ! identBuilder.toString().equals(IDENTIFIER)) {
			throw new InvalidJpegFormat("JFIF segment did not have an identifier of 'JFIF\0'. Instead it had " + identBuilder.toString());
		}
		
		setMajorVersion(limited.readByte());
		setMinorVersion(limited.readByte());
		setUnits(limited.readByte());
		setImageXDensity(limited.readUnsignedShort());
		setImageYDensity(limited.readUnsignedShort());
		
		thumbnail = new ThreeBytesPerPixelThumbnail();
		
		thumbnail.readData(limited, mode);
	}
	
	private int sizeToBytes() {
		return (thumbnail.getWidth() * thumbnail.getHeight()) * 3;
	}

	private void checkProblems() {
		problems.clear();
		
		if (!((majorVersion == 1) && (minorVersion <= 2))) {
			problems.add(new Problem(Problem.ProblemType.WARNING, WARNING_UNKNOWN_VERSION));
		}

		if (units >= 3) {
			problems.add(new Problem(Problem.ProblemType.WARNING, WARNING_UNKNOWN_UNITS));
		}

		List<Problem> subProblems = thumbnail.getProblems();
		
		for (Problem prob : subProblems) {
			problems.add(prob);
		}
	}
}
