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

package bdw.io;

import bdw.io.LimitingDataInput;
import java.io.IOException;

/**
 * Exception to use when to indicate a limitingDataInput has been asked to read
 * more than it can.
 */
public class LimitExceeded extends IOException {

	/**
	 * Number of bytes that couldn't be read (0 indicates unknown)
	 */
	protected int amount;

	/**
	 * The instance that threw this exception
	 */
	protected LimitingDataInput instance;

	/**
	 * Constructor
	 * @param message Exception message
	 */
	public LimitExceeded(String message) {
		this(message, 0, null, null);
	}

	/**
	 * Constructor
	 * @param message Exception message
	 * @param exceededAmount Number of bytes exceeded (0 means unknown)
	 * @param instance The object that couldn't read in the bytes
	 * @param cause An underlying cause of this problem
	 */
	public LimitExceeded(String message, int exceededAmount,
			LimitingDataInput instance, Throwable cause) {
		super(message, cause);
		this.amount = exceededAmount;
		this.instance = instance;
	}

	/**
	 * @return Number of bytes that could not be read (0 indicated unknown)
	 */
	public int getExceededAmount() {
		return amount;
	}

	/**
	 * @return The instance reporting this exception
	 */
	public LimitingDataInput getIntance() {
		return instance;
	}
}
