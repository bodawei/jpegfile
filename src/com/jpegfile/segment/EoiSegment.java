/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 *
 * @author dburrowes
 */
public class EoiSegment extends JpegSegment {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0xD9;

    public static int getMarkerCode() {
        return MARKER;
    }
	
    public EoiSegment() {
    }
}
