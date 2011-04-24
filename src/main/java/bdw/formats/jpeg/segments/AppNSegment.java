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

import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.ParseMode;
import bdw.formats.jpeg.segments.base.SegmentBase;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * A stand-in for all AppN segment types.
 */
public class AppNSegment extends SegmentBase {
	/**
	 * The first conventional marker
	 */
	public static final int START_MARKER = 0xE0;

	/**
	 * The last conventional marker
	 */
	public static final int END_MARKER = 0xEF;

	/**
	 * The data we have read from the file or stream
	 */
	protected byte[] data;

	/**
	 * A reference to the file we are reading from. This is not null
	 * only if we haven't read all data from the file yet.
	 */
	protected RandomAccessFile raFile;

	/**
	 * The offset in the file.
	 */
	protected long fileOffset;
	/**
	 * The number of bytes of data this segment represents
	 */
	protected int contentLength;

	/**
	 * construct the instance (duh)
	 */
	public AppNSegment() {
		setMarker(AppNSegment.START_MARKER);

		raFile = null;
		contentLength = 0;
		data = new byte[0];
	}

	/**
	 * Construct an instance from a stream, parsing it strictly.
	 *
	 * @param stream The stream to read from
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
    public AppNSegment(InputStream stream) throws IOException, InvalidJpegFormat {
		this(stream, ParseMode.STRICT);
    }

	/**
	 * Construct an instance from a stream.
	 *
	 * @param stream The stream to read from
	 * @param mode The mode to parse this in. At this time, no distinction is made between modes.
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public AppNSegment(InputStream stream, ParseMode mode) throws IOException, InvalidJpegFormat {
		this();
		super.readFromStream(stream, mode);
    }

	/**
	 * Construct an instance from a stream. Parses it strictly
	 *
	 * @param file The file to read from
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
    public AppNSegment(RandomAccessFile file) throws IOException, InvalidJpegFormat {
		this(file, ParseMode.STRICT);
    }

	/**
	 * Construct an instance from a stream.
	 *
	 * @param file The file to read from
	 * @param mode The mode to parse this in. At this time, no distinction is made between modes.
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public AppNSegment(RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
		this();
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
		if ((marker >= AppNSegment.START_MARKER) && (marker <= AppNSegment.END_MARKER)) {
			return true;
		}
		return false;
	}

	/**
	 * @param bytes The bytes this should hold. This array is used directly, not copied. This may not be more than 65K bytes
	 */
	public void setBytes(byte[] bytes) {
		if (bytes.length >= 65536) {
			throw new IllegalArgumentException();
		}
		data = bytes;
	}

	/**
	 * @return The bytes this represents
	 * @throws IOException If an exception happens while getting them.
	 */
	public byte[] getBytes() throws IOException {
		forceContentLoading();
		return data;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		super.write(stream);

		forceContentLoading();

		DataOutputStream dataStream = wrapAsDataOutputStream(stream);

		dataStream.writeShort(getBytes().length + 2);
		for (int index = 0; index < getBytes().length; index++) {
			dataStream.write(getBytes()[index]);
		}
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void forceContentLoading() throws IOException {
		if (raFile != null) {
			long currentLoc = raFile.getFilePointer();
			data = new byte[(int)contentLength];
			raFile.seek(fileOffset);

			for (int index = 0; index < contentLength; index++) {
				data[index] = raFile.readByte();
			}

			raFile.seek(currentLoc);
			raFile = null;
			fileOffset = 0;
		}
	}

	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof AppNSegment)) {
			return false;
		} else {
			AppNSegment castOther = (AppNSegment) other;

			try {
				if (getBytes().length != castOther.getBytes().length) {
					return false;
				}

				for (int index = 0; index < getBytes().length; index++) {
					if (getBytes()[index] != castOther.getBytes()[index]) {
						return false;
					}
				}
			} catch (IOException ex) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		try {
			forceContentLoading();
		} catch (IOException e) {
			return 0;
		}

		int hash = 3;
		hash = 29 * hash + Arrays.hashCode(this.data);
		return hash;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void readData(DataInput input, ParseMode mode) throws IOException, InvalidJpegFormat {
		int dataLength = input.readUnsignedShort() - 2;
		byte[] buffer = new byte[dataLength];

		for (int index = 0; index < dataLength; index++) {
			buffer[index] = (byte) input.readByte();
		}

		data = buffer;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void readFromFile(RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
		contentLength = file.readUnsignedShort() - 2;
		byte[] buffer;

		if (contentLength > SegmentBase.READ_LIMIT) {
			buffer = new byte[0];
			raFile = file;
			fileOffset = file.getFilePointer();
			file.skipBytes(contentLength);
		} else {
			buffer = new byte[contentLength];

			for (int count = 0; count < contentLength; count++) {
				buffer[count] = file.readByte();
			}
			data = buffer;
			raFile = null;
		}
	}
}
