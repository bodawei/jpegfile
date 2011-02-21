/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments;

/**
 * Start Of Frame
 * @author dburrowes
 */
public class SofSegment extends SegmentBase {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0xD9;

    public int getMarker() {
        return MARKER;
    }
	
    public SofSegment() {
    }
	
	protected void readFixedData() {
		/*
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
		 * 
		 */
	}
}
