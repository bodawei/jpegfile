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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to annotate Marker classes to indicate a range of marker id's that
 * may be used for instances of that class. For example, an APPN segment can
 * have any of 0xE0-0xE9.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MarkerIdRange {
	int first();
	int last();
}
