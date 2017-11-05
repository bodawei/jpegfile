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
package com.davidjohnburrowes.format.jpeg.support;

/**
 * Special exception used to indicate that a value is not allowable within a
 * JPEG file. For instance given the current frame mode, one field may not be
 * able to be set to a requested value.
 */
public class InvalidJpegFormat extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidJpegFormat(String info) {
	   super(info);
	}

	public InvalidJpegFormat(String info, Throwable cause) {
	   super(info, cause);
	}
}
