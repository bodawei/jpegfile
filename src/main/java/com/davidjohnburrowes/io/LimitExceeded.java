/*
 *  Copyright 2014,2017 柏大衛
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

package com.davidjohnburrowes.io;

import java.io.EOFException;

/**
 * Used to indicate a LimitingDatainput has been asked to read beyond its limit.
 */
public class LimitExceeded extends EOFException {

   private static final long serialVersionUID = 1L;

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
		this(message, 0, null);
	}

	/**
	 * Constructor
	 * @param message Exception message
	 * @param exceededAmount Number of bytes exceeded (0 means unknown)
	 * @param instance The object that couldn't read in the bytes
	 */
	public LimitExceeded(String message, int exceededAmount,
			LimitingDataInput instance) {
		super(message);
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
