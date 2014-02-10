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
package bdw.format.jpeg.data;

import bdw.format.jpeg.support.DataMode;
import bdw.format.jpeg.support.FrameMode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all JPEG data structure classes.
 * This provides common methods used by all subclasses, including JPEG segments,
 * JPEG images, thumbnails, and others.
 *
 * <h3>Direct Manipulation</h3>
 *
 * All concrete DataItem classes can be instantiated and populated via normal
 * methods. In this respect, interacting with them is the same as any other
 * Java object. However, these are subject to the modes, discussed below.
 *
 * <h3>Input from streams and files</h3>
 * All DataItems can also be populated by reading from a stream or a file.
 * Properties of the object are set as they are read, and should one fail,
 * the instance may be left in an only partially populated state.
 *
 * <h3>Modes</h3>
 * All DataItems are subject two three modes: ValidationMode, FrameMode and
 * Hierarchical mode.
 *
 * <h4>FrameMode</h4>
 * JPEG files will have one or more frame segments that carry with them a
 * variety of parameters that dictate how data in other structures should be
 * interpreted. Any particular DataItem, then, can have a FrameMode specified
 * which may affect how its properties are handled. Not all DataItems actually
 * pay attention to this, but many of the Segments and their tables do.
 * For example, the Arithmetic Coding segment will allow a table class of 0 or 1
 * in extended or progressive frame modes, but only 0 in lossless, and its range
 * is undefined in baseline mode. DataItems can also have a null FrameMode
 * (the default) which is interpreted as putting no restrictions on the
 * properties of the object, aside from the limits imposed by the storage size
 * on disk (e.g. the table class, mentioned above, can be no larger than a4 bit
 * usigned integer).
 * Changing the FrameMode on a DataItem is always allowed, though the resulting
 * DataItem may no longer be valid (see below).
 *
 * <h4>Hierarchical</h4>
 * It is possible for a JPEG image to be in hierarchical mode.  Like FrameMode,
 * this can be set on every DataItem, even if it isn't affected by the setting.
 * Ideally, a JPEG parser will set all DataItems to be Hierarchical = true when
 * it is parsing a file.
 *
 * <h4>ValidationMode</h4>
 * Any DataItem can be in either STRICT or LAX ValidationMode.  The default is
 * STRICT.  When in STRICT mode, then setting any property to a value that is
 * out of range for its data type, FrameMode and Hierarchical Mode will cause
 * an IllegalArgumentException to be thrown and the property will not be
 * altered. Similarly, reading from a corrupt JPEG file will cause an exception
 * to be thrown during the reading process.
 * In contrast, in LAX mode, the DataItem will allow values to be specified that
 * are out of range for the JPEG definition.  Switching to LAX mode would allow
 * one to read or create some kinds of corrupt JPEG files.
 *
 * <h3>Validation</h3>
 * One can always ask a DataItem if it isValid, which will true if all of its
 * properties currently conform to the various modes above. Similarly, one
 * can call validate() on a DataItem to get a list of any invalid properties in
 * the DataItem, each represented as an Exception instance.
 * If one creates a DataItem and sets a FrameMode before setting any properties,
 * then the DataItem should always be valid.  However, if one switches to LAX
 * mode, or changes the FrameMode after setting some properties, the DataItem
 * may, conceivably, be invalid.
 *
 * <h3>Round-Trip Fidelity</h3>
 * One of the design goals for this system was to allow one to modify a JPEG
 * file while having few if any side-effects. Some examples of side effects
 * would include: if the source has a series of 0xFF bytes between marker
 * segments, this is legal, and this framework will preserve those, though they
 * can be removed without loss of meaning. Similarly, it is theoretically
 * possible to have extra bytes at the end of some of the segments, and in LAX
 * mode the reading routines will preserve these so that writing the segment
 * back out will retain those.  That is to say, it should be possible to use
 * this framework to read a JPEG file in and write it back out without any
 * changes in the byte stream. The exception to this is that if a JPEG file is
 * has been truncated (e.g. the last N bytes of the file aren't present), then
 * this will end up throwing an EOFException in that process leaving the final
 * segment possibly corrupt.
 */
public abstract class DataItem {

	private DataMode dataMode;
	private boolean hierarchicalMode;
	private FrameMode frameMode;

	public DataItem() {
		frameMode = null;
		hierarchicalMode = false;
		dataMode = DataMode.STRICT;
	}

	/**
	 * Sets the frame mode for this DataItem. This will recursively change the
	 * modes of any child objects.  If this can't be set for any reason, nothing
	 * will be changed and a runtime exception will be thrown. It is important to
	 * note that after changing the FrameMode, the DataItem may return isValid()
	 * as false.
	 *
	 * @param mode The new FrameMode
	 */
	public void setFrameMode(FrameMode mode) {
		FrameMode oldMode = this.frameMode;

		frameMode = mode;

		try {
			changeChildrenModes();
		} catch (RuntimeException e) {
			frameMode = oldMode;
			changeChildrenModes();
			throw e;
		}
	}

	/**
	 * @return the current FrameMode
	 */
	public FrameMode getFrameMode() {
		return frameMode;
	}

	/**
	 * Sets the HierarchicalMode. updates all child objects. If this can't be
	 * set for any reason, nothing will be changed and a runtime exception will
	 * be thrown.
	 *
	 * @param mode The new Hierarchical mode
	 */
	public void setHierarchicalMode(boolean mode) {
		boolean oldMode = hierarchicalMode;

		hierarchicalMode = mode;

		try {
			changeChildrenModes();
		} catch (RuntimeException e) {
			hierarchicalMode = oldMode;
			changeChildrenModes();
			throw e;
		}
	}

	/**
	 * @return the current HierarchicalMode
	 */
	public boolean getHierarchicalMode() {
		return hierarchicalMode;
	}

	/*
	 * Sets the ValidationMode. This will change all children objects. If this
	 * can't be set for any reason, nothing will be changed and a runtime
	 * exception will be thrown. This will strictly check that this and any
	 * children objects conform to the new ValidationMode, and if they do not
	 * this will reject the change.
	 *
	 * @param mode The new ValidationMode
	 */
	public void setDataMode(DataMode mode) {
		DataMode oldMode = this.dataMode;

		this.dataMode = mode;

		try {
			changeChildrenModes();
			checkModeChange();
		} catch (RuntimeException e) {
			this.dataMode = oldMode;
			changeChildrenModes();
			throw e;
		}
	}

	/**
	 * @return the current ValidationMode
	 */
	public DataMode getDataMode() {
		return this.dataMode;
	}

	/**
	 * @return true if the item is strictly valid
	 */
	public boolean isValid() {
		List<Exception> validationErrors = this.validate();

		return validationErrors.isEmpty();
	}

	/**
	 * @return The number of bytes this DataItem (and its children) will take up
	 * when written to disk.
	 */
	public int getSizeOnDisk() {
		return 0;
	}

	/**
	 * Populates this instance with data from a random access file.  If some data
	 * value doesn't match the current modes, or if an IOException occurs, this
	 * DataItem may be left in a corrupt state.
	 *
	 * @param file The file to read from (not null)
	 *
	 * @throws IOException If something really unexpected happens when reading.
	 */
	public void read(RandomAccessFile file) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("Input file may not be null");
		}
	}

	/**
	 * Populates this instance from stream. If some data value doesn't match
	 * the current modes, or if an IOException occurs, this DataItem may be
	 * left in a corrupt state.
	 *
	 * @param stream The stream to read from (not null)
	 *
	 * @throws IOException If something really unexpected happens when reading.
	 */
	public void read(InputStream stream) throws IOException {
		if (stream == null) {
			throw new IllegalArgumentException("Input stream may not be null");
		}
	}

	/**
	 * Writes the contents of this item to the output stream.  This will write
	 * out the contents of the DataItem and its children regardless of whether
	 * they are currently valid.
	 *
	 * @param stream a non-null stream to write data to.
	 * @throw IOException if a problem was encountered while writing
	 * */
	public void write(OutputStream stream) throws IOException {
		if (stream == null) {
			throw new IllegalArgumentException("Stream may not be null");
		}
	}

	/**
	 * @return a list of exceptions, where each exception describes one way
	 * that the DataItem is not STRICTly valid given the current Frame and
	 * Hierarchical modes.  If the DataItem is valid, this will return an empty
	 * list.
	 */
	public List<Exception> validate() {
		return new ArrayList<Exception>();
	}

	/**
	 * Removes any optional data from the marker. In the case of this clss,
	 * clears the number of leading FF count.  Subclasses will clear other
	 * data that isn't strictly necessary.
	 */
	public void clearPassthrough() {
	}

	/**
	 * Subclasses should override this to update children objects to have the
	 * same modes as this DataItem
	 */
	protected void	changeChildrenModes() {
	}

	/**
	 * Subclasses should override this to validate that given the current
	 * modes, it is currently valid.  If not, then this should throw a runtime
	 * exception.
	 */
	protected void checkModeChange() {
	}
}
