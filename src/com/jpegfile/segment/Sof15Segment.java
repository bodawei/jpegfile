/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 * Start of Frame: lossless, arithmetic encoding
 * @author bodawei
 */
public class Sof15Segment extends SofSegment {
    private static final int MARKER = 0xCF;

    public static int getMarkerCode() {
        return MARKER;
    }


	public Sof15Segment() {
		
	}
}
