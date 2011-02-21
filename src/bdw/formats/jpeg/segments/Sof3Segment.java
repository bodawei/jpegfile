/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

/**
 * Start of Frame: Lossless
 * @author bodawei
 */
public class Sof3Segment extends SofSegment {
    private static final int MARKER = 0xC3;

    public static int getMarkerCode() {
        return MARKER;
    }


	public Sof3Segment() {
		
	}
}
