/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

/**
 * Restart Marker 0
 * @author dburrowes
 */
public class Rst0Segment extends SegmentBase {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0xd0;

    public int getMarker() {
        return MARKER;
    }
	
    public Rst0Segment() {
    }
}
