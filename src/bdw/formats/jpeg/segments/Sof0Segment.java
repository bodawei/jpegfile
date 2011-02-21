/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

/**
 * Start of Frame: Baseline
 * @author bodawei
 */
public class Sof0Segment extends SofSegment {
    private static final int MARKER = 0xC0;

    public static int getMarkerCode() {
        return MARKER;
    }

	public Sof0Segment() {
		
	}
}
