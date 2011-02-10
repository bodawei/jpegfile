/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 * Restart Marker 7
 * @author dburrowes
 */
public class Rst7Segment extends RstSegment {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0xd7;

    public static int getMarkerCode() {
        return MARKER;
    }
	
    public Rst7Segment() {
    }
}
