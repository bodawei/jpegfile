/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 * Restart Marker 0
 * @author dburrowes
 */
public class Rst0Segment extends SegmentBase {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0xd0;

    public static int getMarkerCode() {
        return MARKER;
    }
	
    public Rst0Segment() {
    }
}
