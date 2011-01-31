/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 *
 * @author Young Lee
 */
public class JpegSegment {
    // use static initializer to let jpegfile know about this?
    
    public byte getModifier() {
        return 0;
    }

    public int getContentlength() {
        return 0;
    }
    public byte[] getRawContent() {
        return null;
    }

    public void setRawContent(byte[] content) {

    }

    public void forceLoadingOfContent() {

    }

    public void writeToFile() {

    }
}
