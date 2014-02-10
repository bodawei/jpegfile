/*
 *  Copyright 2014 柏大衛
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
package bdw.format.jpeg.marker;

import bdw.format.jpeg.data.GenericSegment;
import bdw.format.jpeg.support.MarkerId;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Segment that represents a comment in a jpeg file.
 * When it exists, it is usually a short string identifying the program
 * that was used to create the jpeg data, or the camera used to take it.
 *
 * Note: This is defined on pages B-16 and B-17 of the standard.
 */
@MarkerId(ComSegment.MARKERID)
public class ComSegment extends GenericSegment {
	/**
	 * Marker code that conventionally represents the com segment.
	 */
	public static final int MARKERID = 0xFE;

	/**
	 * If the user set a string, Cody keeps a copy of what they gave it, so Cody
	 * can later return the original string.
	 */
	private transient String cachedStringComment;

	/**
	 * Construct a plain instance with a comment of ""
	 */
	public ComSegment() {
		super(MARKERID);
		cachedStringComment = null;
	}

	/**
	 * @param comment The new comment bytes (may not be null)
	 */
	public void setComment(byte[] comment) {
		if (comment == null) {
			throw new IllegalArgumentException("comment may not be null");
		}
		if (comment.length > 65534) {
			throw new IllegalArgumentException("Comment must be less than 65534 bytes long");
		}


		cachedStringComment = null;
		setByteArray(comment);
	}

	/**
	 * @return The current comment bytes.
	 */
	public byte[] getComment() {
		return getByteArray();
	}

	/**
	 * Sets the comment using a string value. The string will be written out as a
	 * UTF8 format string. If you want a trailing null, it must be included in
	 * the string object itself.
	 *
	 * @param comment The new comment (may not be null)
	 */
	public void setStringComment(String comment) {
		setStringComment(comment, Charset.forName("UTF8"));
	}

	/**
	 * Sets the comment using a string value. The string will be written out
	 * using the specified character set (as returned by String.getBytes()). If
	 * you want a trailing null, it must be included in the string object itself.
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
		setComment(comment.getBytes(characterSet));
	}

	/**
	 * @return The current comment as a string. If the current comment can be
	 * read as a UTF8 string, this will return it as that. Otherwise, those bytes
	 * will be parsed as ISO-8859-1 characters. If this is not what you expect,
	 * you should get the comment property and parse the bytes yourself.
	 * @throws IOException If something goes wrong during a forceContentLoading
	 */
	public String getStringComment() throws IOException {
		if (cachedStringComment != null) {
			return cachedStringComment;
		}

		return convertCommentToString();
	}

	/**
	 * Two comment segments are equal if they both have the same comment
	 *
	 * @param other The other object to test
	 * @return If other and this are equal
	 */
	@Override
	public boolean equals(Object other) {
		return (super.equals(other) &&
			Arrays.equals(getComment(), ((ComSegment) other).getComment()));
	}

	/**
	 * Returns a string representation of the bytes in the comment property. If
	 * those bytes can be interpreted as UTF8, this will parse them as that and
	 * return them. Otherwise, this will assume that those bytes are ISO-Latin-1
	 * and return a string in that form (naturally, if the bytes are neither
	 * character set, then what you get will probably be junk).
	 *
	 * @return A string representation of the comment (not null, maybe empty)
	 * @throws IOException if problems happen while forceContentLoading.
	 */
	private String convertCommentToString() throws IOException {
		if (getComment().length == 0) {
			return "";
		}
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
			builder.append((char) aChar);
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
				builder.append((char) aChar);
				aChar = reader.read();
			}
		}

		cachedStringComment = builder.toString();
		return cachedStringComment;
	}
}
