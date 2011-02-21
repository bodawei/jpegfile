/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

/**
 * Start of Frame: differential sequential
 * @author bodawei
 */
public class Sof5Segment extends SofSegment {
    private static final int MARKER = 0xC5;

    public static int getMarkerCode() {
        return MARKER;
    }


	public Sof5Segment() {
		
	}
}
