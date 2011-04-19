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
import bdw.formats.jpeg.segments.base.SegmentBase;
import java.io.DataInputStream;
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
	 * True if we have read all data from the input source.
	 */
	protected boolean dataRead;

	/**
	 * The number of bytes of data this segment represents
	 */
	protected int contentLength;


	/**
	 * construct the instance (duh)
	 */
	public AppNSegment() {
		setMarker(0);

		dataRead = true;
		raFile = null;
		contentLength = 0;
		data = new byte[0];
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
	public void readFromStream(InputStream stream) throws IOException, InvalidJpegFormat {
		DataInputStream diStream;

		if (stream instanceof DataInputStream) {
			diStream = (DataInputStream) stream;
		} else {
			diStream = new DataInputStream(stream);
		}

		int dataLength = diStream.readUnsignedShort() - 2;
		byte[] buffer = new byte[dataLength];

		for (int index = 0; index < dataLength; index++) {
			buffer[index] = (byte) diStream.readByte();
		}

		data = buffer;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void readFromFile(RandomAccessFile file) throws IOException, InvalidJpegFormat {
		contentLength = file.readUnsignedShort() - 2;
		byte[] buffer;

		if (contentLength > 1024) {
			buffer = new byte[0];
			raFile = file;
			fileOffset = file.getFilePointer();
			file.skipBytes(contentLength);
			long fileOffset2 = file.getFilePointer();
			dataRead = false;

		} else {
			buffer = new byte[contentLength];

			for (int count = 0; count < contentLength; count++) {
				buffer[count] = file.readByte();
			}
			data = buffer;
			dataRead = true;
		}

	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		DataOutputStream doStream;

		super.write(stream);

		forceContentLoading();

		if (stream instanceof DataOutputStream) {
			doStream = (DataOutputStream) stream;
		} else {
			doStream = new DataOutputStream(stream);
		}

		doStream.writeShort(getBytes().length + 2);
		for (int index = 0; index < getBytes().length; index++) {
			doStream.write(data[index]);
		}

	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void forceContentLoading() throws IOException {
		if ( ! dataRead) {

			long currentLoc = raFile.getFilePointer();
			raFile.seek(fileOffset);
			data = new byte[(int)contentLength];

			for (int index = 0; index < contentLength; index++) {
				data[index] = raFile.readByte();
			}

			dataRead = true;
			raFile.seek(currentLoc);
			raFile = null;	// release our hold on the file
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
				forceContentLoading();

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
}
