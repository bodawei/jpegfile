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

package bdw.formats.jpeg.segments;

import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.ParseMode;
import bdw.formats.jpeg.segments.support.OneBytPerPixelThumbnail;
import bdw.formats.jpeg.segments.support.ParamCheck;
import bdw.format.jpeg.support.Problem;
import bdw.formats.jpeg.segments.support.ThreeBytesPerPixelThumbnail;
import bdw.formats.jpeg.segments.support.Thumbnail;
import bdw.io.LimitingDataInput;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Defines an JFIF APP0 "JFXX" segment.
 * 
 * @see http://www.w3.org/Graphics/JPEG/jfif3.pdf
 */
public class JfxxSegment extends AppNSegment {

	/**
	 * Standard marker for this type
	 */
	public static final int SUBTYPE = 0xE0;
	public static final int ERROR_UNKNOWN_THUMBNAIL_TYPE = 1;

	public static enum ThumbnailType {
		JPEG(0x10),
		ONE_BYTE_PER_PIXEL(0x11),
		THREE_BYTES_PER_PIXEL(0x13);
		
		private int value;
		
		ThumbnailType(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public static ThumbnailType valueOf(int number) {
			if (number == JPEG.value) {
				return ThumbnailType.JPEG;
			}
			
			if (number == ONE_BYTE_PER_PIXEL.value) {
				return ThumbnailType.ONE_BYTE_PER_PIXEL;
			}
			
			if (number == THREE_BYTES_PER_PIXEL.value) {
				return ThumbnailType.THREE_BYTES_PER_PIXEL;
			}
			
			return null;
		}
	}
	
	private static final String IDENTIFIER = "JFXX\0";
	private int extensionCode;
	private Thumbnail thumbnail;

	/**
	 * Constructor
	 */
	public JfxxSegment() throws InvalidJpegFormat {
		super(SUBTYPE);
		setMarker(JfxxSegment.SUBTYPE);
		this.extensionCode = ThumbnailType.THREE_BYTES_PER_PIXEL.getValue();
		this.thumbnail = new ThreeBytesPerPixelThumbnail();
	}

	/**
	 * Construct an instance from a stream.
	 *
	 * @param stream The stream to read from
	 * @param mode The mode to parse this in.
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public JfxxSegment(int subType, InputStream stream, ParseMode mode) throws IOException, InvalidJpegFormat {
		this();
		if (subType != SUBTYPE) {
			throw new InvalidJpegFormat("JfxxSegment can not parse a marker of type: " + subType);
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
	public JfxxSegment(int subType, RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
		this();
		if (subType != SUBTYPE) {
			throw new InvalidJpegFormat("JfxxSegment can not parse a marker of type: " + subType);
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
		return (marker == JfxxSegment.SUBTYPE);
	}
	
	public String getIdentifier() {
		return IDENTIFIER;
	}

	public void setThumbnailType(int thumbnailType) {
		ParamCheck.checkByte(thumbnailType);

		this.extensionCode = thumbnailType;
		checkProblems();
	}

	public int getThumbnailType() {
		return extensionCode;
	}

	public void setThumbnail(Thumbnail thumbnail) {
		this.thumbnail = thumbnail;
	}

	public Thumbnail getThumbnail() {
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

		dataStream.writeShort(8 + 2 + (getThumbnail().getHeight() * getThumbnail().getWidth() * 3));
		
		dataStream.writeByte('J');
		dataStream.writeByte('F');
		dataStream.writeByte('X');
		dataStream.writeByte('X');
		dataStream.writeByte('\0');
		dataStream.writeByte(getThumbnailType());
		getThumbnail().write(stream);
	}

	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof JfxxSegment)) {
			return false;
		}
		
		JfxxSegment jOther = (JfxxSegment) other;
		
		if ((getThumbnailType() == jOther.getThumbnailType()) &&
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
	 * Right now this rejects any unknown thumbnail type.
	 * This could, conceivably, read in the subsequent data
	 * as just raw bytes if we found we needed to handle invalid types.
	 * @inheritdoc
	 */
	@Override
	public void readData(DataInput input, ParseMode mode) throws IOException, InvalidJpegFormat {
		int dataLength = input.readUnsignedShort();
		LimitingDataInput limited = new LimitingDataInput(input, dataLength - 2);
		int index = 0;

		if (dataLength < 8) {
			throw new InvalidJpegFormat("JFXX segment doesn't have the minimum length");
		}
		
		StringBuilder identBuilder = new StringBuilder();
		for (int identCount = 0; identCount < 5; identCount++) {
			identBuilder.append((char)limited.readByte());
		}

		if ( ! identBuilder.toString().equals(IDENTIFIER)) {
			throw new InvalidJpegFormat("JFXX segment did not have an identifier of 'JFXX\\0'. Instead it had " + identBuilder.toString());
		}

		int type = limited.readByte();
		if (ThumbnailType.valueOf(type) == null) {
			throw new InvalidJpegFormat("JFXX segment can not accept a type of " + type);
		}
		setThumbnailType(type);
		
		switch (ThumbnailType.valueOf(type)) {
			case THREE_BYTES_PER_PIXEL:
				thumbnail = new ThreeBytesPerPixelThumbnail();
				thumbnail.readData(limited, mode);
				break;
			case ONE_BYTE_PER_PIXEL:
				thumbnail = new OneBytPerPixelThumbnail();
				thumbnail.readData(limited, mode);
				break;
		}		
	}

	private void checkProblems() {
		problems.clear();
		
		if (ThumbnailType.valueOf(getThumbnailType()) == null) {
			problems.add(new Problem(Problem.ProblemType.ERROR, ERROR_UNKNOWN_THUMBNAIL_TYPE));
		}

		List<Problem> subProblems = thumbnail.getProblems();
		
		for (Problem prob : subProblems) {
			problems.add(prob);
		}
	}
}
