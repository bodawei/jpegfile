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
 * Exception that reports when an integer was attempted to be read from a variable int
 * stream, and was found to be malformed.
 */
public class MalformedIntException extends Exception {

	private static final long serialVersionUID = 1L;
	private int badValue;
	
	public MalformedIntException(String message) {
		super(message);
		this.badValue = 0;
	}

	public MalformedIntException(String message, int badValue) {
		super(message);
		this.badValue = badValue;
	}

	/**
	 * @return The malformed value that prompted this exception
	 */
	public int getBadValue() {
		return this.badValue;
	}
}
