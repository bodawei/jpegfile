/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 *Define Arithmetic coding conditions
 * @author dburrowes
 */
public class DacSegment extends SegmentBase {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0xCC;

    public static int getMarkerCode() {
        return MARKER;
    }
	
    public DacSegment() {
    }
}
