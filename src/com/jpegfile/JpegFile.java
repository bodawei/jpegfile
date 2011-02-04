/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
 * @author Young Lee
 */
public class JpegFile implements Iterable<JpegSegment> {

    protected Map<Integer,Class<JpegSegment>> segmentManagers = new HashMap();
    protected List<JpegSegment> segments;

    public JpegFile(File jpegFile) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException {
        RandomAccessFile file = new RandomAccessFile(jpegFile, "r");
        int aByte;
        while (file.getFilePointer() < file.length()) {
            aByte = file.readUnsignedByte();
            if (aByte != 0xFF) {
                // we have a problem
            } else {
                int markerByte = file.readUnsignedByte();
                Class<JpegSegment> managerClass = this.segmentManagers.get(markerByte);
                JpegSegment manager = managerClass.newInstance();
                this.segments.add(manager);
            }
        }
    }

    public JpegFile() {
        segments = new ArrayList<JpegSegment>();
        this.segmentManagers.put(SoiSegment.MARKER, SoiSegment.class);
        this.segmentManagers.put(EoiSegment.MARKER, EoiSegment.class);
    }
    
    public void writeCopyToFile(File file) {
        
    }

    public void writeCopyToStream(OutputStream stream) {

    }
    
    public void writeToNewFile(File file) {
        
    }

    public void write() {
        
    }

    public File getAssociatedFile() {
        return null;
    }

    public boolean isValid() {
        return false;
    }

    public boolean isStrictlyValid() {
        return false;
    }

    public void addSegment(JpegSegment segment) {

    }

    public void insertSegmentAt(JpegSegment segment, int index) {

    }

    public JpegSegment getSegmentEt(int index) {
        return null;
    }

    public JpegSegment[] getSegments() {
        return null;
    }

    public void removeSegment(JpegSegment segment) {

    }

    public void removeSegmentAt(int index) {

    }

    public Iterator<JpegSegment> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected class JpegFileIterator implements Iterator<JpegSegment> {

        public boolean hasNext() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public JpegSegment next() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
