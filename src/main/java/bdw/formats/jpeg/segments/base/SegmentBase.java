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
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
public abstract class SegmentBase extends JpegDataStructureBase {

	protected static final int READ_LIMIT = 1024;

	private int marker;

	private boolean valid;

	public SegmentBase() {
		valid = true;
	}


	/**
	 * @return true if the segment is strictly valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * @param valid the new value for the valid flag.
	 */
	protected void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * @return the code that represents this segment.
	 */
	public int getMarker() {
		return marker;
	}

	public void setMarker(int newMarker) {
		marker = newMarker;
	}


	/**
	 * @param file The file to read from (not null)
	 * @param mode The mode to parse the file in
	 *
	 * @throws IOException If an error occurs while reading
	 */
	protected void readFromFile(RandomAccessFile file, ParseMode mode) throws IOException, InvalidJpegFormat {
		if (file == null) {
			throw new IllegalArgumentException("Input file may not be null");
		}

		readData(file, mode);
	}

	/**
	 * @param stream The stream to read from (not null)
	 * @param mode The mode to parse the file in
	 *
	 * @throws IOException If an error occurs while reading
	 */
	protected void readFromStream(InputStream stream, ParseMode mode) throws IOException, InvalidJpegFormat {
		if (stream == null) {
			throw new IllegalArgumentException("Input stream may not be null");
		}

		if (stream instanceof DataInputStream) {
			readData((DataInputStream) stream, mode);
		} else {
			readData(new DataInputStream(stream), mode);
		}
	}

	/**
	 * Read all the data and populate this instance.
	 * This resets all contents of this segment.
	 *
	 * @param dataSource The input source to read from
	 *
	 * @throws IOException If something happens while reading
	 */
	protected void readData(DataInput dataSource) throws IOException, InvalidJpegFormat {
	}

	/**
	 * Read all the data and populate this instance.
	 * This resets all contents of this segment.
	 *
	 * @param dataSource The input source to read from
	 * @param mode The mode to read the file in
	 *
	 * @throws IOException If something happens while reading
	 */
	protected void readData(DataInput dataSource, ParseMode mode) throws IOException, InvalidJpegFormat {
	};

	/**
	 * If this segment has not read all of its content from disk, this will force it to
	 * be read.  However, if the content has already been read, this will have no effect.
	 */
	public void forceContentLoading() throws IOException {
	}

	/**
	 * Writes the contents of this segment to the output stream, including the size info,
	 * if appropriate, but not the 0xFF and marker info.
	 *
	 * @param stream a non-null stream to write data to.
	 * @throw IllegalArgumentException if param is null
	 * */
	public void write(OutputStream stream) throws IOException {
		if (stream == null) {
			throw new IllegalArgumentException("Stream may not be null");
		}
	}

	/**
	 * Convenience routine so subclasses can get an output stream
	 * as a DataOutputStream easily
	 *
	 * @param stream The stream to wrap as a DataOutputStream
	 *
	 * @return A DataOutputStream (either wrapping the input, or the input if it already was one)
	 */
	protected DataOutputStream wrapAsDataOutputStream(OutputStream stream) {
		if (stream instanceof DataOutputStream) {
			return (DataOutputStream) stream;
		} else {
			return new DataOutputStream(stream);
		}
	}
}
