/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 * Start of Frame: progressive, arithmetic encoding
 * @author bodawei
 */
public class Sof10Segment extends SofSegment {
    private static final int MARKER = 0xCA;

    public static int getMarkerCode() {
        return MARKER;
    }


	public Sof10Segment() {
		
	}
}
