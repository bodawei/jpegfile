/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 * Start Of Frame
 * @author dburrowes
 */
public class SofSegment extends JpegSegment {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0xD9;

    public static int getMarkerCode() {
        return MARKER;
    }
	
    public SofSegment() {
    }
	
	protected readFixedData() {
		read size (2bytes)
		// need to make sure that the size 
		byte samplePrecision = readByte();
		int imageHeight = readUInt2();
		int imageWidth = readUInt2();
		byte numComponents = readByte();
		new FixedData();
		
		for (numComponents) {
			byte componentId = readByte();
			byte samplingValues = readByte();
			byte quanizationTableId = readByte();
		}
	}
}
