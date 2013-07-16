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
package bdw.format.jpeg.segment;

import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.ParseMode;
import bdw.format.jpeg.segment.base.SegmentBase;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

/**
 * Segment that represents a comment in a jpeg file.
 * When it exists, it is usually a short string identifying the program
 * that was used to create the program, or the camera used to take it.
 */
public class ComSegment extends SegmentBase {
	/**
	 * Marker code that conventionally represents the com segment.
	 */
    public static final int SUBTYPE = 0xFE;

	/**
	 * The string that is the actual comment
	 */
    protected String comment;

	/**
	 * Internal flag. If we read a comment that wasn't a unicode or ascii
	 * compatible string, remember this so we can write it out as as a
	 * windows character set.
	 */
	protected boolean useWindowsCharset;

	/**
	 * Cached file in case we don't want to read the whole comment in now.
	 */
	protected RandomAccessFile raFile;

	/**
	 * The offset into the file to the start of the comment. only has
	 * meaning if raFile isn't null.
	 */
	protected long fileOffset;

	/**
	 * The mode we are supposed to parse the comment in.
	 * Only meaningful if raFile isn't null
	 */
	protected ParseMode mode;

	/**
	 * Construct a plain instance with a comment of ""
	 */
    public ComSegment()  {
		comment = "";
		useWindowsCharset = false;
		raFile = null;
		fileOffset = 0;
		setMarker(ComSegment.SUBTYPE);
	}
		
	/**
	 *  Constructs an instance with the specified subType
	 * 
	 * @param subType the subType for the instance.
	 * @throws InvalidType If the subtype is other than ComSegment.SUBTYPE
	 */
	public ComSegment(int subType) throws InvalidJpegFormat {
		this();
		if (ComSegment.canHandleMarker(subType)) {
			setMarker(subType);
		} else {
			throw new InvalidJpegFormat("The subtype " + subType + " is not applicable to " + this.getClass().getSimpleName());
		}
    }

	/**
	 * Construct an instance from a stream.
	 *
	 * @param stream The stream to read from
	 * @param mode The mode to parse this in. At this time, no distinction is made between modes.
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public ComSegment(int subType, InputStream stream, ParseMode mode) throws IOException, InvalidJpegFormat  {
		this(subType);
		super.readFromStream(stream, mode);
    }

	/**
	 * Construct an instance from a stream.
	 *
	 * @param file The file to read from
	 * @param mode The mode to parse this in. At this time, no distinction is made between modes.
	 * @throws IOException If an error occurs while parsing (most likely EOFException)
	 * @throws InvalidJpegFormat If the data is overtly malformed (at this time, can't happen with a comment)
	 */
	public ComSegment(int subType, RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
		this(subType);
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
		if (marker == ComSegment.SUBTYPE) {
			return true;
		}
		return false;
	}

	/**
	 * @param comment The new comment (may not be null)
	 */
    public void setComment(String comment) {
        if (comment == null) {
            throw new IllegalArgumentException("comment may not be null");
        }

		useWindowsCharset = false;
        this.comment = comment;
    }

	/**
	 * @return The current comment string.
	 * @throws IOException If something goes wrong during a forceContentLoading
	 */
    public String getComment() throws IOException {
		forceContentLoading();
        return comment;
    }

	/**
	 * @inheritdoc
	 *
	 * Writes the comment in JPEG format to the specified stream.
	 * This will try to write the string as ASCII if possible,
	 * otherwise UTF8.  The exception to this is that if the
	 * read routine discovered characters with the 8th bit set, and
	 * couldn't interpret it as a utf8 string, it assumed it was
	 * reading a windows character set string.  In that case,
	 * write it out as a windows string.
	 *
	 * @param stream The stream to write to
	 * @throws IOException If an io problem happens.
	 */
    @Override
    public void write(OutputStream stream) throws IOException {
        super.write(stream);

		forceContentLoading();

		DataOutputStream dataStream = wrapAsDataOutputStream(stream);
		Charset set = Charset.forName("UTF8");
		byte[] bytes;

		// IF appropriate, try to get the best character set.
		// If this jvm doesn't support Cp1252 (windows), try iso latin 1
		// which is pretty similar. If that's not there, then
		// we'll just use utf-8
		if (useWindowsCharset) {
			try {
				set = Charset.forName("Cp1252");
			} catch (Exception e) {
				try {
					set = Charset.forName("ISO8859_1");
				} catch (Exception e2) {
					set = Charset.forName("UTF8");
				}
			}
		}
		bytes = comment.getBytes(set);

        dataStream.writeShort(2 + bytes.length);

        for (int index = 0; index < bytes.length; index++) {
			dataStream.writeByte((int)bytes[index]);
        }
    }

	/**
	 * @inheritdoc
	 */
	@Override
	public void forceContentLoading() throws IOException {
		if (raFile != null) {
			long storedPosition = raFile.getFilePointer();
			raFile.seek(fileOffset);
			readData(raFile, mode);
			raFile.seek(storedPosition);
			raFile = null;
		}
	}

	/**
	 * Two comment segments are equal if they both have the same comment
	 * @param other The other object to test
	 * @return If other and this are equal
	 */
	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof ComSegment)) {
			return false;
		} else {
			try {
				if ( ! getComment().equals(((ComSegment)other).getComment())) {
					return false;
				}
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 71 * hash + (this.comment != null ? this.comment.hashCode() : 0);
		return hash;
	}

	/**
	 * @inheritdoc
	 *
	 * @param file The file to read from (not null)
	 * @param mode The mode to parse the file in
	 *
	 * @throws IOException If an error occurs while reading
	 */
	@Override
	protected void readFromFile(RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
        long position = file.getFilePointer();
		int contentLength = file.readUnsignedShort();

		// If the comment data is too long, defer reading it in.
		if (contentLength-2 > SegmentBase.READ_LIMIT) {
			raFile = file;
			fileOffset = position;
			this.mode = mode;
			file.seek(position);
		} else {
			readData(file, mode);
		}
	}

	/**
	 * @inheritdoc
	 */
	@Override
    protected void readData(DataInput input, ParseMode mode) throws IOException {
        int contentLength = input.readUnsignedShort();
		int totalBytes = contentLength - 2;

        if (totalBytes == 0) {
            setComment("");
        } else {
            int aChar;
			InputStreamReader reader;
			StringBuilder builder = new StringBuilder(contentLength - 2);
			int index = 0;
			byte[] buffer = new byte[totalBytes];

			// Read in the raw bytes
			while (index < totalBytes) {
                buffer[index] = input.readByte();
                index++;
            }

			// Try to parse them as utf8.  We get back 0xFFFD
			// if the reader encounters something that's not a
			// unicode character (note that since ASCII is a
			// exact subset of utf8, this also catches a purely
			// ASCII string.
			reader = new InputStreamReader(new ByteArrayInputStream(buffer), "UTF8");
			aChar = reader.read();
			while ((aChar != -1) && (aChar != 0xFFFD)) {
				builder.append((char)aChar);
				aChar = reader.read();
			}

			// OK, if we encountered a non-uft8 string here, let's
			// just assume it is a windows character set and read it
			// in thusly.  This might be wrong, but no way to tell.
			if (aChar == 0xFFFD) {
				reader = new InputStreamReader(new ByteArrayInputStream(buffer), "Cp1252");
				aChar = reader.read();
				while (aChar != -1) {
					builder.append((char)aChar);
					aChar = reader.read();
				}
				setComment(builder.toString());
				this.useWindowsCharset = true;
			} else {
				setComment(builder.toString());
			}

        }
    }

}
