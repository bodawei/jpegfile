/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

/**
 * Start of Frame: differential progressive
 * @author bodawei
 */
public class Sof6Segment extends SofSegment {
    private static final int MARKER = 0xC6;

    public static int getMarkerCode() {
        return MARKER;
    }


	public Sof6Segment() {
		
	}
}
