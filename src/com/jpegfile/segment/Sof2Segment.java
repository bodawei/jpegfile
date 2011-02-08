/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 * Start of Frame: Progressive
 * @author bodawei
 */
public class Sof2Segment extends SofSegment {
    private static final int MARKER = 0xC2;

    public static int getMarkerCode() {
        return MARKER;
    }


	public Sof2Segment() {
		
	}
}
