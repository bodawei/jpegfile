/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments.support;

import java.io.DataInput;

/**
 *
 * @author bodawei
 */
public class DqtRunLengthHeader {

	public int getInitialZeroBitCount() {
		return 0;
	}

	public void setInitialZeroBitCount(int count) {

	}

	public int getFolowingDataBitCount() {
		return 0;
	}

	public void setFollowingDataBitCount(int count) {

	}

	public int getHuffmanCodingBitMask() {
		return 0;
	}

	public int getHuffmanCoding() {
		return 0;
	}

	public boolean canMatch(DataInput input) {
		return false;
	}
}
