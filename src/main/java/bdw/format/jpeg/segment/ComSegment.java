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
package bdw.format.jpeg.segment;

import bdw.format.jpeg.data.DataItem;
import bdw.format.jpeg.data.Segment;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.Marker;
import bdw.format.jpeg.support.ParseMode;
import bdw.format.jpeg.support.Problem;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Segment that represents a comment in a jpeg file.
 * When it exists, it is usually a short string identifying the program
 * that was used to create the jpeg data, or the camera used to take it.
 * 
 * Note: This is defined on pages B-16 and B-17 of the standard.
 */
@Marker(ComSegment.MARKER)
public class ComSegment extends Segment {
	/**
	 * Marker code that conventionally represents the com segment.
	 */
    public static final int MARKER = 0xFE;

	/**
	 * The string that is the actual comment
	 */
    private transient String cachedStringComment;

	/**
	 * The raw comment data
	 */
    private byte[] comment;

	/**
	 * Cached file in case we don't want to read the whole comment in now.
	 */
	private RandomAccessFile raFile;

	/**
	 * The offset into the file to the start of the comment. only has
	 * meaning if raFile isn't null.
	 */
	private long fileOffset;

	/**
	 * The mode we are supposed to parse the comment in.
	 * Only meaningful if raFile isn't null
	 */
	private ParseMode mode;

	/**
	 * Construct a plain instance with a comment of ""
	 */
    public ComSegment()  {
		cachedStringComment = null;
		raFile = null;
		fileOffset = 0;
		comment = new byte[0];
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public int getMarker() {
		return MARKER;
	}

	/**
	 * @param comment The new comment bytes (may not be null)
	 */
    public void setComment(byte[] comment) {
        if (comment == null) {
            throw new IllegalArgumentException("comment may not be null");
        }

		cachedStringComment = null;
        this.comment = comment;
    }

	/**
	 * @return The current comment bytes.
	 * @throws IOException If something goes wrong during a forceContentLoading
	 */
    public byte[] getComment() throws IOException, InvalidJpegFormat {
		forceContentLoading();
        return comment;
    }

	/**
	 * Sets the comment using a string value.  The string will be
	 * written out as a UTF8 format string.
	 * If you want a trailing null, it must be included in the string object
	 * itself.
	 * 
	 * @param comment The new comment (may not be null)
	 */
    public void setStringComment(String comment) {
		setStringComment(comment, Charset.forName("UTF8"));
    }

	/**
	 * Sets the comment using a string value. The string will be written
	 * out using the specified character set (as returned by String.getBytes()).
	 * If you want a trailing null, it must be included in the string object
	 * itself.
	 * 
	 * @param comment The new comment (may not be null)
	 * @param characterSet The new character set (may not be null)
	 */
    public void setStringComment(String comment, Charset characterSet) {
        if (comment == null) {
            throw new IllegalArgumentException("comment may not be null");
        }

        if (characterSet == null) {
            throw new IllegalArgumentException("Character set may not be null");
        }
		
		cachedStringComment = comment;
		this.comment = comment.getBytes(characterSet);
    }

	/**
	 * @return The current comment as a string. If the current comment can be
	 *	read as a UTF8 string, this will return it as that. Otherwise, those
	 *	bytes will be parsed as ISO-8859-1 characters. If this is not what you
	 *	expect, you should get the comment property and parse the bytes
	 * yourself.
	 * @throws IOException If something goes wrong during a forceContentLoading
	 */
    public String getStringComment() throws IOException, InvalidJpegFormat {
		if (cachedStringComment != null) {
			return cachedStringComment;
		}
		
        return convertCommentToString();
    }

	/**
	 * @inheritdoc
	 *
	 * Writes the comment in JPEG format to the specified stream.
	 * This will write the comment exactly as it is stored in the
	 * comment property (not as in the stringComment property)
	 *
	 * @param stream The stream to write to
	 * @throws IOException If an io problem happens.
	 */
    @Override
    public void write(OutputStream stream) throws IOException, InvalidJpegFormat {
        super.write(stream);

		DataOutputStream dataStream = wrapAsDataOutputStream(stream);
		byte[] rawComment = getComment();
		
		dataStream.writeShort(2 + rawComment.length);

        for (int index = 0; index < rawComment.length; index++) {
			dataStream.writeByte((int)rawComment[index]);
        }
    }

	/**
	 * @inheritdoc
	 */
	@Override
	public void forceContentLoading() throws IOException, InvalidJpegFormat {
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
				return Arrays.equals(getComment(),
						((ComSegment)other).getComment());
			} catch (IOException e) {
				return false;
			} catch (InvalidJpegFormat ijf) {
				return false;
			}
		}
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
	public void readFromFile(RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
        long position = file.getFilePointer();
		int contentLength = file.readUnsignedShort();

		// If the comment data is too long, defer reading it in.
		if (contentLength-2 > Segment.READ_LIMIT) {
			raFile = file;
			fileOffset = position;
			this.mode = mode;
			file.seek(position);
		} else {
			file.seek(position);
			readData(file, mode);
		}
	}

	/**
	 * @inheritdoc
	 */
	@Override
    protected void readData(DataInput input, ParseMode mode) throws IOException, InvalidJpegFormat {
        int contentLength = input.readUnsignedShort();
		int totalBytes = contentLength - 2;
		int index = 0;
		byte[] buffer = new byte[totalBytes];

		// Read in the raw bytes
		try {
			while (index < totalBytes) {
				buffer[index] = input.readByte();
				index++;
			}

			setComment(buffer);
		} catch (EOFException exception) {
			throw new InvalidJpegFormat("file was too short",
					new Problem(Problem.ProblemType.ERROR, DataItem.ERROR_EOF));
		}
    }
	
	/**
	 * Returns a string representation of the bytes in the comment
	 * property. If those bytes can be interpreted as UTF8, this will
	 * parse them as that and return them. Otherwise, this will assume
	 * that those bytes are ISO-Latin-1 and return a string in that form
	 * (this has a chance of mangling the bytes. 
	 * @return A string representation of the comment (not null, maybe empty)
	 * @throws IOException if problems happen while forceContentLoading.
	 */
	private String convertCommentToString() throws IOException, InvalidJpegFormat {
        if (getComment().length == 0) {
            return "";
        } else {
            int aChar;
			byte[] rawComment = getComment();
			InputStreamReader reader;
			StringBuilder builder = new StringBuilder(rawComment.length);

			// Try to parse the comment as utf8.  Cody get back 0xFFFD
			// if the reader encounters something that's not a
			// unicode character (note that since ASCII is a
			// exact subset of utf8, this also catches a purely
			// ASCII string.
			reader = new InputStreamReader(
					new ByteArrayInputStream(rawComment), "UTF8");
			aChar = reader.read();
			while ((aChar != -1) && (aChar != 0xFFFD)) {
				builder.append((char)aChar);
				aChar = reader.read();
			}

			// If Cody encountered a non-uft8 string here, he assumes
			// it is a iso-latin-1 character set and read it
			// in thusly.  This might be wrong, but no way to tell.
			if (aChar == 0xFFFD) {
				reader = new InputStreamReader(
						new ByteArrayInputStream(rawComment), "ISO-8859-1");
				aChar = reader.read();
				while (aChar != -1) {
					builder.append((char)aChar);
					aChar = reader.read();
				}
			}

			cachedStringComment = builder.toString();
			return cachedStringComment;
        }
    }

}
