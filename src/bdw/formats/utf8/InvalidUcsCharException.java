/*
 *  Copyright 2011 柏大衛
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

package bdw.formats.utf8;

/**
 * An exception which reports when an invalide Universal Character Set character is found
 */
public class InvalidUcsCharException extends Exception {
	private static final long serialVersionUID = 1L;
	private int badChar;
	
	public InvalidUcsCharException(int badChar) {
		this.badChar = badChar;
	}
	
	/**
	 * @return The non-UCS character that was found
	 */
	public int getBadChar() {
		return badChar;
	}
}
