/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

/**
 *Define Arithmetic coding conditions
 * @author dburrowes
 */
public class DacSegment extends SegmentBase {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0xCC;

    public int getMarker() {
        return MARKER;
    }
	
    public DacSegment() {
    }
}
