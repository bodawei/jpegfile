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
package bdw.format.jpeg.segment.base;

import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.ParseMode;
import bdw.format.jpeg.support.Problem;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all segments.
 * A segment represents all the data in one segment of a JPEG file after the 0xFF and marker flags,
 * and before the next ones.
 *
 * In addition to inheriting the behavior explicitly mentioned in this class:
 * <ul>
 * <li>It must respond to the canHandlerMarker method</li>
 * <li>It must be able to write()</li>
 * <li>It must be able to have content forced to be loaded</li>
 * <li>It must be able to report on validity</li>
 * <li>It must be able to return the marker that it is representing</li>
 * </ul>
 * subclasses must also provide two constructors:
 * <ul>
 * <li>The first must accept a marker, an input stream, and a parse mode</li>
 * <li>The second must accept a marker, a random access file, and a parse mode</li>
 * </ul>
 *
 * The semantics for the two constructors is important:
 * Each constructor must parse the data is is passed (in the input stream or file) at least
 * far enough to determine whether the data is valid for this kind of segment.  If it is not
 * then the constructor must throw an InvalidJpegFormat. It is up to the caller to clean up
 * after an unsuccessful parse (e.g. rolling back the input stream or resetting the file marker).
 * If the data is conceivable valid, but doesn't match the parsing strictness, then problems should be
 * stored on the segment, but the constructor should still succeed.
 *
 * The random access file constructor may freely choose not to eagerly read in chunks of data that exceed
 * SegmentBase.READ_LIMIT data.
 * This allows users to analyze the structure of a potentially large JPEG file without loading
 * all of the data into memory. At an time, however, the user can force that data into memory.
 *
 * Note: In the case of a large segment, it is not necessary that all bytes be read into memory, but only
 * enough to garantuee that this segment can manage th data.
 * The constructors must also fail if the marker type they are presented is not valid.
 *
 * In addition to these requirements, a segment class is encouraged to provide other useful constructors.
 * The result of calling any other constructor must be a segment which is at least "lax" valid. Which is
 * to say, any values that are absolutely required in order to generate a "lax" valid segment must be
 * included in the constructor calls.
 */
public abstract class SegmentBase extends JpegDataStructureBase {

	protected static final int READ_LIMIT = 1024;

	private int marker;

	private boolean valid;
	protected List<Problem> problems;

	public SegmentBase() {
		valid = true;
		problems = new ArrayList<Problem>();
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

	/**
	 * Used by subclasses to set the marker value
	 */
	protected void setMarker(int newMarker) {
		marker = newMarker;
	}

	public List<Problem> getProblems() {
		return problems;
	}
	
	/**
	 * Called from constructors building a segment from a random access file. This must do the following:
	 * <ul>
	 * <li>This must determine whether the input stream can possibly be parsed as this kind of segment. If
	 *     not it must throw InvalidJpegFormat</li>
	 * <li>If the data is viably this kind of segment (e.g. expected structures are present and neither too large or too short) then this should log any violations of the parse mode as problems on the segment itself</li>
	 * <li>This may, optionally, not read in large segments of data that are not needed to be used to validate the segment. This must later be able to retrieve that data when forceContentLoading is called</li>
	 * <li>This must store enough information that write() will be able to write out the exact same bit sequence that this read in</li>
	 * </ul>
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
	 * Called from constructors building a segment from a random access file. This must do the following:
	 * <ul>
	 * <li>This must determine whether the input stream can possibly be parsed as this kind of segment. If
	 *     not it must throw InvalidJpegFormat</li>
	 * <li>If the data is viably this kind of segment (e.g. expected structures are present and neither too large or too short) then this should log any violations of the parse mode as problems on the segment itself</li>
	 * <li>This must store enough information that write() will be able to write out the exact same bit sequence that this read in</li>
	 * </ul>
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
	 * By default, readFromStream() calls this to do its reading, and so this must conform to the expectations for
	 * that method.  Subclasses that do not need to not read in some of the segment's data are encouraged to have
	 * readFromFile() direct to this as well.
	 *
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
	 * Note, if this is done immediately after reading from an inputstream or file, this must write out
	 * the exact same bytes that were read in.  However, if this was "hand built" or is a modified version
	 * of one read from a file, then this garantuee is not maintained.
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
