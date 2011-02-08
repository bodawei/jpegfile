package com.jpegfile;

import com.jpegfile.segment.EoiSegment;
import com.jpegfile.segment.JpegSegment;
import com.jpegfile.segment.SoiSegment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
public class JpegFile implements Iterable<JpegSegment> {

    protected Map<Integer,Class<? extends JpegSegment>> segmentManagers;
    protected List<JpegSegment> segments;
	protected File diskVersion;

	/**
	 * Builds a new instance of this class with all the standard JpegSegments in it.
	 * @param the file to add the segment handlers into.
	 * */
	public static void fullyConfigureInstance(JpegFile file) {
        file.addSegmentHandler(SoiSegment.class);
        file.addSegmentHandler(EoiSegment.class);
	}
	
	/**
	 * Creates a new JpegFile with an empty list of segments.
	 */
    public JpegFile() {
        segments = new ArrayList<JpegSegment>();
		segmentManagers = new HashMap();
    }

	public JpegFile(File jpegFile) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException {
		this();
		RandomAccessFile file = new RandomAccessFile(jpegFile, "r");
        int aByte;
        while (file.getFilePointer() < file.length()) {
            aByte = file.readUnsignedByte();
            if (aByte != 0xFF) {
                // we have a problem
            } else {
                int markerByte = file.readUnsignedByte();
                Class<? extends JpegSegment> managerClass = this.segmentManagers.get(markerByte);
                JpegSegment manager = managerClass.newInstance();
                this.segments.add(manager);
            }
        }
    }
    
	
	public void addSegmentHandler(Class<? extends JpegSegment> aClass) {
		int maker = aClass.getMarkerSegment();
		segmentManagers.put(5, aClass);
	}

	/**
	 * Writes a copy of the contents of this file to the specified stream.
	 * Note that this is a copy of whatever the set of segments have, not necessarily what
	 * was in the file this was passed in the constructor.
	 * @param stream the stream to write to
	 */
	public void write(OutputStream stream) {
		Iterator<JpegSegment> i = iterator();
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
        return false;
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
	public void addSegment(JpegSegment segment) {
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
    public void insertSegmentAt(JpegSegment segment, int index) {
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
    public JpegSegment getSegmentAt(int index) {
		if ((index >= 0) && (index <= segments.size())) {
			return segments.get(index);
		}
		
        return null;
    }

	/**
	 * Returns a list of all the segments in the file.
	 * @return a new (possibly empty) array containing all the segments in the file in order
	 */
    public List<JpegSegment> getSegments() {
		List<JpegSegment> copy = new ArrayList<JpegSegment>();
		
		copy.addAll(segments);
		
        return copy;
    }

	/**
	 * Removes the specified segment from the file.  If the segment is not in the file
	 * does nothing.
	 * @param segment The segment to remove from the file
	 */
    public void removeSegment(JpegSegment segment) {
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
    public Iterator<JpegSegment> iterator() {
        return segments.listIterator();
    }

}
