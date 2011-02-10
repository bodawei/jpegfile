/*
 *  Copyright 2011 David John Burrowes
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
package com.jpegfile.segment;

/**
 * Base class for all segments
 */
public class JpgSegment extends SegmentBase {
    // use static initializer to let jpegfile know about this?
    private static final int MARKER = 0xC8;

    public static int getMarkerCode() {
        return MARKER;
    }
	
    public JpgSegment() {
    }
}
