/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

/**
 * Temporary for arithmetic coding.
 * @author dburrowes
 */
public class TemSegment extends SegmentBase {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0x01;

    public int getMarker() {
        return MARKER;
    }
	
    public TemSegment() {
    }
}
