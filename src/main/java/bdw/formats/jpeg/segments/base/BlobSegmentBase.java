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
import bdw.util.ByteArrayBuilder;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class contains common code for dealing with large "blobs" of
 * data in the jpeg file
 */
public abstract class BlobSegmentBase extends SegmentBase {

	/**
	 * Amount in increase the buffer size by
	 */
	private static final int BUFFER_INCREMENT = 1024;

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
	protected long diskLength;

	/**
	 * The number of bytes of data in the data buffer
	 */
	protected long dataLength;

	protected ByteArrayBuilder list;

	/**
	 * The buffer of data this segment is managing. This may contain
	 * more entries than dataLength.  That is the true measure of data.
	 */
	protected byte[] data;

	/**
	 * Flag whether we have, in fact, read all the data we are managing in
	 */
	protected boolean dataRead;

	protected boolean isDataSegment;

	/**
	 * Construct
	 */
	public BlobSegmentBase() {
		raFile = null;
		fileOffset = 0;

		isDataSegment = false;
		list = new ByteArrayBuilder();
	}

	public long getDataLength() {
		return list.getSize();
	}

	public byte getDataAt(int index) throws IOException {
		if ((index < 0) || (index > list.getSize())) {
			throw new IndexOutOfBoundsException("Index out of range");
		}

		forceContentLoading();

		return list.getByteAt(index);
	}

	public void setDataAt(int index, byte aByte) throws IOException {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index out of range");
		}

		forceContentLoading();

		list.setByteAt(index, aByte);
	}

	@Override
	public void readFromFile(RandomAccessFile file) throws IOException, InvalidJpegFormat {
		raFile = file;
		fileOffset = file.getFilePointer();

		try {
			byte aByte;

			if ( ! isDataSegment) {
				list.append(file.readByte());
			}

			while (true) {
				aByte = file.readByte();

				if (list.getSize() <= 1024) {
					list.append(aByte);
				} else {
					// abort
				}

				// if this is 0xff 0x(!=0)
				if (aByte == (byte) 0xff) {
					byte markerByte = file.readByte();
					if (markerByte == 0x00) {
						if ( ! isDataSegment) {
							list.append((byte)markerByte);
						}
					} else { // possibly have a substantive marker.
						file.seek(file.getFilePointer() - 2);
						diskLength = file.getFilePointer() - this.fileOffset;
						if (list.getSize() < 1024) {
							raFile = null;
							fileOffset = 0;
						}
						return;
					}
				}
			}
		} catch (EOFException exception) {
			// hmm.
		}

		diskLength = file.getFilePointer() - this.fileOffset;
		if (abort == true) {
			raFile = null;
			fileOffset = 0;
		}
	}

	@Override
	public void readFromStream(InputStream stream) throws IOException, InvalidJpegFormat {
		int aByte;
		boolean keepGoing = true;

		if ( ! isDataSegment) {
			aByte = stream.read();
			if (aByte == -1) {
				keepGoing = false;
			} else {
				list.append((byte)aByte);
			}
		}

		while (keepGoing) {
			stream.mark(2);	// allow for rollback if we find 0xFF 0xXX

			aByte = stream.read();
			switch(aByte) {
				case -1:
					keepGoing = false;
					break;
				case 0xFF:
					int markerByte = stream.read();

					switch (markerByte) {
						case -1:
							// this is anomolous. It may be a bad jpeg file?
							list.append((byte)aByte);
							break;
						case 0x00:
							list.append((byte)aByte);
							if ( ! isDataSegment) {
								list.append((byte)markerByte);
							}
							break;
						default:
							stream.reset();
							keepGoing = false;
							break;
					}
					break;
				default:
					list.append((byte)aByte);
					break;
			}
		}
		raFile = null;
		dataRead = true;
	}

	/**
	 * If this segment has not read all of its content from disk, this will force it to
	 * be read.  However, if the content has already been read, this will have no effect.
	 */
	@Override
	public void forceContentLoading() throws IOException {
		if (raFile != null) {
			long currentLoc = raFile.getFilePointer();
			raFile.seek(fileOffset);
			list = new ByteArrayBuilder(); // diskLength
			for (int index = 0; index < dataLength; index++) {
				byte aByte = raFile.readByte();
				list.append((byte)aByte);
				if (isDataSegment) {
					if (aByte == 0xFF) {
						byte markerByte = raFile.readByte();
						if (markerByte != 0x00) {
							list.append((byte) markerByte);
						}
					}
				}
			}
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

		for (int index = 0; index < list.getSize(); index++) {
			stream.write(list.getByteAt(index));
			if (isDataSegment && (list.getByteAt(index) == (byte)0xFF)) {
				stream.write(0x00);
			}
		}
	}

}
