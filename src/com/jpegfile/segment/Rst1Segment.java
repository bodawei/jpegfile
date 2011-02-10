/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 * Restart Marker 1
 * @author dburrowes
 */
public class Rst1Segment extends SegmentBase {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0xd1;

    public static int getMarkerCode() {
        return MARKER;
    }
	
    public Rst1Segment() {
    }
}
