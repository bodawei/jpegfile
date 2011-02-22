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

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Base class for all segments.
 * A segment represents all the data in one segment of a JPEG file after the 0xFF and marker flags,
 * and before the next ones.  The raw bytes that it represents are accessible as the "Content"
 * property.  However, subclasses of this class may have specialized accessors to access the
 * data more usefully than a raw set of bytes.
 *
 * Segments are expected to be able to be constructed fresh (with no existing data), or
 * from an existing data source (random access file, or a stream).  In the case of a random access
 * file, the segment is free to not actually read the data into memory until it is needed.
 * This allows users to analyze the structure of a potentially large JPEG file without loading
 * all of the data into memory. At an time, however, the user can force that data into memory.
 */
public abstract class SegmentBase {
	protected RandomAccessFile file;
	protected long fileOffset;

	public SegmentBase() {
		file = null;
		fileOffset = 0;
	}

	/**
	 * @return the code that represents this segment.
	 */
    public abstract int getMarker();

	/**
	 *
	 */
	public void readFromFile(RandomAccessFile file) throws IOException {
	}

	/**
	 * @return The number of bytes in the raw segment content
	 */
    public int getContentLength() {
        return 0;
    }

	/**
	 * Returns a copy of the raw content of this segument.  Note that if the
	 * content is not already in memory, this will load it. Thus this may be
	 * quite slow the first time it is called.
	 *
	 * @return The raw bytes that make up the content of this segment.
	 */
    public byte[] getContent() {
        return new byte[0];
    }

	/**
	 * Sets the content of this segment.  Any segment-specific data accessors will
	 * return their portion of this data.  If any content is already here, this
	 * replaces it.
	 *
	 * @param content The content to put in this segment.
	 * @thros IllegalArgumentException if the content is not valid content for this segment type
	 * */
    public void setContent(byte[] content) {
    }

	/**
	 * If this segment has not read all of its content from disk, this will force it to
	 * be read.  However, if the content has already been read, this will have no effect.
	 */
    public void forceContentLoading() {
    }

	/**
	 * Writes the contents of this segment to the output stream, including the size info,
	 * if appropraite, but not the 0xFF and marker info.
	 *
	 * @param stream a non-null stream to write data to.
	 * @throw IllegalArgumentException if param is null
	 * */
    public void writeToFile(OutputStream stream) {
		if (stream == null) {
			throw new IllegalArgumentException("Stream may not be null");
		}
    }
}
