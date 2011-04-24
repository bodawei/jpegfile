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

package bdw.formats.jpeg.segments.base;

import bdw.formats.jpeg.InvalidJpegFormat;
import bdw.formats.jpeg.ParseMode;
import bdw.util.ByteArrayBuilder;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * This class contains common code for dealing with large "blobs" of
 * data in the jpeg file. It is the superclass of both the junk and the
 * data segments, and exists only to provide shared code, since those
 * are largely the same.
 */
public abstract class BlobSegmentBase extends SegmentBase {

	private static final int EOS = -1;

	/**
	 * The data file we're reading from. Will be null if data has been read
	 */
	protected RandomAccessFile raFile;

	/**
	 * Offset within the file.  if raFile is null, the value here is undefined
	 */
	protected long fileOffset;

	/**
	 * The number of bytes of data "on disk"
	 */
	protected long diskLength;

	/**
	 * The array of bytes this segment is managing
	 */
	protected ByteArrayBuilder byteArray;

	/**
	 * If true, parse the data differently than if not.
	 */
	protected boolean isDataSegment;

	/**
	 * Construct
	 */
	public BlobSegmentBase() {
		raFile = null;
		fileOffset = 0;
		diskLength = 0;

		isDataSegment = false;
		byteArray = new ByteArrayBuilder();
	}

	public long getDataLength() throws IOException {
		//TODO Could make this faster by calculating the data length as we scan it.
		forceContentLoading();

		return byteArray.getSize();
	}

	public byte getDataAt(int index) throws IOException {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index out of range");
		}

		forceContentLoading();

		return byteArray.getByteAt(index);
	}

	public void setDataAt(int index, byte aByte) throws IOException {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index out of range");
		}

		forceContentLoading();

		byteArray.setByteAt(index, aByte);
	}

	@Override
	protected void readFromFile(RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
		ByteArrayBuilder builder = new ByteArrayBuilder();
		long offset = file.getFilePointer();
		int byteCount = 0;

		try {
			byte aByte;
			byte markerByte;

			if ( ! isDataSegment) {
				builder.append(file.readByte());
				byteCount++;
			}

			while (true) {
				aByte = file.readByte();
				byteCount++;

				if (byteCount <= SegmentBase.READ_LIMIT) {
					builder.append(aByte);
				}

				if (aByte == (byte) 0xff) {
					markerByte = file.readByte();
					byteCount++;

					if (markerByte == 0x00) {
						if (( ! isDataSegment) && (byteCount <= SegmentBase.READ_LIMIT)) {
							builder.append((byte)markerByte);
						}
					} else { // possibly have a substantive marker.
						builder.deAppend();
						file.seek(file.getFilePointer() - 2);
						break;
					}
				}
			}
		} catch (EOFException exception) {
		}

		if (byteCount > SegmentBase.READ_LIMIT) {
			raFile = file;
			fileOffset = offset;
			diskLength = byteCount;
		} else {
			raFile = null;
			byteArray = builder;
		}
	}

	@Override
	protected void readFromStream(InputStream stream, ParseMode strict) throws IOException, InvalidJpegFormat {
		int aByte;
		boolean keepGoing = true;
		ByteArrayBuilder builder = new ByteArrayBuilder();

		if ( ! isDataSegment) {
			aByte = stream.read();
			if (aByte == BlobSegmentBase.EOS) {
				keepGoing = false;
			} else {
				builder.append((byte)aByte);
			}
		}

		while (keepGoing) {
			stream.mark(2);	// allow for rollback if we find 0xFF 0xXX

			aByte = stream.read();
			switch(aByte) {
				case BlobSegmentBase.EOS:
					keepGoing = false;
					break;
				case 0xFF:
					int markerByte = stream.read();

					switch (markerByte) {
						case BlobSegmentBase.EOS:
							builder.append((byte)aByte);
							// this is anomolous. It may be a bad jpeg file?
							break;
						case 0x00:
							builder.append((byte)aByte);
							if ( ! isDataSegment) {
								builder.append((byte)markerByte);
							}
							break;
						default: // legitimate marker
							stream.reset();
							keepGoing = false;
							break;
					}
					break;
				default:
					builder.append((byte)aByte);
					break;
			}
		}

		byteArray = builder;
		raFile = null;
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
			byteArray = new ByteArrayBuilder();

			for (int index = 0; index < diskLength; index++) {
				byte aByte = raFile.readByte();
				byteArray.append((byte)aByte);
				if (isDataSegment) {
					if (aByte == 0xFF) {
						byte markerByte = raFile.readByte();
						if (markerByte != 0x00) {
							byteArray.append((byte) markerByte);
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

		for (int index = 0; index < byteArray.getSize(); index++) {
			stream.write(byteArray.getByteAt(index));
			if (isDataSegment && (byteArray.getByteAt(index) == (byte)0xFF)) {
				stream.write(0x00);
			}
		}
	}

}
