/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile;

import com.jpegfile.segment.JpegSegment;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Young Lee
 */
public class JpegFile implements Iterable<JpegSegment> {

    protected static List SEGMENT_MANAGERS = new ArrayList();
    protected List<JpegSegment> segments;

    public JpegFile(File jpegFile) {

    }

    public JpegFile() {
        segments = new ArrayList<JpegSegment>();
    }

    public static void addSegmentManager(Class segmentManager) {

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
