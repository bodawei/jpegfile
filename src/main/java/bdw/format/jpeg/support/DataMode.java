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

package bdw.format.jpeg.support;

/**
 * Identify the major mode for interpreting data. The default is STRICT.
 * However, LAX mode may allow one to read in some kinds of corrupt JPEG files.
 */
public enum DataMode {
	/**
	 * Interpret data strictly according to the spec
	 */
	STRICT,

	/**
	 * Allow values as defined by the maximum structural space in the disk
	 * format, regardless of the spec
	 */
	LAX
}
