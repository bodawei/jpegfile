/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

/**
 * Start of Frame: Extended Sequential
 * @author bodawei
 */
public class Sof1Segment extends SofSegment {
    private static final int MARKER = 0xC1;

    public static int getMarkerCode() {
        return MARKER;
    }


	public Sof1Segment() {
		
	}
}
