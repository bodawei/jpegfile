/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 * Restart Marker 2
 * @author dburrowes
 */
public class Rst5Segment extends RstSegment {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0xd5;

    public static int getMarkerCode() {
        return MARKER;
    }
	
    public Rst5Segment() {
    }
}
