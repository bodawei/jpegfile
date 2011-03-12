/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

/**
 *
 * @author dburrowes
 */
public class MultiMarkerSegmentBase extends SegmentBase {
    private int marker;

    public int getMarker() {
	return marker;
    }
    public void setMarker(int newMarker) {
	marker = newMarker;
    }
}
