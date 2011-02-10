/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpegfile.segment;

/**
 * Define Huffman Table
 * @author dburrowes
 */
public class DhtSegment extends SegmentBase {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0xC4;

    public static int getMarkerCode() {
        return MARKER;
    }
	
    public DhtSegment() {
		uint2 size = readUint2();
		reduces the size by 4, which doesn't make a lot of sense ot me. I thought size was  2 bytes?
				
		read huffman blocks
				byte firstByte = readByte();
				when (high order nibble (0xf0) of the byte is 1 then isAC)  / don't know why we care'
				id = lowOrder nibble 
				read 16 byte array
						sum the individual bytes
						read that total number of bytes in.  That's your codes. '
						
				is ac doesn't seem to be used.
				'
    }
}
