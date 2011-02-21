/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

/**
 * Start of Frame: Extended sequential, arithmetic encoding
 * @author bodawei
 */
public class Sof9Segment extends SofSegment {
    private static final int MARKER = 0xC9;

    public static int getMarkerCode() {
        return MARKER;
    }


	public Sof9Segment() {
		
	}
}
