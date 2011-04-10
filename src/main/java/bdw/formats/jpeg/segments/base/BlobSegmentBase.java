/*
 *  Copyright 2011 æŸ�å¤§è¡›
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

package bdw.formats.jpeg.segments.base;

import bdw.formats.jpeg.InvalidJpegFormat;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * This class contains common code for dealing with large "blobs" of
 * data in the jpeg file
 */
public abstract class BlobSegmentBase extends SegmentBase {

	/**
	 * The data file we're reading from. Will be null if data has been read
	 */
	protected RandomAccessFile raFile;

	/**
	 * Offset within the file.  This should be ignored if data has been read
	 */
	protected long fileOffset;

	/**
	 * The number of bytes of data "on disk"
	 */
	protected long dataLength;

	/**
	 * The buffer of data this segment is managing. This may contain
	 * more entries than dataLength.  That is the true measure of data.
	 */
	protected byte[] data;

	/**
	 * Flag whether we have, in fact, read all the data we are managing in
	 */
	protected boolean dataRead;

	protected boolean interpretInitialByte;

	/**
	 * Construct
	 */
	public BlobSegmentBase() {
		raFile = null;
		fileOffset = 0;

		dataLength = 0;
		data = new byte[0];
		dataRead = true;

		interpretInitialByte = false;
	}

	public long getDataLength() {
		return dataLength;
	}

	public byte getDataAt(int index) throws IOException {
		if ((index < 0) || (index > dataLength)) {
			throw new IndexOutOfBoundsException("Index out of range");
		}

		forceContentLoading();

		return data[index];
	}

	public void setDataAt(int index, byte aByte) throws IOException {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index out of range");
		}

		forceContentLoading();

		if (index > dataLength) {
			data = Arrays.copyOf(data, index);
		}

		data[index] = aByte;
	}

	@Override
	public void readFromFile(RandomAccessFile file) throws IOException, InvalidJpegFormat {
		raFile = file;
		fileOffset = file.getFilePointer();

		byte[] buffer = new byte[1024]; // if there's less than 1K of data, store it
		int count = 0;

		try {
			// always read the first byte, and assume it is ok to not analyze
			byte aByte;

			if ( ! interpretInitialByte) {
				aByte = file.readByte();
				count++;
				buffer[count] = aByte;
			}

			while (true) {
				aByte = file.readByte();
				count++;
				if (count < 1024) {
					buffer[count] = aByte;
				}

				// if this is 0xff 0x(!=0)
				if (aByte == (byte) 0xff) {
					int markerByte = file.readByte();
					if (markerByte != 0x00) {   // possibly have a substantive marker.
						dataLength = file.getFilePointer() - this.fileOffset;
						file.seek(file.getFilePointer() - 2);
						count -= 2;
						if (count < 1024) {
							data = buffer;
							raFile = null;
							fileOffset = 0;
							dataRead = true;
						} else {
							data = null;
							dataRead = false;
						}
						return;
					}
				}
			}
		} catch (EOFException exception) {
		}

		dataLength = file.getFilePointer() - this.fileOffset;
		if (count < 1024) {
			data = buffer;
			raFile = null;
			fileOffset = 0;
			dataRead = true;
		}
	}

	@Override
	public void readFromStream(InputStream stream) throws IOException, InvalidJpegFormat {
		byte[] buffer = new byte[1024];
		int count = 0;

		try {
			// always read the first byte, and assume it is ok to not analyze
			byte aByte;

			if ( ! interpretInitialByte) {
				aByte = (byte)stream.read();
				count++;
				buffer[count] = aByte;
			}

			while (true) {
				stream.mark(2);	// so we can roll back if we need to if we find the end

				aByte = (byte)stream.read();
				count++;
				if (count > buffer.length) {
					buffer = Arrays.copyOf(buffer, buffer.length + 1024);
				}
				buffer[count] = aByte;	// may end up ignoring this if it is 0xff

				// if this is 0xff 0x(!=0)
				if (aByte == 0xff) {
					byte markerByte = (byte) stream.read();

					if (markerByte == 0x00) {   // possibly have a substantive marker.
						count++;
						if (count > buffer.length) {
							buffer = Arrays.copyOf(buffer, buffer.length + 1024);
						}
						buffer[count] = markerByte;
					} else {
						dataLength = count - 2;
						stream.reset();
						dataRead = true;
						data = buffer;
						return;
					}
				}
			}
		} catch (EOFException exception) {
		}

		dataLength = count;
		raFile = null;
		dataRead = true;
		data = buffer;
	}

	/**
	 * If this segment has not read all of its content from disk, this will force it to
	 * be read.  However, if the content has already been read, this will have no effect.
	 */
	@Override
	public void forceContentLoading() throws IOException {
		if(dataRead == false) {
			long currentLoc = raFile.getFilePointer();
			raFile.seek(fileOffset);
			data = new byte[(int)dataLength];
			for (int index = 0; index < dataLength; index++) {
				data[index] = raFile.readByte();
			}
			dataRead = true;
			raFile.seek(currentLoc);
			raFile = null;	// release our hold on the file
		}
	}

	/**
	 * Writes the contents of this segment to the output stream, including the size info,
	 * if appropriate, but not the 0xFF and marker info.
	 *
	 * @param stream a non-null stream to write data to.
	 * @throw IllegalArgumentException if param is null
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		if (stream == null) {
			throw new IllegalArgumentException("Stream may not be null");
		}

		forceContentLoading();

		for (int index = 0; index < dataLength; index++) {
			stream.write(data[index]);
		}
	}

}
