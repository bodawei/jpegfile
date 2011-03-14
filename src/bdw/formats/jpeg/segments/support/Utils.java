/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg.segments.support;

public class Utils {
	public static boolean inUInt4Range(int value) {
		if ((value >= 0) && (value < 16)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean inUInt8Range(int value) {
		if ((value >= 0) && (value < 256)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean inUInt16Range(int value) {
		if ((value >= 0) && (value < 65536)) {
			return true;
		} else {
			return false;
		}
	}
}
