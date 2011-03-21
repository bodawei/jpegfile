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
package bdw.formats.jpeg;

import bdw.formats.jpeg.segments.App0Segment;
import bdw.formats.jpeg.segments.AppNSegment;
import bdw.formats.jpeg.segments.ComSegment;
import bdw.formats.jpeg.segments.DacSegment;
import bdw.formats.jpeg.segments.DataSegment;
import bdw.formats.jpeg.segments.DhpSegment;
import bdw.formats.jpeg.segments.DhtSegment;
import bdw.formats.jpeg.segments.DnlSegment;
import bdw.formats.jpeg.segments.DqtSegment;
import bdw.formats.jpeg.segments.DriSegment;
import bdw.formats.jpeg.segments.EoiSegment;
import bdw.formats.jpeg.segments.ExpSegment;
import bdw.formats.jpeg.segments.JpgNSegment;
import bdw.formats.jpeg.segments.JpgSegment;
import bdw.formats.jpeg.segments.JunkSegment;
import bdw.formats.jpeg.segments.RstSegment;
import bdw.formats.jpeg.segments.base.SegmentBase;
import bdw.formats.jpeg.segments.SofSegment;
import bdw.formats.jpeg.segments.SoiSegment;
import bdw.formats.jpeg.segments.SosSegment;
import bdw.formats.jpeg.segments.TemSegment;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class JpegParser implements Iterable<SegmentBase> {

	protected Class<? extends SegmentBase>[] segmentManagers;
	protected List<SegmentBase> segments;
	protected File diskVersion;

	/**
	 * Creates a new JpegFile with an empty list of segments.
	 */
	public JpegParser() {
		segments = new ArrayList<SegmentBase>();
		segmentManagers = new Class[256];
	}

	public JpegParser(File jpegFile) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException {
		this();
	}

	/**
	 * Adds all standard Jpeg segments to this instance.
	 * */
	public void addStandardSegments() {
		addSegmentHandler(SoiSegment.MARKER, SoiSegment.class);
		addSegmentHandler(EoiSegment.MARKER, EoiSegment.class);
		addSegmentHandler(App0Segment.MARKER, App0Segment.class);
		addSegmentHandler(DqtSegment.MARKER, DqtSegment.class);
		addSegmentHandler(SofSegment.RANGE1_START, SofSegment.RANGE1_END, SofSegment.class);
		addSegmentHandler(SofSegment.RANGE2_START, SofSegment.RANGE2_END, SofSegment.class);
		addSegmentHandler(SofSegment.RANGE3_START, SofSegment.RANGE3_END, SofSegment.class);
		addSegmentHandler(SofSegment.RANGE4_START, SofSegment.RANGE4_END, SofSegment.class);
		addSegmentHandler(DhtSegment.MARKER, DhtSegment.class);
		addSegmentHandler(SosSegment.MARKER, SosSegment.class);

		// test
		addSegmentHandler(TemSegment.MARKER, TemSegment.class);
		addSegmentHandler(DacSegment.MARKER, DacSegment.class);
		addSegmentHandler(DnlSegment.MARKER, DnlSegment.class);
		addSegmentHandler(JpgSegment.MARKER, JpgSegment.class);
		addSegmentHandler(RstSegment.START_MARKER, RstSegment.END_MARKER, RstSegment.class);
		addSegmentHandler(DriSegment.MARKER, DriSegment.class);
		addSegmentHandler(DhpSegment.MARKER, DhpSegment.class);
		addSegmentHandler(ExpSegment.MARKER, ExpSegment.class);
		addSegmentHandler(ComSegment.MARKER, ComSegment.class);
		addSegmentHandler(JpgNSegment.START_MARKER, JpgNSegment.END_MARKER, JpgNSegment.class);

		addSegmentHandler(AppNSegment.START_MARKER, AppNSegment.END_MARKER, AppNSegment.class);

		// reserved between 0x02 and BF
	}

	public void addSegmentHandler(int marker, Class<? extends SegmentBase> aClass) {
		segmentManagers[marker] = aClass;
	}

	public void addSegmentHandler(int startMarker, int endMarker, Class<? extends SegmentBase> aClass) {
		for (int index = startMarker; index <= endMarker; index++) {
			segmentManagers[index] = aClass;
		}
	}

	public void readFromFile(File jpegFile) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, InvalidJpegFormat {
		RandomAccessFile file = new RandomAccessFile(jpegFile, "r");
		int aByte;
		int markerByte;
		while (file.getFilePointer() < file.length()) {
			aByte = file.readUnsignedByte();

			if (aByte != 0xFF) {
				JunkSegment data = new JunkSegment();
				data.readFromFile(file);
				this.segments.add(data);
			} else {
				markerByte = 0;
				if (file.getFilePointer() < file.length()) {
					markerByte = file.readUnsignedByte();
				}
				Class managerClass = this.segmentManagers[markerByte];
				SegmentBase manager = (SegmentBase) managerClass.newInstance();
				manager.readFromFile(file);
				this.segments.add(manager);
				if (manager instanceof SosSegment) {
					DataSegment data = new DataSegment();
					data.readFromFile(file);
					this.segments.add(data);
				}
			}
		}
	}

	public void readFromStream(InputStream stream) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, InvalidJpegFormat {
		DataInputStream dataStream = new DataInputStream(stream);
		int aByte;
		int markerByte;
		boolean expectData = false;

		try {
			while (true) {
				aByte = dataStream.readUnsignedByte();

				if (aByte != 0xFF) {
					// do nothing.  we must be encountering raw data.
					// the last marker should have been sos.
					// and isn't this just a strange file format?  all this structured data, and then suddenly a raw spew of data with no sense of size,
					// escapes (0xff00), etc.
				} else {
					markerByte = dataStream.readUnsignedByte();
					if (markerByte == 0x00) {
						// last segment should have been SOS
						// skip it, and keep going
						// this apparently is a way to escape 0xff in the data, and the 00 isn't real data
					} else if (markerByte == 0xFF) {
						// evidently we should just ignore spare ff's.
						// the question is is this the start of a new segment?
					} else {
						Class managerClass = this.segmentManagers[markerByte];
						SegmentBase manager = (SegmentBase) managerClass.newInstance();
						manager.readFromStream(dataStream);
						this.segments.add(manager);
						if (manager instanceof SosSegment) {
							expectData = true;
						}
					}
				}
			}
		} catch (EOFException exception) {
			// we're done.  so gracefully quit.
		}
	}

	/**
	 * Writes a copy of the contents of this file to the specified stream.
	 * Note that this is a copy of whatever the set of segments have, not necessarily what
	 * was in the file this was passed in the constructor.
	 * @param stream the stream to write to
	 */
	public void write(OutputStream stream) throws IOException {
		Iterator<SegmentBase> i = iterator();
		while (i.hasNext()) {
			i.next().write(stream);
		}
	}

	/**
	 * Returns whether the set of segments in the file describe a reasonable jpeg file
	 * for example, starts with a SOI segment, and ends with an EOI. Overlooks details which
	 * are generally OK even if not defined by the spec.
	 * @return true if the segments describe a legitimate Jpeg file
	 */
	public boolean isValid() {
		boolean foundBegin = false;
		boolean foundEnd = false;

		Iterator<SegmentBase> i = iterator();
		while (i.hasNext()) {
			SegmentBase segment = i.next();

			if (segment instanceof SoiSegment) {
				foundBegin = true;
			} else if (segment instanceof EoiSegment) {
				if (!foundBegin) {
					return false;
				} else {
					foundEnd = true;
				}
			}
		}

		if ((foundBegin == false) || (foundEnd == false)) {
			return false;
		}

		return true;
	}

	/**
	 * @return true if the segments describe a jpeg file that exactly matches the jpeg spec.
	 */
	public boolean isStrictlyValid() {
		return false;
	}

	/*
	 * Adds the specified segment to the list, unless it is already there,
	 * in which case add nothing.
	 * @param segment The segment to be added (null is ignored)
	 */
	public void addSegment(SegmentBase segment) {
		if ((segment != null) && (!segments.contains(segment))) {
			segments.add(segment);
		}
	}

	/**
	 * Inserts the segment at the specified location. If the segment is already in the file,
	 * it will be moved and put at the location that (before the move) was index.
	 * @param segment The segment to be inserted (null is ignored)
	 * @param index The index to add the segment at. If out of range, will be put at the start or end of the list
	 */
	public void insertSegmentAt(SegmentBase segment, int index) {
		if (index >= segments.size()) {
			index = segments.size();
		} else if (index < 0) {
			index = 0;
		}

		if ((segment != null) && (!segments.contains(segment))) {
			segments.add(index, segment);
		}
	}

	/**
	 * Returns the segment at the specified index.
	 * @param index the index of the segment
	 * @return the segment at the specified index in the file, or null if no such segment.
	 */
	public SegmentBase getSegmentAt(int index) {
		if ((index >= 0) && (index <= segments.size())) {
			return segments.get(index);
		}

		return null;
	}

	/**
	 * Returns a list of all the segments in the file.
	 * @return a new (possibly empty) array containing all the segments in the file in order
	 */
	public List<SegmentBase> getSegments() {
		List<SegmentBase> copy = new ArrayList<SegmentBase>();

		copy.addAll(segments);

		return copy;
	}

	/**
	 * Removes the specified segment from the file.  If the segment is not in the file
	 * does nothing.
	 * @param segment The segment to remove from the file
	 */
	public void removeSegment(SegmentBase segment) {
		int index = segments.indexOf(segment);
		if (index != -1) {
			removeSegmentAt(index);
		}
	}

	/**
	 * Removes the segment from the specified index in the file
	 * If no segment at that position, nothing is changed
	 * @param index index of the segment to remove
	 */
	public void removeSegmentAt(int index) {
		if ((index >= 0) && (index <= segments.size())) {
			segments.remove(index);
		}
	}

	/**
	 * @return An iterator that will iterate over all the segments in the file
	 */
	public Iterator<SegmentBase> iterator() {
		return segments.listIterator();
	}
}
