/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 * Temporary for arithmetic coding.
 * @author dburrowes
 */
public class RstSegment extends SegmentBase {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0x00;

    public static int getMarkerCode() {
        return MARKER;
    }
	
    public RstSegment() {
    }
}
