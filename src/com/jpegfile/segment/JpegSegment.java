/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

import java.io.OutputStream;

/**
 *
 * @author Young Lee
 */
public class JpegSegment {
    // use static initializer to let jpegfile know about this?
    public static final int MARKER = 0xFF;

    public static int getMarkerCode() {
        return MARKER;
    }

    public int getMarker() {
        return this.MARKER;
    }

    public int getContentlength() {
        return 0;
    }

    public byte[] getRawContent() {
        return new byte[0];
    }

    public void setRawContent(byte[] content) {
    }

    public void forceLoadingOfContent() {
    }

    public void writeToFile(OutputStream stream) {
    }
}
