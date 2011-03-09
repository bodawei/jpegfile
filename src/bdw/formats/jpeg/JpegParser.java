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
import bdw.formats.jpeg.segments.App10Segment;
import bdw.formats.jpeg.segments.App11Segment;
import bdw.formats.jpeg.segments.App12Segment;
import bdw.formats.jpeg.segments.App13Segment;
import bdw.formats.jpeg.segments.App14Segment;
import bdw.formats.jpeg.segments.App15Segment;
import bdw.formats.jpeg.segments.App1Segment;
import bdw.formats.jpeg.segments.App2Segment;
import bdw.formats.jpeg.segments.App3Segment;
import bdw.formats.jpeg.segments.App4Segment;
import bdw.formats.jpeg.segments.App5Segment;
import bdw.formats.jpeg.segments.App6Segment;
import bdw.formats.jpeg.segments.App7Segment;
import bdw.formats.jpeg.segments.App8Segment;
import bdw.formats.jpeg.segments.App9Segment;
import bdw.formats.jpeg.segments.ComSegment;
import bdw.formats.jpeg.segments.DacSegment;
import bdw.formats.jpeg.segments.DhpSegment;
import bdw.formats.jpeg.segments.DhtSegment;
import bdw.formats.jpeg.segments.DnlSegment;
import bdw.formats.jpeg.segments.DqtSegment;
import bdw.formats.jpeg.segments.DriSegment;
import bdw.formats.jpeg.segments.EoiSegment;
import bdw.formats.jpeg.segments.ExpSegment;
import bdw.formats.jpeg.segments.Jpg0Segment;
import bdw.formats.jpeg.segments.Jpg10Segment;
import bdw.formats.jpeg.segments.Jpg11Segment;
import bdw.formats.jpeg.segments.Jpg12Segment;
import bdw.formats.jpeg.segments.Jpg13Segment;
import bdw.formats.jpeg.segments.Jpg1Segment;
import bdw.formats.jpeg.segments.Jpg2Segment;
import bdw.formats.jpeg.segments.Jpg3Segment;
import bdw.formats.jpeg.segments.Jpg4Segment;
import bdw.formats.jpeg.segments.Jpg5Segment;
import bdw.formats.jpeg.segments.Jpg6Segment;
import bdw.formats.jpeg.segments.Jpg7Segment;
import bdw.formats.jpeg.segments.Jpg8Segment;
import bdw.formats.jpeg.segments.Jpg9Segment;
import bdw.formats.jpeg.segments.JpgSegment;
import bdw.formats.jpeg.segments.Rst0Segment;
import bdw.formats.jpeg.segments.Rst1Segment;
import bdw.formats.jpeg.segments.Rst2Segment;
import bdw.formats.jpeg.segments.Rst3Segment;
import bdw.formats.jpeg.segments.Rst4Segment;
import bdw.formats.jpeg.segments.Rst5Segment;
import bdw.formats.jpeg.segments.Rst6Segment;
import bdw.formats.jpeg.segments.Rst7Segment;
import bdw.formats.jpeg.segments.SegmentBase;
import bdw.formats.jpeg.segments.Sof0Segment;
import bdw.formats.jpeg.segments.Sof1Segment;
import bdw.formats.jpeg.segments.Sof2Segment;
import bdw.formats.jpeg.segments.Sof3Segment;
import bdw.formats.jpeg.segments.Sof5Segment;
import bdw.formats.jpeg.segments.Sof6Segment;
import bdw.formats.jpeg.segments.Sof7Segment;
import bdw.formats.jpeg.segments.Sof9Segment;
import bdw.formats.jpeg.segments.Sof10Segment;
import bdw.formats.jpeg.segments.Sof11Segment;
import bdw.formats.jpeg.segments.Sof13Segment;
import bdw.formats.jpeg.segments.Sof14Segment;
import bdw.formats.jpeg.segments.Sof15Segment;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class JpegParser implements Iterable<SegmentBase> {

    protected Map<Integer,Class<? extends SegmentBase>> segmentManagers;
    protected List<SegmentBase> segments;
	protected File diskVersion;


	/**
	 * Creates a new JpegFile with an empty list of segments.
	 */
    public JpegParser() {
        segments = new ArrayList<SegmentBase>();
		segmentManagers = new HashMap<Integer,Class<? extends SegmentBase>>();
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
        addSegmentHandler(Sof0Segment.MARKER, Sof0Segment.class);
        addSegmentHandler(Sof1Segment.MARKER, Sof1Segment.class);
        addSegmentHandler(Sof2Segment.MARKER, Sof2Segment.class);
        addSegmentHandler(Sof3Segment.MARKER, Sof3Segment.class);
        addSegmentHandler(Sof5Segment.MARKER, Sof5Segment.class);
        addSegmentHandler(Sof6Segment.MARKER, Sof6Segment.class);
        addSegmentHandler(Sof7Segment.MARKER, Sof7Segment.class);
        addSegmentHandler(Sof9Segment.MARKER, Sof9Segment.class);
        addSegmentHandler(Sof10Segment.MARKER, Sof10Segment.class);
        addSegmentHandler(Sof11Segment.MARKER, Sof11Segment.class);
        addSegmentHandler(Sof13Segment.MARKER, Sof13Segment.class);
        addSegmentHandler(Sof14Segment.MARKER, Sof14Segment.class);
        addSegmentHandler(Sof15Segment.MARKER, Sof15Segment.class);
        addSegmentHandler(DhtSegment.MARKER, DhtSegment.class);
        addSegmentHandler(SosSegment.MARKER, SosSegment.class);

		// test
        addSegmentHandler(TemSegment.MARKER, TemSegment.class);
        addSegmentHandler(DacSegment.MARKER, DacSegment.class);
        addSegmentHandler(DnlSegment.MARKER, DnlSegment.class);
        addSegmentHandler(JpgSegment.MARKER, JpgSegment.class);
        addSegmentHandler(Rst0Segment.MARKER, Rst0Segment.class);
        addSegmentHandler(Rst1Segment.MARKER, Rst1Segment.class);
        addSegmentHandler(Rst2Segment.MARKER, Rst2Segment.class);
        addSegmentHandler(Rst3Segment.MARKER, Rst3Segment.class);
        addSegmentHandler(Rst4Segment.MARKER, Rst4Segment.class);
        addSegmentHandler(Rst5Segment.MARKER, Rst5Segment.class);
        addSegmentHandler(Rst6Segment.MARKER, Rst6Segment.class);
        addSegmentHandler(Rst7Segment.MARKER, Rst7Segment.class);
        addSegmentHandler(DriSegment.MARKER, DriSegment.class);
        addSegmentHandler(DhpSegment.MARKER, DhpSegment.class);
        addSegmentHandler(ExpSegment.MARKER, ExpSegment.class);
        addSegmentHandler(ComSegment.MARKER, ComSegment.class);
		// This is clearly the wrong design. Need something better
        addSegmentHandler(Jpg0Segment.MARKER, Jpg0Segment.class);
        addSegmentHandler(Jpg1Segment.MARKER, Jpg1Segment.class);
        addSegmentHandler(Jpg2Segment.MARKER, Jpg2Segment.class);
        addSegmentHandler(Jpg3Segment.MARKER, Jpg3Segment.class);
        addSegmentHandler(Jpg4Segment.MARKER, Jpg4Segment.class);
        addSegmentHandler(Jpg5Segment.MARKER, Jpg5Segment.class);
        addSegmentHandler(Jpg6Segment.MARKER, Jpg6Segment.class);
        addSegmentHandler(Jpg7Segment.MARKER, Jpg7Segment.class);
        addSegmentHandler(Jpg8Segment.MARKER, Jpg8Segment.class);
        addSegmentHandler(Jpg9Segment.MARKER, Jpg9Segment.class);
        addSegmentHandler(Jpg10Segment.MARKER, Jpg10Segment.class);
        addSegmentHandler(Jpg11Segment.MARKER, Jpg11Segment.class);
        addSegmentHandler(Jpg12Segment.MARKER, Jpg12Segment.class);
        addSegmentHandler(Jpg13Segment.MARKER, Jpg13Segment.class);
        addSegmentHandler(App1Segment.MARKER, App1Segment.class);
        addSegmentHandler(App2Segment.MARKER, App2Segment.class);
        addSegmentHandler(App3Segment.MARKER, App3Segment.class);
        addSegmentHandler(App4Segment.MARKER, App4Segment.class);
        addSegmentHandler(App5Segment.MARKER, App5Segment.class);
        addSegmentHandler(App6Segment.MARKER, App6Segment.class);
        addSegmentHandler(App7Segment.MARKER, App7Segment.class);
        addSegmentHandler(App8Segment.MARKER, App8Segment.class);
        addSegmentHandler(App9Segment.MARKER, App9Segment.class);
        addSegmentHandler(App10Segment.MARKER, App10Segment.class);
        addSegmentHandler(App11Segment.MARKER, App11Segment.class);
        addSegmentHandler(App12Segment.MARKER, App12Segment.class);
        addSegmentHandler(App13Segment.MARKER, App13Segment.class);
        addSegmentHandler(App14Segment.MARKER, App14Segment.class);
        addSegmentHandler(App15Segment.MARKER, App15Segment.class);
		// reserved between 0x02 and BF
	}

	public void addSegmentHandler(int marker, Class<? extends SegmentBase> aClass) {
		segmentManagers.put(marker, aClass);
	}

	public void readFromFile(File jpegFile) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException {
		RandomAccessFile file = new RandomAccessFile(jpegFile, "r");
        int aByte;
		int markerByte;
        while (file.getFilePointer() < file.length()) {
            aByte = file.readUnsignedByte();

			if (aByte != 0xFF) {
				// do nothing.  we must be encountering raw data.
				// the last marker should have been sos.
				// and isn't this just a strange file format?  all this structured data, and then suddenly a raw spew of data with no sense of size,
				// escapes (0xff00), etc.
			} else {
				markerByte = 0;
				if (file.getFilePointer() < file.length()) {
					markerByte = file.readUnsignedByte();
				}
				if (markerByte == 0x00) {
					// last segment should have been SOS
					// skip it, and keep going
					// this apparently is a way to escape 0xff in the data, and the 00 isn't real data
				} else if (markerByte == 0xFF) {
					// evidently we should just ignore spare ff's.
					// the question is is this the start of a new segment?
				} else {
					Class managerClass = this.segmentManagers.get(markerByte);
					SegmentBase manager = (SegmentBase) managerClass.newInstance();
					manager.readFromFile(file);
					this.segments.add(manager);
				}
            }
        }
	}

	public void readFromStream(InputStream stream) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException {
		DataInputStream dataStream = new DataInputStream(stream);
		int aByte;
		int markerByte;

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
						Class managerClass = this.segmentManagers.get(markerByte);
						SegmentBase manager = (SegmentBase) managerClass.newInstance();
						manager.readFromStream(dataStream);
						this.segments.add(manager);
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
	public void write(OutputStream stream) {
		Iterator<SegmentBase> i = iterator();
		while (i.hasNext()) {
			i.next().writeToFile(stream);
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
				if ( ! foundBegin) {
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
		if ((segment != null) && ( ! segments.contains(segment))) {
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

		if ((segment != null) && ( ! segments.contains(segment))) {
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
