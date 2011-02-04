/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 *
 * @author dburrowes
 */
public class SoiSegment extends JpegSegment {
    public static final int MARKER = 0xD8;

    public static int getMarkerCode() {
        return MARKER;
    }

    public SoiSegment() {
    }
}
