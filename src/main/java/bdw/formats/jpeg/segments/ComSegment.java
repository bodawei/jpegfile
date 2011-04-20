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

/**
 * Segment that represents a comment in a jpeg file
 */
public class ComSegment extends SegmentBase {

    public static final int MARKER = 0xFE;
    protected String comment;
    protected boolean writeTrailingNull;

    public ComSegment() {
        setMarker(ComSegment.MARKER);
        comment = "";
        writeTrailingNull = true;
    }

    public ComSegment(InputStream stream) throws IOException, InvalidJpegFormat {
		this(stream, ParseMode.STRICT);
    }

	public ComSegment(InputStream stream, ParseMode mode) throws IOException, InvalidJpegFormat {
		this();
		super.readFromStream(stream);
    }

    public ComSegment(RandomAccessFile file) throws IOException, InvalidJpegFormat {
		this(file, ParseMode.STRICT);
    }

	public ComSegment(RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
		this();
		super.readFromFile(file);
    }

	public static boolean canHandleMarker(int marker) {
		if (marker == ComSegment.MARKER) {
			return true;
		}
		return false;
	}


    public void setComment(String comment) {
        if (comment == null) {
            throw new IllegalArgumentException("comment may not be null");
        }
        this.comment = comment;
        writeTrailingNull = true;
    }

    public String getComment() {
        return comment;
    }


    @Override
    public void write(OutputStream stream) throws IOException {
        super.write(stream);
        DataOutputStream dataStream;

        if (stream instanceof DataOutputStream) {
            dataStream = (DataOutputStream) stream;
        } else {
            dataStream = new DataOutputStream(stream);
        }

        int totalLength = 2 + comment.length();

        if (writeTrailingNull == true) {
            totalLength ++;
        }
        dataStream.writeShort(totalLength);

        for (int index = 0; index < comment.length(); index++) {
            dataStream.writeByte((int)comment.charAt(index));
        }

        if (writeTrailingNull == true) {
            dataStream.writeByte(0x00);
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
			if ( ! getComment().equals(((ComSegment)other).getComment())) {
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


    @Override
    public void readData(DataInput input) throws IOException {
        int contentLength = input.readUnsignedShort();

        StringBuilder buffer = new StringBuilder(contentLength - 2);
        int charsLeft = contentLength - 2;

        if (charsLeft == 0) {
            setComment("");
            writeTrailingNull = false;
        } else {
            int aChar;

            while (charsLeft > 1) {
                aChar = input.readByte();
                buffer.append((char) aChar);
                charsLeft--;
            }
            aChar = input.readByte();
            if (aChar != 0x00) {
                buffer.append((char) aChar);
                writeTrailingNull = false;
				System.out.println("---------- DID NOT GET A NULL");
            } else {
                writeTrailingNull = true;
            }
                buffer.append((char) aChar);
                writeTrailingNull = false;

				setComment(buffer.toString());
        }
    }

}
