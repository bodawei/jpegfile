package bdw.formats.jpeg.segments;

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



/**
 * Restart Marker 2
 * @author dburrowes
 */
public class Rst2Segment extends RstSegmentBase {
    public static final int MARKER = 0xd2;

    public int getMarker() {
        return MARKER;
    }

    public Rst2Segment() {
    }
}