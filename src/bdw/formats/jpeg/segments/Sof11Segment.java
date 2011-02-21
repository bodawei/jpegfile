/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

/**
 * Start of Frame: lossless, arithmetic encoding
 * @author bodawei
 */
public class Sof11Segment extends SofSegment {
    private static final int MARKER = 0xCB;

    public static int getMarkerCode() {
        return MARKER;
    }


	public Sof11Segment() {
		
	}
}
