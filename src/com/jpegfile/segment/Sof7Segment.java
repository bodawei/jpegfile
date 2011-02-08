/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 * Start of Frame: Differential lossless
 * @author bodawei
 */
public class Sof7Segment extends SofSegment {
    private static final int MARKER = 0xC7;

    public static int getMarkerCode() {
        return MARKER;
    }


	public Sof7Segment() {
		
	}
}
