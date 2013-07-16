/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bdw.format.jpeg.segment.support;

/**
 *
 * @author bodawei
 */
public class ParamCheck {
	public static void checkRange(int value, int lower, int upper) {
		if ((value < lower) || (value > upper)) {
			throw new IllegalArgumentException("Paramater must be between " + lower + " and " + upper);
		}
	}

	public static void checkByte(int value) {
		checkRange(value, 0, 255);
	}

	public static void checkShort(int value) {
		checkRange(value, 0, 65536);
	}
}
