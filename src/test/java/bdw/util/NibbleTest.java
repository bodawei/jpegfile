/*
 *  Copyright 2014 柏大衛
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bdw.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class NibbleTest {

	@Test
	public void getUpper_returnsUpperNibble() {
		assertEquals(13, Nibble.getUpper(0xD4));
	}

	@Test
	public void getLower_returnsLowerNibble() {
		assertEquals(4, Nibble.getLower(0xE4));
	}

	@Test
	public void makeByte_assemblesNibblesIntoAByte() {
		assertEquals((byte)0xC5, Nibble.makeByte(12, 5));
	}

	@Test
	public void makeByte_takesOnlyTheLowest4Bits() {
		assertEquals((byte)0xE7, Nibble.makeByte(-2, 23));
	}
}