/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 * Start of Frame: lossless, arithmetic encoding
 * @author bodawei
 */
public class Sof14Segment extends SofSegment {
    private static final int MARKER = 0xCE;

    public static int getMarkerCode() {
        return MARKER;
    }


	public Sof14Segment() {
		
	}
}
